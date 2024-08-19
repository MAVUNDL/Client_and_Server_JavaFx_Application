package acsse.csc2b.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class will handle the functionalities of the server
 */
public class ImgServer
{
    /**
     * This method creates the server
     * @param args all the args needed by the program
     */
    public static void main(String[] args)
    {
        // creating the server
       ImgServer server = new ImgServer();
    }

    /**
     * This constructor  creates the server and creates all the streams
     */
    public ImgServer()
    {
        int port = 5432;
        // create server socket
        try(ServerSocket serverSocket = new ServerSocket(port))
        {
            System.out.println("server started");
            // keep the server running
            while(true)
            {
                // accept a client
                Socket socket = serverSocket.accept();
                // initialize streams
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                /*
                     Handle requests from client
                 */
                new ClientHandler(dataOut, dataIn, out, in);
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not listen on port: " + port);
            System.out.println("IOException: " + e.getMessage());
        }

    }
}
