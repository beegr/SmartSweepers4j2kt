@file:Suppress("ConstPropertyName")

import Parameters.Companion.parameters
import kotlin.math.*

class NeuralNet {
    companion object {
        private const val numInputs: Size = 4
        private const val numOutputs: Size = 2
        // all the parameters below are determined lazily, so that they can be read into
        // Parameters before any NeuralNet instance is created.
        private val numHiddenLayers: Size by lazy { parameters.iNumHidden }
        private val numNeuronsPerHiddenLayer: Size by lazy { parameters.iNeuronsPerHiddenLayer }

        fun sigmoid(rawOutput: Double) = 1 / (1 + exp(-rawOutput))

        // we need an additional weight for the (neural-net) bias hence the +1
        @Suppress("NOTHING_TO_INLINE")
        private inline fun Size.bias(): Size = this + 1

        private val mapping by lazy {
            sequence {
                var weightAbsoluteIndex = 0
                fun nextWeightRange(count: Int) =
                    (weightAbsoluteIndex..<weightAbsoluteIndex + count)
                        .also { weightAbsoluteIndex += count }

                if (numHiddenLayers == 0) {
                    yield((1..numOutputs).map { nextWeightRange(numInputs.bias()) }.toList())
                } else {
                    yield((1..numNeuronsPerHiddenLayer).map { nextWeightRange(numInputs.bias()) }.toList())
                    for (layer in 2..numHiddenLayers)
                        yield((1..numNeuronsPerHiddenLayer).map { nextWeightRange(numNeuronsPerHiddenLayer.bias()) }
                            .toList())
                    yield((1..numOutputs).map { nextWeightRange(numNeuronsPerHiddenLayer.bias()) }.toList())
                }
            }.toList()
        }

        val numberOfWeights by lazy { mapping.last().last().last + 1 }

        fun DoubleArray.asLayers() = asList().let { wholeSpan ->
            mapping.map { layer ->
                layer.map { neuron ->
                    wholeSpan.subList(neuron.first, neuron.last)
                }
            }
        }
    }

    private val dxWeights = DoubleArray(numberOfWeights) { rand.randomClamped() }
    private val layers = dxWeights.asLayers()

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getWeights() = dxWeights.asList()

    fun putWeights(weightByAbsoluteIndex: ReadWeight) {
        dxWeights.indices.forEach { dxWeights[it] = weightByAbsoluteIndex(it) }
    }

    fun update(inputs: List<Double>) =
        layers.fold(inputs) { passed, layer ->
            layer.map { neuron ->
                neuron.zip(passed.plus(/* bias */ -1.0))
                    .fold(0.0) { acc, weightInputPair -> acc + weightInputPair.first * weightInputPair.second }
                    .let { sigmoid(it) }
            }
        }
}
