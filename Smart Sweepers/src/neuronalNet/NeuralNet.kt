package neuronalNet

import configuration.*
import utils.*
import kotlin.math.exp

private class Neuron(nominalInputSize: Int) {
    // we need an additional weight for the (neural-net) bias hence the nominal+1
    val inputSize = nominalInputSize + 1

    // initial values for weights are triangular random numbers (between -1 and 1) peaking at zero.
    val weights = MutableList(inputSize) { rand.randomClamped() }
}

private class Layer(layerSize: Int, inputsPerNeuron: Int) {
    val neurons = List(layerSize) { Neuron(inputsPerNeuron) }
}

private fun sigmoid(rawOutput: Double, dActivationResponse: Double) =
    1 / (1 + exp(-rawOutput / dActivationResponse))

class NeuralNet {
    private val numInputs = Parameters.iNumInputs
    private val numOutputs = Parameters.iNumOutputs
    private val numHiddenLayers = Parameters.iNumHidden
    private val numNeuronsPerHiddenLayer = Parameters.iNeuronsPerHiddenLayer

    private val layers = sequence {
        // with no hidden layers ...
        if (numHiddenLayers <= 0) {
            // ... there's only a single layer, so it has to directly convert to outputs from inputs
            yield(Layer(numOutputs, numInputs))
        } else {
            // with hidden layers, the first hidden (possibly only) must convert to the hidden from inputs ...
            yield(Layer(numNeuronsPerHiddenLayer, numInputs))
            // ... then any other hidden layers keep converting hidden from hidden ...
            repeat(numHiddenLayers - 1) { yield(Layer(numNeuronsPerHiddenLayer, numNeuronsPerHiddenLayer)) }
            // ... with the last layer converting to outputs from hidden
            yield(Layer(numOutputs, numNeuronsPerHiddenLayer))
        }
    }.toList()

    val numberOfWeights by lazy { layers.flatMap { l -> l.neurons.map { n -> n.weights.size } }.sum() }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getWeights() = layers.flatMap { l -> l.neurons.flatMap { n -> n.weights } }

    fun putWeights(allWeightsForNetFlattened: List<Double>) {
        var weightAbsoluteIndex = 0
        fun nextWeight() = allWeightsForNetFlattened[weightAbsoluteIndex]
            .also { weightAbsoluteIndex += 1 }

        layers.forEach { l ->
            l.neurons.forEach { n ->
                for (which in n.weights.indices) {
                    n.weights[which] = nextWeight()
                }
            }
        }
    }

    fun update(inputs: List<Double>) =
        layers.fold(inputs) { passed, l ->
            l.neurons.map { n ->
                n.weights.zip(passed.plus(Parameters.dBias))
                    .fold(0.0) { acc, weightInputPair -> acc + weightInputPair.first * weightInputPair.second }
                    .let { sigmoid(it, Parameters.dActivationResponse) }
            }
        }
}
