package com.davorsauer.service;

import com.davorsauer.commons.ContentMetadataUtils;
import com.davorsauer.commons.Logger;
import com.davorsauer.domain.Article;
import com.davorsauer.domain.ArticleMetadata;
import com.davorsauer.domain.ArticleType;
import com.davorsauer.domain.ContentData;
import com.davorsauer.error.LoadArticleException;
import com.davorsauer.error.ScanArticlesException;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public void scan() throws Exception {
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

    private Map<String, Article> scan(String path, Integer year) throws Exception {
        final Map<String, Article> articles = new HashMap<>();

        List<GHContent> contents = repository.getRepository().getDirectoryContent("/" + path);
        for (GHContent content : contents) {
            Article article = loadArticle(content, year);
            if (article != null) {
                articles.put(article.getSlug(), article);
            }
        }

        return articles;
    }

    private Article loadArticle(GHContent content, Integer year) throws IOException, LoadArticleException {
        if (!content.isDirectory()) {
            return null;
        }

        GHContent articleIndex = getPossibleContent(repository, content.getPath(), indexNamings.apply(content.getName()));
        if (articleIndex == null) {
            return null;
        }

        GHContent articleMetadata = getPossibleContent(repository, content.getPath(), metaNamings.apply(content.getName()));

        return mapArticle(articleIndex, articleMetadata, year, content.getName());
    }


    private Article mapArticle(GHContent articleIndexContent, GHContent articleMetadataContent, Integer year, String folderName) throws IOException, LoadArticleException {
        String articlePath = articleIndexContent.getPath();
        String slug = year + "_" + folderName;

        Article article = new Article();
        article.setPath(articlePath);
        article.setSlug(slug);
        article.setUrl(ARTICLE_URL + "/" + slug);
        article.setIsArchive(!currentYear.equals(year));

        if (articlePath != null) {
            article.setType(ArticleType.getTypeFromPath(articlePath));
        }

        ArticleMetadata articleMeta = null;
        if (articleMetadataContent != null) {
            try {
                articleMeta = ContentMetadataUtils.readMetadata(articleMetadataContent.read());
            } catch (IOException e) {
                warn("Can't read metadata for article: {}", article.getSlug());
            }

        }

        if (articleMeta == null) {
            articleMeta = new ArticleMetadata();
        }

            ArticleMetadata newArticleMeta = ContentMetadataUtils.readMetadata(repository, markdownService, articleIndexContent, article.getType());
        if (StringUtils.isEmpty(articleMeta.getTitle())
                || StringUtils.isEmpty(articleMeta.getDescription())
                || articleMeta.getPublishDate() == null) {

            if (articleMeta.getPublishDate() == null && newArticleMeta.getPublishDate() != null)
                articleMeta.setPublishDate(newArticleMeta.getPublishDate());

            if (StringUtils.isEmpty(articleMeta.getTitle()) && !StringUtils.isEmpty(newArticleMeta.getTitle()))
                articleMeta.setTitle(newArticleMeta.getTitle());

            if (StringUtils.isEmpty(articleMeta.getDescription()) && !StringUtils.isEmpty(newArticleMeta.getDescription()))
                articleMeta.setDescription(newArticleMeta.getDescription());
        }

        if (articleMeta != null && articleMeta.isPublished() == null && articleMeta.getPublishDate() != null && articleMeta.getPublishDate().getTime() <= System.currentTimeMillis()) {
            articleMeta.setPublished(true);
        }
        article.setMetadata(articleMeta);

        info("Load article: {} :: {} [{}] : {}",article.getMetadata().getPublishDate(), article.getMetadata().getTitle(), article.getMetadata().getTags(), article.getPath());
        return article;
    }

    public ContentData getArticle(String slug) throws Exception {
        return getArticle(slug, null);
    }

    public ContentData getArticle(String slug, String ref) throws Exception {
        String slugParts[] = slug.split("_", 2);
        if (slugParts.length != 2) {
            throw new LoadArticleException("Incorrect article slug");
        }

        String year = slugParts[0];
        String folderName = slugParts[1];
        String path = year + "/" + folderName;

        String content = null;
        ArticleMetadata contentMetadata = null;
        ArticleType articleType = ArticleType.UNDEFINED;
        Article article;

        if (ref != null) {
            GHContent articleIndex = getPossibleContent(repository, path, ref, indexNamings.apply(folderName));
            GHContent articleMetadata = getPossibleContent(repository, path, ref, metaNamings.apply(folderName));

            try {
                article = mapArticle(articleIndex, articleMetadata, Integer.parseInt(year), folderName);
            } catch (IOException e) {
                error("Required article ({}) doesn't exist!", slug);
                throw new LoadArticleException("Required article doesn't exist!");
            }
        } else {
            article = contents.get(slug);
        }

        if (article != null) {
            articleType = article.getType();
            content = repository.getArticleContent(article.getPath(), ref);
            contentMetadata = article.getMetadata();
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

    public String getHTML(String path) throws Exception {
        GHContent content = repository.getRepository().getFileContent(path);

        String html = repository.getArticleContent(content);
        if (ArticleType.MARKDOWN.equals(ArticleType.getTypeFromPath(content.getPath()))) {
            html = markdownService.renderMarkdown(html);
        }

        return html;
    }

}
