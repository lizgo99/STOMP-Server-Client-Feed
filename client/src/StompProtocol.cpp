#include <iostream>
#include <boost/algorithm/string.hpp>
#include <fstream>
#include "../include/StompProtocol.h"
#include "../include/event.h"
#include "Command.cpp"
#include "../include/Game.h"

int StompProtocol::getNewReceiptId()
{
    receiptId++;
    return receiptId;
}
int StompProtocol::getNewSubscriptionId()
{
    subscriptionId++;
    return subscriptionId;
}

std::string StompProtocol::proccessTerminalLine(std::vector<std::string> words , ConnectionHandler &connectionHandler)
{
    std::map<std::string, TerminalCommand> terminalCommandMap = {
        {"login", LOGIN},
        {"join", JOIN},
        {"exit", EXIT},
        {"report", REPORT},
        {"summary", SUMMARY},
        {"logout", LOGOUT},
    };

    std::string stringTerminalCommand = words.at(0);

    TerminalCommand terminalCommand = terminalCommandMap[stringTerminalCommand];
    std::string frame = "";
    if (terminalCommand == LOGIN)
    {
        username = words.at(2);
        std::string passcode = words.at(3);
        frame = "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:" + username + "\n" + "passcode:" + passcode + "\n\n\0";
    }
    else if (terminalCommand == JOIN)
    {
        std::string id = std::to_string(getNewSubscriptionId());
        std::string receipt = std::to_string(getNewReceiptId());
        mapForReceipts[receipt] = words;
        frame = "SUBSCRIBE\ndestination:" + words.at(1) + "\n" + "id:" + id + "\n" + "receipt:" + receipt + "\n\n\0";
    }
    else if (terminalCommand == EXIT)
    {
        std::string id = std::to_string(subscriptionId);
        std::string receipt = std::to_string(getNewReceiptId());
        mapForReceipts[receipt] = words;
        frame = "UNSUBSCRIBE\nid:" + id + "\n" + "receipt:" + receipt + "\n\n\0";
    }
    else if (terminalCommand == REPORT)
    {
        std::string filePath = words.at(1);

        names_and_events report = parseEventsFile(filePath);

        std::string team_a_name = report.team_a_name;
        std::string team_b_name = report.team_b_name;
        std::string destination = team_a_name + "_" + team_b_name;
        std::vector<Event> events = report.events;
        for (Event event : events)
        {
            std::string eventString = "";
            eventString = eventString + "SEND\n";
            eventString = eventString + "destination:" + destination + "\n\n";
            eventString = eventString + "username:" + username + "\n";
            eventString = eventString + "team a:" + team_a_name + "\n";
            eventString = eventString + "team b:" + team_b_name + "\n";
            eventString = eventString + "event name:" + event.get_name() + "\n";
            eventString = eventString + "time:" + std::to_string(event.get_time()) + "\n";
            std::map<std::string, std::string> generalGameUpdates = event.get_game_updates();
            eventString = eventString + "general game updates:\n";
            for (std::pair<std::string, std::string> generalGameUpdate : generalGameUpdates)
            {
                eventString = eventString + generalGameUpdate.first + ":" + generalGameUpdate.second + "\n";
            }
            eventString = eventString + "team a updates:\n";
            std::map<std::string, std::string> teamAUpdates = event.get_team_a_updates();
            for (std::pair<std::string, std::string> teamAUpdate : teamAUpdates)
            {
                eventString = eventString + teamAUpdate.first + ":" + teamAUpdate.second + "\n";
            }
            eventString = eventString + "team b updates:\n";
            std::map<std::string, std::string> teamBUpdates = event.get_team_b_updates();
            for (std::pair<std::string, std::string> teamBUpdate : teamBUpdates)
            {
                eventString = eventString + teamBUpdate.first + ":" + teamBUpdate.second + "\n";
            }
            eventString = eventString + "discription:" + event.get_discription() + "\n";
            eventString = eventString + "\0";
            if (!connectionHandler.sendLine(eventString))
            {
                std::cout << "Disconnected. Exiting...\n"
                          << std::endl;
                canRead = false;
            }
        }
        frame = "report";
    }
    else if (terminalCommand == SUMMARY)
    {
        std::string gameName = words.at(1);
        std::string userName = words.at(2);
        std::string fileName = words.at(3);
        if (mapForSummary.count(gameName) > 0)
        {
            if (mapForSummary[gameName].count(userName) > 0)
            {
                std::string summaryData = mapForSummary[gameName][userName].toString();
                std::ofstream outSummaryFile;

                outSummaryFile.open(fileName, std::ios::out | std::ios::trunc);
                if (outSummaryFile.is_open())
                {
                    outSummaryFile << summaryData;
                    outSummaryFile.close();
                }
                else
                {
                    std::cout << "Unable to open file " << fileName << std::endl;
                }
            }
        }
        else
        {
            std::cout << "No such game" << std::endl;
        }

        frame = "summary";
    }
    else if (terminalCommand == LOGOUT)
    {
        std::string receipt = std::to_string(getNewReceiptId());
        mapForReceipts[receipt] = words;
        frame = "DISCONNECT\nreceipt:" + receipt + "\n\n\0";
    }
    else
    {
        std::cout << "Invalid command" << std::endl;
    }
    return frame;
}

