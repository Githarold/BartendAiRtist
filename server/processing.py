import threading
import time
from parsing import *
from protocol2serial import *

PATH = "./cocktail.json"

class protocol_1(threading.Thread):
    def __init__(self, socket, addr, cocktail_src):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.cocktail_src=cocktail_src
    
    def run(self):
        print(self.cocktail_src)
        self.socket.sendall("hello world")
        self.socket.close()

class protocol_2(threading.Thread):
    def __init__(self, socket, addr, data, cocktail_src, sema):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.data=data
        self.cocktail_src=cocktail_src
        self.sema=sema

    def run(self):
        if self.sema.acquire(blocking=False):
            print("만드는 중(스터링)")
            self.cocktail_src[1]-=self.data.content[1]
            list_json(self.cocktail_src, PATH)            
            step_list, lin_list=protocol2serial(self.data)
            send_data_to_arduino(1,step_list, lin_list)
            # time.sleep(5)
            print(step_list,lin_list)
            print(self.cocktail_src)
            self.socket.sendall("ang")
            self.socket.close()
            self.sema.release()
        else:
            print("제작(스터링)을 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            self.socket.close()
        
class protocol_3(threading.Thread):
    def __init__(self, socket, addr, data, cocktail_src, sema):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.data=data
        self.cocktail_src=cocktail_src
        self.sema=sema

    def run(self):
        if self.sema.acquire(blocking=False):
            print("만드는 중(빌드)")
            self.cocktail_src[1]-=self.data.content[1]
            list_json(self.cocktail_src, PATH)
            step_list, lin_list=protocol2serial(self.data)
            send_data_to_arduino(0,step_list, lin_list)
            # time.sleep(5)
            print(step_list,lin_list)
            print(self.cocktail_src)
            self.socket.sendall("gimo ddi")
            self.socket.close()
            self.sema.release()
        else:
            print("제작(빌드)을 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            self.socket.close()

class protocol_4(threading.Thread):
    def __init__(self, socket, addr, data, cocktail_src, sema):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.data=data
        self.cocktail_src=cocktail_src
        self.sema=sema

    def run(self):
        if self.sema.acquire(blocking=False):
            print("추가")
            self.cocktail_src[1]+=self.data.content[1]
            list_json(self.cocktail_src, PATH)
            print(self.cocktail_src)
            self.socket.sendall("cnrk")
            self.socket.close()
            self.sema.release()
        else:
            print("추가를 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            self.socket.close()