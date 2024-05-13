import threading
from bluetooth import *
from protocol import Protocol
from processing1 import *
from parsing import json_list

PATH = "./cocktail.json"
cocktail_src = json_list(PATH)
sema = threading.Semaphore(1)

BUFSIZE = 1024
PORT = 1
MAXPENDING = 4

def handle_client(client_sock, client_info, cocktail_src):
    try:
        data = Protocol()
        try:
            received_data = client_sock.recv(BUFSIZE).decode()
            data.decode(received_data)
            if data.head == "1":
                t1 = protocol_1(client_sock, client_info, cocktail_src)
                t1.start()
            elif data.head == "2":
                t2 = protocol_2(client_sock, client_info, data, cocktail_src, sema)
                t2.start()
            elif data.head == "3":
                t3 = protocol_3(client_sock, client_info, data, cocktail_src, sema)
                t3.start()
            elif data.head == "4":
                t4 = protocol_4(client_sock, client_info, data, cocktail_src, sema)
                t4.start()
        except BluetoothError as e:
            print(f"Error receiving data from {client_info}: {e}")
    except Exception as e:
        print(f"Error handling client {client_info}: {e}")
    # finally:
    #     client_sock.close()
    #     print(f"Closed connection with {client_info}")

def start_server():
    server_sock = BluetoothSocket(RFCOMM)

    try:
        server_sock.bind(("", PORT))
        server_sock.listen(MAXPENDING)
        print(f"Waiting for connection on RFCOMM channel {PORT}")
    except Exception as e:
        print("Server setup error:", e)
        sys.exit(-1)

    try:
        while True:
            try:
                client_sock, client_info = server_sock.accept()
                print(f"Device {client_info[0]} is connected")
                threading.Thread(target=handle_client, args=(client_sock, client_info, cocktail_src)).start()
            except Exception as e:
                print("Client accept error:", e)
    except KeyboardInterrupt:
        print("Server is shutting down.")
    finally:
        server_sock.close()

if __name__ == "__main__":
    start_server()
