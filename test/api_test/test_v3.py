from openai import OpenAI

class Recommender:
    def __init__(self):
        self.client = OpenAI()
        self.model = "gpt-3.5-turbo"
        self.max_tokens = 500
        self.messages = []

    def generate_cocktail_recipe(self, user_mood):
        # 이전 대화를 포함한 요청 생성
        prompt = self.generate_prompt(user_mood)

        response = self.client.chat.completions.create(
            model=self.model,
            messages=self.messages + [{"role": "system", "content": prompt}],
            max_tokens=self.max_tokens,
        )

        # 모델의 응답 출력 및 이전 대화 갱신
        recommended_drink = response.choices[0].message.content
        print(recommended_drink)
        self.messages.append({"role": "user", "content": user_mood})
        self.messages.append({"role": "assistant", "content": recommended_drink})

    def generate_prompt(self, user_mood):
        # 이전 대화와 현재 사용자 입력을 포함하는 프롬프트 생성
        prompt = f"다음 재료들을 사용하여 대략 200ml의 칵테일을 만들어주세요: 진 500ml, 오렌지 주스 700ml, 돌체 300ml, 레몬 희석액 300ml. 사용자의 기분은 '{user_mood}'입니다. 레시피를 JSON 형식으로 제공하고, 추천 이유는 한글로 작성해주세요."
        return prompt

def main():
    recommender = Recommender()
    while True:
        user_input = input("현재 기분이나 상태를 알려주세요. 기모찌 하게 만들어 드림 : ")
        if user_input.lower() == 'q':
            break
        recommender.generate_cocktail_recipe(user_input)
        print()

if __name__ == "__main__":
    main()
