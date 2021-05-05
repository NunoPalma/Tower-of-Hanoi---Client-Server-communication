package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple greeting server. More information about sockets at:
 * http://download.oracle.com/javase/tutorial/networking/sockets/index.html
 * and in the book "Head First Java" - Chapter 15.
 */
public class Server
{
    // 'throws IOException' enables us to write the code without try/catch blocks
    // but it also keeps us from handling possible IO errors
    // (when for instance there is a problem when connecting with the other party)

    public static void main(String args[]) throws IOException
    {
        final ServerManager mServerManager = new ServerManager();

        // Register service on port 1234
        ServerSocket s = new ServerSocket(1234);
        while (true)
        {
            // Wait and accept a connection

            System.out.println("Waiting for a connection.");
            Socket s1 = s.accept();

            System.out.println("Connection established.");

            // Build DataStreams (input and output) to send and receive messages to/from the client
            OutputStream out = s1.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);

            InputStream in = s1.getInputStream();
            DataInputStream dataIn = new DataInputStream(in);

            if (mServerManager.requestCredentials(dataOut, dataIn, s1)) {
                mServerManager.initGame();
            } else {
                dataOut.writeUTF("Closing the connection");
                mServerManager.cleanup();
            }


            mServerManager.cleanup();
        }
    }
}