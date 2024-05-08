class protocal:
    def __init__(self):
        self.head=-1
        self.content=[]
        self.order=[]

def decode(data):
    result=protocal()
    data=data.split("\n\n")
    result.head=data[0]
    if result.head=='1':
        return result
    content=list(map(int,data[1].split()))
    result.content=content
    if result.head=='3':
        order=list(map(int,data[2].split()))
        result.order=order
    return result

#a="1"
#a="2\n\n2\n3\n4\n5\n6\n7"
#a="3\n\n2\n3\n4\n5\n6\n7\n\n3\n4\n5\n"
a="4\n\n2\n3\n4\n5\n6\n7"

print(decode(a).head)
print(decode(a).content)
print(decode(a).order)
