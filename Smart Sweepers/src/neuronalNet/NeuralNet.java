package neuronalNet;

import java.util.ArrayList;
import java.util.List;

import configuration.Parameters;

public class NeuralNet {
	private int numInputs;
	private int numOutputs;
	private int numHiddenLayers;
	private int neuronsPerHiddenLayer;

	List<SNeuronLayer> neuronLayers;

	public NeuralNet() {
		numInputs = Parameters.iNumInputs;
		numOutputs = Parameters.iNumOutputs;
		numHiddenLayers = Parameters.iNumHidden;
		neuronsPerHiddenLayer = Parameters.iNeuronsPerHiddenLayer;

		neuronLayers = new ArrayList<>();

		createNet();
	}

	public void createNet() {
		if (numHiddenLayers > 0) {

			// create first hidden layer
			neuronLayers.add(new SNeuronLayer(neuronsPerHiddenLayer, numInputs));

			for (int i = 0; i < numHiddenLayers - 1; i++) {
				neuronLayers.add(new SNeuronLayer(neuronsPerHiddenLayer, neuronsPerHiddenLayer));
			}

			// create output layer
			neuronLayers.add(new SNeuronLayer(numOutputs, neuronsPerHiddenLayer));
		} else {
			neuronLayers.add(new SNeuronLayer(numOutputs, numInputs));
		}
	}

	public List<Double> getWeights() {
		List<Double> weights = new ArrayList<>();

		for (int numLayer = 0; numLayer < numHiddenLayers + 1; numLayer++) {
			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				SNeuron neuron = layer.neurons.get(numNeuron);
				for (int numWeight = 0; numWeight < neuron.numInputs; numWeight++) {
					Double weight = neuron.inputWeight.get(numWeight);
					weights.add(weight);
				}
			}
		}
		return weights;
	}

	public void putWeights(List<Double> weights) {
		int cWeight = 0;

		for (int numLayer = 0; numLayer < numHiddenLayers + 1; numLayer++) {
			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				SNeuron neuron = layer.neurons.get(numNeuron);
				for (int numWeight = 0; numWeight < neuron.numInputs; numWeight++) {
					neuron.inputWeight.set(numWeight, weights.get(cWeight++));
				}
			}
		}
	}

	public int getNumberOfWeights() {
		int weights = 0;

		for (int numLayer = 0; numLayer < numHiddenLayers + 1; numLayer++) {
			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				SNeuron neuron = layer.neurons.get(numNeuron);
				for (int numWeight = 0; numWeight < neuron.numInputs; numWeight++) {
					weights++;
				}
			}
		}
		return weights;
	}

	public List<Double> update(List<Double> inputs) {
		List<Double> outputs = new ArrayList<>();

		int cWeight = 0;

		if (inputs.size() != numInputs) {
			return outputs;
		}

		for (int numLayer = 0; numLayer < numHiddenLayers + 1; numLayer++) {
			if (numLayer > 0) {
				inputs.clear();
				inputs.addAll(outputs);
				// inputs = outputs;
			}

			outputs.clear();
			cWeight = 0;

			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				double netInput = 0;

				SNeuron neuron = layer.neurons.get(numNeuron);
				int numInputs = neuron.numInputs;

				for (int numWeight = 0; numWeight < numInputs - 1; numWeight++) {
					netInput += neuron.inputWeight.get(numWeight) * inputs.get(cWeight++);
				}

				netInput += neuron.inputWeight.get(numInputs - 1) * Parameters.dBias;

				outputs.add(sigmoid(netInput, Parameters.dActivationResponse));

				cWeight = 0;
			}
		}

		return outputs;
	}

	private double sigmoid(double netInput, double dActivationResponse) {
		return (1 / (1 + Math.exp(-netInput / dActivationResponse)));
	}
}
