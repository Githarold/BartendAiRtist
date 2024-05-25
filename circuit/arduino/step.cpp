// step.cpp
#include "step.h"




const int ENA[2] = {2, 2}; // 모터 A와 B의 활성화 핀
int IN1[2] = {3, 6};       // 모터 A와 B의 입력 1
int IN2[2] = {4, 7};       // 모터 A와 B의 입력 2
const int ENB[2] = {5, 5}; // 모터 A와 B의 활성화 핀
int IN3[2] = {6, 3};       // 모터 A와 B의 입력 3
int IN4[2] = {7, 4};       // 모터 A와 B의 입력 4




const int delays[] = {45, 38, 32, 27, 23, 20, 17, 15, 13, 11, 10, 9, 8, 7, 7, 6, 6, 6, 5, 5, 5, 5, 5, 5, 4};
int reverseDelays[sizeof(delays) / sizeof(int)];




void setupMotorPins(int motor) {
    pinMode(ENA[motor], OUTPUT);
    pinMode(IN1[motor], OUTPUT);
    pinMode(IN2[motor], OUTPUT);
    pinMode(ENB[motor], OUTPUT);
    pinMode(IN3[motor], OUTPUT);
    pinMode(IN4[motor], OUTPUT);




    digitalWrite(ENA[motor], HIGH);
    digitalWrite(ENB[motor], HIGH);
}




void setupReverseDelays() {
    for (int i = 0; i < sizeof(delays) / sizeof(int); i++) {
        reverseDelays[i] = delays[sizeof(delays) / sizeof(int) - 1 - i];
    }
}




void performSteps(int motor, const int delayArray[]) {
    for (int i = 0; i < sizeof(delays) / sizeof(int); i++) {
        stepMotorSoftStart(motor, delayArray[i]);
    }
}




void softStart(int motor) {
    for (int i = 0; i < sizeof(delays) / sizeof(delays[0]); i++) {
        stepMotorSoftStart(motor, delays[i]);
    }
}




void softStop(int motor) {
    for (int i = sizeof(delays) / sizeof(delays[0]) - 1; i >= 0; i--) {
        stepMotorSoftStart(motor, delays[i]);
    }
}




void stepMotorSoftStart(int motor, int stepDelay) {
    digitalWrite(ENA[motor], HIGH);




    digitalWrite(IN1[motor], HIGH);
    digitalWrite(IN2[motor], LOW);
    digitalWrite(IN3[motor], HIGH);
    digitalWrite(IN4[motor], LOW);
    delay(stepDelay);




    digitalWrite(IN1[motor], HIGH);
    digitalWrite(IN2[motor], LOW);
    digitalWrite(IN3[motor], LOW);
    digitalWrite(IN4[motor], HIGH);
    delay(stepDelay);




    digitalWrite(IN1[motor], LOW);
    digitalWrite(IN2[motor], HIGH);
    digitalWrite(IN3[motor], LOW);
    digitalWrite(IN4[motor], HIGH);
    delay(stepDelay);




    digitalWrite(IN1[motor], LOW);
    digitalWrite(IN2[motor], HIGH);
    digitalWrite(IN3[motor], HIGH);
    digitalWrite(IN4[motor], LOW);
    delay(stepDelay);
}




void performAdditionalSteps(int motor, int steps) {
    int d = 4;  // 빠른 스텝을 위한 작은 딜레이
    for (int i = 0; i < steps; i++) {
        digitalWrite(IN1[motor], HIGH);
        digitalWrite(IN2[motor], LOW);
        digitalWrite(IN3[motor], HIGH);
        digitalWrite(IN4[motor], LOW);
        delay(d);




        digitalWrite(IN1[motor], HIGH);
        digitalWrite(IN2[motor], LOW);
        digitalWrite(IN3[motor], LOW);
        digitalWrite(IN4[motor], HIGH);
        delay(d);




        digitalWrite(IN1[motor], LOW);
        digitalWrite(IN2[motor], HIGH);
        digitalWrite(IN3[motor], LOW);
        digitalWrite(IN4[motor], HIGH);
        delay(d);




        digitalWrite(IN1[motor], LOW);
        digitalWrite(IN2[motor], HIGH);
        digitalWrite(IN3[motor], HIGH);
        digitalWrite(IN4[motor], LOW);
        delay(d);
    }
}




void disk_rotate(int disk_step) {
    if (disk_step == 0) {
        
    }
    else if (disk_step > 0 && disk_step <= 7) {
        // 반시계방향 회전 처리
        disk_step = disk_step - 1;
        digitalWrite(ENB[0], HIGH); // 반시계방향 활성화
        softStart(0);  // 반시계 부드러운 시작 (0.5바퀴)
        performAdditionalSteps(0, disk_step * 50);
        softStop(0);   // 반시계 부드러운 정지 (0.5바퀴)
        digitalWrite(ENB[0], LOW); // 반시계방향 비활성화
    }
    else if (disk_step < 0 && disk_step >= -7) {
        // 시계방향 회전 처리
        disk_step = disk_step + 1;
        digitalWrite(ENB[1], HIGH); // 시계방향 활성화
        softStart(1);  // 시계 부드러운 시작 (0.5바퀴)
        performAdditionalSteps(1, disk_step * -50);
        softStop(1);   // 시계방향 부드러운 정지 (0.5바퀴)
        digitalWrite(ENB[1], LOW); // 시계방향 비활성화
    }
    else {
        // 유효하지 않은 입력일 경우 아무 작업도 수행하지 않음
        
    }
}
