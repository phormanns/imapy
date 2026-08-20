package de.jalin.webmail;

import de.jalin.imap.IMAPyException;

public interface MailboxFinder {

    public void setLogin(String login) throws IMAPyException;

    public String getHost();

    public String getUser();

}
