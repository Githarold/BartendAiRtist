import RPi.GPIO as GPIO
import time
import serial

# 시리얼 포트 설정 (라즈베리파이와 아두이노 간 통신)
# ser = serial.Serial('/dev/ttyACM0', 9600, timeout=1)

# 경고 메시지 비활성화
GPIO.setwarnings(False)

# GPIO 핀 번호 설정 (BCM 모드)
GPIO.setmode(GPIO.BCM)
ENDSTOP_PIN = 14

# 엔드스탑 핀을 입력으로 설정하고 풀다운 저항 활성화
GPIO.setup(ENDSTOP_PIN, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

print("Waiting for endstop signal...")

# 초기 상태를 확인하여 초기 신호를 무시함
initial_state = GPIO.input(ENDSTOP_PIN)

try:
   while True:
       current_state = GPIO.input(ENDSTOP_PIN)
       if current_state == GPIO.HIGH and initial_state == GPIO.LOW:
           print("1")  # 스위치가 눌리면 1을 출력
           #ser.write(b'stop\n')  # 아두이노에 모터를 멈추라는 신호를 보냄
           # 스위치가 눌린 상태가 끝날 때까지 기다림
           while GPIO.input(ENDSTOP_PIN) == GPIO.HIGH:
               time.sleep(0.1)
       initial_state = current_state
       time.sleep(0.1)

except KeyboardInterrupt:
   print("Program terminated by user.")

finally:
   GPIO.cleanup()
