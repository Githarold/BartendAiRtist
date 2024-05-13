import threading
import time
from parsing import *

PATH = "./cocktail.json"

class protocol_1(threading.Thread):
    def __init__(self, socket, addr, cocktail_src):
        super().__init__()
        self.socket=socket
        self.addr=addr
        self.cocktail_src=cocktail_src
    
    def run(self):
        print(self.cocktail_src)

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
            time.sleep(5)
            self.socket.close()
            print(self.cocktail_src)
            self.sema.release()
        else:
            print("제작(스터링)을 위해서는 잠시 기다려주세요")
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
            print(self.data.order)
            time.sleep(5)
            self.socket.close()
            print(self.cocktail_src)
            self.sema.release()
        else:
            print("제작(빌드)을 위해서는 잠시 기다려주세요")
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
            self.socket.close()
            print(self.cocktail_src)
            self.sema.release()
        else:
            print("추가를 위해서는 잠시 기다려주세요")
            self.socket.close()