package com.davorsauer.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by davor on 14/06/15.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView allExceptions(Exception exception) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("message", exception.getMessage());
        mav.setViewName("error");
        return mav;
    }

}
