package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Client(String host, int port, String username) throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread receiver = getReceiver();
        Thread sender = getSender(username);

        System.out.println("Welcome to chat, " + username);

        receiver.start();
        sender.start();

        try {
            sender.join();
            receiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writer.close();
        reader.close();
        socket.close();
    }

    private Thread getSender(String name) {
        return new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            writer.println("@name " + name);
            while (true) {
                String message = scanner.nextLine();
                writer.println(message);
                if (message.contains("@quit"))
                    return;
            }
        });
    }

    private Thread getReceiver() {
        return new Thread(() -> {
            while (true) {
                try {
                    String message = reader.readLine();
                    if (message.equals("Server: @quit"))
                        return;
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        try {
            new Client(host, port, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
