package com.davorsauer.service;

import com.davorsauer.commons.Logger;
import com.davorsauer.error.LoadArticleException;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Created by davor on 23/06/15.
 */
@Service
public class MarkdownService implements Logger {

    private Invocable invocable;

    public MarkdownService() {
        try {
            invocable = loadMarkdownEngine();
        } catch (ScriptException e) {
            error("Can't load JavaScript engine", e);
        }
    }

    private Invocable loadMarkdownEngine() throws ScriptException {
        trace("Loading JS engine (nashorn)");
        InputStream inputStream = BlogService.class.getClassLoader().getResourceAsStream("javascript/marked.min.js");

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");

        engine.eval(new InputStreamReader(inputStream));

        return (Invocable) engine;
    }

    public String renderMarkdown(String markdown) throws LoadArticleException {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }


        if (invocable != null) {
            try {
                Object result = invocable.invokeFunction("marked", markdown);
                return result.toString();
            } catch (Exception e) {
                new LoadArticleException("Can't load article markdown", e);
            }
        } else {
            warn("JavaScript Markdown engine is not loaded!");
        }

        return markdown;
    }

}
