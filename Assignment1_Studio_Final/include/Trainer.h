#ifndef TRAINER_H_
#define TRAINER_H_

#include <vector>
#include "Customer.h"
#include "Workout.h"

typedef std::pair<int, Workout> OrderPair;

class Trainer{
public:

    Trainer(int t_capacity);//Constructor
    Trainer(const Trainer &other);//Copy Constructor
    virtual ~Trainer();//Destructor
    Trainer(Trainer&& other);//Move Constructor
    Trainer& operator=(const Trainer &other);//Copy Assignment
    Trainer& operator=(Trainer &&other);//Move Assignment
    int getCapacity() const;
    void addCustomer(Customer* customer);
    void removeCustomer(int id);
    Customer* getCustomer(int id);
    std::vector<Customer*>& getCustomers();
    std::vector<OrderPair>& getOrders();
    void order(const int customer_id, const std::vector<int>& workout_ids, const std::vector<Workout>& workout_options);
    void openTrainer();
    void closeTrainer();
    int getSalary();
    bool isOpen() const;
    int howManyCustomers();

private:
    int salary;
    int capacity;
    bool open;
    std::vector<Customer*> customersList;
    std::vector<OrderPair> orderList; //A list of pairs for each order for the trainer - (customer_id, Workout)
    void clear();
    void copy(const int& other_capacity, const bool& other_open, const std::vector<Customer*>& other_customersList, const std::vector<OrderPair>& other_orderList );
};


#endif