package neuronalNet;

import java.util.ArrayList;
import java.util.List;

import utils.rand;

public class SNeuron {
	int numInputs;
	List<Double> inputWeight;

	public SNeuron(int numInputs) {
		this.numInputs = numInputs + 1;
		inputWeight = new ArrayList<>();

		for (int i = 0; i < this.numInputs; i++) {
			inputWeight.add(rand.randomClamped());
		}
	}
}
