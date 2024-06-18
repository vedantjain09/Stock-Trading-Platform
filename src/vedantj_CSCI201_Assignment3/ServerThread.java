//package vedantj_CSCI201_Assignment3;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//
//public class ServerThread extends Thread {
//    private Socket socket;
//    private Server server;
//    private PrintWriter out;
//    private BufferedReader in;
//    
//    // Make startserver a class with a threads extension, every client that connects
//    // make a new thread for it, then read through balances for each client and send trade output + timestamp
//    // accordingly//
//    
//    public ServerThread(Socket socket, Server server) {
//        this.socket = socket;
//        this.server = server;
//    }
//
//    @Override
//    public void run() {
//        try {
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                // Process input from clients. For simplicity, we'll just echo it back.
//                out.println("Echo: " + inputLine);
//            }
//        } catch (IOException e) {
//            System.out.println("Exception in ServerThread: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try {
//                in.close();
//                out.close();
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}



//package vedantj_CSCI201_Assignment3;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//
//public class ServerThread extends Thread {
//    private Socket socket;
//    private Server server;
//    private PrintWriter out;
//    private BufferedReader in;
//    private int traderId;
//
//    public ServerThread(Socket socket, Server server, int traderId) {
//        this.socket = socket;
//        this.server = server;
//        this.traderId = traderId;
//        try {
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            start(); // Start the thread immediately upon creation
//        } catch (IOException e) {
//            System.out.println("Exception in ServerThread constructor for trader " + traderId + ": " + e.getMessage());
//        }
//    }
//
//    public void sendMessage(String message) {
//        out.println(message); // Send message to the connected client
//    }
//
//    public int getTraderId() {
//        return traderId; // Return the trader ID associated with this thread
//    }
//
//    @Override
//    public void run() {
//        try {
//            // Initially send a welcome message or any other initialization messages
//            sendMessage("Connected as Trader ID " + traderId);
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                // Example: Process or log incoming messages, in this simulation we'll just echo them back
//                System.out.println("Message from trader " + traderId + ": " + inputLine);
//                // Echo back the message to the client, in a real application, this could be commands from the client
//                sendMessage("Server echo: " + inputLine);
//            }
//        } catch (IOException e) {
//            System.out.println("Exception in ServerThread.run() for trader " + traderId + ": " + e.getMessage());
//        } finally {
//            try {
//                out.close();
//                in.close();
//                socket.close(); // Close the socket when done
//            } catch (IOException e) {
//                System.out.println("Exception closing resources in ServerThread for trader " + traderId + ": " + e.getMessage());
//            }
//        }
//    }
//}


package vedantj_CSCI201_Assignment3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private int traderId;
    private double profit;
    private double cost;

    public ServerThread(Socket socket, Server server, int traderId) {
        this.socket = socket;
        this.server = server;
        this.traderId = traderId;
        profit = 0;
        cost = 0;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            start(); // Start the thread immediately upon creation
        } catch (IOException e) {
            System.out.println("Exception in ServerThread constructor for trader " + traderId + ": " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        out.println(message); // Send message to the connected client
        out.flush(); // Ensure the message is sent immediately
    }

    public int getTraderId() {
        return traderId; // Return the trader ID associated with this thread
    }
    
    public void profit (double add) {
    	profit += add;
    }
    
    public void cost (double add) {
    	profit += add;
    }
    
    

    @Override
    public void run() {
        try {
            // Initially send a welcome message or any other initialization messages
            sendMessage("Connected as Trader ID " + traderId);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // This area could be adapted if the server expects commands or messages from the client
                // For instance, you might handle trade confirmation messages or client requests here
                System.out.println("Message from trader " + traderId + ": " + inputLine);
                // Echo back or process the client message
                // sendMessage("Server echo: " + inputLine); // Optional based on your protocol
                
                // If implementing client commands, you might handle them here
                // Example: if(inputLine.equals("CHECK_BALANCE")) { checkBalance(); }
            }
        } catch (IOException e) {
            System.out.println("Exception in ServerThread.run() for trader " + traderId + ": " + e.getMessage());
        } finally {
            try {
                out.close();
                in.close();
                socket.close(); // Close the socket when done
            } catch (IOException e) {
                System.out.println("Exception closing resources in ServerThread for trader " + traderId + ": " + e.getMessage());
            }
        }
    }
    
    // Example method for handling balance check - this would require support on the server side
//    private void checkBalance() {
//        double balance = server.getTraderBalance(traderId);
//        sendMessage("Your balance is: $" + balance);
//    }
}

