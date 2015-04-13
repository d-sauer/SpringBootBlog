package com.davorsauer.controller;

import com.davorsauer.commons.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by davor on 11/04/15.
 */

@Controller
public class WebController implements Logger {

    @RequestMapping("/")
    public String index() {
        trace("page: index");
        return "index";
    }

    @RequestMapping("/hello")
    public String index2(Model model) {
        trace("page: index2");
        return "index_bk";
    }

}
