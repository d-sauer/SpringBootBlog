package com.davorsauer.controller;

import com.davorsauer.commons.Logger;
import com.davorsauer.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by davor on 13/04/15.
 */
@Controller
public class BlogController implements Logger {

    @Autowired
    private BlogService blogService;

    @RequestMapping(value = "/blog/{slug}", method = {RequestMethod.GET})
    public String blog(@PathVariable("slug") String slug, Model model) {
        debug("Open blog: {}", slug);

        String content = blogService.getHtml(slug);
        model.addAttribute("blog_content", content);

        return "blogTemplate";
    }

}
