#include <iostream>
#include <fstream>
#include <string>

void createLines(std::string filepath) {
  std::ofstream myfile(filepath);
  if (myfile.is_open()) {
    for (int i = 0; i < 1000; i++) {
      std::string data("WRITE " + std::to_string(i) + " " + std::to_string(i) + "\n");
      myfile << data;
    }
    myfile.close();
  } else {
    std::cout << "Unable to open file";
  }
}

int main(int argc, char **argv) {
  createLines("log.txt");
}




