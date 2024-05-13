import sys
import os

# 현재 파일의 디렉토리 경로를 구하고, 상위 폴더 경로를 sys.path에 추가
current_dir = os.path.dirname(__file__)
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from protocol import Protocol

test_data=Protocol()
test_data.head=2
test_data.content=[1,0,2,0,3,0,4,0]

test_data2=Protocol()
test_data2.head=3
test_data2.content=[1,0,2,0,3,0,4,0]
test_data2.order=  [3,0,4,0,2,0,1,0]

def protocol2serial(data : Protocol):
    disk_rotation_list, dispensor_activate_list = [], []
    if data.head==2:
        temp=0
        for val in data.content:
            temp+=1
            if val!=0:
                disk_rotation_list.append(temp)
                dispensor_activate_list.append(val)
                temp=0
    elif data.head==3:
        before=0
        for x in range(max(data.order)):
            new=data.order.index(x+1)
            temp=new-before
            before=new
            disk_rotation_list.append(temp)
            dispensor_activate_list.append(data.content[new])

    return disk_rotation_list, dispensor_activate_list

# print(protocol2serial(test_data))
# print(protocol2serial(test_data2))