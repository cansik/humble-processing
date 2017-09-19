package ch.bildspur.humble.converter

import io.humble.ferry.Buffer
import java.awt.image.DataBufferByte
import io.humble.ferry.JNIReference
import java.util.concurrent.atomic.AtomicReference
import io.humble.video.MediaPicture
import io.humble.video.PixelFormat
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.ComponentColorModel
import java.awt.image.Raster
import java.awt.image.PixelInterleavedSampleModel
import java.nio.ByteOrder
import java.awt.image.DataBufferInt


class BgrIntConverter
/**
 * Construct as converter to translate [MediaPicture]s to and from
 * [BufferedImage]s of type [BufferedImage.TYPE_3BYTE_BGR].
 *
 * @param pictureType
 * the picture type recognized by this converter
 * @param pictureWidth
 * the width of pictures
 * @param pictureHeight
 * the height of pictures
 * @param imageWidth
 * the width of images
 * @param imageHeight
 * the height of images
 */
(pictureType: PixelFormat.Type, pictureWidth: Int,
 pictureHeight: Int, imageWidth: Int, imageHeight: Int) : AMediaPictureConverter(pictureType, PixelFormat.Type.PIX_FMT_BGR24, BufferedImage.TYPE_3BYTE_BGR, pictureWidth, pictureHeight, imageWidth, imageHeight) {

    // a private copy we use as the resample buffer when converting back and forth. saves time.
    private var mResampleMediaPicture: MediaPicture? = null

    init {
        mResampleMediaPicture = if (willResample())
            MediaPicture.make(imageWidth,
                    imageHeight, requiredPictureType)
        else
            null
    }

    /** {@inheritDoc}  */
    override fun toPicture(output: MediaPicture?,
                  input: BufferedImage, timestamp: Long): MediaPicture? {
        var output = output
        // validate the image

        validateImage(input)

        if (output == null) {
            output = MediaPicture.make(mPictureWidth,
                    mPictureHeight, pictureType)
        }

        // get the image byte buffer buffer

        val imageBuffer = input.raster.dataBuffer
        var imageBytes: ByteArray? = null
        var imageInts: IntArray? = null

        // handle byte buffer case

        if (imageBuffer is DataBufferByte) {
            imageBytes = imageBuffer.data
        } else if (imageBuffer is DataBufferInt) {
            imageInts = imageBuffer.data
        } else {
            throw IllegalArgumentException(
                    "Unsupported BufferedImage data buffer type: " + imageBuffer.dataType)
        }// if it's some other type, throw
        // handle integer buffer case

        // create the video picture and get it's underlying buffer

        val ref = AtomicReference<JNIReference>(null)
        val picture = if (willResample()) mResampleMediaPicture else output
        try {
            var buffer: Buffer? = picture!!.getData(0)
            val size = picture.getDataPlaneSize(0)
            var pictureByteBuffer = buffer!!.getByteBuffer(0,
                    size, ref)
            buffer.delete()
            buffer = null

            if (imageInts != null) {
                pictureByteBuffer!!.order(ByteOrder.BIG_ENDIAN)
                val pictureIntBuffer = pictureByteBuffer.asIntBuffer()
                pictureIntBuffer.put(imageInts)
            } else {
                pictureByteBuffer!!.put(imageBytes)
            }
            pictureByteBuffer = null
            picture.timeStamp = timestamp
            picture.isComplete = true

            // resample as needed
            if (willResample()) {
                resample(output!!, picture, mToPictureResampler!!)
            }
            return output
        } finally {
            if (ref.get() != null)
                ref.get().delete()
        }
    }

    /** {@inheritDoc}  */
    override fun toImage(output: BufferedImage?, input: MediaPicture): BufferedImage {
        var output = output
        validatePicture(input)
        // test that the picture is valid
        if (output == null) {
            val bytes = ByteArray(if (willResample()) mResampleMediaPicture!!.getDataPlaneSize(0) else input.getDataPlaneSize(0))
            // create the data buffer from the bytes

            val db = DataBufferByte(bytes, bytes.size)

            // create an a sample model which matches the byte layout of the
            // image data and raster which contains the data which now can be
            // properly interpreted
            val w = mImageWidth
            val h = mImageHeight

            val sm = PixelInterleavedSampleModel(
                    db.dataType, w, h, 3, 3 * w, mBandOffsets)
            val wr = Raster.createWritableRaster(sm, db, null)

            // create a color model

            val colorModel = ComponentColorModel(
                    mColorSpace, false, false, ColorModel.OPAQUE, db.dataType)

            // return a new image created from the color model and raster

            output = BufferedImage(colorModel, wr, false, null)
        }

        val picture: MediaPicture
        // resample as needed
        val ref = AtomicReference<JNIReference>(null)
        try {
            if (willResample()) {
                picture = resample(mResampleMediaPicture!!, input, mToImageResampler!!)
            } else {
                picture = input
            }

            val buffer = picture.getData(0)
            val size = picture.getDataPlaneSize(0)
            val byteBuf = buffer.getByteBuffer(0,
                    size, ref)
            buffer.delete()

            // get the bytes out of the image
            val db = output.raster
                    .dataBuffer as DataBufferByte
            val bytes = db.data
            // and copy them in.
            byteBuf.get(bytes, 0, size)

            // return a new image created from the color model and raster

            return output
        } finally {
            if (ref.get() != null)
                ref.get().delete()
        }
    }

    override fun delete() {
        if (mResampleMediaPicture != null)
            mResampleMediaPicture!!.delete()
        mResampleMediaPicture = null

        super.delete()
    }

    companion object {
        // band offsets requried by the sample model

        private val mBandOffsets = intArrayOf(2, 1, 0)

        // color space for this converter

        private val mColorSpace = ColorSpace
                .getInstance(ColorSpace.CS_sRGB)
    }
}