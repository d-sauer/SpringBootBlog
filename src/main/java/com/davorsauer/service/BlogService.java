package com.davorsauer.service;

import com.davorsauer.config.BlogProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by davor on 13/04/15.
 */
@Service
public class BlogService {

    @Autowired
    private BlogProperties blogProperties;


    public String getHtml(String blogSlug) {
        return "<b>custom blog text from " + blogSlug + "</b>";
    }

}
