package de.jalin.webmail.impl;

import de.jalin.imap.IMAPyException;

public class HostsharingMailboxFinder extends AbstractMailboxFinder {

    @Override
    public void setLogin(String login, String password) throws IMAPyException {
        this.setPassword(password);
        if (login == null || login.length() < 5) {
            throw new IMAPyException("invalid login");
        }
        this.setUser(login);
        this.setHost(login.substring(0, 5) + ".hostsharing.net");
    }

}
