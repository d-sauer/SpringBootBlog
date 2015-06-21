package com.davorsauer.service;

import com.davorsauer.commons.ContentMetadataUtils;
import com.davorsauer.commons.Logger;
import com.davorsauer.config.BlogProperties;
import com.davorsauer.domain.ArticleType;
import com.davorsauer.domain.ContentData;
import com.davorsauer.domain.Article;
import com.davorsauer.domain.ArticleMetadata;
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
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by davor on 13/04/15.
 */
@Service
public class BlogService implements Logger {

    private static final String ARTICLES_URL = "blog";

    private GitHubRepositoryService repository;

    private BlogProperties blogProperties;

    private final Integer currentYear = LocalDate.now().getYear();

    private Map<String, Article> contents = new HashMap<>();

    private Invocable invocable;

    private ReentrantLock lock = new ReentrantLock();

    private final Function<String, String[]> indexNamings = (customName) -> new String[]{"index.html", "index.htm", "index.md", customName + ".html", customName + ".htm", customName + ".md"};

    private final Function<String, String[]> metaNamings = (customName) -> new String[]{"metadata", customName + ".meta", customName};

    private final Function<Article, Long> getPublishDate = (contentLocation -> {
        if (contentLocation.getMetadata() != null && contentLocation.getMetadata().getPublishDate() != null) {
            return contentLocation.getMetadata().getPublishDate().getTime();
        } else {
            return 0L;
        }
    }
    );

    private final Comparator<Map.Entry<String, Article>> sortByValue = (entry1, entry2) -> {
        long publishDate1 = getPublishDate.apply(entry1.getValue());
        long publishDate2 = getPublishDate.apply(entry2.getValue());

        if (publishDate1 > publishDate2)
            return -1;
        else if (publishDate1 < publishDate2)
            return 1;
        return 0;
    };




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

    public Collection<Article> getContents() {
        return contents.values();
    }

    public void scan() throws IOException, ScanArticlesException {
        if (lock.isLocked()) {
            throw new ScanArticlesException("Scanning for articles in progress");
        }

        lock.lock();
        try {
            final Map<String, Article> articles = new HashMap<>();

            List<GHContent> contents = repository.getRepository().getDirectoryContent("/");
            for (GHContent content : contents) {
                if (content.isDirectory()) {
                    try {
                        String path = content.getPath();
                        Integer year = Integer.parseInt(content.getName());

                        Map<String, Article> yearArticles = scanYear(path, year);
                        articles.putAll(yearArticles);
                    } catch (NumberFormatException e) {
                        // ignore, move forward
                    }
                }
            }

            Map<String, Article> sortedArticles = new LinkedHashMap<>();
            articles.entrySet().stream().sorted(sortByValue).forEach(ea -> {
                sortedArticles.put(ea.getKey(), ea.getValue());
            });
            this.contents = sortedArticles;
        } finally {
            lock.unlock();
        }
    }

    private Map<String, Article> scanYear(String path, Integer year) throws IOException {
        final Map<String, Article> articles = new HashMap<>();

        List<GHContent> contents = repository.getRepository().getDirectoryContent("/" + path);
        for (GHContent content : contents) {
            if (content.isDirectory()) {

                GHContent articleIndex = getPossibleContent(content.getPath(), indexNamings.apply(content.getName()));
                GHContent articleMetadata = getPossibleContent(content.getPath(), metaNamings.apply(content.getName()));

                if (articleIndex != null) {
                    String articlePath = articleIndex.getPath();
                    String slug = year + "_" + content.getName();

                    Article article = new Article();
                    article.setPath(articlePath);
                    article.setSlug(slug);
                    article.setUrl(ARTICLES_URL + "/" + slug);
                    article.setIsArchive(!currentYear.equals(year));

                    if (articlePath != null) {
                        article.setType(ArticleType.getTypeFromPath(articlePath));
                    }

                    if (articleMetadata != null) {
                        ArticleMetadata articleMeta = ContentMetadataUtils.readMetadata(articleMetadata.read());
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

    public ContentData getArticle(String slug, String ref) throws LoadArticleException {
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

        return new ContentData(content, new ArticleMetadata());
    }

    public ContentData getArticle(String slug) throws LoadArticleException {
        Article article = contents.get(slug);
        if (article != null) {
            if (article.getType() == ArticleType.MARKDOWN)
                return new ContentData(getMarkdownArticle(article.getPath()), article.getMetadata());
            else
                return new ContentData(repository.getArticleContent(article.getPath()), article.getMetadata());
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
