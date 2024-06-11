import json
import random

# 1번부터 8번까지의 물의 양을 랜덤으로 생성하여 딕셔너리에 저장
water_levels = {f"{i}": random.randint(50, 100) for i in range(1, 9)}

# JSON 파일에 저장
with open('cocktail.json', 'w') as file:
    json.dump(water_levels, file, indent=4)
