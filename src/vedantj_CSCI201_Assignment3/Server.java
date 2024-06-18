package vedantj_CSCI201_Assignment3;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.Math;


public class Server {
    private static final int PORT = 3456;
    private List<ServerThread> serverThreads = new ArrayList<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private List<Trade> trades = new ArrayList<>();
    private HashMap<Integer, Double> traders = new HashMap<>();
    private int expectedTraderCount = 0; // Expected number of traders to connect
    private int nextTraderId = 1; // To track the next trader ID for new connections
    long programStartTime; // Initialize at the start of the server
    private CountDownLatch latch;


    public Server() {
    	this.programStartTime = System.currentTimeMillis(); // Set program start time
    }
    
    private boolean loadTraders(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2 || !parts[0].matches("\\d+") || !parts[1].matches("\\d+")) {
                    System.out.println("Invalid line format in traders file: " + line);
                    return false; // Indicate failure
                }
                int traderId = Integer.parseInt(parts[0]);
                expectedTraderCount++;
                Double balance = Double.parseDouble(parts[1]);
                traders.put(traderId, balance);
            }
        } catch (IOException e) {
            System.out.println("Error reading traders file: " + e.getMessage());
            return false; // Indicate failure
        }
        return true; // Successfully loaded
    }

    private boolean loadTrades(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3 || !parts[0].matches("\\d+") || !parts[2].matches("-?\\d+")) {
                    System.out.println("Invalid line format in schedule file: " + line);
                    return false; // Indicate failure
                }
                int time = Integer.parseInt(parts[0]);
                String ticker = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                Trade trade = new Trade(time, ticker, quantity);
                trades.add(trade);
                //scheduler.schedule(() -> executeTrade(trade), time, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            System.out.println("Error reading schedule file: " + e.getMessage());
            return false; // Indicate failure
        }
        return true; // Successfully loaded
    }
    
    private void startTradeExecution() {
    	latch = new CountDownLatch(trades.size());
        programStartTime = System.currentTimeMillis(); // Reset start time when beginning trade execution
    	//programStartTime = Instant.now();
        for (Trade trade : trades) {
        	long timeElapsed = System.currentTimeMillis() - programStartTime;
//            System.out.println(trade.getTicker() + " " + trade.getTime());
        	long delay = (trade.getTime() * 1000L) - timeElapsed; // Delay in milliseconds
        	delay = Math.abs(delay);
        	//System.out.println(trade.getTicker() + " " + trade.getTime() + " " + delay);
            scheduler.schedule(() -> {
				executeTrade(trade);
			}, delay, TimeUnit.MILLISECONDS);
        }
        
        new Thread(() -> {
            try {
                latch.await(); // Wait for all trades to complete
                // After all trades have been executed, broadcast the message
                broadcastCompletionMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void broadcastCompletionMessage() {
        String completionMessage = "Processing Done";
        // Print on the server console
        System.out.println(completionMessage);
        // Broadcast to all connected clients
        serverThreads.forEach(st -> st.sendMessage(completionMessage));
    }
    
    private String formatRelativeTimestamp(long millisElapsed) {
        long hours = TimeUnit.MILLISECONDS.toHours(millisElapsed);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisElapsed) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisElapsed) % 60;
        long millis = millisElapsed % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }
    
    private void executeTrade(Trade trade) {
        String priceDataJson = fetchStockPrice(trade.getTicker());
        double price = parsePriceFromJson(priceDataJson);
        double tradeTotal = price * Math.abs(trade.getQuantity());
        boolean tradeExecuted = false; // Flag to indicate if the trade has been executed

        for (ServerThread st : serverThreads) {
            Double balance = traders.get(st.getTraderId());
            if (trade.getQuantity() > 0) { // For purchases
                if (balance != null && balance >= tradeTotal && !tradeExecuted) {
                    balance -= tradeTotal; // Update balance
                    traders.put(st.getTraderId(), balance); // Save updated balance
                    tradeExecuted = true; // Set flag to true as trade is now executed
                } else {
                    continue; // Skip to next iteration if the trader cannot fulfill the purchase
                }
            }
            // Proceed to send messages if it's a sale or if it's a purchase that can be fulfilled
            if (tradeExecuted || trade.getQuantity() < 0) {
                sendTradeMessages(st, trade, price, tradeTotal);
                break; // Break after sending messages to avoid multiple notifications
            }
        }
        
        latch.countDown();
        
        
    }

    private void sendTradeMessages(ServerThread st, Trade trade, double price, double tradeTotal) {
        // Calculate timestamps
        long timeElapsedBeforeTrade = System.currentTimeMillis() - programStartTime;
        String timestampBeforeTrade = formatDuration(timeElapsedBeforeTrade);
        String tradeType = trade.getQuantity() > 0 ? "purchase" : "sale";
        
        try {
            // Assigned trade message
            st.sendMessage("[" + timestampBeforeTrade + "] Assigned " + tradeType + " of " + Math.abs(trade.getQuantity()) + 
                " stock(s) of " + trade.getTicker() + ". Total cost estimate = " + price + " * " + Math.abs(trade.getQuantity()) + 
                " = " + tradeTotal + ".");
            
            // Starting trade message
            long timeElapsedStartTrade = System.currentTimeMillis() - programStartTime;
            String timestampStartTrade = formatDuration(timeElapsedStartTrade);
            st.sendMessage("[" + timestampStartTrade + "] Starting " + tradeType + " of " + Math.abs(trade.getQuantity()) +
                " stock(s) of " + trade.getTicker() + ". Total cost = " + tradeTotal + ".");
            
            // Simulate trade execution time
            Thread.sleep(1000);
            
            // Finished trade message
            long timeElapsedEndTrade = System.currentTimeMillis() - programStartTime;
            String timestampEndTrade = formatDuration(timeElapsedEndTrade);
            st.sendMessage("[" + timestampEndTrade + "] Finished " + tradeType + " of " + Math.abs(trade.getQuantity()) + " stock(s) of " + trade.getTicker() + ".");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
    
    private String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }

    private String fetchStockPrice(String ticker) {
        String apiKey = "cnsigjpr01qtn496opt0cnsigjpr01qtn496optg";
        String queryUrl = "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(queryUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString(); // This returns JSON string, you should parse it to get the price
    }

    private double parsePriceFromJson(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return jsonObject.get("c").getAsDouble(); // Assuming "c" is the current price field
    }

    private String getCurrentTimestamp() {
        return java.time.LocalTime.now().toString();
    }
    
    synchronized void checkAndStartTradeExecution() {
        // Check if all traders are connected
        if (serverThreads.size() == expectedTraderCount) {
            System.out.println("All traders have arrived! Starting service.");
            // Broadcast start message to all traders
            serverThreads.forEach(st -> st.sendMessage("Starting service."));
            // Schedule and execute trades
            startTradeExecution();
            //trades.forEach(this::executeTrade);
        }
    }
    
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            System.out.println("Waiting for traders...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection from: " + clientSocket.getInetAddress());
                
                ServerThread st = new ServerThread(clientSocket, this, nextTraderId++);
                serverThreads.add(st);
                
                // Update the number of traders needed
                int tradersNeeded = expectedTraderCount - serverThreads.size();
                
                // Check if we are still waiting for more traders
                if (tradersNeeded > 0) {
                    System.out.println("Waiting for " + tradersNeeded + " more trader(s)...");
                    st.sendMessage("Waiting for " + tradersNeeded + " more trader(s)...");
                }

                // If all traders have connected, start the trade execution process
                if (tradersNeeded == 0) {
                    checkAndStartTradeExecution();
                }
            }
        } catch (IOException ioe) {
            System.out.println("IOE in Server: " + ioe.getMessage());
        }
    }



    public static void main(String[] args) throws IOException {
        Server server = new Server();
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        
        boolean tradersLoaded = false;
        while (!tradersLoaded) {
            try {
                System.out.print("What is the name of the traders file? ");
                String tradersFilename = consoleReader.readLine();
                tradersLoaded = server.loadTraders(tradersFilename);
                if (!tradersLoaded) {
                    System.out.println("Please provide a correctly formatted traders file.");
                } else {
                    System.out.println("The traders file has been properly read.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        
        boolean tradesLoaded = false;
        while (!tradesLoaded) {
            try {
                System.out.print("What is the name of the schedule file? ");
                String scheduleFilename = consoleReader.readLine();
                tradesLoaded = server.loadTrades(scheduleFilename);
                if (!tradesLoaded) {
                    System.out.println("Please provide a correctly formatted schedule file.");
                } else {
                    System.out.println("The schedule file has been properly read.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        
        System.out.println("Listening on port " + PORT + ".");
        server.startServer();
    }
}