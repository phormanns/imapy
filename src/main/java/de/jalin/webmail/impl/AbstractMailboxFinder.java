package de.jalin.webmail.impl;

import de.jalin.webmail.MailboxFinder;

public abstract class AbstractMailboxFinder implements MailboxFinder {

    private String host;
    private String user;

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getUser() {
        return user;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
