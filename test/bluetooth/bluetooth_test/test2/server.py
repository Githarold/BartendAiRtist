from bluetooth import *
import sys

BUFSIZE = 1024
PORT = 1
MAXPENDING = 4

def main():
    server_sock = BluetoothSocket(RFCOMM)
    
    try:
        server_sock.bind(("", PORT))
    except Exception as e:
        print("server: bind error", e)
        sys.exit(-1)
    
    try:
        server_sock.listen(MAXPENDING)
    except Exception as e:
        print("server: listen error", e)
        sys.exit(-1)
    
    print("Waiting for connection on RFCOMM channel %d" % PORT)

    try:
        client_sock, client_info = server_sock.accept()
        print("Device %s is connected" % client_info[0])
    except Exception as e:
        print("server: accept error", e)
        sys.exit(-1)
    
    try:
        data = client_sock.recv(BUFSIZE)
        if len(data) == 0: raise Exception("read error")
        print("Received message: %s" % data.decode('utf-8'))
    except Exception as e:
        print("server: read error", e)
        sys.exit(-1)

    client_sock.close()
    server_sock.close()

if __name__ == "__main__":
    main()
