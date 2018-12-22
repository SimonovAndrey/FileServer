package server;

import util.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {
    private Map<String, Connection> users = new HashMap<>();
    private ServerSocket socket;
    private final int MAX_USERS = 20;
    private int numUsers = 0;

    public Server(int port) throws IOException {
        socket = new ServerSocket(port);

        while (numUsers < MAX_USERS) {
            System.out.println(numUsers);
            Socket userSocket = socket.accept();
            addConnection(userSocket);
            numUsers++;
        }
    }

    private void addConnection(Socket userSocket) throws IOException {
        var user = new Connection(this, userSocket);
        var name = user.getName();
        users.put(name, user);

        new Thread(user).start();
    }

    public boolean isConnected(String username) {
        return users.containsKey(username);
    }

    public void removeUser(String username) {
        users.remove(username);
        numUsers--;
    }

    public synchronized void sendPrivateMessage(String sender, String receiver, String message) {
        users.get(receiver).getWriter().println(sender + ": " + message);
    }

    public synchronized void sendMessage(String name, String message) {
        Set<String> userNames = users.keySet();
        for (String user : userNames) {
            users.get(user).getWriter().println(name + ": " + message);
            System.out.println("message sent");
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try {
            new Server(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
