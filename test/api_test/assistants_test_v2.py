import ast
from openai import OpenAI, AssistantEventHandler

class EventHandler(AssistantEventHandler):
    def __init__(self):
        super().__init__()
        self.file_mode = False                  # 파일 기록 모드 초기화
        self.data_file = open("data.txt", "w")  # 데이터 파일을 쓰기 모드로 열기
        self.buffer = ""                        # 파일에 쓸 데이터를 임시로 저장할 버퍼

    def on_text_created(self, text) -> None:
        # 텍스트가 생성되었을 때 호출되는 메서드 (여기서는 사용하지 않음)
        pass

    def on_text_delta(self, delta, snapshot):
        # 텍스트 변화가 있을 때마다 호출되는 메서드
        self.check_and_process_text(delta.value)

    def check_and_process_text(self, text):
        # '@' 문자를 검사하여 출력 또는 파일 쓰기를 처리하는 메서드
        if '@' in text:
            self.file_mode = True
            parts = text.split('@')
            print(parts[0], end="", flush=True)   # '@' 앞 부분은 표준 출력
            self.buffer += parts[1]               # '@' 뒷 부분은 버퍼에 추가
        elif self.file_mode:
            self.buffer += text  # 파일 모드인 경우 텍스트를 버퍼에 추가
        else:
            print(text, end="", flush=True)  # 파일 모드가 아닌 경우 표준 출력

    def finalize_buffer(self):
        # 버퍼의 내용을 파일에 쓰고 버퍼 비우기
        if self.buffer:
            self.data_file.write(self.buffer + "\n")
            self.data_file.flush()  # 파일의 버퍼를 강제로 비워 디스크에 쓰기
            # self.buffer = ""  # 버퍼 초기화
            
    def close(self):
        self.finalize_buffer()  # 마지막 데이터를 파일에 저장
        self.data_file.close()  # 파일 닫기

    # def __del__(self):
    #     # 객체 소멸 시 호출되는 메서드
    #     self.finalize_buffer()  # 마지막 데이터를 파일에 저장
    #     self.data_file.close()  # 파일 닫기

# OpenAI 클라이언트 초기화
client = OpenAI()

# Few-shot Learning을 위한 예시 입력 선언
example_input_dict = {
    'Vodka': 700, 'Rum': 700, 'Gin': 700, 'Diluted Lemon Juice': 1000, 
    'Triple Sec': 500, 'Cranberry Juice': 1000, 'Grapefruit Juice': 1000, 'Orange Juice': 800
}

real_input_dict = {
    'Vodka': 1000, 'Rum': 700, 'Gin': 800, 'Diluted Lemon Juice': 800, 
    'Triple Sec': 500, 'Cranberry Juice': 900, 'Grapefruit Juice': 1000, 'Orange Juice': 800
}

example_user_mood1 = "오늘따라 뭔가 시원하고 달콤한 칵테일이 마시고 싶어. 피로도 풀리고 기분도 좋아지는 그런 종류로."
example_gpt_response1 = "그런 기분에 딱 맞는 칵테일로 '모히토'를 추천드릴게요. 신선한 민트와 라임이 들어가 상큼하고 시원한 맛이 특징이랍니다. 럼 주를 기반으로 해서 달콤한 맛도 느끼실 수 있고요."
example_user_mood2 = "I'm in the mood for something tropical and refreshing."
example_gpt_response2 = "Based on your mood and the ingredients you have, I recommend a Tropical Punch. It's refreshing and perfect for a tropical vibe."

real_user_mood = "오늘 차여서 기분이 우울해. 기분 전환을 하고싶어"

example_output_list1 = [
    {'Rum': 1, 'Diluted Lemon Juice': 2, 'Orange Juice': 3},  # 순서
    {'Rum': 2, 'Diluted Lemon Juice': 3, 'Orange Juice': 4}   # 30ml 펌프 횟수
]

example_output_list2 = [
    {'Rum': 1, 'Orange Juice': 2, 'Grapefruit Juice': 3, 'Triple Sec': 4, 'Cranberry Juice': 5},
    {'Rum': 2, 'Orange Juice': 4, 'Grapefruit Juice': 4, 'Triple Sec': 1, 'Cranberry Juice': 2}
]

# AI 바텐더 Assistant 생성
bartender = client.beta.assistants.create(
    name="AI Bartender",
    instructions="""\
You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict', \
then consider the user's mood and preferences to recommend a cocktail. Ensure that the total volume of ingredients does not exceed 250ml.\
Use a specific delimiter (@) to separate the cocktail recommendation from the recipe,\
which should be provided in a structured list format, including two dictionaries:\
one for the order of ingredients and another for the number of 30ml pumps required for each ingredient.\
""",
    model="gpt-4-turbo",
)

# 대화를 관리할 Thread 생성
thread = client.beta.threads.create()

while True:
    # 사용자 입력 처리 스트리밍
    event_handler = EventHandler()
    
    real_user_mood = input("당신에게 딱 맞는 칵테일을 추찬해드립니다! : ")
    if real_user_mood == 'q':
        break
    
    with client.beta.threads.runs.stream(
        thread_id=thread.id,
        assistant_id=bartender.id,
        instructions=f"""
        Example 1:
        Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood1}'
        Output: {example_gpt_response1}@{example_output_list1}
        
        Example 2:
        Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood2}'
        Output: {example_gpt_response2}@{example_output_list2}
        
        You have to response in Korean:
        Inventory - {real_input_dict}, Mood/Preference - '{real_user_mood}'
        """,
        event_handler=event_handler
        
    ) as stream:
        stream.until_done()
        
    event_handler.close()
    
    print('\n\n')

def adjust_pumps(recipe):
    total_pumps = sum(recipe[1].values())
    target_pumps = 7
    if total_pumps > target_pumps:
        print("Adjusting recipe...")
        adjustment_ratio = target_pumps / total_pumps
        adjusted_pumps = {ingredient: round(pumps * adjustment_ratio)
                          for ingredient, pumps in recipe[1].items()}
        return (recipe[0], adjusted_pumps)
    else:
        return recipe
    
# file = open("data.txt", "r")
# data = file.read().strip()
# print(data)