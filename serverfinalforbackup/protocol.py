class Protocol:
    def __init__(self):
        self.head = -1
        self.content = [0,0,0,0,0,0,0,0]
        self.order = [0,0,0,0,0,0,0,0]

    def decode(self,data):
        self.content = [0,0,0,0,0,0,0,0]
        self.order = [0,0,0,0,0,0,0,0]
        data = data.split("\n\n")
        self.head = data[0]
        temp=1
        if self.head == '1':
            return None
        self.content = list(map(int, data[1].split()))
        if self.head == '2':
            for i in range(8):
                if self.content[i]>0:
                    self.order[i]=temp
                    temp+=1
                      
        if self.head == '3':
            self.order = list(map(int, data[2].split()))
        return None