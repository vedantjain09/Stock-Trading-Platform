# JoesStocks v2.0 - A Real-Time Stock Trading Platform

## Overview

JoesStocks v2.0 is an advanced stock trading platform that implements real-time trade execution over a network using multi-threading and concurrency control. This project builds upon the initial JoesStocks application by adding real data retrieval from the Finnhub API, networking capabilities, and handling multiple traders simultaneously.

## Features

- **Real-Time Trade Execution**: Execute trades in real-time with multiple traders over a network.
- **Concurrency Management**: Handle multiple trades and traders concurrently with proper synchronization.
- **API Integration**: Fetch real-time stock prices from the Finnhub API.
- **Data Validation**: Ensure the validity of input data from CSV files.
- **Multi-Client Support**: Allow multiple traders to connect and execute trades simultaneously.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Finnhub API Key

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/joesstocks-v2.git
   cd joesstocks-v2
   ```

2. **Set Up Finnhub API Key**:
   - Obtain an API key from [Finnhub](https://finnhub.io/).
   - Update the API key in `src/main/resources/apikey.properties`.

3. **Prepare CSV Files**:
   - Create two CSV files: `schedule.csv` and `traders.csv`.
     - `schedule.csv` should contain trade schedules in the format: `timestamp,ticker,quantity`.
     - `traders.csv` should contain trader information in the format: `serial_number,initial_balance`.

### Usage

#### Server

1. **Run the Server**:
   ```bash
   java -cp target/joesstocks-v2.jar com.joesstocks.Server
   ```

2. **Provide Input Files**:
   - When prompted, enter the names of the `schedule.csv` and `traders.csv` files.

3. **Wait for Traders**:
   - The server will wait for the specified number of trader clients to connect before starting the service.

#### Client

1. **Run the Client**:
   ```bash
   java -cp target/joesstocks-v2.jar com.joesstocks.Client
   ```

2. **Provide Server Details**:
   - When prompted, enter the server hostname (e.g., `localhost`) and port (default: `3456`).

3. **Wait for All Traders**:
   - The client will wait for all required traders to connect before initiating trades.

### Example CSV Files

#### `schedule.csv`
```
0,AAPL,2
0,AAPL,-1
0,AMD,5
3,MSFT,-1
3,MSFT,-2
9,MSFT,-1
9,MSFT,1
9,TSLA,2
12,TSLA,10
12,TSLA,-3
```

#### `traders.csv`
```
1,1000
2,2000
```

### Program Execution

1. **Server Output**:
   ```
   What is the name of the schedule file? schedule.csv
   The schedule file has been properly read.
   What is the name of the traders file? traders.csv
   The traders file has been properly read.
   Listening on port 3456.
   Waiting for traders...
   Connection from: /127.0.0.1
   Waiting for 1 more trader(s)...
   Connection from: /127.0.0.1
   Starting service.
   Processing complete.
   ```

2. **Client Output**:
   ```
   Welcome to JoesStocks v2.0!
   Enter the server hostname: localhost
   Enter the server port: 3456
   1 more trader is needed before the service can begin.
   Waiting...
   All traders have arrived!
   Starting service.
   [00:00:00.006] Assigned purchase of 2 stock(s) of AAPL. Total cost estimate = 185.85 * 2 = 371.70.
   [00:00:01.013] Finished purchase of 2 stock(s) of AAPL.
   ...
   ```

### Acknowledgments

- [Finnhub API](https://finnhub.io/) for providing stock market data.
- The CSCI 201 Spring 2024 course for the initial project guidelines and requirements.

---
