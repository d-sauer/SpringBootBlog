package com.davorsauer.domain;

import java.util.Arrays;

/**
 * Created by davor on 06/05/15.
 */
public enum ArticleType {
    HTML("html", "htm"), MARKDOWN("md"), UNDEFINED;

    private String [] extension;
    ArticleType(String ...extension) {
        this.extension = extension;
    }

    public static ArticleType getType(String extension) {
        for(ArticleType type : ArticleType.values()) {
            if(Arrays.asList(type.extension).contains(extension)) {
                return type;
            }
        }

        return ArticleType.UNDEFINED;
    }
    public static ArticleType getTypeFromPath(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1);
        return getType(extension);
    }
}
