# STOMP-Server-Client-Feed

A client-server implementation of the STOMP (Simple Text Oriented Messaging Protocol) protocol for message passing between clients.

## Project Structure

- `client/`: C++ implementation of the STOMP client
- `server/`: Java implementation of the STOMP server

## Running with Docker (Recommended)

Docker provides a consistent environment across all platforms, making it easy to build and run the application without worrying about platform-specific dependencies.

### Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop) installed on your system
- [Docker Compose](https://docs.docker.com/compose/install/) (usually included with Docker Desktop)

### Building and Running with Docker

1. **Start the server and client containers**:

   ```bash
   docker-compose up --build
   ```

   This will:
   - Build the server container with Maven and Java
   - Build the client container with GCC and Boost
   - Start both containers with the server listening on port 7777

2. **Connect to the client container**:

   The client container will be started with an interactive terminal. You can now use STOMP commands.

3. **To run only the server**:

   ```bash
   docker-compose up stomp-server
   ```

4. **To run only the client**:

   ```bash
   docker-compose up stomp-client
   ```

5. **To stop all containers**:

   ```bash
   docker-compose down
   ```

## Manual Setup (Without Docker)

### Prerequisites

#### For All Platforms

- Java JDK 8 or higher (for the server)
- Maven (for building the server)
- C++ compiler with C++11 support
- Boost library (1.66.0 or higher recommended)

#### Platform-Specific Requirements

##### macOS

- Xcode Command Line Tools
- Homebrew (recommended for installing Boost)

##### Linux

- g++ compiler
- Boost development libraries

##### Windows

- MinGW-w64 or MSYS2 with g++ (recommended)
- Visual Studio with C++ support (alternative)
- Boost libraries for Windows

### Installing Boost

#### macOS

```bash
brew install boost
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install libboost-all-dev
```

#### Windows

1. **Download Boost**:

   - Visit the [Boost website](https://www.boost.org/users/download/)
   - Download the latest version (1.83.0 or newer)

2. **Extract Boost**:

   - Extract the downloaded archive to a location like `C:\boost`
   - This location will be referred to as `BOOST_PATH` in the build process

3. **Build Boost Libraries** (if needed):
   - Open Command Prompt in administrator mode
   - Navigate to the Boost directory:
     ```
     cd C:\boost
     ```
   - Run the bootstrap script:
     ```
     bootstrap.bat gcc
     ```
   - Build the required Boost libraries:
     ```
     b2 --with-system --with-thread --with-date_time --with-regex --with-serialization toolset=gcc
     ```

### Building and Running Manually

#### Server (All Platforms)

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

##### macOS and Linux

```bash
cd client
make clean
make
./bin/StompWCIClient
```

##### Windows (MinGW/MSYS2)

1. **Install MinGW-w64 or MSYS2** (if not already installed)

   - Download and install from [MSYS2 website](https://www.msys2.org/)
   - Install the necessary packages:
     ```bash
     pacman -S mingw-w64-x86_64-gcc mingw-w64-x86_64-make
     ```

2. **Build the client**:

   - Open MSYS2 MinGW 64-bit terminal
   - Navigate to the client directory:
     ```bash
     cd /path/to/STOMP-Server-Client-Feed/client
     ```
   - Build with the Boost path specified:
     ```bash
     make clean
     make BOOST_PATH=C:/boost
     ```
   - Replace `C:/boost` with your actual Boost installation path

3. **Run the client**:
   ```bash
   ./bin/StompWCIClient.exe
   ```

## Client Usage

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

## Troubleshooting

### Docker Issues

1. **Port Conflicts**:
   - If you get an error about port 7777 being in use, change the port mapping in `docker-compose.yml`

2. **Container Communication**:
   - If the client cannot connect to the server, ensure they are on the same Docker network

### Windows Build Issues

1. **Boost Path Issues**:
   - If you get errors about missing Boost headers, ensure the Boost path is correctly specified:
     ```
     make BOOST_PATH=C:/path/to/your/boost
     ```

2. **Library Linking Errors**:
   - Check that you've built the required Boost libraries
   - Verify the library path is correct in the makefile

## Notes for Cross-Platform Development

- The project has been configured to work across macOS, Linux, and Windows
- Docker provides the most consistent experience across all platforms
- The makefile automatically detects the operating system and sets appropriate compiler flags
- For Windows, it adds the `.exe` extension to the executable
