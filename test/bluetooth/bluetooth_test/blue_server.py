import bluetooth

server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

port = 1
server_sock.bind(("", port))
server_sock.listen(1)

client_sock, address = server_sock.accept()
print(f"Accepted connection from {address}")

while True:
    data = client_sock.recv(1024)
    if not data:
        break
    print("Received:", data)

client_sock.close()
server_sock.close()
