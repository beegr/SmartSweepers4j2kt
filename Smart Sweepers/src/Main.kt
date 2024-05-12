@file:Suppress("ConstPropertyName")

import configuration.*
import minesweeper.*
import utils.Timer
import java.awt.*
import java.awt.event.*
import javax.swing.*
import kotlin.system.*

class Gui : JComponent() {
    lateinit var controller: Controller

    override fun paint(g: Graphics) {
        super.paintComponent(g)
        controller.render(g as Graphics2D)
    }
}

object Main {
    private const val applicationName = "Smart Sweepers 4j v1.0"

    @JvmStatic
    fun main(args: Array<String>) {
        Main::class.java.getResourceAsStream("params.ini")?.let { Parameters.loadInParameters(it) }
            ?: throw IllegalStateException("compile problem: couldn't read internal params.ini")
        val panel = Gui()
        val frame = JFrame(applicationName).also { it.add(panel) }
        val controller = Controller(frame).also { panel.controller = it }

        with(frame) {
            addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_F -> controller.fastRenderToggle()
                    }
                }
            })

            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(Parameters.WindowWidth, Parameters.WindowHeight)
            isVisible = true
        }

        val timer = Timer(Parameters.iFramesPerSecond.toFloat()).also { it.start() }
        var done = false
        while (!done) {
            if (timer.readyForNextFrame() || controller.fastRender()) {
                if (!controller.update()) done = true
                frame.repaint()
            }
            if (!controller.fastRender()) Thread.sleep(1)
        }
        frame.isVisible = false
        exitProcess(0)
    }
}
