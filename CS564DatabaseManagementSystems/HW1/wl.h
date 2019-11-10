/**
 * @file wl.cpp
 * @author  yishiun chang 
 *
 * Description: Basic definition of class WL that stores words and the indexes of them in a map
 * Student Name: yi-shiun, chang
 * UW Campus ID: 9080813901
 * email: chang242@wisc.edu
 */

#ifndef WL_H
#define WL_H
#include <iostream>
#include <fstream>
#include <sstream>
#include <ctype.h> 
#include <stdio.h>
#include <string>
#include <vector>
#include <map>

using namespace std;

class WL{
    
public:
    WL();
    void load(string); // load a file and store data in locator
    void locate(string, string); // get information from locator
    bool isLoad(string); // check whether command is "load"
    bool isLocate(string); // check whether command is "locate"
    bool isNew(string); // check whether command is "new"
    bool isEnd(string); // check whether command is "end"
    bool allDigit(string); // check whether a string is composed of numbers
    map<string, vector<int> > locator; // store word versus indexes
};

#endif