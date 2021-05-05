package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * A simple greeting client. More information about sockets at:
 * http://download.oracle.com/javase/tutorial/networking/sockets/index.html
 * and in the book "Head First Java" - Chapter 15.
 */
public class Client
{
    // 'throws IOException' enables us to write the code without try/catch blocks
    // but it also keeps us from handling possible IO errors
    // (when for instance there is a problem when connecting with the other party)
    public static void main(String args[]) throws IOException
    {
        initProcess();
    }

    private static void initProcess() throws IOException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Connect to Server Hanói? [Y/N]");

            String response = scanner.nextLine();

            if (response.equals("Y"))
                break;
            else if(response.equals("N")) {
                System.out.println("Bye bye.");
                System.exit(0);
            }
        }

        // Open your connection to a server, at port 1234
        Socket socket = new Socket("localhost", 1234);

        // Build DataStreams (input and output) to send and receive messages to/from the server
        InputStream in = socket.getInputStream();
        DataInputStream dataIn = new DataInputStream(in);

        OutputStream out = socket.getOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);

        validateAuthenticationRequest(dataOut, dataIn);

        // Cleanup operations, close the streams, socket and then exit
        dataOut.close();
        dataIn.close();
        socket.close();
    }

    private static void validateAuthenticationRequest(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {

        if (authenticate(dataOutputStream, dataInputStream)) {
            initGame(dataOutputStream, dataInputStream);
        }
    }

    static boolean authenticate(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        Scanner scanner = new Scanner(System.in);

        String userNameRequest = dataInputStream.readUTF();
        System.out.println(userNameRequest);

        String userName = scanner.nextLine();

        dataOutputStream.writeUTF(userName);

        String passwordRequest = dataInputStream.readUTF();
        System.out.println(passwordRequest);

        String password = scanner.nextLine();

        dataOutputStream.writeUTF(password);

        String authenticationResponse = dataInputStream.readUTF();
        System.out.println(authenticationResponse);

        if (authenticationResponse.contains("Invalid credentials")) {
            String response = scanner.nextLine();

            if (response.equals("Y")) {
                dataOutputStream.writeUTF(response);
                return authenticate(dataOutputStream, dataInputStream);
            } else
                dataOutputStream.writeUTF(response);
        } else {
            return authenticationResponse.equals("Welcome " + userName + ", the server accepted you!");
        }

        return false;
    }

    static void initGame(DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) {
                while (true) {
                    String message = dataInputStream.readUTF();
                    if (message.equals("END"))
                        break;
                    System.out.println(message);
                }
                dataOutputStream.writeUTF(scanner.nextLine());
            }
        } catch (IOException ioe) {
            initProcess();
        }

    }
}