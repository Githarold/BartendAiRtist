from flask import Flask

app = Flask(__name__)

@app.route('/')
def home():
    return '쌈@뽕한 바텐더'

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)