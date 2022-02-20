#include "Action.h"
#include "Trainer.h"
#include "Studio.h"

extern Studio *backup;

BaseAction::BaseAction(): errorMsg(), status() {}

ActionStatus BaseAction::getStatus() const {
    return status;
}

void BaseAction::complete() {
    status = COMPLETED;
}

void BaseAction::error(std::string errorMsg) {
    status = ERROR;
    this->errorMsg = errorMsg;//initialize the errorMsg of OpenTrainer
    std::cout << getErrorMsg() << std::endl;
}

std::string BaseAction::getErrorMsg() const {
    return errorMsg;
}

BaseAction *BaseAction::clone() {
    return nullptr;
}

//Open Trainer
OpenTrainer::OpenTrainer(int id, std::vector<Customer *> &customersList) : trainerId(id), customers(customersList) {}


void OpenTrainer::act(Studio &studio) {
    openString = "open " + std::to_string(trainerId) + " ";
    for (int i = 0; i <(int) (customers.size()); ++i) {
        Customer *customer_curr = customers.at(i);
        std::string c_name = customer_curr->getName();
        std::string c_strategy = customer_curr->toString();
        openString += c_name + "," + c_strategy + " ";
    }
    int tid = trainerId;
    Trainer *getTrainer = studio.getTrainer(tid);
    if ((getTrainer == nullptr) ||( getTrainer->isOpen() )||(
        trainerId >= studio.getNumOfTrainers() )|| (trainerId<0)) {
        BaseAction::error("Workout session does not exist or is already open");
        for (int i = 0; i <(int) (customers.size()); ++i) {
            delete customers[i];
        }
    } else {
        getTrainer->openTrainer();
        complete();
        for (int i = 0; i < (int)(customers.size()); ++i) {
            getTrainer->addCustomer(customers.at(i));
        }
    }

    if (getStatus() == COMPLETED) {
        openString += "Completed";
    } else {
        openString += "Error: " + getErrorMsg();
    }
}


std::string OpenTrainer::toString() const {
    return openString;
}

BaseAction *OpenTrainer::clone() {
    OpenTrainer *open = new OpenTrainer(trainerId, customers);
    open->openString = this->openString;
    return open;
}

//Order
Order::Order(int id) : trainerId(id) {}

void Order::act(Studio &studio) {
    int tid = this->trainerId;
    Trainer *getTrainer = studio.getTrainer(tid);
    if ((getTrainer == nullptr) || (!(getTrainer->isOpen())))
        BaseAction::error("Trainer does not exist or is not open");
    else {
        std::vector<Customer *> &v_customers = getTrainer->getCustomers();
        for (int i = 0; i < (int)(v_customers.size()); ++i) {
            Customer *customer_curr = v_customers.at(i);
            if (!(customer_curr->getOrderStatus())) {
                std::vector<Workout> &workout_options = studio.getWorkoutOptions();
                std::vector<int> workout_ids = customer_curr->order(workout_options);
                getTrainer->order(customer_curr->getId(), workout_ids, workout_options);
                customer_curr->changeOrderStatus();
            }
        }
        std::vector<OrderPair> &v_orders = getTrainer->getOrders();
        std::cout << "order " + std::to_string(tid) << std::endl;
        for (int i = 0; i <(int)(v_orders.size()); ++i) {
            OrderPair pair = v_orders.at(i);
            int customer_id = pair.first;
            Customer *customer = getTrainer->getCustomer(customer_id);
            std::string customer_name = customer->getName();
            Workout workout = pair.second;
            std::string workout_name = workout.getName();
            std::cout << customer_name + " Is Doing " + workout_name << std::endl;
        }
        complete();
    }

}

std::string Order::toString() const {
    std::string order_s = "order " + std::to_string(trainerId);
    if (getStatus() == COMPLETED) {
        order_s = order_s + " Completed";
        return order_s;
    }
    order_s = order_s + " Error: " + getErrorMsg();
    return order_s;
}

