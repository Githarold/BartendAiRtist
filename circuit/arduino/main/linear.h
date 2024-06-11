
//linear.h


#ifndef LINEAR_H
#define LINEAR_H




extern const int ENC;
extern int IN5;
extern int IN6;






void linear_up(int power, int time);
void linear_down(int power, int time);
void linear_stop(int power, int time);


void dispenser_activate(int up_power,int down_power, int time);


#endif
