
//linear.cpp


#include <Arduino.h>  // Arduino 기본 헤더 파일 포함
#include "linear.h"


// 핀 번호 업데이트
const int ENC = 9;   // 모터 활성화 핀 (PWM 사용 가능)
int IN5 = 10;         // 모터 입력 5
int IN6 = 8;         // 모터 입력 6


void linear_up(int power, int time) {
    digitalWrite(IN5, HIGH);  // IN1을 IN5로 변경
    digitalWrite(IN6, LOW);   // IN2를 IN6으로 변경
    analogWrite(ENC, power);
    delay(time);
}


void linear_down(int power, int time) {
    digitalWrite(IN5, LOW);   // IN1을 IN5로 변경
    digitalWrite(IN6, HIGH);  // IN2를 IN6으로 변경
    analogWrite(ENC, power);
    delay(time);
}


void linear_stop(int power, int time) {
    digitalWrite(IN5, LOW);  // IN1을 IN5로 변경
    digitalWrite(IN6, LOW);  // IN2를 IN6으로 변경
    analogWrite(ENC, power);
    delay(time);
}


void dispenser_activate(int up_power,int down_power, int time) {
  linear_up(up_power,time);
  linear_stop(up_power,time);
  linear_down(down_power,time);
  delay(time);


}



