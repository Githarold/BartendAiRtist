import bluetooth

# 서비스 검색
service_matches = bluetooth.find_service(name='SampleServer',
                                          uuid=bluetooth.SERIAL_PORT_CLASS)

if len(service_matches) == 0:
    print("No services found")
    exit(0)

# 첫 번째 서비스 선택
first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print("Connecting to \"%s\" on %s" % (name, host))

# Bluetooth 통신 연결
sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
sock.connect((host, port))

# 데이터 전송
sock.send("Hello World!")

# Bluetooth 통신 종료
sock.close()