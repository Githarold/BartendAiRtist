import board
import neopixel
import time

# GPIO 18 (물리적 핀 12)를 사용하고, 24개의 WS2812 LED를 제어합니다.
pixel_pin = board.D18
num_pixels = 24

# 네오픽셀 객체를 생성합니다.
pixels = neopixel.NeoPixel(pixel_pin, num_pixels, auto_write=False)

def single_led_rotate(wait, color):
    # 하나의 LED만 켜지면서 회전하는 효과
    for i in range(num_pixels):
        pixels.fill((0, 0, 0))  # 모든 LED를 끕니다.
        pixels[i] = color       # 현재 LED를 켭니다.
        pixels.show()
        time.sleep(wait)
        print("Program terminated by user.")


# 회전 속도 (초 단위)
wait = 0.1

# LED 색상
color = (255, 255, 255)  # 흰색

while True:
    # 하나의 LED가 회전하는 효과를 줍니다.
    single_led_rotate(wait, color)

