#pragma once

#include "../include/ConnectionHandler.h"
#include "../include/Game.h"
#include <sstream>
#include <vector>
#include <string>
// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    volatile bool canRead;
    volatile bool clientIsActive;
    volatile int subscriptionId;
    volatile int receiptId;
    std::string username;
    std::map<std::string, std::vector<std::string>> mapForReceipts;   // <receitpId, vectorMessage>
    std::map<std::string, std::map<std::string, Game>> mapForSummary; // <channel, <username, Game>>ˇ

public:
    StompProtocol() : canRead(true) , clientIsActive (false) , subscriptionId(0), receiptId(0), username(""), mapForReceipts(), mapForSummary() {}
    virtual ~StompProtocol() {}
    int getNewReceiptId();
    int getNewSubscriptionId();
    bool getclientIsActive();
    void setclientIsActive(bool clientIsActive);
    bool getCanRead();
    void setCanRead(bool canRead);
    void clearClientProperties();
    std::string proccessTerminalLine(std::vector<std::string> words , ConnectionHandler &connectionHandler);
    void decode(std::string answer , ConnectionHandler &connectionHandler);
};
