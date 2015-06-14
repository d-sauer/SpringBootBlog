package com.davorsauer.service;

import com.davorsauer.commons.Logger;
import com.davorsauer.config.BlogProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.pegdown.PegDownProcessor;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by davor on 06/05/15.
 */
//@RunWith(MockitoJUnitRunner.class)
public class BlogServiceTest implements Logger {

    @Test
    public void testGetArticleMarkdown() throws Exception {
        Path markdownFilePath = Paths.get(this.getClass().getClassLoader().getResource("test_article.md").getPath());

        final StringBuilder buffer = new StringBuilder();
        try(BufferedReader reader = Files.newBufferedReader(markdownFilePath)) {
            reader.lines().forEach(buffer::append);
        }

        PegDownProcessor mdProcessor = new PegDownProcessor();
        String html = mdProcessor.markdownToHtml(buffer.toString());

        info(html);
    }
}