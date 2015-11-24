package neuronalNet;

import java.util.ArrayList;
import java.util.List;

public class SNeuronLayer {
	int numNeurons;

	List<SNeuron> neurons;

	public SNeuronLayer(int numNeurons, int numInputsPerNeuron) {
		this.numNeurons = numNeurons;
		neurons = new ArrayList<>();

		for (int i = 0; i < numNeurons; i++) {
			neurons.add(new SNeuron(numInputsPerNeuron));
		}
	}
}
