import bluetooth

bd_addr = "E0:0A:F6:49:E5:1C"  # 블루투스 기기의 주소를 입력하세요.
port = 1

sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
sock.connect((bd_addr, port))
sock.send("Hello, Bluetooth!")

sock.close()