void StompProtocol::decode(std::string answer , ConnectionHandler &connectionHandler)
{
        std::map<std::string, SocketCommand> socketCommandMap = {
        {"CONNECTED", CONNECTED},
        {"MESSAGE", MESSAGE},
        {"RECEIPT", RECEIPT},
        {"ERROR", ERROR},
    };

    std::vector<std::string> lines;
    boost::split(lines, answer, boost::is_any_of("\n"));

    std::string stringSocketCommand = lines.at(0);
    SocketCommand socketCommand = socketCommandMap[stringSocketCommand];

    if (socketCommand == CONNECTED)
    {
        std::cout << "Login successful" << std::endl;
    }
    else if (socketCommand == MESSAGE)
    {
        std::string username = "";
        std::string channel = "";
        for (uint i = 1; i < lines.size(); i++)
        {
            std::vector<std::string> header;
            boost::split(header, lines.at(i), boost::is_any_of(":"));
            if (header.size() == 2)
            {
                std::string left = header.at(0);
                std::string right = header.at(1);
                if (left.compare("username") == 0)
                {
                    username = right;
                }
                if (left.compare("destination") == 0)
                {
                    channel = right;
                }
            }
        }

        if (mapForSummary.count(channel) > 0)
        {
            if (mapForSummary[channel].count(username) > 0)
            {
                mapForSummary[channel][username].updateGame(lines);
            }
            else
            {
                Game game = Game();
                game.updateGame(lines);
                mapForSummary[channel][username] = game;
            }
        }
        else
        {
            Game game = Game();
            game.updateGame(lines);
            mapForSummary[channel][username] = game;
        }

        std::cout << answer << std::endl;
    }
    else if (socketCommand == RECEIPT)
    {
        std::string receiptId = "";
        for (uint i = 1; i < lines.size(); i++)
        {
            std::vector<std::string> header;

            boost::split(header, lines.at(i), boost::is_any_of(":"));
            if (header.size() == 2)
            {
                std::string left = header.at(0);
                std::string right = header.at(1);
                if (left.compare("receipt-id") == 0)
                {
                    receiptId = right;
                }
            }
        }

        std::vector<std::string> sentMessage = mapForReceipts[receiptId];
        std::string commmand = sentMessage.at(0);

        if (commmand.compare("logout") == 0)
        {
            connectionHandler.close();
            clearClientProperties();
        }
        else if (commmand.compare("join") == 0)
        {
            std::cout << "Joined channel " << sentMessage.at(1) << std::endl;
        }
        else if (commmand.compare("exit") == 0)
        {
            std::cout << "Exited channel " << sentMessage.at(1) << std::endl;
        }
    }
    else if (socketCommand == ERROR)
    {
        std::cout << answer << std::endl;
        connectionHandler.close();
        clearClientProperties();
    }
}

bool StompProtocol::getclientIsActive()
{
    return clientIsActive;
}
void StompProtocol::setclientIsActive(bool clientIsActive)
{
    this->clientIsActive = clientIsActive;
}

void StompProtocol::setCanRead(bool canRead)
{
    this->canRead = canRead;
}
bool StompProtocol::getCanRead(){
    return canRead;
}
void StompProtocol::clearClientProperties()
{
    clientIsActive = false;
    username = "";
    subscriptionId = 0;
    receiptId = 0;
}
