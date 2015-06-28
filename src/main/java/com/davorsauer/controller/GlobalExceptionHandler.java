package com.davorsauer.controller;

import com.davorsauer.commons.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by davor on 14/06/15.
 */
@ControllerAdvice
public class GlobalExceptionHandler implements Logger {

    @ExceptionHandler(Exception.class)
    public ModelAndView allExceptions(Exception exception) {
        error("Blog exception", exception);

        String message = exception.getMessage();
        if (StringUtils.isEmpty(message))
            message = "Article is not available!";

        ModelAndView mav = new ModelAndView();
        mav.addObject("message", message);
        mav.setViewName("error");
        return mav;
    }

}
