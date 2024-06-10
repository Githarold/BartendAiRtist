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

example_user_mood1 = "오늘은 상쾌하고 열대의 풍미가 가득한 칵테일을 마시고 싶어요."
example_gpt_response1 = "그런 날에는 '허리케인'을 추천드릴게요. 럼과 오렌지 주스가 어우러져 상쾌하고 달콤한 맛이 특징입니다. 열대 지방의 느낌을 가득 느끼실 수 있을 거예요.§[0,2,0,0,0,2,0,1]"

example_user_mood2 = "오늘 뭔가 상큼하고 쌉쌀한 맛이 나는 칵테일이 생각나네요."
example_gpt_response2 = "그렇다면 '그레이 하운드'를 추천드려요. 보드카와 자몽 주스가 어우러져 상큼하면서도 쌉쌀한 맛이 매력적인 칵테일입니다.§[2,0,0,0,0,0,0,3]"

example_user_mood3 = "오늘은 좀 세련된 분위기의 칵테일을 마시고 싶어요."
example_gpt_response3 = "세련된 분위기를 원하신다면 '코스모폴리탄'이 제격입니다. 보드카와 크랜베리 주스, 그리고 트리플 섹이 어우러져 우아한 맛을 느끼실 수 있습니다.§[2,0,0,1,0,0,0,1]"

example_user_mood4 = "오늘은 강렬한 맛이 나는 칵테일이 마시고 싶어요."
example_gpt_response4 = "그렇다면 '레드 데빌'을 추천드립니다. 보드카와 크랜베리 주스, 그리고 레몬 주스가 어우러져 강렬하고 상큼한 맛을 느끼실 수 있습니다.§[2,0,0,0,1,0,0,4]"

example_user_mood5 = "오늘은 깔끔하면서도 우아한 칵테일을 마시고 싶어요."
example_gpt_response5 = "그렇다면 '화이트 레이디'를 추천드려요. 진과 트리플 섹, 그리고 레몬 주스가 어우러져 깔끔하고 우아한 맛을 자랑하는 칵테일입니다.§[0,0,2,1,1,0,0,0]"

example_user_mood6 = "친구들과 함께 즐길 수 있는 재미있는 칵테일이 필요해요."
example_gpt_response6 = "그렇다면 '롱 비치 아이스티'를 추천드릴게요. 보드카, 럼, 진, 그리고 크랜베리 주스가 어우러져 강렬하면서도 상쾌한 맛을 느끼실 수 있습니다.§[1,1,1,1,1,0,0,0]"

example_user_mood7 = "오늘은 상큼하고 달콤한 칵테일이 생각나요."
example_gpt_response7 = "상큼하고 달콤한 맛을 원하신다면 '레몬 드롭 마티니'를 추천드립니다. 보드카와 레몬 주스, 그리고 트리플 섹이 어우러져 상큼하면서도 달콤한 맛이 일품인 칵테일입니다.§[2,0,0,1,1,0,0,0]"

# AI 바텐더 Assistant 생성
bartender = client.beta.assistants.create(
    name="AI Bartender",
    instructions="""\
You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict', \
then consider the user's mood and preferences to recommend a cocktail. Ensure that the total volume of ingredients does not exceed 250ml.\
Use a specific delimiter (§) to separate the cocktail recommendation from the recipe,\
which should be provided in a structured list format, including two dictionaries:\
one for the "Order" of ingredients and another for the "Integer" number of 30ml pumps required for each ingredient.\
Ingredients in order: [Vodka, Rum, Gin, Triple Sec, Diluted Lemon Juice, Orange Juice, Grapefruit Juice, Cranberry Juice]\
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
    instructions=f"""
    Ingredients in order: [Vodka, Rum, Gin, Triple Sec, Diluted Lemon Juice, Orange Juice, Grapefruit Juice, Cranberry Juice]
    
    Example 1:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood1}'
    Output: {example_gpt_response1}
    
    Example 2:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood2}'
    Output: {example_gpt_response2}

    Example 3:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood3}'
    Output: {example_gpt_response3}

    Example 4:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood4}'
    Output: {example_gpt_response4}

    Example 5:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood5}'
    Output: {example_gpt_response5}
    
    Example 6:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood6}'
    Output: {example_gpt_response6}
    
    Example 7:
    Input: Inventory - {example_input_dict}, Mood/Preference - '{example_user_mood7}'
    Output: {example_gpt_response7}        
    
    You have to respond in Korean:
    Input: Inventory - {real_input_dict}, Mood/Preference - '{real_user_mood}'
    Output: 
    """,
    )

    recipe_string = None
    if run.status == 'completed': 
        # 가장 최근의 메시지만 가져옵니다.
        message = client.beta.threads.messages.list(thread_id=thread.id, order="desc", limit=1)
        if message.data:
            content_block = message.data[0].content[0]  # 가장 최근의 메시지의 첫 번째 content block
            if content_block.type == 'text':
                try:
                    recommend_reason, recipe_string = content_block.text.value.split('§')
                    print(recommend_reason)
                except ValueError:
                    print(f"Invalid Response: {content_block.text.value}")
                print(f"Elapsed time: {time.time() - start_time}")
    else:
        print(f"Run Status: {run.status}")
        
    print()

def adjust_pumps(recipe_string):
    try:
        recipe = ast.literal_eval(recipe_string)
    except (SyntaxError, ValueError) as e:
        print(f"레시피 파싱 오류: {e}")
        return None

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

# Recipe adjustment example
adjusted_recipe = adjust_pumps(recipe_string)
if adjusted_recipe:
    print(adjusted_recipe)
