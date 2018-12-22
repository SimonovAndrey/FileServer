package util;

import server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {

    private Server server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String name;

    public Connection(Server server, Socket userSocket) throws IOException {
        this.server = server;
        reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
        writer = new PrintWriter(userSocket.getOutputStream(), true);

        String message = reader.readLine();
        name = message.substring("@name ".length());
    }

    public String getName() {
        return name;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = reader.readLine();
                if (message == null) {
                    disconnect();
                    return;
                }
                if (message.contains("@quit")) {
                    quit();
                    return;
                } else if (message.contains("@senduser")) {
                    sendUser(message);
                } else {
                    System.out.println("Connection message sent");
                    sendAllUsers(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAllUsers(String message) {
        server.sendMessage(name, message);
    }

    private void sendUser(String message) {
        String receiver = message.substring("@senduser ".length());
        if (server.isConnected(receiver)) {
            try {
                System.out.println(receiver);
                server.sendPrivateMessage(name, receiver, reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            server.sendPrivateMessage("Server", name, "User not found");
        }
    }

    private void disconnect() {
        server.removeUser(name);
    }

    private void quit() {
        server.sendPrivateMessage("Server", name, "@quit");
        server.removeUser(name);
    }
}
