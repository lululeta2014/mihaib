#! /usr/bin/env python3

import math
import struct
import wave

def readFile(filename):
    wRead = wave.open(filename, 'r')
    try:
        ch = wRead.getnchannels()
        if ch != 2:
            raise ValueError('Expected 2 channels')
        channels = ([], [])
        framerate = wRead.getframerate()

        sampWidth = wRead.getsampwidth()
        if sampWidth != 2:
            raise ValueError('Expected sample width 2 bytes')
        maxVal = 2**(sampWidth*8 - 1) - 1
        structFmt = '<h'
        totFrames = wRead.getnframes()
        for crtFrame in range(totFrames):
            fBytesLeft = wRead.readframes(1)
            for crtCh in range(len(channels)):
                fCrtBytes = fBytesLeft[:sampWidth]
                fBytesLeft = fBytesLeft[sampWidth:]
                fInt = struct.unpack(structFmt, fCrtBytes)[0]
                fFloat = fInt / maxVal
                if fFloat > 1:
                    fFloat = 1
                elif fFloat < -1:
                    fFloat = -1
                channels[crtCh].append(fFloat)
    finally:
        wRead.close()

    return framerate, channels

def writeFile(filename, framerate, channels):
    if len(channels) != 2:
        raise ValueError('Expected 2 channels')
    sampWidth = 2
    maxVal = 2**(sampWidth*8 - 1) - 1
    structFmt = '<h'
    wWrite = wave.open(filename, 'w')
    try:
        wWrite.setnchannels(len(channels))
        wWrite.setsampwidth(sampWidth)
        wWrite.setframerate(framerate)
        wWrite.setnframes(len(channels[1]))
        for frameNo in range(len(channels[1])):
            for ch in channels:
                fFloat = ch[frameNo]
                if fFloat > 1:
                    fFloat = 1
                elif fFloat < -1:
                    fFloat = -1
                fInt = int(fFloat * maxVal)
                fBytes = struct.pack(structFmt, fInt)
                wWrite.writeframes(fBytes)
    finally:
        wWrite.close()

def mute(channel):
    result = []
    for i in range(len(channel)):
        result.append(0)
    return result

def scaleVolume(channel, factor):
    result = []
    for x in channel:
        result.append(x * factor)
    return result

def delayMillis(channel, framerate, millis):
    amount = int(framerate * millis / 1000)
    result = []
    for i in range(len(channel[:amount])):
        result.append(0)
    result.extend(channel[:len(channel)-len(result)])
    return result

def invert(channel):
    result = []
    for x in channel:
        result.append(-x)
    return result

def sine(freq, framerate, millis):
    result = []
    amount = int(framerate * millis / 1000)
    cycleLen = (framerate / freq)
    for i in range(amount):
        radians = i * freq * 2*math.pi / framerate
        result.append(math.sin(radians))
    return result

def add(*channels):
    return [sum(z) for z in zip(*channels)]

def norm(ch):
    m = max(map(abs, ch))
    if m <= 1:
        return list(ch)
    return [x/m for x in ch]

def mainV1():
    framerate, (l, r) = readFile('a.wav')
    print(framerate, len(l), len(r))
    print(l[:10])
    print('MAX', max(l), 'MIN', min(l))
    writeFile('b.wav', framerate, (scaleVolume(l, 0.7), r))

def mainV2():
    framerate = 44100
    fundamental = 110
    scaleFactor = 0.8
    crtScale = 1
    tones = []
    duration = 3000
    for i in range(1, 20):
        tones.append(scaleVolume(
            sine(i * fundamental, framerate, duration), crtScale))
        crtScale *= scaleFactor
    ch = norm(add(*tones))
    writeFile('b.wav', framerate, (ch, ch))

if __name__ == '__main__':
    #mainV1()
    #mainV2()
    print('Simple example of reading and writing audio data (WAVE file)')
    print('Choose something to do (edit the source)')
