package neuronalNet

import configuration.*
import generics.*
import utils.*
import kotlin.math.*

// we need an additional weight for the (neural-net) bias hence the +1
@Suppress("NOTHING_TO_INLINE")
private inline fun Size.bias(): Size = this + 1

private class Neuron(nominalInputSize: Size) {
    val inputSize: Size = nominalInputSize.bias()

    // initial values for weights are triangular random numbers (between -1 and 1) peaking at zero.
    val weights = MutableList(inputSize) { rand.randomClamped() }
}

private class Layer(layerSize: Size, inputsPerNeuron: Size) {
    val neurons = List(layerSize) { Neuron(inputsPerNeuron) }
}

private fun sigmoid(rawOutput: Double, dActivationResponse: Double) =
    1 / (1 + exp(-rawOutput / dActivationResponse))

class NeuralNet {
    companion object {
        // all the parameters below are determined lazily, so that they can be read into
        // Parameters before any NeuralNet instance is created.
        private val numInputs: Size by lazy {
            Parameters.iNumInputs.also { require(it > 0) { "inputs: $it, must be positive" } }
        }
        private val numOutputs: Size by lazy {
            Parameters.iNumOutputs.also { require(it > 0) { "outputs: $it, must be positive" } }
        }
        private val numHiddenLayers: Size by lazy {
            Parameters.iNumHidden.also { require(it >= 0) { "hidden layers: $it, must not be negative" } }
        }
        private val numNeuronsPerHiddenLayer: Size by lazy {
            Parameters.iNeuronsPerHiddenLayer.also { require(numHiddenLayers == 0 || it > 0) { "neurons per hidden layer: $it, must be positive when using hidden layers" } }
        }

        @JvmStatic
        val numberOfWeights by lazy {
            if (numHiddenLayers == 0) numOutputs * numInputs.bias()
            else numNeuronsPerHiddenLayer * numInputs.bias() +
                    (numHiddenLayers - 1) * numNeuronsPerHiddenLayer * numNeuronsPerHiddenLayer.bias() +
                    numOutputs * numNeuronsPerHiddenLayer.bias()
        }
    }

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

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getWeights() = layers.flatMap { l -> l.neurons.flatMap { n -> n.weights } }

    fun putWeights(weightByAbsoluteIndex: ReadWeight) {
        var weightAbsoluteIndex = 0
        fun nextWeight() = weightByAbsoluteIndex(weightAbsoluteIndex)
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
