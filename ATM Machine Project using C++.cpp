
#include <iostream>
using namespace std;
int main() {
   int pin;
   int password=123;
   int balance=10000;
   bool istrue=true;
   int errorCount=0;
   int choice;
   double withdrawalmount,depositamount;
   cout<<"\n Enter your pin :";
       cin>>pin;
   do{
      if(pin==password){
          cout<<" ****** wellcome to CBE *******"<<endl;
          cout<<"                               "<<endl;
          cout<<"****  1.withdrewal            **** "<<endl;
          cout<<"****  2.deposit               **** "<<endl;
          cout<<"****  3.balance               **** "<<endl;
          cout<<"****  4.exit                  **** "<<endl;
           cout<<"enter your choice :"<<endl;
           cin>>choice;
           system("cls");
           if(choice==1){
             cout<<"enter your withdrawamount :";
             cin>>withdrawalmount;
             if(withdrawalmount<=balance){
             balance-=withdrawalmount;
             cout<<"your balance is :"<<balance;}
             else{
                cout<<"your balance is not suffiicient :";
             }
           }
           else if(choice==2){
             cout<<"enter your depositamount :";
             cin>>depositamount;
             balance+=depositamount;
             cout<<"your balance is :"<<balance;
           }
           else if(choice==3){
            cout<<"your balance is :"<<balance;
                }
            else{
               istrue=false;
                   }
                }
    else {
            errorCount++;
            cout<<"\n Enter your pin again:";
       cin>>pin;
            if(errorCount==3){
                istrue=false;
               }
         else{
           cout<<"wrong pin! ";
                     }
    }

   }while(istrue!=false);
    return 0;
}
