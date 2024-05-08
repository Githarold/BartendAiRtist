// main.ino
#include <Arduino.h>
#include "step.h"


void setup() {
    // 모터 핀 설정
    setupMotorPins(0); // 모터 A 설정
    setupMotorPins(1); // 모터 B 설정
   
    // 역방향 딜레이 설정
    setupReverseDelays();
}


void loop() {
   
    disk_rotate(1); //반시계방향으로 한바퀴
    delay(1000);   //딜레이


 
    disk_rotate(-2); //시계방향으로 두바퀴
    delay(5000);    //딜레이
}





