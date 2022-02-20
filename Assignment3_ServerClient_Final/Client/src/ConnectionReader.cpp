
#include <ConnectionReader.h>

#include "ConnectionReader.h"
using namespace std;
ConnectionReader::ConnectionReader(ConnectionHandler* c, bool *lO, bool *t) : connectionHandler(c), logOut(lO), terminate(t)  {}



void ConnectionReader::run() {
    *logOut=false;
    *terminate=false;

    while (!(*terminate)) {

        char* opCodeArr = new char[2];
        connectionHandler->getBytes(opCodeArr, 2);
        short opCode = bytesToShort(opCodeArr);

        string outPut;

     if (opCode==9){

         outPut="NOTIFICATION";
         char * temp = new char[1];
         connectionHandler->getBytes(temp, 1);
         if (temp[0]==0)
            outPut= outPut+ " PM";
         else
            outPut= outPut+ " PUBLIC";
         string postingUser = "";
         connectionHandler->getLine(postingUser);
         outPut= outPut +" "+(postingUser.substr(0, (postingUser.size()-1)));
         string content = "";
         connectionHandler->getLine(content);
         outPut= outPut+ " " + (content.substr(0, (content.size()-1)));
         delete[] temp;
     }

     if (opCode==10){
         outPut = "ACK";

         connectionHandler->getBytes(opCodeArr, 2);
         short msgOpCode = bytesToShort(opCodeArr);

         outPut= outPut+ " " + to_string(msgOpCode);

         if(msgOpCode==4){
             string temp;
             connectionHandler->getLine(temp);
             outPut = outPut + " " + temp.substr(0,temp.size()-1);
         }


        if (msgOpCode==8 || msgOpCode==7){
            connectionHandler->getBytes(opCodeArr, 2);//For age
            short age = bytesToShort(opCodeArr);

            outPut= outPut+ " " + to_string(age);

            connectionHandler->getBytes(opCodeArr, 2);//for numPosts
            short numPosts = bytesToShort(opCodeArr);

            outPut= outPut+ " " + to_string(numPosts);

            connectionHandler->getBytes(opCodeArr, 2);//for numFollower
            short numFollower = bytesToShort(opCodeArr);

            outPut= outPut+ " " + to_string(numFollower);

            connectionHandler->getBytes(opCodeArr, 2);//for numFollowing
            short numFollowing = bytesToShort(opCodeArr);

            outPut= outPut+ " " + to_string(numFollowing);
        }

        if( msgOpCode==3){
            *terminate=true;
        }
     }

     if (opCode==11){
         outPut="ERROR";

         connectionHandler->getBytes(opCodeArr, 2);
         short errorCheck = bytesToShort(opCodeArr);

         outPut= outPut + " " + to_string(errorCheck);
         if (errorCheck==3){
             *logOut=false;
         }
     }

     if (outPut!="")
         cout << outPut << endl;

     delete[] opCodeArr;

    }
}

short ConnectionReader::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;

}

