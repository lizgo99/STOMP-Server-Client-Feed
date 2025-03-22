#pragma once

#include <string>
#include <vector>

class User
{
private:
    std::string username;
public:
	User(std::string username);
	virtual ~User();
    std::string getUsername();
    void setUsername(std::string username);
    
    

};