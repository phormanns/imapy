package de.jalin.webmail.impl;

import de.jalin.webmail.MailboxFinder;

public abstract class AbstractMailboxFinder implements MailboxFinder {

    private String imapHost;
    private String smtpHost;
    private String imapUser;
    private String smtpUser;

    @Override
    public String getImapHost() {
        return imapHost;
    }

    @Override
    public String getImapUser() {
        return imapUser;
    }

    @Override
    public String getSmtpHost() {
        return smtpHost;
    }

    @Override
    public String getSmtpUser() {
        return smtpUser;
    }
    
    public void setImapHost(String imapHost) {
        this.imapHost = imapHost;
    }

    public void setImapUser(String imapUser) {
        this.imapUser = imapUser;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

}
