#pragma once

#include <vector>
#include <string>
#include "../include/StompProtocol.h"
#include "../include/ConnectionHandler.h"
#include "../include/Game.h"

// TODO: implement the STOMP protocol
class StompClient
{
private:
    StompProtocol &stompProtocol;
    ConnectionHandler connectionHandler;
public:
    StompClient(StompProtocol &_stompProtocol) : stompProtocol(_stompProtocol), connectionHandler(){}
    ~StompClient() {}
    void decodeAndSendTerminalCommand(std::string line);
    void decodeFromSocket();
    bool isConnected();
    bool canReadFromSocket();
};
