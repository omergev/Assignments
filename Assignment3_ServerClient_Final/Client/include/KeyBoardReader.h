
#ifndef CLIENT_KEYBOARDREADER_H
#define CLIENT_KEYBOARDREADER_H
#include "ConnectionHandler.h"
using namespace std;

class KeyBoardReader {

public:
    KeyBoardReader(ConnectionHandler* c, bool* lO, bool* t);

    void run();
    void shortToBytes(short num, char* bytesArr);
private:
    ConnectionHandler* connectionHandler;
    bool* logOut;
    bool* terminate;
};

#endif //CLIENT_KEYBOARDREADER_H
