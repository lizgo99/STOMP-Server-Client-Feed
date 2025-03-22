FROM gcc:11

# Install required dependencies
RUN apt-get update && apt-get install -y \
    libboost-all-dev \
    make \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the source code
COPY . .

# Build the client
RUN make clean && make

# Set the entry point
ENTRYPOINT ["./bin/StompWCIClient"]
