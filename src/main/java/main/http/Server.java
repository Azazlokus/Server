package main.http;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private int port;
    private String directory;

    public Server(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            while(true){
                var socket = server.accept();
                Thread thread = new Handler(socket, this.directory);
                thread.start();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        var port = Integer.parseInt(args[0]);
        var directory = args[1];
        new Server(port, directory).start();
    }
}