package ch.bildspur.humble

import processing.core.PGraphics
import processing.core.PImage

interface Video {
    fun frameRate()
    fun speed(speed: Float)
    fun duration() : Float
    fun time() : Float
    fun jump(location : Float)
    fun available(): Boolean
    fun play()
    fun loop()
    fun noLoop()
    fun pause()
    fun stop()
    fun read() : PImage
}