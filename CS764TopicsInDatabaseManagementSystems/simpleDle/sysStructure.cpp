#include <map>
#include <string>
#include <iostream>
#include "sysStructure.h"

namespace littleBadger {
  // empty map container 
  std::map<int, std::string> mapStructure;

  const void initMapStructure() {
    for (int i = 0; i < 1000; i++) {
      mapStructure.insert(std::pair<int, std::string>(i, std::to_string(i)));
    }
  }

  const void readMap(int key) {
    std::cout << "read " <<  mapStructure.find(key)->second << " finish" << std::endl;
  }

  const void writeMap(int key, std::string value) {
    std::cout << "write " << value << " finish" << std::endl;
    mapStructure.find(key)->second = value;
  }

  const void deleteMap(int key) {
    mapStructure.erase(key);
  }
}