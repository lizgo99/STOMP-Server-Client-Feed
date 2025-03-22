FROM maven:3.8-openjdk-11

# Set the working directory
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package

# Command to run the server
ENTRYPOINT ["mvn", "exec:java", "-Dexec.mainClass=bgu.spl.net.impl.stomp.StompServer", "-Dexec.args=7777 tpc"]
