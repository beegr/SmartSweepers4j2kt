import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import minesweeper.Controller;
import utils.Timer;
import configuration.Parameters;

public class Main {

	static String applicationName = "Smart Sweepers 4j v1.0";

	Controller controller = null;

	public static void main(String[] args) {

		Parameters.loadInParameters(Main.class.getResourceAsStream("params.ini"));
		Gui panel = new Gui();
		JFrame frame = new JFrame(applicationName);
		frame.add(panel);

		Controller controller = new Controller(frame);

		panel.setController(controller);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_F:
					controller.fastRenderToggle();
					break;
				case KeyEvent.VK_R:
					if (controller != null) {
						// controller = new Controller(frame);
					}
				case KeyEvent.VK_P:
					// TODO pause mode
				}
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(Parameters.WindowWidth, Parameters.WindowHeight);
		frame.setVisible(true);

		Timer timer = new Timer(Parameters.iFramesPerSecond);
		timer.start();

		boolean done = false;

		while (!done) {
			if (timer.readyForNextFrame() || controller.fastRender()) {
				if (!controller.update()) {
					done = true;
				}

				frame.repaint();
			}
			if (!controller.fastRender()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		frame.setVisible(false);
		System.exit(0);
	}

}
