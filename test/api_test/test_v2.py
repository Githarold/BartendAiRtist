from openai import OpenAI

class Recommender:
    def __init__(self):
        self.client = OpenAI()
        self.model = "gpt-3.5-turbo"
        self.max_tokens = 500

    def test(self, user_input):
        prompt = f"다음 재료들을 사용하여 만들 수 있는 200ml 분량의 칵테일을 추천해주세요 : 진 500ml, \
        오렌지 주스 700ml, 돌체 300ml, 레몬 희석액 300ml. 사용자의 기분은 \
        다음과 같습니다: '{user_input}'. 레시피를 JSON(영어) 형식으로 제공하고, 추천 이유는 한글로 작성해주세요."
        
        response = self.client.chat.completions.create(
            model=self.model,
            messages=[
                {"role": "system", "content": prompt},
            ],
            max_tokens=self.max_tokens,
        )

        # JSON 형식의 응답 가정
        recommended_drink = response.choices[0].message.content
        print(recommended_drink)

if __name__ == "__main__":
    chat_gpt = Recommender()
    user_input = input("현재 기분이나 상태를 알려주세요. 기모찌 하게 만들어 드림 : ")
    print()
    
    while user_input != 'q':
        chat_gpt.test(user_input)
        user_input = input("현재 기분이나 상태를 알려주세요. 기모찌 하게 만들어 드림 : ")
        print()
