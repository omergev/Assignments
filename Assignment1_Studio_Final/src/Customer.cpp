//
// Created by spl211 on 13/11/2021.
//
#include "Customer.h"
#include <algorithm>
#include <utility>
//Customer
Customer:: Customer(std::string c_name, int c_id) : name(std::move(c_name)) , id(c_id){
}

Customer:: ~Customer() = default;

std::string Customer:: getName() const{
    return name;
}
int Customer:: getId() const{
    return id;
}
void Customer:: changeOrderStatus(){
    alreadyOrdered = true;
}
bool Customer:: getOrderStatus() const{
    return alreadyOrdered;
}

//Sweaty Customer
SweatyCustomer:: SweatyCustomer(std::string name, int id): Customer(std::move(name), id) {

}

SweatyCustomer:: ~SweatyCustomer() = default;


std::vector<int> SweatyCustomer:: order(const std::vector<Workout> &workout_options){
    std::vector<int> order;
    for (int i = 0; i < (int)(workout_options.size()); ++i) {
        if(workout_options[i].getType() == CARDIO){
            order.push_back(workout_options[i].getId());
        }
    }
    return order;
}
std::string SweatyCustomer:: toString() const{
    return "swt";
}

Customer* SweatyCustomer:: clone() const {
    return new SweatyCustomer(getName(),getId()) ;
}

//Cheap Customer
CheapCustomer:: CheapCustomer(std::string name, int id): Customer(name,id){

}

CheapCustomer:: ~CheapCustomer() = default;


std::vector<int> CheapCustomer:: order(const std::vector<Workout> &workout_options){
    std::vector<int> order;
    int cheapest = INT32_MAX;
    int cheapest_id = -1;
    for (int i = 0; i <(int)(workout_options.size()) ; ++i) {
        if(workout_options[i].getPrice() < cheapest){
            cheapest = workout_options[i].getPrice();
            cheapest_id = workout_options[i].getId();
        }
    }
    if(cheapest_id != -1){
        order.push_back(cheapest_id);
    }
    return order;

}
std::string CheapCustomer:: toString() const{
    return "chp";
}

Customer* CheapCustomer:: clone() const {
    return new CheapCustomer(getName(), getId());
}

//Heavy Muscle Customer
HeavyMuscleCustomer:: HeavyMuscleCustomer(std::string name, int id): Customer(std::move(name),id){

}

HeavyMuscleCustomer:: ~HeavyMuscleCustomer() = default;


std::vector<int> HeavyMuscleCustomer:: order(const std::vector<Workout> &workout_options){

    std::vector<int> orderWorkout;
    typedef std::pair<int, int> intPair;
    std::vector<intPair> SortWorkout = std::vector<intPair>();
    for (size_t i = 0; i < workout_options.size(); i++) { //Push all kinds of anaerobic
        if (workout_options[i].getType() == ANAEROBIC)
            SortWorkout.push_back(std::make_pair(workout_options[i].getId(), workout_options[i].getPrice()));
    }
    //Sort the anaerobic activities
    while (!SortWorkout.empty()) {
        int max = -1;
        int maxId = -1;
        int index = -1;
        for (int i = 0; i < (int)(SortWorkout.size()); i++) {
            intPair pair = SortWorkout[i];
            if (max == -1 || max < pair.second || (max == pair.second && maxId < pair.first)) {
                max = pair.second;
                maxId = pair.first;
                index = i;
            }
        }
        SortWorkout.erase(SortWorkout.begin() + index);
        orderWorkout.push_back(maxId);
    }
    return orderWorkout;
}

std::string HeavyMuscleCustomer:: toString() const{
    return "mcl";
}

Customer* HeavyMuscleCustomer:: clone() const {
    return new HeavyMuscleCustomer(getName(), getId());
}


//Full Body Customer
FullBodyCustomer:: FullBodyCustomer(std::string name, int id): Customer(std::move(name),id){

}

FullBodyCustomer:: ~FullBodyCustomer() = default;


std::vector<int> FullBodyCustomer:: order(const std::vector<Workout> &workout_options){
    std::vector<int> order;
    int cheapest_cardio = INT32_MAX;
    int cheapest_cardio_id = -1;
    int expensive_mix = INT32_MIN;
    int expensive_mix_id = -1;
    int cheapest_anaerobic = INT32_MAX;
    int cheapest_anaerobic_id = -1;

    for (int i = 0; i < (int)(workout_options.size()) ; ++i) {
        if((workout_options[i].getPrice() < cheapest_cardio) && (workout_options[i].getType() == CARDIO) ){
            cheapest_cardio = workout_options[i].getPrice();
            cheapest_cardio_id = workout_options[i].getId();
        }
        if((workout_options[i].getPrice() > expensive_mix) && (workout_options[i].getType() == MIXED) ){
            expensive_mix = workout_options[i].getPrice();
            expensive_mix_id = workout_options[i].getId();
        }
        if((workout_options[i].getPrice() < cheapest_anaerobic) && (workout_options[i].getType() == ANAEROBIC)){
            cheapest_anaerobic = workout_options[i].getPrice();
            cheapest_anaerobic_id = workout_options[i].getId();
        }
    }
    //Check if there is such workouts
    if(cheapest_cardio_id != 1){
        order.push_back(cheapest_cardio_id);
    }
    if(expensive_mix_id != 1){
        order.push_back(expensive_mix_id);
    }
    if(cheapest_anaerobic_id != 1){
        order.push_back(cheapest_anaerobic_id);
    }
    return order;

}
std::string FullBodyCustomer:: toString() const{
    return "fbd";
}

Customer* FullBodyCustomer:: clone() const {
    return new FullBodyCustomer(getName(), getId());
}

