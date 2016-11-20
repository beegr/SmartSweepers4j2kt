package minesweeper;

import generics.GenericAlgorithm;
import generics.Genome;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import utils.C2DMatrix;
import utils.Random;
import utils.SPoint;
import utils.SVector2D;
import configuration.Parameters;

public class Controller {

	List<Genome> thePopulation;
	List<Minesweeper> sweepers;
	List<SVector2D> mines;
	List<SVector2D> obstacles;

	GenericAlgorithm genericAlg;

	int numSweepers;
	int numMines;
	int numWeightsinNN;

	List<SPoint> sweeperVB;
	List<SPoint> mineVB;

	List<Double> avgFitness;
	List<Double> bestFitness;

	Color redPen;
	Color bluePen;
	Color greenPen;
	Color oldPen;

	JFrame mainWindow;

	boolean fastRender;
	int ticks;
	int generations;
	int xClient;
	int yClient;

	Random rand;

	final SPoint[] sweeper = new SPoint[] { new SPoint(-1, -1), new SPoint(-1, 1), new SPoint(-0.5, 1),
			new SPoint(-0.5, -1), new SPoint(0.5, -1), new SPoint(1, -1), new SPoint(1, 1), new SPoint(0.5, 1),
			new SPoint(-0.5, -0.5), new SPoint(0.5, -0.5), new SPoint(-0.5, 0.5), new SPoint(-0.25, 0.5),
			new SPoint(-0.25, 1.75), new SPoint(0.25, 1.75), new SPoint(0.25, 0.5), new SPoint(0.5, 0.5) };

	final SPoint[] mine = new SPoint[] { new SPoint(-1, -1), new SPoint(-1, 1), new SPoint(1, 1), new SPoint(1, -1) };

	public Controller(JFrame frame) {
		numSweepers = Parameters.iNumSweepers;
		fastRender = false;
		ticks = 0;
		numMines = Parameters.iNumMines;
		mainWindow = frame;
		generations = 0;
		xClient = Parameters.WindowWidth;
		yClient = Parameters.WindowHeight;

		sweepers = new ArrayList<>();
		rand = new Random();
		bestFitness = new ArrayList<>();
		avgFitness = new ArrayList<>();

		for (int i = 0; i < numSweepers; i++) {
			sweepers.add(new Minesweeper());
		}

		numWeightsinNN = sweepers.get(0).getNumberOfWeights();

		genericAlg = new GenericAlgorithm(numSweepers, Parameters.dMutationRate, Parameters.dCrossoverRate, numWeightsinNN);

		thePopulation = genericAlg.getChromos();

		for (int i = 0; i < numSweepers; i++) {
			sweepers.get(i).putWeights(thePopulation.get(i).getWeights());
		}

		mines = new ArrayList<>();
		for (int i = 0; i < numMines; i++) {
			mines.add(new SVector2D(rand.randomFloat() * xClient, rand.randomFloat() * yClient));
		}
		obstacles = new ArrayList<>();
		for (int i = 0; i < numMines; i++) {
			obstacles.add(new SVector2D(rand.randomFloat() * xClient, rand.randomFloat() * yClient));
		}

		bluePen = Color.BLUE;
		redPen = Color.RED;
		greenPen = Color.GREEN;
		oldPen = null;

		sweeperVB = new ArrayList<>();
		for (int i = 0; i < sweeper.length; i++) {
			sweeperVB.add(sweeper[i]);
		}

		mineVB = new ArrayList<>();
		for (int i = 0; i < mine.length; i++) {
			mineVB.add(mine[i]);
		}
	}

	public boolean fastRender() {
		return fastRender;
	}

	public void setFastRender(boolean arg) {
		fastRender = arg;
	}

	public void fastRenderToggle() {
		fastRender = !fastRender;
	}

	public void worldTransform(List<SPoint> buffer, SVector2D pos) {
		C2DMatrix matTransform = new C2DMatrix();

		matTransform.scale(Parameters.dMineScale, Parameters.dMineScale);

		matTransform.translate(pos.getX(), pos.getY());

		matTransform.transformSPoints(buffer);
	}

	public boolean update() {
		if (ticks++ < Parameters.iNumTicks) {
			for (int i = 0; i < numSweepers; i++) {
				Minesweeper currentSweeper = sweepers.get(i);
				if (!currentSweeper.update(mines, obstacles)) {
					System.out.println("Wront amout of NN inputs!");
					return false;
				}

				int grabHit = currentSweeper.checkForMine(mines, Parameters.dMineScale);

				if (grabHit >= 0) {
					currentSweeper.incrementFitness();

					mines.set(grabHit, new SVector2D(rand.randomFloat() * xClient, rand.randomFloat() * yClient));
				}
				int grabTrap = currentSweeper.checkForObstacle(obstacles, Parameters.dMineScale);
				
				if (grabTrap >= 0) {
					currentSweeper.decrementFitness();
					
					obstacles.set(grabTrap, new SVector2D(rand.randomFloat() * xClient, rand.randomFloat() * yClient));
				}

				thePopulation.get(i).setFitness(currentSweeper.getFitness());
			}
		} else {
			avgFitness.add(genericAlg.getAverageFitness());
			bestFitness.add(genericAlg.getBestFitness());

			removeValues(avgFitness, 100);
			removeValues(bestFitness, 100);
			
			++generations;
			ticks = 0;

			thePopulation = genericAlg.runEpoche(thePopulation);

			for (int i = 0; i < numSweepers; i++) {
				sweepers.get(i).putWeights(thePopulation.get(i).getWeights());
				sweepers.get(i).reset();
			}
			mainWindow.repaint();
		}
		return true;
	}

