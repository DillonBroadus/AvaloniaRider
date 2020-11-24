package me.fornever.avaloniarider.previewer

import me.fornever.avaloniarider.controlmessages.FrameMessage

fun nonTransparent(frame: FrameMessage): Boolean {
    return frame.data.any { it != 0.toByte() }
}
