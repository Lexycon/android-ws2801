#!/usr/bin/python
import socket
import sys 
import threading
import SocketServer
from BaseHTTPServer import BaseHTTPRequestHandler
import SimpleHTTPServer

# Set up LED Thread
from LEDThread import LEDThread

ledThread = LEDThread()
ledThread.start()

# Set up UDP Server to receive commands from app
UDP_IP = ""
UDP_PORT = 6454


sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP
sock.bind((UDP_IP, UDP_PORT))

try:
    while True:
        #print >>sys.stderr, 'waiting to receive message'
        data, address = sock.recvfrom(1024)

        #print >>sys.stderr, 'received %s bytes from %s' % (len(data), address)
        #print >>sys.stderr, data
	
        if data:
            if((len(data) > 18) and (data[0:8] == "Art-Net\x00")):
                ledThread.setData(data)
                ledThread.setMode(8)
            
            elif len(data)>7:
                ledThread.setData(data)
                ledThread.setMode(7)

            elif data[:1] == 'A': # send current values
                sColor = ledThread.getColor()
                iBrightness = ledThread.getBrightness()
                sent = sock.sendto('A' + str(iBrightness) + "," + sColor, address)
                print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)		
	
            elif data[:1] == 'B': # set brightness
                iBrightness = data[1:]
                print >>sys.stderr, iBrightness
                ledThread.setBrightness(iBrightness)
                ledThread.setMode(1)

            elif data[:1] == '0': # toggle relais
                ledThread.toggleRelais()

            elif data[:1] == 'C': # set single color
                sRed = data[1:3]
                sGreen = data[3:5]
                sBlue = data[5:7]
                ledThread.setColor(sRed, sGreen, sBlue)
                ledThread.setMode(1)

            elif data[:1] == '2': # set rainbow_colors
                ledThread.setMode(2)

            elif data[:1] == '3': # set rainbow_cycle
                ledThread.setMode(3)

            elif data[:1] == '4': # set rainbow_cycle_successive
                ledThread.setMode(4)

            elif data[:1] == '5': # set rainbow_cycle_moving
                ledThread.setMode(5)

            elif data[:1] == '6': # set appear_from_back
                ledThread.setMode(6)

except KeyboardInterrupt:
        print >>sys.stderr, 'LEDServer shutdown'
        ledThread.setMode(0)
        ledThread.do_run = False
        ledThread.join()