BaseAction *Order::clone() {
    return new Order(trainerId);
}

//Move Customer
MoveCustomer::MoveCustomer(int src, int dst, int customerId) : srcTrainer(src), dstTrainer(dst), id(customerId) {}

void MoveCustomer::act(Studio &studio) {
    Trainer *src_Trainer = studio.getTrainer(srcTrainer);
    Trainer *dst_Trainer = studio.getTrainer(dstTrainer);
    if ((src_Trainer == nullptr) || (!(src_Trainer->isOpen())) || (dst_Trainer == nullptr) || (!(dst_Trainer->isOpen())) ||src_Trainer->getCustomer(id) == nullptr || dst_Trainer->getCapacity() - dst_Trainer->howManyCustomers() == 0)
        BaseAction::error("Cannot move customer");
    else {
        Customer *move_customer = src_Trainer->getCustomer(id);
        src_Trainer->removeCustomer(id);
        dst_Trainer->addCustomer(move_customer);
        if (move_customer->getOrderStatus()) {
            std::vector<Workout> &workout_option = studio.getWorkoutOptions();
            std::vector<int> workout_ids = move_customer->order(workout_option);
            dst_Trainer->order(id, workout_ids, workout_option);
        }
        if (studio.getTrainer(srcTrainer)->howManyCustomers() == 0) {
            studio.getTrainer(srcTrainer)->closeTrainer();
        }
        complete();
    }
}

std::string MoveCustomer::toString() const {
    std::string moveCustomer_s =
            "move " + std::to_string(srcTrainer) + " " + std::to_string(dstTrainer) + " " + std::to_string(id);
    if (getStatus() == COMPLETED) {
        moveCustomer_s = moveCustomer_s + " Completed";
        return moveCustomer_s;
    }
    moveCustomer_s = moveCustomer_s + " Error: " + getErrorMsg();
    return moveCustomer_s;
}

BaseAction *MoveCustomer::clone() {
    return new MoveCustomer(srcTrainer, dstTrainer, id);
}

//Close
Close::Close(int id) : trainerId(id) {}

void Close::act(Studio &studio) {
    Trainer *t = studio.getTrainer(trainerId);
    if ((t != nullptr) && (t->isOpen())) {
        t->closeTrainer();
        std::cout << ("Trainer " + std::to_string(trainerId) + " closed. Salary " + std::to_string(t->getSalary()) +
                      "NIS") << std::endl;
        complete();
    } else {
        error("Trainer does not exist or is not open");
    }
}

std::string Close::toString() const {
    std::string close_s = "close " + std::to_string(trainerId);
    if (getStatus() == COMPLETED) {
        close_s = close_s + " Completed";
        return close_s;
    }
    close_s = close_s + " Error: " + getErrorMsg();
    return close_s;
}

BaseAction *Close::clone() {
    return new Close(trainerId);
}

//Close all
CloseAll::CloseAll() = default;

void CloseAll::act(Studio &studio) {
    for (int i = 0; i < studio.getNumOfTrainers(); ++i) {
        Trainer *t = studio.getTrainer(i);
        if (studio.getTrainer(i)->isOpen()) {
            t->closeTrainer();
            std::cout << ("Trainer " + std::to_string(i) + " closed. Salary " + std::to_string(t->getSalary()) + "NIS")
                      << std::endl;
        }
    }
    const std::vector<BaseAction *> actions = (std::vector<BaseAction *> &) studio.getActionsLog();
}

std::string CloseAll::toString() const {
    return "closeall Completed";
}

BaseAction *CloseAll::clone() {
    return new CloseAll();
}

//Print Workout Options
PrintWorkoutOptions::PrintWorkoutOptions() = default;

