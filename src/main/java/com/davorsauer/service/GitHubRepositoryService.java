package com.davorsauer.service;

import com.davorsauer.config.GitHubProperties;
import com.davorsauer.error.LoadArticleException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by davor on 22/04/15.
 */
@Service
public class GitHubRepositoryService {

    @Autowired
    private GitHubProperties properties;

    private GHRepository repository;

    public GHRepository getRepository() throws Exception {
        if (repository != null)
            return repository;
        GitHub gitHub = null;

        if (!StringUtils.isEmpty(properties.getoAuth())) {
            gitHub = GitHub.connectUsingOAuth(properties.getoAuth());
            return repository;
        } else if (!StringUtils.isEmpty(properties.getUser()) && !StringUtils.isEmpty(properties.getPassword())) {
            gitHub = GitHub.connectUsingPassword(properties.getUser(), properties.getPassword());
        }

        if (gitHub == null) {
            throw new Exception("Missing GitHub properties for connecting to repository (username/password, or username and OAuth key)");
        }

        repository = gitHub.getRepository(properties.getUser() + "/" + properties.getRepository());
        return repository;
    }

    public String getArticleContent(String repositoryPath) throws Exception {
        return getArticleContent(repositoryPath, null);
    }


    public String getArticleContent(String repositoryPath, String ref) throws Exception {
        GHContent content;
        try {
            if (ref == null) {
                content = getRepository().getFileContent(repositoryPath);
            } else {
                content = getRepository().getFileContent(repositoryPath, ref);
            }

            return getArticleContent(content);
        } catch (IOException e) {
            throw new LoadArticleException(e);
        }
    }

    public String getArticleContent(GHContent content) throws LoadArticleException {
        InputStream inputStream = null;
        try {
            if (content != null) {
                StringBuilder buffer = new StringBuilder();
                byte[] readBuffer = new byte[1024];
                inputStream = content.read();
                while (inputStream.read(readBuffer) != -1) {
                    buffer.append(new String(readBuffer, "UTF-8"));
                    readBuffer = new byte[1024];
                }

                return buffer.toString();
            } else {
                throw new LoadArticleException("No content for " + content.getName() + " : " + content.getGitUrl());
            }
        } catch (IOException e) {
            throw new LoadArticleException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new LoadArticleException(e);
                }
            }
        }
    }

}
