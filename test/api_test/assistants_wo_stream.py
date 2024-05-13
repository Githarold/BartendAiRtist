import ast, time
from openai import OpenAI

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
example_user_mood2 = "오늘 정말 무더운 날이네요. 뭔가 시원하고 상쾌한 음료가 마시고 싶어요."
example_gpt_response2 = "열대의 상쾌함을 원하신다면 '트로피칼 펀치'를 추천드리고 싶어요. 이 칵테일은 자몽 주스와 오렌지 주스를 기반으로 하고 있어서 상큼하고, 럼과 트리플 섹이 더해져 풍미가 풍부합니다. 정말 열대 지방의 분위기를 느낄 수 있죠."
# example_user_mood2 = "I'm in the mood for something tropical and refreshing."
# example_gpt_response2 = "Based on your mood and the ingredients you have, I recommend a Tropical Punch. It's refreshing and perfect for a tropical vibe."

example_output_list1 = [\
{'Rum': 1, 'Diluted Lemon Juice': 2, 'Orange Juice': 3},\
{'Rum': 2, 'Diluted Lemon Juice': 3, 'Orange Juice': 4}\
]

example_output_list2 = [\
{'Rum': 1, 'Orange Juice': 2, 'Grapefruit Juice': 3, 'Triple Sec': 4, 'Cranberry Juice': 5},\
{'Rum': 2, 'Orange Juice': 4, 'Grapefruit Juice': 4, 'Triple Sec': 1, 'Cranberry Juice': 2}\
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
    real_user_mood = input("당신에게 딱 맞는 칵테일을 추천해드립니다! : ")
    if real_user_mood == 'q':
        break
    
    start_time = time.time()
    run = client.beta.threads.runs.create_and_poll(
    thread_id=thread.id,
    assistant_id=bartender.id,
    instructions=f"""\
Example 1:
Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood1}'
Output: {example_gpt_response1}@{example_output_list1}

Example 2:
Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood2}'
Output: {example_gpt_response2}@{example_output_list2}

You have to response in Korean:
Inventory - {real_input_dict}, Mood/Preference - '{real_user_mood}'
"""\
    )

    if run.status == 'completed': 
        messages = client.beta.threads.messages.list(thread_id=thread.id)
        for message in messages.data:
            for content_block in message.content:
                if content_block.type == 'text':
                    recommend_reason, recipe_string = content_block.text.value.split('@')
                    print(recommend_reason)
                    print(f"Elapsed time: {time.time() - start_time}")
    else:
        print(f"Run Status: {run.status}")
        
    print()

def adjust_pumps(recipe_string):
    recipe = ast.literal_eval(recipe_string)
    total_pumps = sum(recipe[1].values())
    target_pumps = 7
    if total_pumps > target_pumps:
        print("Adjusting recipe...")
        print(f"Original recipe: {recipe[1]}")
        adjustment_ratio = target_pumps / total_pumps
        adjusted_pumps = {ingredient: round(pumps * adjustment_ratio)
                          for ingredient, pumps in recipe[1].items()}
        print(f"Adjusted recipe: {adjusted_pumps}")
        return (recipe[0], adjusted_pumps)
    else:
        return recipe

print(adjust_pumps(recipe_string))