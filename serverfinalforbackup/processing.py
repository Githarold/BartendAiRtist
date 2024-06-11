import threading
import time
from parsing import *
from protocol2serial import *
import subprocess

PATH = "./cocktail.json"

class protocol_1(threading.Thread):
    def __init__(self, socket, addr, cocktail_src):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.cocktail_src=cocktail_src
    
    def run(self):
        print(self.cocktail_src)
        data=list2string(self.cocktail_src)
        self.socket.sendall(data)
        # self.socket.close()

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
            for i in range(8):
                self.cocktail_src[i]-=self.data.content[i]
            list_json(self.cocktail_src, PATH)
            step_list, lin_list=[], []        
            step_list, lin_list=generate_lists(self.data.order,self.data.content)
            subprocess.call("sudo /home/pi4-7/blue/bin/python ~/BartendAiRtist/circuit/rpi/neopixel_run.py", shell=True)
            send_data_to_arduino(1,step_list, lin_list)
            # time.sleep(3)
            self.socket.sendall("string complete")
            # self.socket.close()
            self.sema.release()
            self.data.content = [0,0,0,0,0,0,0,0]
            self.data.order = [0,0,0,0,0,0,0,0]
            subprocess.call("sudo /home/pi4-7/blue/bin/python ~/BartendAiRtist/circuit/rpi/neopixel_done.py", shell=True)
        else:
            print("제작(스터링)을 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            self.sema.release()
            # self.socket.close()
        
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
            for i in range(8):
                self.cocktail_src[i]-=self.data.content[i]
            list_json(self.cocktail_src, PATH)
            step_list, lin_list=generate_lists(self.data.order,self.data.content)
            send_data_to_arduino(0,step_list, lin_list)
            # time.sleep(3)
            self.socket.sendall("build complete")
            # self.socket.close()
            self.sema.release()
        else:
            print("제작(빌드)을 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            self.sema.release()
            # self.socket.close()

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
            self.cocktail_src=self.data.content
            list_json(self.cocktail_src, PATH)
            print(self.cocktail_src)
            self.socket.sendall("추가vv")
            # self.socket.close()
            self.sema.release()
            
        else:
            print("추가를 위해서는 잠시 기다려주세요")
            self.socket.sendall("wait")
            # self.socket.close()