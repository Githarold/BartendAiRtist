//main.ino
#include <Arduino.h>
#include "step.h"
#include "linear.h"
#include "dc.h"


#define MAX_SIZE 10  // 최대 리스트 크기를 10으로 정의


int disk_rotate_list[MAX_SIZE];
int dispenser_push_list[MAX_SIZE];
int listSize = 0;  // 실제 사용되는 리스트의 크기


int dc_motor_state = 0;  // 전역 변수로 선언


void setup() {
    Serial.begin(9600);  // 시리얼 통신을 9600 baud rate로 시작합니다.


    // 모터 핀 설정
    setupMotorPins(0);        // 스텝모터 A핀 설정
    setupMotorPins(1);        // 스텝모터 B핀 설정


    // 역방향 딜레이 설정
    setupReverseDelays();     // 모터의 역방향 운동에 사용할 딜레이를 설정합니다.


    // 선형 모터 핀 설정
    
    pinMode(ENC, OUTPUT);
    pinMode(IN5, OUTPUT);
    pinMode(IN6, OUTPUT);


    pinMode(END, OUTPUT);
    pinMode(IN7, OUTPUT);
    pinMode(IN8, OUTPUT);
}


void loop() {
   
    

    if (Serial.available() > 0) {
        String data = Serial.readStringUntil('\n');
        
        parseData(data);  // 데이터 파싱 함수 호출

        
        
        // 모터 작동 로직 실행
        for (int i = 0; i < listSize; i++) {
            disk_rotate(disk_rotate_list[i]);
            delay(1000);
           
            for (int j = 0; j < dispenser_push_list[i]; j++) {
                dispenser_activate(255, 255, 1000);
                delay(1000);  // dispenser_activate 사이의 딜레이
            }
        }

      
      if (dc_motor_state == 1) {
        stir(63, 50, 2);
        delay(1000);
      }

      Serial.println("1");
    }
}




void parseData(String data) {
    int firstComma = data.indexOf(',');
    dc_motor_state = data.substring(0, firstComma).toInt();  // dc_input 파싱



    int firstBracket = data.indexOf('[', firstComma);
    int secondBracket = data.indexOf(']', firstBracket + 1);
    int thirdBracket = data.indexOf('[', secondBracket + 1);
    int lastBracket = data.indexOf(']', thirdBracket + 1);


    // 디스크 회전 목록 파싱
    String diskData = data.substring(firstBracket + 1, secondBracket);
    listSize = 0;  // 리스트 크기 초기화
    int startPos = 0;
    int endPos = diskData.indexOf(',');


    while (endPos != -1) {
        disk_rotate_list[listSize] = diskData.substring(startPos, endPos).toInt();
        startPos = endPos + 1;
        endPos = diskData.indexOf(',', startPos);
        listSize++;
    }
    disk_rotate_list[listSize] = diskData.substring(startPos).toInt();  // 마지막 원소 처리
    listSize++;  // 리스트 크기 업데이트


    // 디스펜서 푸시 목록 파싱
    String dispenserData = data.substring(thirdBracket + 1, lastBracket);
    startPos = 0;
    endPos = dispenserData.indexOf(',');
    int dispenserCount = 0;


    while (endPos != -1) {
        dispenser_push_list[dispenserCount] = dispenserData.substring(startPos, endPos).toInt();
        startPos = endPos + 1;
        endPos = dispenserData.indexOf(',', startPos);
        dispenserCount++;
    }
    dispenser_push_list[dispenserCount] = dispenserData.substring(startPos).toInt();  // 마지막 원소 처리
}
