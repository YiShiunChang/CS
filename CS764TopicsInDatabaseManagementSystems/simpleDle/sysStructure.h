#pragma once

#include <string>

namespace littleBadger {

  const void initMapStructure();

  const void readMap(int key);

  const void writeMap(int key, std::string value);

  const void deleteMap(int key);
}