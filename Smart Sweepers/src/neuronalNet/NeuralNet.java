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

		for (int numLayer = 0; numLayer < neuronLayers.size(); numLayer++) {
			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				SNeuron neuron = layer.neurons.get(numNeuron);
				weights.addAll(neuron.inputWeight);
			}
		}
		return weights;
	}

	public void putWeights(List<Double> weights) {
		int cWeight = 0;

		for (int numLayer = 0; numLayer < neuronLayers.size(); numLayer++) {
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

		for (int numLayer = 0; numLayer < neuronLayers.size(); numLayer++) {
			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				SNeuron neuron = layer.neurons.get(numNeuron);
				weights += neuron.numInputs;
			}
		}
		return weights;
	}

	public List<Double> update(List<Double> inputs) {
		List<Double> outputs = new ArrayList<>();

		if (inputs.size() != numInputs) {
			return outputs;
		}

		for (int numLayer = 0; numLayer < neuronLayers.size(); numLayer++) {
			if (numLayer > 0) {
				inputs.clear();
				inputs.addAll(outputs);
			}

			outputs.clear();

			SNeuronLayer layer = neuronLayers.get(numLayer);
			for (int numNeuron = 0; numNeuron < layer.numNeurons; numNeuron++) {
				double netInput = 0;

				SNeuron neuron = layer.neurons.get(numNeuron);
				int numInputs = neuron.numInputs;

				for (int numWeight = 0; numWeight < numInputs - 1; numWeight++) {
					netInput += neuron.inputWeight.get(numWeight) * inputs.get(numWeight);
				}

				netInput += neuron.inputWeight.get(numInputs - 1) * Parameters.dBias;

				outputs.add(sigmoid(netInput, Parameters.dActivationResponse));

			}
		}

		return outputs;
	}

	private double sigmoid(double netInput, double dActivationResponse) {
		return (1 / (1 + Math.exp(-netInput / dActivationResponse)));
	}
}
