package com.davorsauer.commons;

import com.davorsauer.domain.Article;
import com.davorsauer.service.GitHubRepositoryService;
import org.kohsuke.github.GHContent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by davor on 23/06/15.
 */
public class BlogUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BlogUtils.class);

    public static final Function<String, String[]> indexNamings = (customName) -> new String[]{"index.html", "index.htm", "index.md", customName + ".html", customName + ".htm", customName + ".md"};

    public static final Function<String, String[]> metaNamings = (customName) -> new String[]{"metadata", customName + ".meta", customName};

    public static final Function<Article, Long> getPublishDate = (contentLocation -> {
        if (contentLocation.getMetadata() != null && contentLocation.getMetadata().getPublishDate() != null) {
            return contentLocation.getMetadata().getPublishDate().getTime();
        } else {
            return 0L;
        }
    }
    );


    public static final Comparator<Map.Entry<String, Article>> sortByValue = (entry1, entry2) -> {
        long publishDate1 = getPublishDate.apply(entry1.getValue());
        long publishDate2 = getPublishDate.apply(entry2.getValue());

        if (publishDate1 > publishDate2)
            return -1;
        else if (publishDate1 < publishDate2)
            return 1;
        return 0;
    };

    public static GHContent getPossibleContent(GitHubRepositoryService repository, String path, String[] variations) {
        return getPossibleContent(repository, path, null, variations);
    }


    public static GHContent getPossibleContent(GitHubRepositoryService repository, String path, String ref, String[] variations) {
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
            } catch (Exception e) {
                LOG.error("Reading possible content, path:" + path + "(" + variation + "), ref:" + Objects.toString(ref, "master"), e);
            }
        }

        return content;
    }
}
