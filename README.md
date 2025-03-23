# STOMP Server-Client Feed System

## Project Overview
This project implements a STOMP (Simple Text Oriented Messaging Protocol) messaging system consisting of a server and client. The system allows clients to connect to the server, subscribe to topics, publish messages, and receive updates in real-time. The main focus is on implementing a reliable communication system for a sports event feed where users can report, subscribe to, and receive game updates across different channels.

## Architecture

### Server
- Implemented in Java using Maven
- Multithreaded TCP server that handles STOMP protocol messages
- Supports both thread-per-client and reactor (non-blocking I/O) operation modes
- Manages user connections, subscriptions, and message routing
- Implements connection persistence and message delivery guarantees
- Handles user authentication and channel management

### Client
- Implemented in C++ with Boost libraries
- Command-line interface for user interaction
- Handles asynchronous communication with the server
- Uses Boost.Asio for network communication
- Processes STOMP frames for protocol compliance
- Supports concurrent message sending and receiving
- Implements a robust event reporting system for game updates
- Maintains subscription state across reconnections

### Prerequisites

#### For All Platforms

- Java JDK 8 or higher (for the server)
- Maven (for building the server)
- C++ compiler with C++11 support
- Boost library (1.66.0 or higher recommended)

## Building and Running the Project

### Running with Docker (Recommended)

#### Prerequisites
- Docker and Docker Compose installed on your system

#### Quick Start
1. **Build and start all containers**:
   ```bash
   docker-compose up --build -d
   ```

2. **Run the client application**:
   ```bash
   docker-compose run --rm stomp-client ./bin/StompWCIClient
   ```
   * The client will also start the server automatically.

3. **Connect to the server**:
   Once in the client application, connect using:
   ```
   login stomp-server:7777 <username> <password>
   ```
   * If connection issues occur, you can find the server's internal IP address with:
        ```bash
        docker network inspect stomp-server-client-feed_stomp-network
        ```
        Then use that IP address (e.g., `172.18.0.2:7777`) to connect

4. **Stop the containers**:
   ```bash
   docker-compose down
   ```

### Running Manually

#### Server

1. **Build the server**:

   ```bash
   cd server
   mvn clean install
   ```

2. **Run the server**:
   - Thread-per-client mode:
     ```bash
     mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
     ```
   - Reactor mode:
     ```bash
     mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 reactor"
     ```

#### Client

1. **Build the client**:
   ```bash
    cd client
    make clean all
   ```

2. **Run the client**:
   ```bash
   ./bin/StompWCIClient
   ```

### Client Usage

After starting the client, you can use the following commands:

1. **Login to the server**:

   ```
   login <host>:<port> <username> <password>
   ```

   Example: `login 127.0.0.1:7777 user1 pass1`

2. **Join a channel**:

   ```
   join <channel_name>
   ```

   Example: `join germany_spain`

3. **Exit a channel**:

   ```
   exit <channel_name>
   ```

   Example: `exit germany_spain`

4. **Send a report**:

   ```
   report <json_file_path>
   ```

   Example: `report data/events1_partial.json`

5. **Logout**:
   ```
   logout
   ```

## Technical Stack
- **Server**: Java 11+, Maven
- **Client**: C++, Boost (System, Thread, Regex, Filesystem)
- **Containerization**: Docker, Docker Compose
- **Protocol**: STOMP 1.2