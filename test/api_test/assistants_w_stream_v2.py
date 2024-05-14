from openai import OpenAI, AssistantEventHandler

class EventHandler(AssistantEventHandler):
    def __init__(self):
        super().__init__()
        self.file_mode = False
        self.data_file = open("data.txt", "w")
        self.buffer = ""

    def on_text_created(self, text) -> None:
        pass

    def on_text_delta(self, delta, snapshot):
        self.check_and_process_text(delta.value)

    def check_and_process_text(self, text):
        if '@' in text:
            self.file_mode = True
            parts = text.split('@')
            print(parts[0], end="", flush=True)
            self.buffer = parts[1]
        elif self.file_mode:
            self.buffer += text
        else:
            print(text, end="", flush=True)

    def __del__(self):
        if self.buffer:
            self.data_file.write(self.buffer)
            self.data_file.flush()
        self.data_file.close()

# OpenAI 클라이언트 초기화
client = OpenAI()

example_input_dict = {
    'Vodka': 700, 'Rum': 700, 'Gin': 700, 'Diluted Lemon Juice': 1000, 
    'Triple Sec': 500, 'Cranberry Juice': 1000, 'Grapefruit Juice': 1000, 'Orange Juice': 800
}

real_input_dict = {
    'Vodka': 1000, 'Rum': 700, 'Gin': 800, 'Diluted Lemon Juice': 800, 
    'Triple Sec': 500, 'Cranberry Juice': 900, 'Grapefruit Juice': 1000, 'Orange Juice': 800
}

example_user_mood1 = "오늘따라 뭔가 시원하고 달콤한 칵테일이 마시고 싶어. 피로도 풀리고 기분도 좋아지는 그런 종류로."
example_gpt_response1 = "그런 기분에 딱 맞는 칵테일로 '모히토'를 추천드릴게요. 신선한 민트와 라임이 들어가 상큼하고 시원한 맛이 특징이랍니다. 럼 주를 기반으로 해서 달콤한 맛도 느끼실 수 있고요.@ [{'Rum': 1, 'Diluted Lemon Juice': 2, 'Orange Juice': 3}, {'Rum': 2, 'Diluted Lemon Juice': 3, 'Orange Juice': 4}]"
example_user_mood2 = "I'm in the mood for something tropical and refreshing."
example_gpt_response2 = "Based on your mood and the ingredients you have, I recommend a Tropical Punch. It's refreshing and perfect for a tropical vibe.@ [{'Rum': 1, 'Orange Juice': 2, 'Grapefruit Juice': 3, 'Triple Sec': 4, 'Cranberry Juice': 5}, {'Rum': 2, 'Orange Juice': 4, 'Grapefruit Juice': 4, 'Triple Sec': 1, 'Cranberry Juice': 2}]"

real_user_mood = "오늘 차여서 기분이 우울해. 기분 전환을 하고싶어"

bartender = client.beta.assistants.create(
    name="AI Bartender",
    instructions=f"""\
You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict', 
then consider the user's mood and preferences to recommend a cocktail. Ensure that the total volume of ingredients does not exceed 250ml.
Use a specific delimiter (@) to separate the cocktail recommendation from the recipe,
which should be provided in a structured list format, including two dictionaries:
one for the order of ingredients and another for the number of 30ml pumps required for each ingredient.

Example 1:
Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood1}'
Output: {example_gpt_response1}

Example 2:
Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood2}'
Output: {example_gpt_response2}
""",
    model="gpt-4-turbo",
)

thread = client.beta.threads.create()

while True:
    real_user_mood = input("당신에게 딱 맞는 칵테일을 추천해드립니다! : ")
    if real_user_mood == 'q':
        break
    
    with client.beta.threads.runs.stream(
        thread_id=thread.id,
        assistant_id=bartender.id,
        instructions=f"""
        Input: Inventory - {real_input_dict}, Mood/Preference - '{real_user_mood}'
        """,
        event_handler=EventHandler()
        
    ) as stream:
        stream.until_done()
    
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
    
# 프로그램 종료 후 파일 내용 확인
file = open("data.txt", "r")
data = file.read()
print(data)
file.close()
