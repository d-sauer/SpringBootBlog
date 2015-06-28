package com.davorsauer.commons;

import com.davorsauer.domain.ArticleMetadata;
import com.davorsauer.domain.ArticleType;
import com.davorsauer.error.LoadArticleException;
import com.davorsauer.service.GitHubRepositoryService;
import com.davorsauer.service.MarkdownService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by davor on 06/05/15.
 *
 * @see ArticleMetadata
 */
public class ContentMetadataUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String PROPERTY_NAME_REGEX = "^\\s*(\\S+):";
    private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile(PROPERTY_NAME_REGEX);

    private static final Logger LOG = LoggerFactory.getLogger(ContentMetadataUtils.class);

    public static ArticleMetadata readMetadata(InputStream metadata) throws IOException {
        StringBuilder buffer = new StringBuilder();
        byte[] readBuffer = new byte[1024];
        try {
            while (metadata.read(readBuffer) != -1) {
                buffer.append(new String(readBuffer, "UTF-8"));
            }
        } finally {
            metadata.close();
        }

        return readMetadata(buffer.toString());
    }

    public static ArticleMetadata readMetadata(String metadata) {
        ArticleMetadata articleMetadata = new ArticleMetadata();

        //
        // Map properties and values
        //
        Map<String, String> values = new HashMap<>();
        String key = null;
        StringBuilder buffer = null;
        String lines[] = metadata.split("\\n");
        for (String line : lines) {
            Matcher matcher = PROPERTY_NAME_PATTERN.matcher(line);
            if (matcher.find()) {
                if (buffer != null && key != null) {
                    values.put(key, buffer.toString());
                }

                key = matcher.group(1).toLowerCase();
                buffer = new StringBuilder();
                buffer.append(line.substring(matcher.end()));
            } else if (buffer != null && key != null) {
                buffer.append(line);
            }
        }
        values.put(key, buffer.toString());


        //
        // Put mapped properties to CustomMetadata object
        //
        Class<ArticleMetadata> clazz = ArticleMetadata.class;
        Method methods[] = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set") && method.getReturnType() != null && method.getParameterCount() == 1) {
                String propertyName = method.getName().substring(3).toLowerCase();
                String value = values.get(propertyName);
                if (value != null) {
                    Class<?> methodParam = method.getParameterTypes()[0];

                    try {
                        if (Date.class.isAssignableFrom(methodParam)) {
                            Date date = dateFormat.parse(value);
                            method.invoke(articleMetadata, date);
                        } else if (Set.class.isAssignableFrom(methodParam)) {
                            Set<String> set = new HashSet<>();
                            Arrays.asList(value.split(",")).forEach(element -> set.add(element.trim()));
                            method.invoke(articleMetadata, set);
                        } else if (Boolean.class.isAssignableFrom(methodParam)) {
                            method.invoke(articleMetadata, Boolean.parseBoolean(value));
                        } else if (String.class.isAssignableFrom(methodParam)) {
                            method.invoke(articleMetadata, value);
                        }
                    } catch (Exception e) {
                        LOG.error("Can't parse property:" + propertyName + " value:" + value + " to object of:" + methodParam.getName() + ", for method: " + method.getName(), e);
                    }
                }
            }
        }


        return articleMetadata;
    }


    public static String writeMetadata(ArticleMetadata articleMetadata) {
        StringBuilder buffer = new StringBuilder();

        // Read all getMethods
        Class<ArticleMetadata> clazz = ArticleMetadata.class;
        Method methods[] = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getReturnType() != null && method.getParameterCount() == 0) {
                try {
                    Object methodResult = method.invoke(articleMetadata, null);
                    if (methodResult != null) {
                        buffer.append(method.getName().substring(3));
                        buffer.append(":");

                        if (methodResult instanceof Date) {
                            buffer.append(dateFormat.format((Date) methodResult));
                        } else if (methodResult instanceof Collection) {
                            StringJoiner join = new StringJoiner(",");
                            ((Collection) methodResult).forEach(element -> join.add(element.toString()));
                            buffer.append(join.toString());
                        } else {
                            buffer.append(methodResult.toString());
                        }

                        buffer.append("\n");
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // skip property
                }
            }
        }

        return buffer.toString();
    }

    public static String getFormatDate(Date formatDate, String format) {
        if (format == null) {
            return dateFormat.format(formatDate);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(formatDate);
        }
    }

    public static ArticleMetadata readMetadata(GitHubRepositoryService repository, MarkdownService markdownService, GHContent articleIndex, ArticleType articleType) throws LoadArticleException {
        ArticleMetadata metadata = new ArticleMetadata();

        //
        // Created date
        //
        try {
            // TODO Read create date from GitHub for that file
            metadata.setPublishDate(new Date());
        } catch (Exception e) {
            // can't read creating date
            LOG.error("readMetadata", e);
        }


        //
        // Title, description
        //
        String html = repository.getArticleContent(articleIndex);
        if (ArticleType.MARKDOWN.equals(articleType)) {
            html = markdownService.renderMarkdown(html);
        }

        Document htmlDoc = Jsoup.parse(html);
        // Get title
        String title = htmlDoc.body().getElementsByTag("h1").text();
        if (StringUtils.isEmpty(title))
            title = "Unknown title";
        metadata.setTitle(title);

        // Get first paragraph
        String paragraph = htmlDoc.body().getElementsByTag("p").text();
        metadata.setDescription(paragraph);

        return metadata;
    }

}
