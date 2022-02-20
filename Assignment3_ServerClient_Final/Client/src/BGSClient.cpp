
#include <ConnectionHandler.h>
#include <KeyBoardReader.h>
#include <ConnectionReader.h>
#include <thread>
using namespace std;

int main (int argc, char *argv[]){

    string host = argv[1];
    short port = atoi(argv [2]);

    if (argc < 3){
        std::cerr << "Usage:" << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    bool *t = new bool;
    bool *r = new bool;

    ConnectionHandler handler(host, port);
    if(!handler.connect()) {
        std::cerr << "cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    KeyBoardReader keyBoardReader(&handler,r,t);
    ConnectionReader connectionReader(&handler,r,t);
    thread thread2(&ConnectionReader::run, &connectionReader);
    thread thread1(&KeyBoardReader::run, &keyBoardReader);
    thread2.join();
    thread1.join();

    delete r;
    delete t;
}
