package ch.bildspur.humble.test;

import org.junit.Test;
import processing.core.PApplet;

import static org.junit.Assert.assertEquals;

/**
 * Created by cansik on 21.03.17.
 */
public class ProcessingTest {

    @Test
    public void processingRunTest() {
        Sketch sketch = new Sketch();
        PApplet.runSketch(new String[]{"Sketch "}, sketch);

        // wait for sketch to exit
        while(!sketch.finished)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(1, 1);
    }
}