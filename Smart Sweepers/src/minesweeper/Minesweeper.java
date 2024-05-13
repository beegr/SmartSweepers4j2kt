package minesweeper;

import java.util.ArrayList;
import java.util.List;

import neuronalNet.NeuralNet;
import utils.C2DMatrix;
import utils.rand;
import utils.SPoint;
import utils.SVector2D;
import configuration.Parameters;

public class Minesweeper {

	private NeuralNet itsBrain;
	private SVector2D position;
	private SVector2D lookAt;

	private double rotation;
	private double speed;
	private double lTrack;
	private double rTrack;
	private double fitness;
	private double scale;

	int closestMine;

	public Minesweeper() {
		lTrack = 0.16;
		rTrack = 0.16;
		scale = Parameters.iSweeperScale;
		closestMine = 0;
		itsBrain = new NeuralNet();

		lookAt = new SVector2D(0, 0);

		reset();
	}

	public void reset() {
		fitness = 0;
		rotation = rand.randomFloat() * Math.PI * 2;
		position = new SVector2D(rand.randomFloat() * Parameters.WindowWidth, rand.randomFloat() * Parameters.WindowHeight);
	}

	public void worldTransform(List<SPoint> sweeper) {
		C2DMatrix matTransform = new C2DMatrix();

		matTransform.scale(scale, scale);

		matTransform.rotate(rotation);

		matTransform.translate(position.getX(), position.getY());

		matTransform.transformSPoints(sweeper);
	}

	public boolean update(List<SVector2D> mines) {
		List<Double> inputs = new ArrayList<>();

		SVector2D closestMine = getClosestMine(mines);

		closestMine = closestMine.normalize();

		inputs.add(closestMine.getX());
		inputs.add(closestMine.getY());

		inputs.add(lookAt.getX());
		inputs.add(lookAt.getY());

		List<Double> output = itsBrain.update(inputs);

		if (output.size() < Parameters.iNumOutputs) {
			return false;
		}

		lTrack = output.get(0);
		rTrack = output.get(1);

		double rotForce = lTrack - rTrack;

		rotForce = clamp(rotForce, -Parameters.dMaxTurnRate, Parameters.dMaxTurnRate);

		rotation += rotForce;

		speed = lTrack + rTrack;

		lookAt.setX(-Math.sin(rotation));
		lookAt.setY(Math.cos(rotation));

		position.plusAssign(lookAt.times(speed));

		if (position.getX() > Parameters.WindowWidth)
			position.setX(0);
		if (position.getX() < 0)
			position.setX(Parameters.WindowWidth);
		if (position.getY() > Parameters.WindowHeight)
			position.setY(0);
		if (position.getY() < 0)
			position.setY(Parameters.WindowHeight);

		return true;
	}

	private SVector2D getClosestMine(List<SVector2D> mines) {
		double closestSoFar = Double.MAX_VALUE;
		SVector2D closestObect = new SVector2D(0, 0);

		for (int i = 0; i < mines.size(); i++) {
			SVector2D currentMine = mines.get(i);
			double lenToOjbect = currentMine.minus(position).length();

			if (lenToOjbect < closestSoFar) {
				closestSoFar = lenToOjbect;
				closestObect = position.minus(currentMine);
				closestMine = i;
			}
		}
		return closestObect;
	}

	public int checkForMine(List<SVector2D> mines, double size) {
		SVector2D distanceToObject = position.minus(mines.get(closestMine));
		if (distanceToObject.length() < (size + 5)) {
			return closestMine;
		}

		return -1;
	}

	public SVector2D getPosition() {
		return position;
	}

	public void incrementFitness() {
		++fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public void putWeights(List<Double> w) {
		itsBrain.putWeights(w);
	}

	public int getNumberOfWeights() {
		return itsBrain.getNumberOfWeights();
	}

	private double clamp(double arg, double min, double max) {
		if (arg < min) {
			return min;
		}
		if (arg > max) {
			return max;
		}
		return arg;
	}

}
