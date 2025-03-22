#include "../include/Game.h"
#include <boost/algorithm/string.hpp>
#include <iostream>

void Game::updateGame(std::vector<std::string> lines)
{
    std::string time;
    std::string description;
    std::string eventName;
    std::string teamA;
    std::string teamB;

    for (uint i = 1; i < lines.size(); i++)
    {
        std::vector<std::string> header;
        boost::split(header, lines.at(i), boost::is_any_of(":"));
        if (header.size() == 2)
        {
            std::string left = header.at(0);
            std::string right = header.at(1);
            if (left.compare("team a") == 0)
            {
                teamA = right;
            }
            if (left.compare("team b") == 0)
            {
                teamB = right;
            }
            if (left.compare("event name") == 0)
            {
                eventName = right;
            }
            if (left.compare("time") == 0)
            {
                time = right + " - ";
            }
            if (left.compare("general game updates") == 0)
            {
                for (uint j = i + 1; j < lines.size() && lines.at(j).compare("team a updates:") != 0; j++, i++)
                {
                    std::vector<std::string> stat;
                    boost::split(stat, lines.at(j), boost::is_any_of(":"));
                    if (stat.size() == 2)
                    {
                        std::string leftStat = stat.at(0);
                        std::string rightStat = stat.at(1);
                        general_stats[leftStat] = rightStat;
                    }
                }
            }
            if (left.compare("team a updates") == 0)
            {
                for (uint j = i + 1; j < lines.size() && lines.at(j).compare("team b updates:") != 0; j++, i++)
                {
                    std::vector<std::string> stat;
                    boost::split(stat, lines.at(j), boost::is_any_of(":"));
                    if (stat.size() == 2)
                    {
                        std::string leftStat = stat.at(0);
                        std::string rightStat = stat.at(1);
                        team_a_stats[leftStat] = rightStat;
                    }
                }
            }
            if (left.compare("team b updates") == 0)
            {
                for (uint j = i + 1; j < lines.size() - 1 && lines.at(j).find("discription:") != 0; j++, i++)
                {
                    std::vector<std::string> stat;
                    boost::split(stat, lines.at(j), boost::is_any_of(":"));
                    if (stat.size() == 2)
                    {
                        std::string leftStat = stat.at(0);
                        std::string rightStat = stat.at(1);
                        team_b_stats[leftStat] = rightStat;
                    }
                }
            }
            if (left.compare("discription") == 0)
            {
                description = right;
            }
        }
    }
    name = teamA + " vs " + teamB;
    summary = summary + time + eventName + ":\n\n" + description + "\n\n\n";
}

std::string Game::toString()
{
    std::string output = "";
    output = output + name + "\n";
    output = output + "Game stats:\n";
    output = output + "General stats:\n";
    for (auto it = general_stats.begin(); it != general_stats.end(); it++)
    {
        output = output + it->first + ": " + it->second + "\n";
    }
    std::vector<std::string> gameName;
    boost::split(gameName, name, boost::is_any_of(" "));
    std::string team_a_name = gameName.at(0);
    std::string team_b_name = gameName.at(2);
    output = output + team_a_name + " stats:\n";
    for (auto it = team_a_stats.begin(); it != team_a_stats.end(); it++)
    {
        output = output + it->first + ": " + it->second + "\n";
    }
    output = output + team_b_name + " stats:\n";
    for (auto it = team_b_stats.begin(); it != team_b_stats.end(); it++)
    {
        output = output + it->first + ": " + it->second + "\n";
    }
    output = output + "Game event reports:\n";
    output = output + summary;
    return output;
}