# Cross-platform makefile for STOMP client

# Detect operating system
UNAME_S := $(shell uname -s)

# Default target
all: clean StompWCIClient

# Set compiler and flags based on OS
ifeq ($(UNAME_S),Darwin)
    # macOS
    CXX := clang++
    # Add explicit isysroot for macOS and direct path to C++ headers
    SDK_PATH := $(shell xcrun --show-sdk-path)
    
    CXXFLAGS := -c -Wall -Weffc++ -g -std=c++11 -Iinclude -isysroot $(SDK_PATH)
    # Add C++ headers path directly
    CXXFLAGS += -isystem $(SDK_PATH)/usr/include/c++/v1
    # Add Boost paths if installed
    ifneq ($(shell which brew),)
        BREW_PREFIX := $(shell brew --prefix)
        CXXFLAGS += -I$(BREW_PREFIX)/include
        LDFLAGS := -L$(BREW_PREFIX)/lib -lboost_system -lpthread
    else
        LDFLAGS := -lboost_system -lpthread
    endif
    # Define output directory and executable extension
    OUTDIR := bin
    EXE := 
else ifeq ($(UNAME_S),Linux)
    # Linux
    CXX := g++
    CXXFLAGS := -c -Wall -Weffc++ -g -std=c++11 -Iinclude
    LDFLAGS := -lboost_system -lpthread
    # Define output directory and executable extension
    OUTDIR := bin
    EXE := 
else
    # Windows (assumes MinGW/MSYS environment)
    CXX := g++
    CXXFLAGS := -c -Wall -Weffc++ -g -std=c++11 -Iinclude
    # For Windows, we might need to specify Boost paths explicitly
    # Adjust these paths according to your Boost installation
    BOOST_PATH ?= C:/boost
    CXXFLAGS += -I$(BOOST_PATH)/include
    LDFLAGS := -L$(BOOST_PATH)/lib -lboost_system-mt -lpthread
    # Define output directory and executable extension
    OUTDIR := bin
    EXE := .exe
endif

# Make sure the output directory exists
$(shell mkdir -p $(OUTDIR))

# Main target
StompWCIClient: $(OUTDIR)/ConnectionHandler.o $(OUTDIR)/StompClient.o $(OUTDIR)/event.o $(OUTDIR)/Command.o $(OUTDIR)/StompProtocol.o $(OUTDIR)/Game.o
	$(CXX) -o $(OUTDIR)/StompWCIClient$(EXE) $(OUTDIR)/ConnectionHandler.o $(OUTDIR)/StompClient.o $(OUTDIR)/event.o $(OUTDIR)/Command.o $(OUTDIR)/StompProtocol.o $(OUTDIR)/Game.o $(LDFLAGS)

# Object files
$(OUTDIR)/ConnectionHandler.o: src/ConnectionHandler.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/ConnectionHandler.o src/ConnectionHandler.cpp

$(OUTDIR)/StompClient.o: src/StompClient.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/StompClient.o src/StompClient.cpp

$(OUTDIR)/event.o: src/event.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/event.o src/event.cpp

$(OUTDIR)/Command.o: src/Command.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/Command.o src/Command.cpp

$(OUTDIR)/StompProtocol.o: src/StompProtocol.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/StompProtocol.o src/StompProtocol.cpp

$(OUTDIR)/Game.o: src/Game.cpp
	$(CXX) $(CXXFLAGS) -o $(OUTDIR)/Game.o src/Game.cpp

# Clean target
clean:
	rm -f $(OUTDIR)/*

.PHONY: all clean StompWCIClient