void PrintWorkoutOptions::act(Studio &studio) {
    std::vector<Workout> &options = studio.getWorkoutOptions();
    for (int i = 0; i <(int)(options.size()); ++i) {
        if (options[i].getType() == CARDIO) {
            std::cout << (options[i].getName() + ", Cardio, " + std::to_string(options[i].getPrice())) << std::endl;
        } else if (options[i].getType() == ANAEROBIC) {
            std::cout << (options[i].getName() + ", Anaerobic, " + std::to_string(options[i].getPrice())) << std::endl;
        } else {
            std::cout << (options[i].getName() + ", Mixed, " + std::to_string(options[i].getPrice())) << std::endl;
        }

    }
    complete();
}

std::string PrintWorkoutOptions::toString() const {
    return "workout_options Completed";
}

BaseAction *PrintWorkoutOptions::clone() {
    return new PrintWorkoutOptions();
}

//Print Train Status
PrintTrainerStatus::PrintTrainerStatus(int id) : trainerId(id) {}

void PrintTrainerStatus::act(Studio &studio) {
    Trainer *t = studio.getTrainer(trainerId);
    if (t->isOpen()) {
        std::cout << ("Trainer " + std::to_string(trainerId) + " status: open") << std::endl;
        std::cout << "Customers:" << std::endl;
        std::vector<Customer *> &c = t->getCustomers();
        std::vector<std::pair<int, Workout>> &o = t->getOrders();
        for (int i = 0; i <(int)(c.size()); ++i) {
            std::cout << (std::to_string(c[i]->getId()) + " " + c[i]->getName()) << std::endl;
        }
        std::cout << "Orders:" << std::endl;
        for (int i = 0; i <(int)(o.size()); ++i) {
            std::cout << (o[i].second.getName() + " " + std::to_string(o[i].second.getPrice()) + "NIS " +
                          std::to_string(o[i].first)) << std::endl;
        }
        std::cout << ("Current Trainer's Salary: " + std::to_string(t->getSalary()) + "NIS") << std::endl;
    } else {
        std::cout << "Trainer " + std::to_string(trainerId) + " status: close" << std::endl;
    }
    complete();
}

std::string PrintTrainerStatus::toString() const {
    return "status " + std::to_string(trainerId) + " Completed";
}

BaseAction *PrintTrainerStatus::clone() {
    return new PrintTrainerStatus(trainerId);
}

//Print Action Logs
PrintActionsLog::PrintActionsLog() = default;

void PrintActionsLog::act(Studio &studio) {
    std::vector<BaseAction *> vec = studio.getActionsLog();
    for (int i = 0; i < (int)(vec.size()); ++i)
        std::cout << vec[i]->toString() << std::endl;
    complete();
}

std::string PrintActionsLog::toString() const {
    std::string s = "log Completed";
    return s;
}

BaseAction *PrintActionsLog::clone() {
    return new PrintActionsLog();
}

//Backup Studio
BackupStudio::BackupStudio() = default;

void BackupStudio::act(Studio &studio) {
    if (backup != nullptr) {
        delete backup;
        backup = nullptr;
    }
    backup = new Studio(studio);
    complete();
}

std::string BackupStudio::toString() const {
    return "backup Completed";
}

BaseAction *BackupStudio::clone() {
    return new BackupStudio();
}

//Restore Studio
RestoreStudio::RestoreStudio() = default;

void RestoreStudio::act(Studio &studio) {
    if (backup == nullptr)//check if (extern)backup is empty (default is 0)
        error("No backup available");
    else {
        Studio *tempStudio = new Studio(*backup);
        studio = std::move(*tempStudio);
        delete tempStudio;
    }
}

std::string RestoreStudio::toString() const {
    std::string restoreStudio_s = "restore";
    if (getStatus() == COMPLETED) {
        restoreStudio_s = restoreStudio_s + " Completed";
        return restoreStudio_s;
    }
    restoreStudio_s = restoreStudio_s + " Error: " + getErrorMsg();
    return restoreStudio_s;
}

BaseAction *RestoreStudio::clone() {
    return new RestoreStudio();
}
