@file:Suppress("ConstPropertyName")

import kotlin.math.*

class NeuralNet(
    numInputs: Size,
    numHiddenLayers: Size,
    numNeuronsPerHiddenLayer: Size,
    numOutputs: Size,
    val sigmoid: (Double) -> Double = { 1 / (1 + exp(-it)) }
) {
    // we need an additional weight for the (neural-net) bias hence the +1
    @Suppress("NOTHING_TO_INLINE")
    private inline fun Size.bias(): Size = this + 1

    private val mapping =
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

    val numberOfWeights = mapping.last().last().last + 1

    fun toNetCompute(dxWeights: Weights): (List<Weight>) -> List<Weight> {
        val layers = dxWeights.asList().let { wholeSpan ->
            mapping.map { layer ->
                layer.map { neuron ->
                    wholeSpan.subList(neuron.first, neuron.last)
                }
            }
        }
        return { inputs: List<Double> ->
            layers.fold(inputs) { passed, layer ->
                layer.map { neuron ->
                    neuron.zip(passed.plus(/* bias */ -1.0))
                        .fold(0.0) { acc, weightInputPair -> acc + weightInputPair.first * weightInputPair.second }
                        .let { sigmoid(it) }
                }
            }
        }
    }
}