	private void removeValues(List<?> list, int maxEntries) {
		while(list.size() > maxEntries){
			list.remove(0);
		}
	}
	
	public void render(Graphics2D g2) {
		String s = "Generation: " + generations;
		g2.drawString(s, 5, 20);

		if (!fastRender) {
			oldPen = g2.getColor();
			g2.setColor(greenPen);

			for (int i = 0; i < numMines; i++) {

				List<SPoint> mineVBnew = new ArrayList<SPoint>();
				for (SPoint point : mineVB) {
					mineVBnew.add(new SPoint(point.getX(), point.getY()));
				}

				worldTransform(mineVBnew, mines.get(i));

				for (int vert = 0; vert < mineVBnew.size() - 1; vert++) {
					int currVert = vert;
					SPoint firstPoint = mineVBnew.get(currVert++);
					SPoint secondPoint = mineVBnew.get(currVert);
					Shape line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
					g2.draw(line);
				}
				SPoint firstPoint = mineVBnew.get(mineVBnew.size() - 1);
				SPoint secondPoint = mineVBnew.get(0);
				Shape line2 = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line2);
			}
			
			g2.setColor(Color.GRAY);

			for (int i = 0; i < numMines; i++) {

				List<SPoint> mineVBnew = new ArrayList<SPoint>();
				for (SPoint point : mineVB) {
					mineVBnew.add(new SPoint(point.getX(), point.getY()));
				}

				worldTransform(mineVBnew, obstacles.get(i));

				for (int vert = 0; vert < mineVBnew.size() - 1; vert++) {
					int currVert = vert;
					SPoint firstPoint = mineVBnew.get(currVert++);
					SPoint secondPoint = mineVBnew.get(currVert);
					Shape line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
					g2.draw(line);
				}
				SPoint firstPoint = mineVBnew.get(mineVBnew.size() - 1);
				SPoint secondPoint = mineVBnew.get(0);
				Shape line2 = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line2);
			}

			g2.setColor(redPen);

			for (int i = 0; i < numSweepers; i++) {
				if (i == Parameters.iNumElite) {
					g2.setColor(oldPen);
				}

				List<SPoint> sweeperVB = new ArrayList<>();
				for (SPoint sPoint : this.sweeperVB) {
					sweeperVB.add(new SPoint(sPoint.getX(), sPoint.getY()));
				}

				sweepers.get(i).worldTransform(sweeperVB);

				for (int vert = 0; vert < 3; vert++) {
					int currVert = vert;
					SPoint firstPoint = sweeperVB.get(currVert++);
					SPoint secondPoint = sweeperVB.get(currVert);
					Shape line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
					g2.draw(line);
				}
				SPoint firstPoint = sweeperVB.get(3);
				SPoint secondPoint = sweeperVB.get(0);
				Shape line2 = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line2);

				for (int vert = 4; vert < 7; vert++) {
					int currVert = vert;
					firstPoint = sweeperVB.get(currVert++);
					secondPoint = sweeperVB.get(currVert);
					Shape line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
					g2.draw(line);
				}
				firstPoint = sweeperVB.get(7);
				secondPoint = sweeperVB.get(4);
				line2 = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line2);

				firstPoint = sweeperVB.get(8);
				secondPoint = sweeperVB.get(9);
				Shape line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line);

				for (int vert = 10; vert < 15; vert++) {
					int currVert = vert;
					firstPoint = sweeperVB.get(currVert++);
					secondPoint = sweeperVB.get(currVert);
					line = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
					g2.draw(line);
				}
				firstPoint = sweeperVB.get(15);
				secondPoint = sweeperVB.get(10);
				line2 = new Line2D.Double(firstPoint.getX(), firstPoint.getY(), secondPoint.getX(), secondPoint.getY());
				g2.draw(line2);
			}
			g2.setColor(oldPen);
		} else {
			plotStats(g2);
		}
	}

	private void plotStats(Graphics2D g2) {
		String s = "Best Fitness: " + String.format("%.0f", genericAlg.getBestFitness());
		g2.drawString(s, 5, 40);

		s = "Average Fitness: " + String.format("%.2f", genericAlg.getAverageFitness());
		g2.drawString(s, 5, 60);

		float hSlice = (float) (xClient / (bestFitness.size() + 1.0));
		float vSlice = (float) (yClient / ((genericAlg.getBestFitness() + 1) * 2));

		float x = 0;

		oldPen = g2.getColor();
		g2.setColor(redPen);

		if (bestFitness.size() == 0 || avgFitness.size() == 0) {
			return;
		}
		double firstX = 0;
		double firstY = yClient;
		double secondX = x;
		double secondY = yClient - vSlice * bestFitness.get(0);
		Shape line = new Line2D.Double(firstX, firstY, secondX, secondY);
		g2.draw(line);
		for (int vert = 1; vert < bestFitness.size(); vert++) {
			firstX = secondX;
			firstY = secondY;

			secondX = x;
			secondY = yClient - vSlice * bestFitness.get(vert);
			line = new Line2D.Double(firstX, firstY, secondX, secondY);
			g2.draw(line);

			x += hSlice;
		}

		g2.setColor(bluePen);
		x = 0;
		firstX = 0;
		firstY = yClient;
		secondX = x;
		secondY = yClient - vSlice * avgFitness.get(0);
		line = new Line2D.Double(firstX, firstY, secondX, secondY);
		g2.draw(line);
		for (int vert = 1; vert < avgFitness.size(); vert++) {
			firstX = secondX;
			firstY = secondY;

			secondX = x;
			secondY = yClient - vSlice * avgFitness.get(vert);
			line = new Line2D.Double(firstX, firstY, secondX, secondY);
			g2.draw(line);

			x += hSlice;
		}

		g2.setColor(oldPen);
	}
}
