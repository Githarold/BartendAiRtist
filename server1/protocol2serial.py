from protocol import Protocol
import time
import serial

ser = serial.Serial('/dev/ttyACM0', 9600, timeout=1)

def generate_lists(step_input, linear_input):
   print("step_input: ",step_input)
   print("linear_input: ", linear_input)
   values, indices = [], []
   # 0이 아닌 원소와 그 인덱스를 저장
   for i, val in enumerate(step_input):
       if val != 0:
           values.append(val)
           indices.append(i)


   # 값에 따라 오름차순 정렬
   combined = sorted(zip(values, indices))
   values, indices = zip(*combined) if combined else ([], [])


   # disk_pre_rotation과 disk_rotation_list 생성
   disk_pre_rotation = [index + 1 for index in indices]
   disk_rotation_list = [
       disk_pre_rotation[i] - disk_pre_rotation[i - 1] if i > 0 else disk_pre_rotation[i] - 1
       for i in range(len(disk_pre_rotation))
   ]


   # disk_rotation_list의 모든 항목의 합을 0으로 만드는 음수값 추가
   total_sum = sum(disk_rotation_list)
   disk_rotation_list.append(-total_sum)


   # dispensor_activate_list 생성
   dispensor_activate_list = [linear_input[index] for index in indices]


   return disk_rotation_list, dispensor_activate_list



def protocol2serial(data : Protocol):
    disk_rotation_list, dispensor_activate_list = [], []
    if data.head=="2":
        temp=0
        for val in data.content:
            if val!=0:
                disk_rotation_list.append(temp)
                dispensor_activate_list.append(val)
                temp=0
            temp+=1
    elif data.head=="3":
        before=0
        print(data.order)
        for x in range(max(data.order)):
            new=data.order.index(x+1)
            temp=new-before
            before=new
            disk_rotation_list.append(temp)
            dispensor_activate_list.append(data.content[new])
    total=sum(disk_rotation_list)
    disk_rotation_list.append(-total)
    print("debug: ", disk_rotation_list, dispensor_activate_list)
    return disk_rotation_list, dispensor_activate_list

def send_data_to_arduino(dc_input, disk_rotation_list, dispensor_activate_list):
   data_string = f"{dc_input},{disk_rotation_list},{dispensor_activate_list}\n"
   print("Sent data:", data_string)
   ser.write(data_string.encode())


   while True:
       if ser.in_waiting > 0:
           response = ser.readline().decode('utf-8').strip()
           print("Received from Arduino:", response)
           print("Finished!")
           break
       time.sleep(0.1)



def list2string(src_list):
    return '\n'.join(map(str,src_list))