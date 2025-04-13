#include <iostream>
#include <fstream>
using namespace std;

struct Hotel {
    int room_no;
    char name[30];
    char address[50];
    char phone[15];
    long days;
    long cost;
    char rtype[30];
    long pay;
};

void bookRoom(Hotel hotel[], int &totalRooms);
void displayCustomer(Hotel hotel[], int totalRooms);
void roomsAllotted(Hotel hotel[], int totalRooms);
void modifyRoom(Hotel hotel[], int &totalRooms);  
int checkRoom(int roomNum, Hotel hotel[], int totalRooms);
void restaurantMenu(Hotel hotel[], int totalRooms);
void saveToFile(Hotel hotel[], int totalRooms);
void loadFromFile(Hotel hotel[], int &totalRooms);
void mainMenu(Hotel hotel[], int &totalRooms);

int main() {
    int totalRooms = 0;
    Hotel hotel[100];
    loadFromFile(hotel, totalRooms);
    mainMenu(hotel, totalRooms);
    return 0;
}

void loadFromFile(Hotel hotel[], int &totalRooms) {
    ofstream createFile("rooms.txt", ios::app | ios::binary);
    createFile.close();

    ifstream file("rooms.txt", ios::in | ios::binary);
    if (file.is_open()) {
        while (file.read((char*)&hotel[totalRooms], sizeof(Hotel))) {
            totalRooms++;
        }
        file.close();
    } else {
        cout << "Error loading data from file.\n";
    }
}

void mainMenu(Hotel hotel[], int &totalRooms) {
    int choice;
    while (true) {
        cout << "\n\t\t\t\t ***************************";
        cout << "\n\t\t\t\t **Hotel Management System**";
        cout << "\n\t\t\t\t ***************************";
        cout << "\n\t\t\t\t **      MAIN MENU        **";
        cout << "\n\t\t\t\t ***************************";
        cout << "\n\n\n\t\t\t\t1. Book A Room";
        cout << "\n\t\t\t\t2. Customer Information";
        cout << "\n\t\t\t\t3. Show Rooms Allotted";
        cout << "\n\t\t\t\t4. Edit Customer Details";
        cout << "\n\t\t\t\t5. Order Food from Restaurant";
        cout << "\n\t\t\t\t6. Exit";
        cout << "\n\n\t\t\t\tEnter Your Choice: ";
        cin >> choice;

        switch (choice) {
            case 1:
                bookRoom(hotel, totalRooms);
                break;
            case 2:
                displayCustomer(hotel, totalRooms);
                break;
            case 3:
                roomsAllotted(hotel, totalRooms);
                break;
            case 4:
                modifyRoom(hotel, totalRooms);  
                break;
            case 5:
                restaurantMenu(hotel, totalRooms);
                break;
            case 6:
                saveToFile(hotel, totalRooms);
                return;
            default:
                cout << "\n\n\t\t\tWrong choice.";
                cout << "\n\t\t\tPress any key to continue.";
        }
    }
}

void bookRoom(Hotel hotel[], int &totalRooms) {
    int r;
    cout << "\nENTER CUSTOMER DETAILS";
    cout << "\nRoom Number: ";
    cin >> r;

    if (checkRoom(r, hotel, totalRooms) == 1) {
        cout << "\nSorry, Room is already booked.\n";
    } else {
        hotel[totalRooms].room_no = r;
        cout << "Name: ";
        cin >> hotel[totalRooms].name;
        cout << "Address: ";
        cin >> hotel[totalRooms].address;
        cout << "Phone Number: ";
        cin >> hotel[totalRooms].phone;
        cout << "Number of Days: ";
        cin >> hotel[totalRooms].days;


        if (hotel[totalRooms].room_no >= 1 && hotel[totalRooms].room_no <= 50) {
            const char *roomType = "Deluxe";
            int i = 0;
            while (roomType[i] != '\0') {
                hotel[totalRooms].rtype[i] = roomType[i];
                i++;
            }
            hotel[totalRooms].cost = hotel[totalRooms].days * 1000;
        } else if (hotel[totalRooms].room_no >= 51 && hotel[totalRooms].room_no <= 80) {
            const char *roomType = "Executive";
            int i = 0;
            while (roomType[i] != '\0') {
                hotel[totalRooms].rtype[i] = roomType[i];
                i++;
            }
            hotel[totalRooms].cost = hotel[totalRooms].days * 1250;
        } else if (hotel[totalRooms].room_no >= 81 && hotel[totalRooms].room_no <= 100) {
            const char *roomType = "Presidential";
            int i = 0;
            while (roomType[i] != '\0') {
                hotel[totalRooms].rtype[i] = roomType[i];
                i++;
            }
            hotel[totalRooms].cost = hotel[totalRooms].days * 1500;
        }
        cout << "\nRoom has been booked.";
        totalRooms++;
        saveToFile(hotel, totalRooms);
    }
}

