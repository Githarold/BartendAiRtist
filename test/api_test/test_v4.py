from openai import OpenAI
import json

class Recommender:
    def __init__(self):
        self.client = OpenAI(api_key='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')
        self.model = "gpt-3.5-turbo"
        self.max_tokens = 500
        self.messages = []

    def generate_cocktail_recipe(self, user_mood):
        # Few-shot learning
        example1 = {
            "input": "기분이 우울할 때",
            "output": {
                "name": "블루 라군",
                "ingredients": ["보드카", "블루 큐라소", "레몬 주스", "소다"],
                "reason": "상쾌한 맛이 기분을 전환시켜 줍니다."
            }
        }
        example2 = {
            "input": "기분이 행복할 때",
            "output": {
                "name": "마가리타",
                "ingredients": ["데킬라", "트리플 섹", "라임 주스"],
                "reason": "새콤달콤한 맛이 기쁜 기분을 더욱 돋보이게 합니다."
            }
        }
        prompt = self.generate_prompt(user_mood, example1, example2)

        response = self.client.chat.completions.create(
            model=self.model,
            messages=self.messages + [{"role": "system", "content": prompt}],
            max_tokens=self.max_tokens,
        )

        recommended_drink = response.choices[0].message.content
        recipe = json.loads(recommended_drink)
        print(json.dumps(recipe, indent=2, ensure_ascii=False))
        self.messages.append({"role": "user", "content": user_mood})
        self.messages.append({"role": "assistant", "content": recommended_drink})

    def generate_prompt(self, user_mood, example1, example2):
        prompt = f"Input: {example1['input']}\nOutput: {json.dumps(example1['output'], ensure_ascii=False)}\n\n"
        prompt += f"Input: {example2['input']}\nOutput: {json.dumps(example2['output'], ensure_ascii=False)}\n\n"
        prompt += f"Input: {user_mood}\nOutput:"
        return prompt

def main():
    recommender = Recommender()
    while True:
        user_input = input("현재 기분이나 상태를 알려주세요. 당신에게 딱 맞는 칵테일을 추천해드립니다 : ")
        if user_input.lower() == 'q':
            break
        recommender.generate_cocktail_recipe(user_input)
        print()

if __name__ == "__main__":
    main()
