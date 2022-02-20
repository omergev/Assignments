#ifndef STUDIO_H_
#define STUDIO_H_

#include <vector>
#include <string>
#include "Workout.h"
#include "Trainer.h"
#include "Action.h"


class Studio{		
public:
	Studio();//Empty Constructor
    Studio(const std::string &configFilePath);//Constructor;;;
    //Rule of five
    virtual ~Studio();//Destructor
    Studio(const Studio &other);//Copy Constructor
    Studio(Studio &&other);//Move Constructor
    Studio& operator=(const Studio &other);//Copy Assignment
    Studio& operator=(Studio &&other);//Move Assignment
    void start();
    int getNumOfTrainers() const;
    Trainer* getTrainer(int tid);
	const std::vector<BaseAction*>& getActionsLog() const; // Return a reference to the history of actions
    std::vector<Workout>& getWorkoutOptions();
    void closeStudio();

private:
    int numOfTrainers;
    bool open;
    std::vector<Trainer*> trainers;
    std::vector<Workout> workout_options;
    std::vector<BaseAction*> actionsLog;
    int IDcounter =0;
    void copy(const int &other_numOfTrainer, const bool &other_open, const std::vector<Trainer*>& other_trainers,const std::vector<Workout>& other_workout_options,
              const std::vector<BaseAction*>& other_actionsLog, const int& other_IDcounter);
    void clear();
};

#endif