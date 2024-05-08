import socket

def start_client():
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect(('127.0.0.1', 3000))  # 서버에 연결

    client_socket.sendall('Hello from client!'.encode())  # 서버에게 데이터 송신
    data = client_socket.recv(1024)  # 서버로부터 데이터 수신
    print(f'Received: {data.decode()}')

    client_socket.close()

if __name__ == '__main__':
    start_client()