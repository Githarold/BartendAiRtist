class Protocol:
    def __init__(self):
        self.head = -1
        self.content = [0,0,0,0,0,0,0,0]
        self.order = [0,0,0,0,0,0,0,0]

    def decode(self,data):
        data = data.split("\n\n")
        self.head = data[0]
        if self.head == '1':
            return None
        self.content = list(map(int, data[1].split()))
        if self.head == '3':
            self.order = list(map(int, data[2].split()))
        return None