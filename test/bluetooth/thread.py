import threading

# 공유 변수
a = 0
lock = threading.Lock()

class WorkerThread(threading.Thread):
    def __init__(self, name):
        super().__init__()
        self.name = name

    def run(self):
        global a
        # 변수에 안전하게 접근
        with lock:
            temp = a
            print(f"{self.name} sees a as {temp}")
            a = temp + 1
            print(f"{self.name} increments a to {a}")

# 메인 스레드에서 스레드 생성 및 시작
thread1 = WorkerThread("Thread-1")
thread2 = WorkerThread("Thread-2")

thread1.start()
thread2.start()

thread1.join()
thread2.join()

print(f"Final value of a is {a}")
