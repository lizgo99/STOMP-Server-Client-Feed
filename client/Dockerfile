FROM gcc:11

# Install only the required Boost components
RUN apt-get update && apt-get install -y \
    libboost-system-dev \
    libboost-thread-dev \
    libboost-regex-dev \
    libboost-filesystem-dev \
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

# Use bash as the default command
# CMD ["/bin/bash"]
