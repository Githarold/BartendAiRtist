# import bluetooth

# server_mac = "E0:0A:F6:49:E5:1C" # 서버의 MAC 주소 입력
# port = 1

# client_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
# client_sock.connect((server_mac, port))

# data = "Hello, Bluetooth!"
# client_sock.send(data)

# client_sock.close()

import bluetooth
import signal
import sys

# 서버 정보
HOST = "E0:0A:F6:49:E5:1C"  # 블루투스 컨트롤러 맥 주소
UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"


def signal_handler(sig, frame):
    client_socket.close()
    sys.exit()


signal.signal(signal.SIGINT, signal_handler)

# port는 모르고 name이나 uuid를 아는 경우
# address 를 설정하고 나머지를 None으로 하는 경우 결과가 여러개 나올 수 있음
# name=None은 None인 결과를 찾는 것이 아님. None이 아니더라도 나머지 정보만 일치하면 결과에 포함 됨
# 결과는, 'host', 'name', 'description', 'provider',
# 'protocol', 'service-classes', 'profiles', 'service_id'를 키로하는 딕셔너리
# address와 다른 하나의 정보만 알면 거의 하나로 특정지어짐
services = bluetooth.find_service(name=None, uuid=UUID, address=HOST)

if len(services) == 0:
    print("장치를 찾지 못했습니다.")
    sys.exit()

service = services[0]
port = service["port"]
print("포트 :", port)

# 블루투스 클라이언트 소켓 생성
client_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

# 서버 접속
client_socket.connect((HOST, port))

try:
    while True:
        data = input("client : ")
        if data:
            client_socket.send(data.encode())
            data = client_socket.recv(1024)
            print("server :", data)

except:
    pass

client_socket.close()