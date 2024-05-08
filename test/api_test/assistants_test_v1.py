from openai import OpenAI, AssistantEventHandler
import json

class EventHandler(AssistantEventHandler):
    def __init__(self):
        super().__init__()  # 상위 클래스의 생성자 호출
        self.data_file = open("data.jsonl", "a")

    def on_text_created(self, text) -> None:
        print(f"\nAssistant > {text}")

    def on_text_delta(self, delta, snapshot):
        print(delta.value, end="", flush=True)
    
    def on_tool_call_created(self, tool_call):
        if tool_call.type == 'function':
            json.dump(tool_call.function.output, self.data_file)
            self.data_file.write("\n")
    
    def on_tool_call_delta(self, delta, snapshot):
        if delta.type == 'function':
            json.dump(delta.function.output, self.data_file)
            self.data_file.write("\n")

    def __del__(self):
        self.data_file.close()

# OpenAI 클라이언트 초기화
client = OpenAI(api_key='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')

# Few-shot Learning을 위한 예시 입력 선언
example_input_dict = {
    'Vodka': 700, 
    'Rum': 700, 
    'Gin': 700, 
    'Diluted Lemon Juice': 1000, 
    'Triple Sec': 500, 
    'Cranberry Juice': 1000, 
    'Grapefruit Juice': 1000, 
    'Orange Juice': 800
}

real_input_dict = {
    'Vodka': 1000, 
    'Rum': 700, 
    'Gin': 800, 
    'Diluted Lemon Juice': 800, 
    'Triple Sec': 500, 
    'Cranberry Juice': 900, 
    'Grapefruit Juice': 1000, 
    'Orange Juice': 800
}

example_user_mood1 = "오늘따라 뭔가 시원하고 달콤한 칵테일이 마시고 싶어. 피로도 풀리고 기분도 좋아지는 그런 종류로."
example_gpt_response1 = "그런 기분에 딱 맞는 칵테일로 '모히토'를 추천드릴게요. 신선한 민트와 라임이 들어가 상큼하고 시원한 맛이 특징이랍니다.\
                        럼 주를 기반으로 해서 달콤한 맛도 느끼실 수 있고요."
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
    instructions=f"""
    You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict', then consider the user's mood and preferences to recommend a cocktail. Use a specific delimiter (@) to separate the cocktail recommendation from the recipe, which should be provided in a structured list format, including two dictionaries: one for the order of ingredients and another for the number of 30ml pumps required for each ingredient.

    Example 1:
    Input: Inventory - {example_input_dict}, Mood/Preference - {example_user_mood1}'
    Output: {example_gpt_response1}@{example_output_list1}"
    
    Example 2:
    Input: Inventory - {example_input_dict}, Mood/Preference - {example_user_mood2}'
    Output: {example_gpt_response2}@{example_output_list2}"    
    
    """,
    model="gpt-4-turbo",
)

# 대화를 관리할 Thread 생성
thread = client.beta.threads.create()

# 사용자 입력 처리 스트리밍
event_handler = EventHandler()

with client.beta.threads.runs.stream(
    thread_id=thread.id,
    assistant_id=bartender.id,
    instructions=f"Inventory - {real_input_dict}, Mood/Preference - {real_user_mood}",
    event_handler=event_handler
) as stream:
    stream.until_done()
