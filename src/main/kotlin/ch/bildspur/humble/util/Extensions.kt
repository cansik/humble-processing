package ch.bildspur.humble.util

import processing.core.PGraphics

fun PGraphics.stackMatrix(block: (g: PGraphics) -> Unit) {
    this.pushMatrix()
    block(this)
    this.popMatrix()
}

fun PGraphics.draw(block: (g: PGraphics) -> Unit) {
    this.beginDraw()
    block(this)
    this.endDraw()
}