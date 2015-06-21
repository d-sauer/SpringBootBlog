package com.davorsauer.domain;

/**
 * Created by davor on 05/05/15.
 */
public class Article implements Comparable<Article> {

    private String path;

    private String url;

    private String slug;

    private boolean isArchive;

    private ArticleMetadata metadata;

    private ArticleType type = ArticleType.HTML;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setIsArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }

    public ArticleMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ArticleMetadata metadata) {
        this.metadata = metadata;
    }

    public ArticleType getType() {
        return type;
    }

    public void setType(ArticleType type) {
        this.type = type;
    }

    @Override
    public int compareTo(Article content) {
        if (this == content)
            return 0;

        ArticleMetadata thisMetadata = getMetadata();
        ArticleMetadata compareMetadata = content.getMetadata();

        if (thisMetadata != null && compareMetadata != null) {
            Long thisPublishDate = thisMetadata.getPublishDate() != null ? thisMetadata.getPublishDate().getTime() : 0;
            Long comparePublishDate = compareMetadata.getPublishDate() != null ? compareMetadata.getPublishDate().getTime() : 0;

            if (thisPublishDate == comparePublishDate)
                return 0;
            else if (thisPublishDate > comparePublishDate)
                return -1;
            else if (thisPublishDate < comparePublishDate)
                return 1;
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Article) {
            return this.getUrl().equals(((Article) obj).getUrl());
        }

        return false;
    }
}
