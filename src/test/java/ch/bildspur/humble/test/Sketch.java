package ch.bildspur.humble.test;


import ch.bildspur.humble.HumbleVideo;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class Sketch extends PApplet {
    public final static int OUTPUT_WIDTH = 640;
    public final static int OUTPUT_HEIGHT = 480;

    public final static int FRAME_RATE = 60;

    HumbleVideo video;

    public void settings() {
        size(OUTPUT_WIDTH, OUTPUT_HEIGHT, FX2D);
        PJOGL.profile = 1;
    }

    public void setup() {
        frameRate(FRAME_RATE);

        video = new HumbleVideo(this, dataPath("abstract.mp4"));
    }

    public void draw() {
        // clear screen
        background(0);

        PImage frame = video.read();
        image(frame,0, 0, width, height);

        fill(0, 255, 0);
        text("FPS: " + frameRate, 20, 20);
    }

    public void stop()
    {
        video.stop();
    }
}
