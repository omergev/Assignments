//
// Created by spl211 on 13/11/2021.
//
#include "Workout.h"
#include "Studio.h"
#include "fstream"
#include "string"
//Empty Constructor
Studio:: Studio():  numOfTrainers(0), open(false){
    trainers = std::vector<Trainer*>();
    workout_options = std::vector<Workout>();
    actionsLog = std::vector<BaseAction*>();
}
//Constructor
Studio:: Studio(const std::string &configFilePath) :  numOfTrainers(0), open(false){
    trainers = std::vector<Trainer*>();
    workout_options = std::vector<Workout>();
    actionsLog = std::vector<BaseAction*>();
    std::ifstream file(configFilePath);
    std::string str;
    int lineCounter = 1;
    int idCounter = 0;
    //Read all the lines from config file
    while(std::getline(file,str)){
        //Get line 2 with num of trainers
        if(lineCounter == 2){
            numOfTrainers = std::stoi(str);
        }
        //Create all the trainers
        else if( lineCounter == 5){
            // pos is the position for the next ',' .
            std::size_t pos1 = -1;
            std::size_t pos2 = str.find(',');

            while(pos2 !=std::string::npos ){
                std::string s_capacity = str.substr(pos1+1,pos2-pos1-1);
                int i_capacity = std::stoi(s_capacity);
                Trainer *t = new Trainer(i_capacity);
                trainers.push_back(t);
                pos1 = pos2;
                pos2 = str.find(',' , pos2+1);
            }
            std::string s_capacity = str.substr(pos1+1);
            int i_capacity = std::stoi(s_capacity);
            Trainer *t = new Trainer(i_capacity);
            trainers.push_back(t);


        }
        //Create all the workout options
        else if( (!str.empty() ) &&  (lineCounter > 7)){
            std::size_t pos1 = str.find(',');
            std::size_t pos2 = str.find(',' , pos1+1);
            std::string name = str.substr(0,pos1);
            std::string type = str.substr(pos1+2,pos2-pos1-2);
            std::string s_price = str.substr(pos2+2);
            int i_price = std::stoi(s_price);
            WorkoutType w;
            if(type =="Cardio"){
                w = CARDIO;
            }
            else if(type == "Mixed"){
                w = MIXED;
            }
            else{
                w= ANAEROBIC;
            }
            workout_options.push_back(Workout (idCounter,name,i_price,w));
            idCounter++;
        }
        lineCounter++;
    }

}

//Destructor
Studio:: ~Studio() { clear(); }

void Studio::clear() {
    for (int i = 0; i <(int)(trainers.size()); ++i) {
        if(trainers[i] != nullptr){
            delete trainers[i];
            trainers[i] = nullptr;
        }
    }
    for (int i = 0; i < (int)(actionsLog.size()); ++i) {
        if(actionsLog[i] != nullptr){
            delete actionsLog[i];
            actionsLog[i] = nullptr;
        }
    }
    trainers.clear();
    actionsLog.clear();
    workout_options.clear();
    open = false;
    numOfTrainers = 0;
    IDcounter = 0;
    //TODO: to check if we need(and why) to initialize open=0...
}

//Copy Constructor
Studio:: Studio(const Studio &other):  numOfTrainers(other.numOfTrainers), open(other.open)  {
    copy(other.numOfTrainers, other.open, other.trainers, other.workout_options, other.actionsLog, other.IDcounter);

}

void Studio::copy(const int &other_numOfTrainer, const bool &other_open, const std::vector<Trainer *> &other_trainers,
                  const std::vector<Workout> &other_workout_options, const std::vector<BaseAction *> &other_actionsLog,
                  const int &other_IDcounter) {
    numOfTrainers = other_numOfTrainer;
    open = other_open;
    IDcounter = other_IDcounter;
    //Deep copy
    for (int i = 0; i <(int)(other_workout_options.size()) ; ++i) {
        Workout copy_workout = other_workout_options.at(i);
        workout_options.push_back(copy_workout);
    }
    for (int i = 0; i < (int)(other_trainers.size()); ++i) {
        Trainer *copy_trainer = new Trainer(*(other_trainers.at(i)));//Copy constructor of Trainer
        trainers.push_back(copy_trainer);
    }

    for (int i = 0; i < (int)(other_actionsLog.size()); ++i) {
        BaseAction* copy_action = other_actionsLog.at(i)->clone(); // Use clone method for actions
        actionsLog.push_back(copy_action);
    }
}

//Assignment Operator
Studio& Studio:: operator=(const Studio &other){
    if(this == &other){
        return *this;
    }
    clear();
    copy(other.numOfTrainers, other.open, other.trainers, other.workout_options, other.actionsLog, other.IDcounter);
    return *this;
}

//Move Constructor
Studio:: Studio(Studio &&other){
    clear();
    open = other.open;
    IDcounter = other.IDcounter;
    numOfTrainers = other.numOfTrainers;
    // Use move assignment operator
    trainers = std::move(other.trainers);
    actionsLog = std::move(other.actionsLog);
    workout_options = std::move(other.workout_options);
}

//Move Assignment
Studio& Studio:: operator=(Studio &&other) {
    if (this != &other) {
        clear();
        open = other.open;
        IDcounter = other.IDcounter;
        numOfTrainers = other.numOfTrainers;
        //Use move assignment operator
        trainers = std::move(other.trainers);
        workout_options = std::move(other.workout_options);
        actionsLog = std::move(other.actionsLog);
    }
    return *this;
}

