import board
import neopixel
import time

# GPIO 18 (물리적 핀 12)를 사용하고, 24개의 WS2812 LED를 제어합니다.
pixel_pin = board.D18
num_pixels = 24

# 네오픽셀 객체를 생성합니다.
pixels = neopixel.NeoPixel(pixel_pin, num_pixels, auto_write=False)

def fill_color(duration, color):
    # LED를 지정된 색상으로 채우고, 지정된 시간 동안 유지합니다.
    pixels.fill(color)
    pixels.show()
    time.sleep(duration)
    pixels.fill((0, 0, 0))  # 모든 LED를 끕니다.
    pixels.show()

# LED 색상 (빨간색)
color = (0, 255, 0)

# LED를 3초 동안 빨간색으로 켜지게 합니다.
fill_color(3, color)
