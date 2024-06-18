package vedantj_CSCI201_Assignment3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private BufferedReader console;

    public Client(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            console = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Welcome to JoesStocks v2.0!");

            // Listening for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                        System.out.println(serverMessage);
                        if (serverMessage.contains("All traders have arrived!")) {
                            performTrades();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void performTrades() {
        // Simulate performing trades here
        // For demonstration purposes, this is a placeholder
        // In a real scenario, this method would handle trade execution logic based on server commands
        System.out.println("Starting service.");
        // Example: output.println("TRADE_EXECUTED,AAPL,100");
    }

    public static void main(String[] args) throws Exception {
        // Client setup
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the server hostname: ");
        String hostname = console.readLine();

        System.out.print("Enter the server port: ");
        int port = Integer.parseInt(console.readLine());

        new Client(hostname, port);
    }
}
