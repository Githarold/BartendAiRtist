import socket
import threading
from protocol import Protocol
from processing import *
from parsing import json_list

PATH = "C:/Users/seongbin/OneDrive - UOS/바탕 화면/대학교/4학년 1학기/임베디드시스템/테스트/cocktail.json"

cocktail_src=json_list(PATH)
sema=threading.Semaphore(1)

def start_server():
    global cocktail_src
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # server_socket.bind(('192.168.0.11', 5000))
    server_socket.bind(('localhost', 5000))
    server_socket.listen()
    server_socket.settimeout(1)  # 1초 후에 타임아웃

    try:
        while True:
            try:
                client_socket, addr = server_socket.accept()
            except socket.timeout:
                continue  # 타임아웃 발생 시 계속 대기
            
            data=Protocol()
            received_data = client_socket.recv(1024).decode()
            data.decode(received_data)
            if data.head=="1":
                t1 = protocol_1(client_socket, addr, cocktail_src)
                t1.start()
            elif data.head=="2":
                t2 = protocol_2(client_socket, addr, data ,cocktail_src, sema)
                t2.start()
            elif data.head=="3":
                t3 = protocol_3(client_socket, addr, data ,cocktail_src, sema)
                t3.start()
            elif data.head=="4":
                t4 = protocol_4(client_socket, addr, data ,cocktail_src, sema)
                t4.start()


    except KeyboardInterrupt:
        print("Server is shutting down.")
        server_socket.close()

if __name__ == '__main__':
    start_server()
