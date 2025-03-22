#include <iostream>
#include <thread>
#include <boost/algorithm/string.hpp>
#include <fstream>
#include "../include/StompClient.h"
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"
#include "../include/Game.h"
#include "Command.cpp"

class KeyboardReader
{
private:
    StompClient &_stompClient;

public:
    KeyboardReader(StompClient &client) : _stompClient(client) {}

    void readFromKeyboard()
    {
        while (1)
        {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            _stompClient.decodeAndSendTerminalCommand(line);
        }
    }
};

class SocketReader
{
private:
    StompClient &_stompClient;

public:
    SocketReader(StompClient &client) : _stompClient(client) {}

    void readFromSocket()
    {
        while (1)
        {
            if (_stompClient.isConnected() && _stompClient.canReadFromSocket())
            {
                _stompClient.decodeFromSocket();
            }
        }
    }
};

int main(int argc, char *argv[])
{
    StompProtocol stompProtocol;
    StompClient stompClient(stompProtocol);
    KeyboardReader keyboardReader(stompClient);
    SocketReader socketReader(stompClient);

    std::thread keyboardThread(&KeyboardReader::readFromKeyboard, &keyboardReader);
    std::thread socketThread(&SocketReader::readFromSocket, &socketReader);

    keyboardThread.join();
    socketThread.join();

    return 0;
}

void StompClient::decodeAndSendTerminalCommand(std::string line)
{
    std::vector<std::string> words;
    boost::split(words, line, boost::is_any_of(" "));
    std::string frame = "";
    if (!connectionHandler.isConnected())
    {
        if (words.at(0).compare("login") == 0)
        {
            std::vector<std::string> hostAndPort;
            boost::split(hostAndPort, words.at(1), boost::is_any_of(":"));
            std::string host = hostAndPort.at(0);
            short port = std::stoi(hostAndPort.at(1));

            ConnectionHandler newConnectionHandler(host, port);
            this->connectionHandler = newConnectionHandler;

            if (!connectionHandler.connect())
            {
                std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
            }
            else
            {
                frame = stompProtocol.proccessTerminalLine(words, connectionHandler);
                stompProtocol.setclientIsActive(true);
                if (!connectionHandler.sendLine(frame))
                {
                    std::cout << "Disconnected. Exiting...\n"
                              << std::endl;
                    stompProtocol.setCanRead(false);
                }
            }
        }
        else
        {
            std::cout << "You must login first" << std::endl;
        }
    }
    else // the client is connected
    {
        if (words.at(0).compare("login") == 0)
        {
            std::cout << "This client is connected. log out and try again" << std::endl;
        }
        else
        {
            frame = stompProtocol.proccessTerminalLine(words, connectionHandler);

            if (frame.compare("summary") != 0 && frame.compare("report") != 0)
            {
                if (!connectionHandler.sendLine(frame))
                {
                    std::cout << "Disconnected. Exiting...\n"
                              << std::endl;
                    stompProtocol.setCanRead(false);
                }
            }
        }
    }
}

void StompClient::decodeFromSocket()
{
    std::string answer;
    if (connectionHandler.isConnected())
    {
        if (!connectionHandler.getLine(answer))
        {
            std::cout << "Disconnected. Exiting...\n"
                      << std::endl;
            stompProtocol.setCanRead(false);
        }
        else
        {
            stompProtocol.decode(answer, connectionHandler);
        }
    }
    else
    {
        stompProtocol.setCanRead(false);
    }
}


bool StompClient::isConnected()
{
    return stompProtocol.getclientIsActive();
}

bool StompClient::canReadFromSocket()
{
    return stompProtocol.getCanRead();
}