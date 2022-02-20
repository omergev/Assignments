//
// Created by spl211 on 13/11/2021.
//
#include "Trainer.h"
//Constructor
Trainer:: Trainer(int t_capacity): salary(0), capacity(t_capacity) , open(false){
    orderList = std::vector<OrderPair>();
}

//Destructor
Trainer:: ~Trainer() {clear();}
void Trainer::clear() {
    for (int i = 0; i < (int)(customersList.size()); ++i) {
        if(customersList.at(i)!= nullptr) {
            delete customersList.at(i);
            customersList.at(i)= nullptr;
        }
    }
    customersList.clear();
    orderList.clear();
    capacity = 0;
    open = false;
    salary = 0;
}

//Copy Constructor
Trainer::Trainer(const Trainer &other): salary(other.salary), capacity(other.capacity), open(other.open) {
    copy(other.capacity, other.open, other.customersList, other.orderList);
}
void Trainer::copy(const int &other_capacity, const bool &other_open,
                   const std::vector<Customer *> &other_customersList, const std::vector<OrderPair> &other_orderList) {

    for (int i = 0; i < (int)(other_orderList.size()); ++i) {
        int id  = other_orderList.at(i).first;
        Workout workout  = other_orderList.at(i).second;
        OrderPair pair(id,workout);
        orderList.push_back(pair);
    }
    for (int i = 0; i < (int)(other_customersList.size()); ++i) {
        customersList.push_back(other_customersList.at(i)->clone());
    }
}

//Move Constructor
Trainer:: Trainer(Trainer&& other): salary(other.salary), capacity(other.capacity), open(other.open){

    clear();
    customersList = std::move(other.customersList);
    orderList = std::move(other.orderList);


}

//Copy Assignment
Trainer& Trainer:: operator=(const Trainer &other){
    if(this == &other){
        return *this;
    }
    clear();
    copy(other.capacity, other.open, other.customersList, other.orderList);
    return *this;
}

//Move Assignment
Trainer& Trainer:: operator=(Trainer &&other){
    if(this != &other){
        clear();
        customersList = std::move(other.customersList);
        orderList = std::move(other.orderList);
        open = other.open;
        salary = other.salary;
        capacity = other.capacity;
    }

    return *this;
}

int Trainer:: getCapacity() const{
    return capacity;
}

void Trainer:: addCustomer(Customer* customer){
    customersList.push_back(customer);
}

void Trainer:: removeCustomer(int id){
//Check if the customer made any orders and delete them.
    std::vector<std::pair<int, Workout>> tmpVec;
    if(getCustomer(id)->getOrderStatus()){
        for (int i = 0; i < (int)(orderList.size()); ++i) {
            tmpVec.push_back(orderList[i]);
            }
        orderList.clear();
        for (int i = 0; i < (int)(tmpVec.size()); ++i) {
            if(tmpVec[i].first != id){
                orderList.push_back(orderList[i]);
             }
        }
    }
//Delete the specific customer from the customers list.
    bool flag = true;
    for (int i = 0; i < (int)(customersList.size()) && flag; ++i) {
        if(customersList[i]->getId() == id ){
            customersList.erase(customersList.begin()+i);
            flag = false;
        }
    }
}

Customer* Trainer:: getCustomer(int id){

    //Return nullptr if there is no customer with this ID.
    for (int i = 0; i < (int)(customersList.size()); ++i) {
        if(customersList[i]->getId() == id ){
            return customersList[i];
        }
    }
    return nullptr;
}

std::vector<Customer*>& Trainer:: getCustomers(){
    std::vector<Customer*> &ref = customersList;
    return ref;
}

std::vector<OrderPair>& Trainer:: getOrders(){
    std::vector<OrderPair> &ref = orderList;
    return ref;
}

void Trainer:: order(const int customer_id, const std::vector<int>& workout_ids, const std::vector<Workout>& workout_options){
    //Create order pairs and assign them to the order pairs vector.
    for (int i = 0; i < (int)(workout_ids.size()); ++i) {
        OrderPair order(customer_id, workout_options[workout_ids[i]]);
        orderList.push_back(order);
    }
}

void Trainer::  openTrainer(){
    open = true;
}

void Trainer:: closeTrainer(){
     open = false;
     //Accumulate the salary from other sessions + this one and update the total salary.
    for (int i = 0; i < (int)(orderList.size()); ++i) {
        salary += orderList[i].second.getPrice();
    }
    for (int i = 0; i <(int)(customersList.size()); ++i) {
        delete customersList[i];
        customersList[i] = nullptr;
    }
    //Clear the vectors from any elements
    customersList.clear();
    orderList.clear();
     
}

int Trainer:: getSalary(){
    //Accumulate the salary from other sessions + this one
    int tmp_salary = salary;
    for (int i = 0; i < (int)(orderList.size()); ++i) {
        tmp_salary += orderList[i].second.getPrice();
    }
    return tmp_salary;
}

bool Trainer:: isOpen() const{
    return open;
}
int Trainer:: howManyCustomers(){
    return (int)(customersList.size());
}
