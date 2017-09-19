package ch.bildspur.humble.converter

import io.humble.video.MediaPicture
import io.humble.video.MediaPictureResampler
import io.humble.video.PixelFormat
import io.humble.video.awt.MediaPictureConverter

import java.awt.image.BufferedImage

/** An abstract converter class from which specific converters can be
 * derived to do the actual conversions.  This class establishes if
 * the [MediaPicture] needs to be re-sampled, and
 * if so, creates appropriate [MediaPictureResampler] objects to do
 * that.
 *
 * Note: Package Private
 */

abstract class AMediaPictureConverter
/**
 * Construct an abstract Converter.  This will create a
 * [MediaPictureResampler]
 * to change color-space or resize the picture as needed for the
 * conversions specified.
 *
 * @param pictureType the recognized [MediaPicture] type
 * @param requiredPictureType the picture type requred to translate to
 * and from the BufferedImage
 * @param imageType the recognized [BufferedImage] type
 * @param pictureWidth the width of picture
 * @param pictureHeight the height of picture
 * @param imageWidth the width of image
 * @param imageHeight the height of image
 */
(
        // the recognized BufferedImage type

        private val mPictureType: PixelFormat.Type,
        // the recognized BufferedImage type

        /**
         * Return the Type which matches the [ ] type.
         *
         * @return the picture type which allows for image translation.
         */

        protected val requiredPictureType: PixelFormat.Type,
        // the recognized BufferedImage type

        private val mImageType: Int,
        /** The width of the pictures.  */

        protected var mPictureWidth: Int,
        /** The height of the pictures.  */

        protected var mPictureHeight: Int,
        /** The width of the images.  */

        protected var mImageWidth: Int,
        /** The height of the images.  */

        protected var mImageHeight: Int) : MediaPictureConverter {
    /** Re-sampler called when converting image to picture, may be null.  */

    protected var mToPictureResampler: MediaPictureResampler? = null

    /** Re-sampler called when converting picture to image, may be null.  */

    protected var mToImageResampler: MediaPictureResampler? = null

    // the description of this convert

    private val mDescription: String

    init {
        // by default there is no resample description

        var resampleDescription = ""

        // if the picture type is not the type or size required, create the
        // resamplers to fix that

        if (mPictureType != this.requiredPictureType
                || mPictureWidth != mImageWidth
                || mPictureHeight != mImageHeight) {

            mToImageResampler = MediaPictureResampler.make(
                    mImageWidth, mImageHeight, this.requiredPictureType,
                    mPictureWidth, mPictureHeight, mPictureType, 0)
            mToImageResampler!!.open()

            mToPictureResampler = MediaPictureResampler.make(
                    mPictureWidth, mPictureHeight, mPictureType,
                    mImageWidth, mImageHeight, this.requiredPictureType, 0)
            mToPictureResampler!!.open()

            resampleDescription = "Pictures will be resampled to and from " +
                    this.requiredPictureType + " during translation."
        }

        // construct the description of this converter

        mDescription = "A converter which translates [" +
                mPictureWidth + "x" + mPictureHeight + "] MediaPicture type " +
                mPictureType + " to and from [" + mImageWidth + "x" + mImageHeight +
                "] BufferedImage type " + mImageType + ".  " + resampleDescription
    }// record the image and picture parameters

    /** {@inheritDoc}  */

    override fun getPictureType(): PixelFormat.Type {
        return mPictureType
    }

    /** {@inheritDoc}  */

    override fun getImageType(): Int {
        return mImageType
    }

    /** {@inheritDoc}  */

    override fun willResample(): Boolean {
        return null != mToPictureResampler && null != mToImageResampler
    }

    /**
     * Test that the passed image is valid and conforms to the
     * converters specifications.
     *
     * @param image the image to test
     *
     * @throws IllegalArgumentException if the passed [         ] is NULL;
     * @throws IllegalArgumentException if the passed [         ] is not the correct type. See [         ][.getImageType].
     */

    protected fun validateImage(image: BufferedImage?) {
        // if the image is NULL, throw up

        if (image == null)
            throw IllegalArgumentException("The passed image is NULL.")

        // if image is not the correct type, throw up

        if (image.type != imageType)
            throw IllegalArgumentException(
                    "The passed image is of type #" + image.type +
                            " but is required to be of BufferedImage type #" +
                            imageType + ".")
    }

    /**
     * Test that the passed picture is valid and conforms to the
     * converters specifications.
     *
     * @param picture the picture to test
     *
     * @throws IllegalArgumentException if the passed [         ] is NULL;
     * @throws IllegalArgumentException if the passed [         ] is not complete.
     * @throws IllegalArgumentException if the passed [         ] is not the correct type.
     */

    protected fun validatePicture(picture: MediaPicture?) {
        // if the picture is NULL, throw up

        if (picture == null)
            throw IllegalArgumentException("The picture is NULL.")

        // if the picture is not complete, throw up

        if (!picture.isComplete)
            throw IllegalArgumentException("The picture is not complete.")

        // if the picture is an invalid type throw up

        val type = picture.format
        if (type != pictureType && willResample() && type != mToImageResampler!!.outputFormat)
            throw IllegalArgumentException(
                    "Picture is of type: " + type + ", but must be " +
                            pictureType + (if (willResample())
                        " or " + mToImageResampler!!.outputFormat
                    else
                        "") +
                            ".")
    }

    /** {@inheritDoc}  */

    override fun getDescription(): String {
        return mDescription
    }

    /** Get a string representation of this converter.  */

    override fun toString(): String {
        return description
    }

    override fun delete() {
        if (mToPictureResampler != null)
            mToPictureResampler!!.delete()
        mToPictureResampler = null
        if (mToImageResampler != null)
            mToImageResampler!!.delete()
        mToImageResampler = null
    }

    /**
     * Re-sample a picture.
     *
     * @param picture1 the picture to re-sample
     * @param resampler the picture re-samper to use
     *
     * @throws RuntimeException if could not re-sample picture
     */
    protected fun resample(input: MediaPicture,
                           resampler: MediaPictureResampler): MediaPicture {
        // create new picture object

        val output = MediaPicture.make(
                resampler.outputWidth,
                resampler.outputHeight,
                resampler.outputFormat)
        return resample(output, input, resampler)
    }

    protected fun resample(output: MediaPicture, input: MediaPicture, resampler: MediaPictureResampler): MediaPicture {
        // resample

        resampler.resample(output, input)

        // return the resample picture

        return output

    }
}