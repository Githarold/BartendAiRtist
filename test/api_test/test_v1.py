from openai import OpenAI


class Recommender:
    def __init__(self):
        self.client = OpenAI(api_key='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')

        self.model = "gpt-3.5-turbo"
        self.max_tokens = 500

    def test(self, user_input):
        prompt = f"진 500ml, 오렌지 주스 700ml, 돌체 300ml, 레몬 희석액 300ml \
        를 가지고 만들 수 있는 칵테일의 레시피 알려줘. 내 기분과 상태는 이런 상태야: {user_input}"

        # Create a request in message format for the GPT model
        response = self.client.chat.completions.create(
            model=self.model,
            messages=[
                {
                    "role": "system",
                    "name": "example_user",
                    "content": "진 500ml, 오렌지 주스 700ml, 돌체 300ml, \
                    레몬 희석액 300ml를 가지고 만들 수 있는 칵테일의 레시피 알려줘. \
                    나는 지금 기분이 별로 안좋고 취하고 싶어. 레시피는 각 음료를 30ml 단위로 몇 번 넣을지만 알려줘. \
                    그리고 레시피를 작성하기 전엔 '!' 로 표시해줘.",
                },
                {"role": "system", "name": "example_assistant", "content": "오렌지 진 피즈와 레몬 진 쿨러를 \
                    추천드릴게요 !오렌지 진 피즈: 진 2회, \
                    오렌지 주스 3회, 돌체 1회, 레몬 희석액 1회 / !레몬 진 쿨러: 진 3회, 레몬 희석액 2회, \
                    돌체 2회, 오렌지 주스 1회"},
                {"role": "user", "content": prompt},
            ],
            max_tokens=self.max_tokens,
        )

        # Extract the recommended food text from the response
        recommended_drink = response.choices[0].message.content.strip()

        print(recommended_drink)
    
if __name__ == "__main__":    
    chat_gpt = Recommender()
    user_input = input("당신의 현재 기분과 상태를 알려주세요. 기모찌하게 만들어주겠음 : ")

    while user_input != 'q':
        chat_gpt.test(user_input)
        user_input = input("당신의 현재 기분과 상태를 알려주세요. 기모찌하게 만들어주겠음 : ")