void Studio:: start(){
    if(!open){
        open = true;
        std::cout<< "Studio is now open!" <<std::endl;
        while(open){
            std::string s;
            getline(std:: cin,s);
            if(s.find("closeall") == 0){
                CloseAll *closeall = new CloseAll;
                closeall->act(*this);
                closeStudio();
                this->actionsLog.push_back(closeall);
            }
            else if(s.find("close") == 0){
                std::size_t pos = s.find(' ');
                std::string str1 = s.substr(pos+1);
                int trainerID =  std::stoi(str1);
                Close *close = new Close(trainerID);
                close->act(*this);
                this->actionsLog.push_back(close);
            }
            else if(s.find("backup") == 0){
                BackupStudio *backup = new BackupStudio();
                backup->act(*this);
                this->actionsLog.push_back(backup);
            }
            else if(s.find("restore") == 0){
                RestoreStudio *restore = new RestoreStudio;
                restore->act(*this);
                this->actionsLog.push_back(restore);
            }
            else if(s.find("log") == 0){
                PrintActionsLog *log = new PrintActionsLog();
                log->act(*this);
                this->actionsLog.push_back(log);
            }
            else if(s.find("workout") == 0){
                PrintWorkoutOptions *workout = new PrintWorkoutOptions();
                workout->act(*this);
                this->actionsLog.push_back(workout);
            }
            else if(s.find("move") == 0){
                std::size_t pos1 =s.find(' ');
                std::size_t pos2 =s.find(' ' , pos1+1);
                std::size_t pos3 =s.find(' ' , pos2+1);
                std::string str1 = s.substr(pos1+1,pos2-pos1-1);
                std::string str2 = s.substr(pos2+1,pos3-pos2-1);
                std::string str3 = s.substr(pos3+1);
                int src =  std::stoi(str1);
                int dst =  std::stoi(str2);
                int customerid =  std::stoi(str3);
                MoveCustomer *move_Customer = new MoveCustomer(src,dst,customerid);
                move_Customer->act(*this);
                this->actionsLog.push_back(move_Customer);
            }

            else if(s.find("order") == 0){
                std::size_t pos = s.find(' ');
                std::string str1 = s.substr(pos+1);
                int trainerID =  std::stoi(str1);
                Order *order = new Order(trainerID);
                order->act(*this);
                this->actionsLog.push_back(order);
            }
            else if(s.find("status") == 0){
                std::size_t pos = s.find(' ');
                std::string str1 = s.substr(pos+1);
                int trainerID =  std::stoi(str1);
                if(trainerID>=0 && trainerID<numOfTrainers){
                    PrintTrainerStatus *status = new PrintTrainerStatus(trainerID);
                    status->act(*this);
                    this->actionsLog.push_back(status);
                }
            }
            else if(s.find("open") == 0){
                // pos1 : next 'space' , pos2: next: 'comma'
                std::vector<Customer *> customersList;
                std::size_t pos1 = s.find(' ',5);
                std::size_t pos2 = s.find(',',pos1+1);
                std::string str1 = s.substr(5,pos1-5);
                int trainerID =  std::stoi(str1);
                int trainerCapacity = -1;
                if((trainerID<numOfTrainers) && (trainerID>=0)){
                    trainerCapacity = getTrainer(trainerID)->getCapacity();
                }
                while((pos2 != std::string::npos) &&( trainerCapacity>0)){
                    std::string customerName = s.substr(pos1+1,pos2-pos1-1);
                    std::string strategie = s.substr(pos2+1,3);
                    if(strategie == "swt"){
                        SweatyCustomer *customer = new SweatyCustomer(customerName, IDcounter);
                        customersList.push_back(customer);
                    }
                    else if(strategie == "chp"){
                        CheapCustomer *customer = new CheapCustomer(customerName, IDcounter);
                        customersList.push_back(customer);
                    }
                    else if(strategie == "mcl"){
                        HeavyMuscleCustomer *customer = new HeavyMuscleCustomer(customerName, IDcounter);
                        customersList.push_back(customer);
                    }
                    else{
                        FullBodyCustomer *customer = new FullBodyCustomer(customerName, IDcounter);
                        customersList.push_back(customer);
                    }
                    trainerCapacity--;
                    IDcounter++;
                    pos1 = s.find(' ' , pos1+1);
                    pos2 = s.find(',' , pos2+1);
                }
                OpenTrainer *open_trainer = new OpenTrainer(trainerID, customersList);
                open_trainer->act(*this);
                this->actionsLog.push_back(open_trainer);
            }
        }

    }
}
int Studio:: getNumOfTrainers() const{
    return numOfTrainers;
}
Trainer* Studio:: getTrainer(int tid){
    if((tid < 0) || (tid>=(int)(trainers.size()) ) ){
        return nullptr;
    }
    return trainers[tid];
}
const std::vector<BaseAction*>& Studio:: getActionsLog() const{
    return actionsLog;
} // Return a reference to the history of actions
std::vector<Workout>& Studio:: getWorkoutOptions(){
    return workout_options;
}
void Studio:: closeStudio(){
    open = false;
}

