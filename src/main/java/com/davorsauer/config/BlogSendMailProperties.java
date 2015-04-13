package com.davorsauer.config;

import com.davorsauer.commons.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by davor on 13/04/15.
 */
@Component
@ConfigurationProperties(prefix = "blog.sendmail")
public class BlogSendMailProperties implements Logger {

    private String host = "smtp.gmail.com";

    private Integer port = 587;

    private boolean debug = false;

    private boolean auth = true;

    private boolean starttls = true;

    private String localhost;

    private String userFrom;

    private String passwordFrom;

    private String userTo;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }

    public String getLocalhost() {
        if (localhost == null || localhost.length() == 0) {
            InetAddress ip = null;
            try {
                ip = InetAddress.getLocalHost();
                if (ip != null) {
                    localhost = ip.toString();
                }
            } catch (UnknownHostException e) {
                error("Can't get localhost IP address", e);
            }
        }
        return localhost;
    }

    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public String getPasswordFrom() {
        return passwordFrom;
    }

    public void setPasswordFrom(String passwordFrom) {
        this.passwordFrom = passwordFrom;
    }

    public String getUserTo() {
        return userTo;
    }

    public void setUserTo(String userTo) {
        this.userTo = userTo;
    }
}
