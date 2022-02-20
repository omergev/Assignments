
#include <KeyBoardReader.h>

#include "KeyBoardReader.h"
#include "ConnectionHandler.h"
#include <boost/algorithm/string.hpp>
#include <ctime>


using namespace std;


KeyBoardReader::KeyBoardReader(ConnectionHandler* c, bool* lO, bool* t): connectionHandler(c), logOut(lO), terminate(t)  {}

void KeyBoardReader::run() {
    *logOut=false;
    *terminate=false;


    while (!(*terminate)) {
        bool out = false;
        while (*logOut==true){
            if(*terminate==true){
                out =true;
                break;
            }
        }
        if(out==true)
            break;

        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);

        int len = line.length();
        vector<string> words;

        boost::split(words, line, boost::is_any_of(" "));


        char opCode[2];

        if (words[0] == "REGISTER") {

            shortToBytes(1, opCode);

            connectionHandler->sendBytes(opCode, 2);
            connectionHandler->sendLine(words[1]);
            connectionHandler->sendLine(words[2]);
            connectionHandler->sendLine(words[3]);

        }

        if (words[0] == "LOGIN") {

            shortToBytes(2, opCode);

            connectionHandler->sendBytes(opCode, 2);
            connectionHandler->sendLine(words[1]);
            connectionHandler->sendLine(words[2]);
            connectionHandler->sendLine(words[3]);
            //TODO: check how to send captcha
//            char captcha[1];
//            captcha[0] = '\1';
//            connectionHandler->sendBytes(captcha, 1);
        }
        if (words[0] == "LOGOUT") {
            shortToBytes(3, opCode);
            connectionHandler->sendBytes(opCode, 2);
            *logOut=true;
        }

        if (words[0] == "FOLLOW"){
            shortToBytes(4, opCode);
            connectionHandler->sendBytes(opCode, 2);
            char zeroOrOne[1];// = new char[1];
//            char zeroOrOne =words[1][0];
            if (words[1]=="0")
                zeroOrOne[0] = '0';
            else{
                zeroOrOne[0] = '1';
            }
            connectionHandler->sendBytes(zeroOrOne, 1);
            connectionHandler->sendLine(words[2]);
            }

        if (words[0] == "POST"){
            shortToBytes(5, opCode);
            connectionHandler->sendBytes(opCode, 2);
            string content = "";
            for (int i = 1; i < words.size(); ++i) {
                content += words[i] + " ";
            }
            if(!content.empty()){
                content.pop_back();
            }
            connectionHandler->sendLine(content);
        }

        if (words[0] == "PM"){
            shortToBytes(6, opCode);
            connectionHandler->sendBytes(opCode, 2);
            connectionHandler->sendLine(words[1]);//UserName
            string content = "";
            for (int i = 2; i < words.size(); ++i) {
                content += words[i] + " ";
            }
            if(!content.empty()){
                content.pop_back();
            }
            connectionHandler->sendLine(content);

            string ToSend = "";
            time_t now = time(0); // get current date and time
            tm *ltm = localtime(&now);
            // print various components of tm structure.
            int year = 1900 + ltm->tm_year;
            int month = 1 + ltm->tm_mon;
            int day = ltm->tm_mday;
            int hour = ltm->tm_hour;
            int minutes = ltm->tm_min;
            ToSend += std::to_string(hour) + ":";
            ToSend += std::to_string(minutes);
            if (std::to_string(day).size() == 1)
                ToSend += " 0" + std::to_string(day) + "-";
            else
                ToSend += " " + std::to_string(day) + "-";
            if (std::to_string(month).size() == 1)
                ToSend += "0" + std::to_string(month) + "-";
            else
                ToSend += std::to_string(month) + "-";
            ToSend += std::to_string(year);
            connectionHandler->sendLine(ToSend);
        }

        if (words[0] == "LOGSTAT"){

            shortToBytes(7, opCode);
            connectionHandler->sendBytes(opCode, 2);
        }

        if (words[0] == "STAT"){

            shortToBytes(8, opCode);
            connectionHandler->sendBytes(opCode, 2);
            connectionHandler->sendLine(words[1]);

        }
        if(words[0] == "BLOCK"){

            shortToBytes(12, opCode);
            connectionHandler->sendBytes(opCode, 2);
            connectionHandler->sendLine(words[1]);

        }
        char finish [1];
        finish[0] = ';';
        connectionHandler->sendBytes(finish, 1);
    }


};
void KeyBoardReader::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
