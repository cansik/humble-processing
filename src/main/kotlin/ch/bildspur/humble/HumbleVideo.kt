package ch.bildspur.humble

import io.humble.video.*
import io.humble.video.MediaPacket
import io.humble.video.Rational
import io.humble.video.awt.MediaPictureConverter
import io.humble.video.awt.MediaPictureConverterFactory
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt




class HumbleVideo(val parent: PApplet, val filename: String) : Video {
    private val demuxer = Demuxer.make()!!

    private var videoStreamId = -1
    private var streamStartTime = Global.NO_PTS
    private lateinit var videoDecoder: Decoder
    private lateinit var picture : MediaPicture
    private lateinit var converter : MediaPictureConverter
    private var image: BufferedImage? = null
    private val packet = MediaPacket.make()

    private var systemStartTime = System.nanoTime()
    private val systemTimeBase = Rational.make(1, 1000000000)
    private lateinit var streamTimebase :Rational

    private lateinit var frame : PImage

    init {
        open()
    }

    private fun open()
    {
        demuxer.open(filename, null, false, true, null, null)

        val numStreams = demuxer.numStreams

        for (i in 0 until numStreams) {
            val stream = demuxer.getStream(i)
            streamStartTime = stream.startTime
            val decoder = stream.decoder
            if (decoder != null && decoder.codecType === MediaDescriptor.Type.MEDIA_VIDEO) {
                videoStreamId = i
                videoDecoder = decoder
                // stop at the first one.
                break
            }
        }
        if (videoStreamId == -1)
            throw RuntimeException("could not find video stream in container: " + filename)

        videoDecoder.open(null, null)

        picture = MediaPicture.make(
                videoDecoder.width,
                videoDecoder.height,
                videoDecoder.pixelFormat)

        frame = PImage(videoDecoder.width,
                videoDecoder.height,
                PConstants.RGB)

        converter = MediaPictureConverterFactory.createConverter(
                MediaPictureConverterFactory.HUMBLE_BGR_24,
                picture)

        streamTimebase = videoDecoder.timeBase
    }

    fun close()
    {
        demuxer.close()
    }

    override fun frameRate() {

    }

    override fun speed(speed: Float) {

    }


    override fun duration(): Float {
        return 0f
    }

    override fun time(): Float {
        return 0f
    }

    override fun jump(location: Float) {
    }

    override fun available(): Boolean {
        return false
    }

    override fun play() {

    }

    override fun loop() {
    }

    override fun noLoop() {
       }

    override fun pause() {
     }

    override fun stop() {
     }

    override fun read(): PImage {
        // default reading packet
        while (demuxer.read(packet) >= 0) {
            if (packet.streamIndex == videoStreamId) {
                var offset = 0
                var bytesRead = 0
                do {
                    bytesRead += videoDecoder.decode(picture, packet, offset)
                    if (picture.isComplete) {
                        image = converter.toImage(image, picture)
                        updateFrame()
                        return frame
                    }
                    offset += bytesRead
                } while (offset < packet.size)
            }
        }

        // non-flushed packages
        if(picture.isComplete) {
            videoDecoder.decode(picture, null, 0)
            if (picture.isComplete) {
                image = converter.toImage(image, picture)
                updateFrame()
                return frame
            }
        }

        return frame
    }

    private fun updateFrame()
    {
        if(image == null)
            return

        // very slow
        (0 until frame.width).forEach { x ->
            (0 until frame.height).forEach {y ->
                frame.set(x, y, image!!.getRGB(x, y))
            }
        }
        frame.updatePixels()


        /*
        frame.loadPixels()
        val g = (frame.native as BufferedImage).createGraphics()
        g.color= Color.cyan
        g.fillRect(100, 100,100, 100)
        g.dispose()
        frame.updatePixels()
        */
    }
}