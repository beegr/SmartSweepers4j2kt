@file:Suppress("ConstPropertyName")

import Parameters.Companion.parameters
import java.awt.*
import java.awt.event.*
import java.awt.geom.*
import java.io.*
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

    fun newController(gui: Gui, repaint: () -> Unit) = Controller().also {
        gui.controller = it
        it.repaint = repaint
    }

    @JvmStatic
    fun main(args: Array<String>) {
        File(args.firstOrNull() ?: "params.ini")
            .let { if (it.exists()) it else null }
            ?.let { Parameters.loadInParameters(FileInputStream(it)) }
            ?: throw IllegalStateException("couldn't locate params.ini; it should be in the working directory or at the location specified as first argument")
        val panel = Gui()
        val frame = JFrame(applicationName).also { it.add(panel) }
        var controller = newController(panel, frame::repaint)

        val timer = Timer(parameters.iFramesPerSecond.toFloat()).also { it.start() }
        var done = false
        with(frame) {
            addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_Q -> done = true
                        KeyEvent.VK_F -> controller.fastRenderToggle()
                        KeyEvent.VK_P -> timer.togglePause()
                        KeyEvent.VK_R -> { // reset, start from scratch
                            controller = newController(panel, frame::repaint)
                            timer.start()
                        }
                    }
                }
            })

            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(parameters.iWindowWidth, parameters.iWindowHeight)
            isVisible = true
        }

        while (!done) {
            if (timer.paused) Thread.sleep(250)
            else {
                if (timer.readyForNextFrame() || controller.fastRender) {
                    controller.update()
                    if (!controller.fastRender) frame.repaint()
                }
                if (!controller.fastRender) Thread.sleep(1)
            }
        }
        frame.isVisible = false
        exitProcess(0)
    }
}
