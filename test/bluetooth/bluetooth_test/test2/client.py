from bluetooth import *
import sys

SERVER_MAC = 'E0:0A:F6:49:E5:1C'  # 서버의 블루투스 MAC 주소, 실제 사용할 주소로 변경 필요
PORT = 1
BUFSIZE = 1024

def main():
    sock = BluetoothSocket(RFCOMM)

    try:
        sock.connect((SERVER_MAC, PORT))
        print("Connected to the server.")
    except Exception as e:
        print("Failed to connect to the server:", e)
        sys.exit(-1)

    try:
        message = "Hello from client!"
        sock.send(message)
        print("Sent message: %s" % message)
    except Exception as e:
        print("Failed to send message:", e)
        sys.exit(-1)

    try:
        data = sock.recv(BUFSIZE)
        print("Received message: %s" % data.decode('utf-8'))
    except Exception as e:
        print("Failed to receive message:", e)
        sys.exit(-1)

    sock.close()

if __name__ == "__main__":
    main()
