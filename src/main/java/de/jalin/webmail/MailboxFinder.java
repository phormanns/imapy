package de.jalin.webmail;

import de.jalin.imap.IMAPyException;

public interface MailboxFinder {

    public void setLogin(String login) throws IMAPyException;

    public String getImapHost();

    public String getImapUser();

    public String getSmtpHost();

    public String getSmtpUser();

}
