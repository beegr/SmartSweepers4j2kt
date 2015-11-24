import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import minesweeper.Controller;

public class Gui extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6233705413503026349L;
	Controller controller;

	public void setController(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		controller.render(g2);

	}

}
