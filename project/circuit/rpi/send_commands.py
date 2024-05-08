import serial
import time




# 아두이노와의 시리얼 연결 설정
ser = serial.Serial('/dev/ttyACM0', 9600)  # 'ttyUSB0'는 연결된 아두이노의 포트에 따라 달라질 수 있습니다.
time.sleep(2)  # 아두이노가 재시작하는 동안 기다립니다.




def send_command(cmd):
   """아두이노에 명령을 전송하는 함수"""
   ser.write(str(cmd).encode())  # 명령을 문자열로 변환 후 바이트로 인코딩하여 전송
   time.sleep(0.5)  # 아두이노가 명령을 처리하는 시간을 기다림




try:
   # 예제 명령 전송
   send_command(1)  # 명령 1 전송
   time.sleep(1)  # 명령 사이에 충분한 시간을 두어 처리할 수 있도록 함
   send_command(0)  # 모터 정지 명령 전송




finally:
   ser.close()  # 시리얼 포트 정리


