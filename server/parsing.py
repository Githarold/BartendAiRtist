import json

def json_list(path):
    with open(path, 'r') as file:
        data = json.load(file)

    # 딕셔너리에서 값을 추출하여 리스트에 저장
    water_levels_list = [data[f"{i}"] for i in range(1, 9)]

    return water_levels_list

def list_json(data, path):
    # 1번부터 8번까지의 물의 양을 data.content에서 가져와서 딕셔너리에 저장
    water_levels = {f"{i+1}": amount for i, amount in enumerate(data)}

    # JSON 파일에 저장
    with open(path, 'w') as file:
        json.dump(water_levels, file, indent=4)