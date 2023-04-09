package project.server;

import project.reflection.DIEngine;
import project.reflection.MyReflection;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int TCP_PORT = 8080;

    public static void main(String[] args) throws IOException {

        DIEngine diEngine = new DIEngine();

        try {
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server is running at http://localhost:"+TCP_PORT);
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new ServerThread(socket)).start();
                //mr.pathInterceptor();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}