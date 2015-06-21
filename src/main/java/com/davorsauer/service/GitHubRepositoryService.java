package com.davorsauer.service;

import com.davorsauer.config.GitHubProperties;
import com.davorsauer.error.LoadArticleException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public GHRepository getRepository() throws IOException {
        if (repository != null)
            return repository;

        GitHub gitHub = GitHub.connectUsingPassword(properties.getUser(), properties.getPassword());
        repository = gitHub.getRepository(properties.getUser() + "/" + properties.getRepository());
        return repository;
    }

    public String getArticleContent(String repositoryPath) throws LoadArticleException {
        return getArticleContent(repositoryPath, null);
    }


    public String getArticleContent(String repositoryPath, String ref) throws LoadArticleException {
        InputStream inputStream = null;
        try {
            GHContent content = null;
            if (ref == null) {
                content = getRepository().getFileContent(repositoryPath);
            } else {
                content = getRepository().getFileContent(repositoryPath, ref);
            }

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
                throw new RuntimeException("No content for " + (ref == null ? "" : "ref:" + ref + " and ") + "path:" + repositoryPath);
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
