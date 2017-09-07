import threading
import sys 

# Simple demo of of the WS2801/SPI-like addressable RGB LED lights.
import time
import RPi.GPIO as GPIO
 
# Import the WS2801 module.
import Adafruit_WS2801
import Adafruit_GPIO.SPI as SPI

class LEDThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)

        # Configure the count of pixels:
        self.PIXEL_COUNT = 28
        self.PIXEL_SIZE = 3
         
        # Alternatively specify a hardware SPI connection on /dev/spidev0.0:
        self.SPI_PORT   = 0
        self.SPI_DEVICE = 0
        
        self.pixels = Adafruit_WS2801.WS2801Pixels(self.PIXEL_COUNT, spi=SPI.SpiDev(self.SPI_PORT, self.SPI_DEVICE), gpio=GPIO)

        # Default values for starting color
        self.sRed = "ff"
        self.sGreen = "ff"
        self.sBlue = "ff"
        self.iBrightness = 50
        self.mode = 0
        self.relais = 1
        self.data = bytearray()
        self.gamma = bytearray(256)
        

        # Define Relais
        self.RELAIS_1_GPIO = 17
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.RELAIS_1_GPIO, GPIO.OUT)
        GPIO.output(self.RELAIS_1_GPIO, GPIO.HIGH)


    def wheel(self, pos):
        if pos < 85:
            return Adafruit_WS2801.RGB_to_color(pos * 3, 255 - pos * 3, 0)
        elif pos < 170:
            pos -= 85
            return Adafruit_WS2801.RGB_to_color(255 - pos * 3, 0, pos * 3)
        else:
            pos -= 170
            return Adafruit_WS2801.RGB_to_color(0, pos * 3, 255 - pos * 3)

    def wheel_rgb(self, pos):
        if pos < 85:
            return Adafruit_WS2801.RGB_to_color(255, 0, 0)
        elif pos < 170:
            return Adafruit_WS2801.RGB_to_color(0, 255, 0)
        else:
            return Adafruit_WS2801.RGB_to_color(0, 0, 255)

    def rainbow_colors(self, wait=0.05):
        for j in range(256):
            for i in range(self.pixels.count()):
                if self.mode != 2: return
                self.pixels.set_pixel(i, self.wheel(((256 // self.pixels.count() + j)) % 256) )
            self.pixels.show()
            if wait > 0:
                time.sleep(wait)

    def rainbow_cycle(self, wait=0.005):
        for j in range(256):
            for i in range(self.pixels.count()):
                if self.mode != 3: return
                self.pixels.set_pixel(i, self.wheel(((i * 256 // self.pixels.count()) + j) % 256) )
            self.pixels.show()
            if wait > 0:
                time.sleep(wait)

    def rainbow_cycle_successive(self, wait=0.05):
        self.pixels.clear()
        for i in range(self.pixels.count()):
            if self.mode != 4: return
            self.pixels.set_pixel(i, self.wheel(((i * 256 // self.pixels.count())) % 256) )
            self.pixels.show()
            if wait > 0:
                time.sleep(wait)
        for k in range(self.pixels.count()):
            if self.mode != 4: return
            self.pixels.set_pixel(k, Adafruit_WS2801.RGB_to_color( 0, 0, 0 ))
            self.pixels.show()
            if wait > 0:
                time.sleep(wait)

    def rgb_cycle_moving(self, wait=0.05):
        self.pixels.clear() 
        for j in range(self.pixels.count()):
            for i in range(self.pixels.count()):
                if self.mode != 5: return
                self.pixels.set_pixel((j+i)%self.PIXEL_COUNT, self.wheel_rgb( (i * 256 // self.pixels.count() % 256) ))
            self.pixels.show()
            if wait > 0:
                time.sleep(wait)
  
    def appear_from_back(self, color=(255, 0, 0)):
        for i in range(self.pixels.count()):
            for j in reversed(range(i, self.pixels.count())):
                if self.mode != 6: return
                self.pixels.clear()
                # first set all pixels at the begin
                for k in range(i):
                    self.pixels.set_pixel(k, Adafruit_WS2801.RGB_to_color( color[0], color[1], color[2] ))
                # set then the pixel at position j
                self.pixels.set_pixel(j, Adafruit_WS2801.RGB_to_color( color[0], color[1], color[2] ))
                self.pixels.show()
                time.sleep(0.005)

    def singleColor(self):
        self.mode = 0
        self.pixels.clear()
        fDimFactor = float(self.iBrightness) / 100
        sNewRed = int(round(int(self.sRed,16)*fDimFactor))
        sNewGreen =  int(round(int(self.sGreen,16)*fDimFactor))
        sNewBlue = int(round(int(self.sBlue,16)*fDimFactor))
        for k in range(self.pixels.count()):
            self.pixels.set_pixel(k, Adafruit_WS2801.RGB_to_color( sNewRed, sNewGreen, sNewBlue ))
        self.pixels.show()

    def show_stream(self):
        self.mode = 0
        self.pixels.clear()
	
        pixels_in_buffer = len(self.data) / self.PIXEL_SIZE

        for pixel_index in range(pixels_in_buffer):
            pixel = bytearray(self.data[(pixel_index * self.PIXEL_SIZE):((pixel_index * self.PIXEL_SIZE) + self.PIXEL_SIZE)])
            self.pixels.set_pixel(pixel_index, Adafruit_WS2801.RGB_to_color( pixel[0], pixel[1], pixel[2] ))
        self.pixels.show()

    def setData(self,data):
        self.data = data

    def setColor(self, r, g, b):
        self.sRed = r
        self.sGreen = g
        self.sBlue = b

    def getColor(self):
        return "" + self.sRed + self.sGreen + self.sBlue

    def setBrightness(self, b):
        self.iBrightness = b

    def getBrightness(self):
        return self.iBrightness

    def setMode(self, i):
        self.mode = i

    def toggleRelais(self):
        if self.relais == 1:
            GPIO.output(self.RELAIS_1_GPIO, GPIO.HIGH) # aus
            time.sleep(3) #sleep 3s
        else:
            GPIO.output(self.RELAIS_1_GPIO, GPIO.LOW) # an
            time.sleep(3) #sleep 3s
            if self.mode == 0:
                self.singleColor()
        self.relais = not self.relais

    def run(self):
        while getattr(self, "do_run", True):
            if self.relais == 1:
                if self.mode == 1: self.singleColor()
                if self.mode == 2: self.rainbow_colors()
                if self.mode == 3: self.rainbow_cycle()
                if self.mode == 4: self.rainbow_cycle_successive()
                if self.mode == 5: self.rgb_cycle_moving()
                if self.mode == 6: self.appear_from_back()
                if self.mode == 7: self.show_stream()
            time.sleep(0.01)

	print >>sys.stderr, "Shutdown thread..."
