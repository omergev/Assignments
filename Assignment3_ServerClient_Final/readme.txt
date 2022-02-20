HOW TO RUN THE PROGRAM:

#1 + #1.1

- Server:


//TPC
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="7777"

//Reactor
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 5"


- Client:
make clean
make
./bin/BGSclient 127.0.0.1 7777



#1.2 Message Example:

Register - REGISTER Idan 1997 28/28/1997
Log in - LOGIN Idan 1997 1
Log out - LOGOUT
Follow - FOLLOW 0 Idan
Unfollow - FOLLOW 1 Idan
Post - POST SPL IS THE MOST AWESOME COURSE EVER @Omer
Private Message - PM Omer DO YOU THINK SPL IS THE MOST AWESOME COURSE EVER?!
LogStat - LOGSTAT
Stat - STAT Marina|Adler
Block - BLOCK Omer


# 2 filtered words - Server->src->main->java->bgu->spl->net->api->Message->ClientToServer->PM
We create field - "private String[] forbiddenWords = {"Trump" , "War"};" In this array we store the forbidden words in order to filter.
