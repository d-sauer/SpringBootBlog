package com.davorsauer.controller;

import com.davorsauer.commons.Logger;
import com.davorsauer.commons.NotifyType;
import com.davorsauer.dto.ContactReq;
import com.davorsauer.dto.ContactRes;
import com.davorsauer.service.BlogService;
import com.davorsauer.service.SendMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by davor on 11/04/15.
 */
@Controller
public class WebController implements Logger {

    @Lazy
    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private BlogService blogService;


    @RequestMapping("/")
    public String index(Model model) {

        model.addAttribute("articles", blogService.getContents());
        model.addAttribute("test", "test content " + new Date());

        return "index";
    }

    @RequestMapping(value = {"/about"})
    public String about() {

        return "about";
    }

    @RequestMapping(value = {"/portfolio"})
    public String portfolio() {

        return "portfolio";
    }

    @RequestMapping(value = {"/contact"})
    public String contact() {

        return "contact";
    }

    @RequestMapping(value = { "/contact_send" }, method = RequestMethod.POST)
    public @ResponseBody
    ContactRes contactSend(HttpServletRequest request) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String subject = request.getParameter("subject");
        String message = request.getParameter("message");

        ContactReq creq = new ContactReq();
        creq.setName(name);
        creq.setEmail(email);
        creq.setSubject(subject);
        creq.setMessage(message);

        ContactRes cr = new ContactRes();
        cr.setStatus(0);
        boolean canSend = true;
        if (name == null || name.trim().length() == 0) {
            canSend = false;
            cr.setMessage("Enter name");
        }
        else if (email == null || email.trim().length() == 0 || !email.contains("@")) {
            canSend = false;
            cr.setMessage("Enter email");
        }
        else if (subject == null || subject.trim().length() == 0) {
            canSend = false;
            cr.setMessage("Enter subject");
        }
        else if (message == null || message.trim().length() == 0) {
            canSend = false;
            cr.setMessage("Enter message");
        }

        if (canSend==false) {
            cr.setNotifyType(NotifyType.ALERT);
        } else {

            try {
                StringBuilder msg = new StringBuilder();
                msg.append("User header:");

                Enumeration<String> en = request.getHeaderNames();
                while(en.hasMoreElements()) {
                    String key = en.nextElement();
                    msg.append("\n" + key + ": " + request.getHeader(key));
                }

                msg.append("\n\nFrom: " + name + "<" + email + ">");
                msg.append("\n\nMessage:\n" + message);

                sendMailService.sendMail(email, name, subject, msg.toString());
                cr.setStatus(1);
                cr.setNotifyType(NotifyType.SUCCESS);
                cr.setMessage("Email was sent!");
            } catch(Exception e) {
                error("Sending email error!", e);

                cr.setNotifyType(NotifyType.FAILURE);
                cr.setMessage("Email wasn't sent!");
            }
        }
        return cr;
    }

}
