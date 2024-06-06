@file:Suppress("ConstPropertyName")

import configuration.*
import minesweeper.*
import utils.Timer
import java.awt.*
import java.awt.event.*
import java.awt.geom.*
import javax.swing.*
import kotlin.system.*

class Gui : JComponent() {
    lateinit var controller: Controller

    override fun paint(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        // as draw(line) and drawString are overloaded methods, we need to be explicit
        // with the parameters to reference the correct one.
        controller.line = { x1, y1, x2, y2 -> g2.draw(Line2D.Double(x1, y1, x2, y2)) }
        controller.text = { s, x, y -> g2.drawString(s, x, y) }

        val savedColor = g2.color
        controller.oldPen = { g2.color = savedColor }
        controller.redPen = { g2.color = Color.RED }
        controller.greenPen = { g2.color = Color.GREEN }
        controller.bluePen = { g2.color = Color.BLUE }

        controller.render()
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
        val controller = Controller().also { panel.controller = it }

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
            if (timer.readyForNextFrame() || controller.fastRender) {
                if (!controller.update()) done = true
                frame.repaint()
            }
            if (!controller.fastRender) Thread.sleep(1)
        }
        frame.isVisible = false
        exitProcess(0)
    }
}
