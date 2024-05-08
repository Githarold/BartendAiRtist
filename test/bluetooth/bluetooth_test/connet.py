import asyncio
from bleak import BleakClient

# 위에서 얻은 BLE 장치의 주소
address = "DC:A6:32:7B:04:EC"

# 장치와 연결해제시 발생하는 콜백 이벤트
def on_disconnect(client):
    print("Client with address {} got disconnected!".format(client.address))

async def run(address):
    # 장치 주소를 이용해 client 클래스 생성
    client = BleakClient(address)
    try:
        # 장치 연결 해제 콜백 함수 등록
        client.set_disconnected_callback(on_disconnect)
        # 장치 연결 시작
        await client.connect()
        print('connected')    
    except Exception as e:
        # 연결 실패시 발생
        print('error: ', e, end='')        
    finally:
        print('start disconnect')
        # 장치 연결 해제
        await client.disconnect()

loop = asyncio.get_event_loop()
loop.run_until_complete(run(address))
print('done')