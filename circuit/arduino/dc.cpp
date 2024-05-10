//dc.cpp
#include <Arduino.h>  // Arduino 기본 헤더 파일 포함
#include "dc.h"


// Define pin constants for easy modifications
const int END = 11;   // Motor enable pin (PWM capable pin)
int IN7 = 12;   // Motor input 1
int IN8 = 13;  // Motor input 2


void dc_cw(int power, int time){
    digitalWrite(END, LOW); // 모터 활성화 전 모든 출력을 LOW로 설정
    digitalWrite(IN7, LOW);
    digitalWrite(IN8, LOW);


    for (int speed = 0; speed <= power; speed++) {
        analogWrite(END, speed);
        delay(10);
    }


    digitalWrite(IN7, HIGH);
    digitalWrite(IN8, LOW);
    analogWrite(END, power);
    delay(time);


    for (int speed = power; speed >= 0; speed--) {
        analogWrite(END, speed);
        delay(10);
    }
}


void dc_ccw(int power, int time) {
    digitalWrite(END, LOW); // 모터 활성화 전 모든 출력을 LOW로 설정
    digitalWrite(IN7, LOW);
    digitalWrite(IN8, LOW);


    for (int speed = 0; speed <= power; speed++) {
        analogWrite(END, speed);
        delay(10);
    }


    digitalWrite(IN7, LOW);
    digitalWrite(IN8, HIGH);
    analogWrite(END, power);
    delay(time);


    for (int speed = power; speed >= 0; speed--) {
        analogWrite(END, speed);
        delay(10);
    }
}




void stir(int power, int time, int count){
  for (int i = 0; i < count ; i++){
    dc_cw(power,time);
    dc_ccw(power,time);
  }
  dc_cw(0,5000);
}









