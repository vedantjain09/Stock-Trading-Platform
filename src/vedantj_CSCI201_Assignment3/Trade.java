package vedantj_CSCI201_Assignment3;

public class Trade {
    private int time;
    private String ticker;
    private double quantity;

    public Trade(int time, String ticker, int quantity) {
        this.time = time;
        this.ticker = ticker;
        this.quantity = quantity;
    }

    // Getters
    public int getTime() {
        return time;
    }

    public String getTicker() {
        return ticker;
    }

    public double getQuantity() {
        return quantity;
    }
    
    @Override
    public String toString() {
        // Helper method for logging and debugging
        return String.format("Time: %d, Ticker: %s, Quantity: %d", time, ticker, quantity);
    }

	public int getTraderId() {
		// TODO Auto-generated method stub
		return 1;
	}
}
