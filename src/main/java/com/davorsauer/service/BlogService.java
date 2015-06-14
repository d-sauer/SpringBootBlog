package com.davorsauer.service;

import com.davorsauer.commons.ContentMetadataUtils;
import com.davorsauer.commons.Logger;
import com.davorsauer.config.BlogProperties;
import com.davorsauer.domain.ArticleType;
import com.davorsauer.domain.ContentLocation;
import com.davorsauer.domain.ContentMetadata;
import com.davorsauer.error.LoadArticleException;
import com.davorsauer.error.ScanArticlesException;
import org.kohsuke.github.GHContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by davor on 13/04/15.
 */
@Service
public class BlogService implements Logger {

    private static final String ARTICLES_URL = "blog";

    private final Integer currentYear = LocalDate.now().getYear();

    private Map<String, ContentLocation> contents = new TreeMap<>();

    private Invocable invocable;

    private ReentrantLock lock = new ReentrantLock();

    private final Function<String, String[]> indexNamings = (customName) -> new String[]{"index.html", "index.htm", "index.md", customName + ".html", customName + ".htm", customName + ".md"};

    private final Function<String, String[]> metaNamings = (customName) -> new String[]{"metadata", "metadata.yaml", "metadata.yml", "index.yaml", "index.yml", customName + ".yaml", customName + ".yml"};

    private GitHubRepositoryService repository;

    private BlogProperties blogProperties;


    @Autowired
    public BlogService(BlogProperties blogProperties, GitHubRepositoryService repository) {
        this.blogProperties = blogProperties;
        this.repository = repository;

        try {
            scan();
        } catch (Exception e) {
            error("Can't load blog articles", e);
        }

        try {
            invocable = loadMarkdownEngine();
        } catch (ScriptException e) {
            error("Can't load JavaScript engine", e);
        }
    }

    public Collection<ContentLocation> getContents() {
        return contents.values();
    }

    public void scan() throws IOException, ScanArticlesException {
        if (lock.isLocked()) {
            throw new ScanArticlesException("Scanning for articles in progress");
        }

        lock.lock();
        try {
            final Map<String, ContentLocation> articles = new TreeMap<>();

            List<GHContent> contents = repository.getRepository().getDirectoryContent("/");
            for (GHContent content : contents) {
                if (content.isDirectory()) {
                    try {
                        String path = content.getPath();
                        Integer year = Integer.parseInt(content.getName());

                        Map<String, ContentLocation> yearArticles = scanYear(path, year);
                        articles.putAll(yearArticles);
                    } catch (NumberFormatException e) {
                        // ignore, move forward
                    }
                }
            }

            this.contents = articles;
        } finally {
            lock.unlock();
        }
    }

    private Map<String, ContentLocation> scanYear(String path, Integer year) throws IOException {
        final Map<String, ContentLocation> articles = new TreeMap<>();

        List<GHContent> contents = repository.getRepository().getDirectoryContent("/" + path);
        for (GHContent content : contents) {
            if (content.isDirectory()) {

                GHContent articleIndex = getPossibleContent(content.getPath(), indexNamings.apply(content.getName()));
                GHContent articleMetadata = getPossibleContent(content.getPath(), metaNamings.apply(content.getName()));

                if (articleIndex != null) {
                    String articlePath = articleIndex.getPath();
                    String slug = year + "_" + content.getName();

                    ContentLocation article = new ContentLocation();
                    article.setPath(articlePath);
                    article.setSlug(slug);
                    article.setUrl(ARTICLES_URL + "/" + slug);
                    article.setIsArchive(!currentYear.equals(year));

                    if (articlePath != null) {
                        article.setType(ArticleType.getTypeFromPath(articlePath));
                    }

                    if (articleMetadata != null) {
                        ContentMetadata articleMeta = ContentMetadataUtils.readMetadata(articleMetadata.read());
                        article.setMetadata(articleMeta);
                        if (articleMeta.isPublished() == null && articleMeta.getPublishDate() != null && articleMeta.getPublishDate().getTime() <= System.currentTimeMillis()) {
                            articleMeta.setPublished(true);
                        }
                    }

                    articles.put(article.getSlug(), article);
                    info("Load article: " + article.getMetadata().getPublishDate().toString() + " :: " + article.getMetadata().getTitle() + ", tags:" + article.getMetadata().getTags().toString() + ", location:" + article.getPath());
                }
            }
        }

        return articles;
    }


    private GHContent getPossibleContent(String path, String[] variations) {
        return getPossibleContent(path, null, variations);
    }

    private GHContent getPossibleContent(String path, String ref, String[] variations) {
        GHContent content = null;
        for (String variation : variations) {
            try {
                if (ref == null) {
                    content = repository.getRepository().getFileContent(path + "/" + variation);
                } else {
                    content = repository.getRepository().getFileContent(path + "/" + variation, ref);
                }

                if (content != null)
                    break;
            } catch (IOException e) {
                // try next variation
            }
        }

        return content;
    }

    public String getArticle(String slug, String ref) throws LoadArticleException {
        String slugParts[] = slug.split("_", 2);
        if (slugParts.length != 2) {
            throw new LoadArticleException("Incorrect article slug");
        }

        String year = slugParts[0];
        String article = slugParts[1];
        String path = year + "/" + article;

        // find content file
        GHContent index = getPossibleContent(path, ref, indexNamings.apply(article));
        ArticleType articleType = ArticleType.getTypeFromPath(index.getPath());

        String content = repository.getArticleContent(index.getPath(), ref);
        if (ArticleType.MARKDOWN == articleType) {
            content = convertMarkdown(content);
        }

        if (index == null) {
            throw new LoadArticleException("Can't find article with defined slug");
        }

        return content;
    }

    public String getArticle(String slug) throws LoadArticleException {
        ContentLocation article = contents.get(slug);
        if (article != null) {
            if (article.getType() == ArticleType.MARKDOWN)
                return getMarkdownArticle(article.getPath());
            else
                return repository.getArticleContent(article.getPath());
        } else {
            error("Required article ({}) doesn't exist!", slug);
            throw new LoadArticleException("Required article doesn't exist!");
        }
    }

    private Invocable loadMarkdownEngine() throws ScriptException {
        InputStream inputStream = BlogService.class.getClassLoader().getResourceAsStream("javascript/marked.min.js");

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");

        engine.eval(new InputStreamReader(inputStream));

        return (Invocable) engine;
    }

    private String getMarkdownArticle(String repositoryPath) throws LoadArticleException {
        String markdown = repository.getArticleContent(repositoryPath);
        return convertMarkdown(markdown);
    }

    private String convertMarkdown(String markdown) throws LoadArticleException {
        if (invocable != null) {
            try {
                Object result = invocable.invokeFunction("marked", markdown);
                return result.toString();
            } catch (Exception e) {
                new LoadArticleException("Can't load article markdown", e);
            }
        } else {
            warn("JavaScript Markdown engine is not loaded!");
        }

        return markdown;
    }

}
