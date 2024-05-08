# import bluetooth

# server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

# port = 1
# server_sock.bind(("", port))
# server_sock.listen(1)

# client_sock, address = server_sock.accept()
# print("Accepted connection from", address)

# data = client_sock.recv(1024)
# print("Received:", data)

# client_sock.close()
# server_sock.close()

import bluetooth
import signal
import sys

HOST = ""  # '블루투스 컨트롤러 맥 주소'를 직접 입력해도 됨
PORT = bluetooth.PORT_ANY
UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"


def signal_handler(sig, frame):
    try:
        connected_socket.close()

    except:
        pass

    server_socket.close()
    sys.exit()


signal.signal(signal.SIGINT, signal_handler)

# 블루투스 서버 소켓 생성
server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_socket.bind((HOST, PORT))
server_socket.listen(1)

port = server_socket.getsockname()[1]
print("포트 :", port)

# 블루투스 서비스 advertise
bluetooth.advertise_service(
    server_socket,
    name="server",
    service_id=UUID,
    service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
    profiles=[bluetooth.SERIAL_PORT_PROFILE],
)

# 클라이언트 접속 대기
connected_socket, client_address = server_socket.accept()

try:
    while True:
        data = connected_socket.recv(1024)
        print("client : ", data)
        connected_socket.send(data)

except:
    pass

connected_socket.close()
server_socket.close()