#pragma once

#include <string>
#include <vector>
#include <map>


class Game
{
private:
	std::string name;
	std::string summary;
	std::map<std::string, std::string> general_stats;
	std::map<std::string, std::string> team_a_stats;
	std::map<std::string, std::string> team_b_stats;
public:
	Game() : name(""), 
			 summary(""), 
			 general_stats(), 
			 team_a_stats(), 
			 team_b_stats(){}
	virtual ~Game(){}
	void updateGame(std::vector<std::string> lines);
	std::string toString();
};