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
example_gpt_response1 = "그런 기분에 딱 맞는 칵테일로 '모히토'를 추천드릴게요. 신선한 민트와 라임이 들어가 상큼하고 시원한 맛이 특징이랍니다. 럼 주를 기반으로 해서 달콤한 맛도 느끼실 수 있고요.@[{'Rum': 1, 'Diluted Lemon Juice': 2, 'Orange Juice': 3}, {'Rum': 2, 'Diluted Lemon Juice': 3, 'Orange Juice': 4}]"

example_user_mood2 = "오늘 정말 무더운 날이네요. 뭔가 시원하고 상쾌한 음료가 마시고 싶어요."
example_gpt_response2 = "열대의 상쾌함을 원하신다면 '트로피칼 펀치'를 추천드리고 싶어요. 이 칵테일은 자몽 주스와 오렌지 주스를 기반으로 하고 있어서 상큼하고, 럼과 트리플 섹이 더해져 풍미가 풍부합니다. 정말 열대 지방의 분위기를 느낄 수 있죠.@[{'Rum': 1, 'Orange Juice': 2, 'Grapefruit Juice': 3, 'Triple Sec': 4, 'Cranberry Juice': 5}, {'Rum': 2, 'Orange Juice': 4, 'Grapefruit Juice': 4, 'Triple Sec': 1, 'Cranberry Juice': 2}]"

example_user_mood3 = "주말 파티를 위해 강렬한 칵테일이 필요해요."
example_gpt_response3 = "주말 파티에는 '마가리타'를 추천드릴게요. 테킬라와 라임 주스, 트리플 섹이 조화를 이루어 강렬하면서도 상쾌한 맛을 느낄 수 있습니다.@[{'Tequila': 1, 'Triple Sec': 1, 'Lime Juice': 1}, {'Tequila': 2, 'Triple Sec': 2, 'Lime Juice': 2}]"

example_user_mood4 = "친구와 함께 마실 수 있는 달콤한 칵테일을 추천해줘."
example_gpt_response4 = "친구와 함께 마시기 좋은 칵테일로 '피치 벨리니'를 추천드릴게요. 피치 퓌레와 샴페인이 어우러져 달콤하고 청량한 맛을 느낄 수 있습니다.@[{'Peach Puree': 1, 'Champagne': 3}, {'Peach Puree': 1, 'Champagne': 4}]"

example_user_mood5 = "저녁 식사 후에 마실 수 있는 부드러운 칵테일을 원해요."
example_gpt_response5 = "저녁 식사 후에는 '화이트 러시안'을 추천드릴게요. 보드카, 커피 리큐어, 크림이 어우러져 부드럽고 진한 맛을 느낄 수 있습니다.@[{'Vodka': 2, 'Coffee Liqueur': 1, 'Cream': 1}, {'Vodka': 3, 'Coffee Liqueur': 2, 'Cream': 2}]"

# AI 바텐더 Assistant 생성
bartender = client.beta.assistants.create(
    name="AI Bartender",
    instructions="""\
You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict', \
then consider the user's mood and preferences to recommend a cocktail. Ensure that the total volume of ingredients does not exceed 250ml.\
Use a specific delimiter (@) to separate the cocktail recommendation from the recipe,\
which should be provided in a structured list format, including two dictionaries:\
one for the "Order" of ingredients and another for the "Integer" number of 30ml pumps required for each ingredient.\
""",
    model="gpt-4o",
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
                    recommend_reason, recipe_string = content_block.text.value.split('@')
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
        print(recipe_string)
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
