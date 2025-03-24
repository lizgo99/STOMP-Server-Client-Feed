FROM maven:3.8-openjdk-11

# Set the working directory
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package

# Set default values for port and server type
ENV PORT=7777
ENV SERVER_TYPE=reactor

# Command to run the server
ENTRYPOINT ["sh", "-c", "mvn exec:java -Dexec.mainClass=bgu.spl.net.impl.stomp.StompServer -Dexec.args=\"$PORT $SERVER_TYPE\""]