package com.davorsauer.controller;

import com.davorsauer.commons.Logger;
import com.davorsauer.config.BlogProperties;
import com.davorsauer.domain.ContentData;
import com.davorsauer.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;

/**
 * Created by davor on 13/04/15.
 */
@Controller
public class BlogController implements Logger {

    private static final String DEFAULT_BLOG_TEMPLATE = "blogTemplate";

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogProperties blogProperties;

    @RequestMapping(value = "/blog", method = {RequestMethod.GET})
    public String redirect() {
        return "redirect:/";
    }


    @RequestMapping(value = "/blog/{slug}", method = {RequestMethod.GET})
    public String blog(@PathVariable("slug") @CacheKey String slug, Model model) throws Exception {
        ContentData content = blogService.getArticle(slug);
        model.addAttribute("blog_content", content.getContent());
        if (content.getMetadata() != null) {
            if (content.getMetadata().getTags() != null)
                model.addAttribute("blog_tags", content.getMetadata().getTags());
            if (content.getMetadata().getPublishDate() != null)
                model.addAttribute("blog_publish_date", content.getMetadata().getFormatPublishDate());
        }

        return DEFAULT_BLOG_TEMPLATE;
    }

    @RequestMapping(value = "/preview/{branch}/{slug}", method = {RequestMethod.GET})
    public String preview(@PathVariable("branch") String branch, @PathVariable("slug") String slug, Model model) throws Exception {
        ContentData content = blogService.getArticle(slug, branch);
        model.addAttribute("blog_content", content.getContent());
        if (content.getMetadata() != null) {
            if (content.getMetadata().getTags() != null)
                model.addAttribute("blog_tags", content.getMetadata().getTags());
            if (content.getMetadata().getPublishDate() != null)
                model.addAttribute("blog_publish_date", content.getMetadata().getFormatPublishDate());
        }

        return DEFAULT_BLOG_TEMPLATE;
    }

    @RequestMapping(value = {"/blog_reload"})
    public String blogReload() throws Exception {
        info("Start reload..");
        blogService.scan();
        info("End reload!");

        return "redirect:/";
    }

}
