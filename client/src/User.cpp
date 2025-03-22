#include "../include/User.h"
#include <string>

User::User(std::string username) : username(username){}

User::~User(){}


std::string User::getUsername(){
    return this->username;
}

void User::setUsername(std::string username){
    this->username = username;
}

