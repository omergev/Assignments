
#ifndef CLIENT_CONNECTIONREADER_H
#define CLIENT_CONNECTIONREADER_H

#include "ConnectionHandler.h"


class ConnectionReader {
public:
    ConnectionReader(ConnectionHandler* c, bool* lO, bool* t);

    void run();
    short bytesToShort(char* bytesArr);
private: ConnectionHandler* connectionHandler;
    bool* logOut;
    bool* terminate;

};

#endif //CLIENT_CONNECTIONREADER_H
