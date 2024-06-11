from protocol import Protocol
import time
import serial

ser = serial.Serial('/dev/ttyACM0', 9600, timeout=1)

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
   ser.write(data_string.encode())
   print("Sent data:", data_string)


   while True:
       if ser.in_waiting > 0:
           response = ser.readline().decode('utf-8').strip()
           print("Received from Arduino:", response)
           print("Finished!")
           break
       time.sleep(0.1)



def list2string(src_list):
    return '\n'.join(map(str,src_list))