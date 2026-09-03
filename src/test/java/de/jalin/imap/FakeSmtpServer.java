package de.jalin.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class FakeSmtpServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final List<String> commands = new ArrayList<>();
    private final StringBuilder messageData = new StringBuilder();
    private volatile boolean messageReceived;

    FakeSmtpServer() throws IOException {
        serverSocket = new ServerSocket(0);
        serverThread = new Thread(this::serve);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    int getPort() {
        return serverSocket.getLocalPort();
    }

    List<String> getRecipientCommands() {
        synchronized (commands) {
            return new ArrayList<>(commands);
        }
    }

    String getMessageData() {
        synchronized (messageData) {
            return messageData.toString();
        }
    }

    boolean isMessageReceived() {
        return messageReceived;
    }

    private void serve() {
        try (Socket socket = serverSocket.accept()) {
            socket.setSoTimeout(20000);
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            final OutputStream out = socket.getOutputStream();
            writeLine(out, "220 fake.smtp.test ESMTP");
            boolean inData = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (inData) {
                    if (".".equals(line)) {
                        inData = false;
                        messageReceived = true;
                        writeLine(out, "250 2.0.0 OK");
                    } else {
                        synchronized (messageData) {
                            messageData.append(line).append('\n');
                        }
                    }
                    continue;
                }
                synchronized (commands) {
                    commands.add(line);
                }
                final String command = line.toUpperCase();
                if (command.startsWith("EHLO")) {
                    out.write(("250-fake.smtp.test\r\n250-8BITMIME\r\n250-AUTH PLAIN\r\n250 SIZE 35000000\r\n").getBytes(StandardCharsets.US_ASCII));
                    out.flush();
                } else if (command.startsWith("AUTH")) {
                    writeLine(out, "235 2.7.0 Authentication successful");
                } else if (command.startsWith("MAIL FROM")) {
                    writeLine(out, "250 2.1.0 OK");
                } else if (command.startsWith("RCPT TO")) {
                    writeLine(out, "250 2.1.5 OK");
                } else if (command.startsWith("DATA")) {
                    writeLine(out, "354 End data with <CR><LF>.<CR><LF>");
                    inData = true;
                } else if (command.startsWith("QUIT")) {
                    writeLine(out, "221 2.0.0 Bye");
                    return;
                } else {
                    writeLine(out, "250 2.0.0 OK");
                }
            }
        } catch (IOException e) {
        }
    }

    private static void writeLine(final OutputStream out, final String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

}