void displayCustomer(Hotel hotel[], int totalRooms) {
    int r;
    cout << "\nEnter Room Number: ";
    cin >> r;

    for (int i = 0; i < totalRooms; ++i) {
        if (hotel[i].room_no == r) {
            cout << "\nCustomer Details";
            cout << "\nRoom Number: " << hotel[i].room_no;
            cout << "\nName: " << hotel[i].name;
            cout << "\nAddress: " << hotel[i].address;
            cout << "\nPhone Number: " << hotel[i].phone;
            cout << "\nStaying for " << hotel[i].days << " days.";
            cout << "\nRoom Type: " << hotel[i].rtype;
            cout << "\nTotal cost of stay: " << hotel[i].cost;
            return;
        }
    }
    cout << "\nRoom is Vacant.";
}

void roomsAllotted(Hotel hotel[], int totalRooms) {
    cout << "\n\t\t\t    LIST OF ROOMS ALLOTTED";
    cout << "\n\n +---------+------------------+-----------------+--------------+--------------+";
    cout << "\n | Room No.|    Guest Name    |      Address    |   Room Type  |  Contact No. |";
    cout << "\n +---------+------------------+-----------------+--------------+--------------+";

    for (int i = 0; i < totalRooms; i++) {
        cout << "\n |" << hotel[i].room_no << " |";
        cout << hotel[i].name << " |";
        cout << hotel[i].address << " |";
        cout << hotel[i].rtype << " |";
        cout << hotel[i].phone << " |";
    }
    cout << "\n +---------+------------------+-----------------+--------------+--------------+";
}

void modifyRoom(Hotel hotel[], int &totalRooms) {
    int choice, r;
    cout << "\n MODIFY MENU";
    cout << "\n\n1. Modify Customer Information.";
    cout << "\n2. Customer Check Out.";
    cout << "\n3. Delete Room.";  
    cout << "\nEnter your choice: ";
    cin >> choice;

    cout << "\nEnter Room Number: ";
    cin >> r;

    for (int i = 0; i < totalRooms; i++) {
        if (hotel[i].room_no == r) {
            switch (choice) {
                case 1:
                    cout << "Enter new Name: ";
                    cin >> hotel[i].name;
                    cout << "Enter new Address: ";
                    cin >> hotel[i].address;
                    cout << "Enter new Phone Number: ";
                    cin >> hotel[i].phone;
                    cout << "Enter new Number of Days: ";
                    cin >> hotel[i].days;
                    break;
                case 2:
                    cout << "Checking out customer...\n";
                    hotel[i].room_no = 0;  
                    break;
                case 3:
                    
                    hotel[i].room_no = 0;  
                    cout << "\nRoom " << r << " has been deleted.\n";

        
                    for (int j = i; j < totalRooms - 1; j++) {
                        hotel[j] = hotel[j + 1];  
                    }
                    totalRooms--; 
                    saveToFile(hotel, totalRooms);  
                    return;
                default:
                    cout << "\nWrong choice!";
                    break;
            }
            saveToFile(hotel, totalRooms);  
            return;
        }
    }
    cout << "\nRoom not found!";
}

void restaurantMenu(Hotel hotel[], int totalRooms) {
    int r, ch;
    cout << "\nRESTAURANT MENU:";
    cout << "\n1. Order Breakfast";
    cout << "\n2. Order Lunch";
    cout << "\n3. Order Dinner";
    cout << "\nEnter your choice: ";
    cin >> ch;

    cout << "\nEnter Room Number: ";
    cin >> r;

    for (int i = 0; i < totalRooms; i++) {
        if (hotel[i].room_no == r) {
            switch (ch) {
                case 1:
                    hotel[i].pay += 500;
                    break;
                case 2:
                    hotel[i].pay += 800;
                    break;
                case 3:
                    hotel[i].pay += 1000;
                    break;
                default:
                    cout << "\nInvalid choice!";
            }
            cout << "\nOrder has been placed.";
            saveToFile(hotel, totalRooms);
            return;
        }
    }
    cout << "\nRoom not found!";
}

int checkRoom(int roomNum, Hotel hotel[], int totalRooms) {
    for (int i = 0; i < totalRooms; i++) {
        if (hotel[i].room_no == roomNum) {
            return 1;  
        }
    }
    return 0; 
}

void saveToFile(Hotel hotel[], int totalRooms) {
    ofstream file("rooms.txt", ios::out | ios::binary);
    if (file.is_open()) {
        for (int i = 0; i < totalRooms; i++) {
            file.write((char*)&hotel[i], sizeof(Hotel));
        }
        file.close();
    } else {
        cout << "Error saving data to file.\n";
    }
}
