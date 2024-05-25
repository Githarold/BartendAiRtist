import RPi.GPIO as GPIO
import time

# 경고 메시지 비활성화
GPIO.setwarnings(False)

# GPIO 핀 번호 설정 (BCM 모드)
GPIO.setmode(GPIO.BCM)
ENDSTOP_PIN = 14

# 엔드스탑 핀을 입력으로 설정하고 풀업 저항 활성화
GPIO.setup(ENDSTOP_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)

def endstop_callback(channel):
    if GPIO.input(ENDSTOP_PIN) == GPIO.LOW:
        print("1")  # 스위치가 눌리면 1을 출력

# 엔드스탑 핀에 이벤트 감지기 설정 (떨어지는 신호를 감지)
try:
    GPIO.add_event_detect(ENDSTOP_PIN, GPIO.FALLING, callback=endstop_callback, bouncetime=200)
except RuntimeError as e:
    print(f"Failed to add edge detection: {e}")

print("Waiting for endstop signal...")

try:
    while True:
        # 메인 루프를 유지하면서 다른 작업을 수행할 수 있습니다.
        time.sleep(1)

except KeyboardInterrupt:
    print("Program terminated by user.")

finally:
    GPIO.cleanup()
    #new



