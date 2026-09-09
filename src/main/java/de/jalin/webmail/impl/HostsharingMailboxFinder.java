package de.jalin.webmail.impl;

import de.jalin.imap.IMAPyException;

public class HostsharingMailboxFinder extends AbstractMailboxFinder {

    @Override
    public void setLogin(String login) throws IMAPyException {
        if (login == null || login.length() < 5) {
            throw new IMAPyException("invalid login");
        }
        this.setImapUser(login);
        this.setSmtpUser(login);
        final String webspaceDomain = login.substring(0, 5) + ".hostsharing.net";
        this.setImapHost(webspaceDomain);
        this.setSmtpHost(webspaceDomain);
    }

}
