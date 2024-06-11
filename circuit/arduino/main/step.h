// step.h
#ifndef STEP_H
#define STEP_H


#include <Arduino.h>


// 모터 핀에 대한 상수 정의
extern const int ENA[2];
extern int IN1[2];
extern int IN2[2];
extern const int ENB[2];
extern int IN3[2];
extern int IN4[2];


// 딜레이 배열에 대한 상수 정의
extern const int delays[];
extern int reverseDelays[];


// 모터 핀을 설정하는 함수
void setupMotorPins(int motor);


// 역방향 딜레이를 설정하는 함수
void setupReverseDelays();


// 스텝 실행 함수
void performSteps(int motor, const int delayArray[]);


// 모터를 부드럽게 시작하는 함수
void softStart(int motor);


// 모터를 부드럽게 정지하는 함수
void softStop(int motor);


// 모터를 부드럽게 시작하는 함수
void stepMotorSoftStart(int motor, int stepDelay);


// 추가 스텝 실행 함수
void performAdditionalSteps(int motor, int steps);


// 디스크 회전 함수
void disk_rotate(int disk_step);


#endif
