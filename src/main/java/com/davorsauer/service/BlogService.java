package com.davorsauer.service;

import com.davorsauer.commons.ContentMetadataUtils;
import com.davorsauer.commons.Logger;
import com.davorsauer.domain.Article;
import com.davorsauer.domain.ArticleMetadata;
import com.davorsauer.domain.ArticleType;
import com.davorsauer.domain.ContentData;
import com.davorsauer.error.LoadArticleException;
import com.davorsauer.error.ScanArticlesException;
import org.kohsuke.github.GHContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.davorsauer.commons.BlogUtils.*;

/**
 * Created by davor on 13/04/15.
 */
@Service
public class BlogService implements Logger {

    public static final String ARTICLE_URL = "blog";

    private GitHubRepositoryService repository;

    private final Integer currentYear = LocalDate.now().getYear();

    private Map<String, Article> contents = new HashMap<>();

    private MarkdownService markdownService;

    private ReentrantLock lock = new ReentrantLock();


    @Autowired
    public BlogService(GitHubRepositoryService repository, MarkdownService markdownService) {
        this.repository = repository;
        this.markdownService = markdownService;

        try {
            scan();
        } catch (Exception e) {
            error("Can't load blog articles", e);
        }
    }

    public Collection<Article> getArticles() {
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

                        Map<String, Article> yearArticles = scan(path, year);
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

    private Map<String, Article> scan(String path, Integer year) throws IOException {
        final Map<String, Article> articles = new HashMap<>();

        List<GHContent> contents = repository.getRepository().getDirectoryContent("/" + path);
        for (GHContent content : contents) {
            if (content.isDirectory()) {

                GHContent articleIndex = getPossibleContent(repository, content.getPath(), indexNamings.apply(content.getName()));
                GHContent articleMetadata = getPossibleContent(repository, content.getPath(), metaNamings.apply(content.getName()));

                if (articleIndex != null) {
                    String articlePath = articleIndex.getPath();
                    String slug = year + "_" + content.getName();

                    Article article = new Article();
                    article.setPath(articlePath);
                    article.setSlug(slug);
                    article.setUrl(ARTICLE_URL + "/" + slug);
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

    private void mapArticle() {

    }

    public ContentData getArticle(String slug) throws LoadArticleException {
        return getArticle(slug, null);
    }

    public ContentData getArticle(String slug, String ref) throws LoadArticleException {
        String slugParts[] = slug.split("_", 2);
        if (slugParts.length != 2) {
            throw new LoadArticleException("Incorrect article slug");
        }

        String year = slugParts[0];
        String articleSlug = slugParts[1];
        String path = year + "/" + articleSlug;

        String content = null;
        ArticleMetadata contentMetadata = null;
        ArticleType articleType = ArticleType.UNDEFINED;

        if (ref != null) {
            GHContent index = getPossibleContent(repository, path, ref, indexNamings.apply(articleSlug));
            articleType = ArticleType.getTypeFromPath(index.getPath());

            content = repository.getArticleContent(index.getPath(), ref);
        } else {
            Article article = contents.get(slug);
            if (article != null) {
                articleType = article.getType();
                content = repository.getArticleContent(article.getPath());
                contentMetadata = article.getMetadata();
            }
        }

        if (content == null) {
            error("Required article ({}) doesn't exist!", slug);
            throw new LoadArticleException("Required article doesn't exist!");
        }

        if (ArticleType.MARKDOWN == articleType) {
            content = markdownService.renderMarkdown(content);
        }

        return new ContentData(content, contentMetadata);
    }

}
