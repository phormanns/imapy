package de.jalin.imap;

public class MailAttachment {

    private final String fileName;
    private final String contentType;
    private final byte[] data;

    public MailAttachment(final String fileName, final String contentType, final byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

}
