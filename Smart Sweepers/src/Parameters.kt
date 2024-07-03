import java.io.*

class Parameters private constructor(
    val iNumHidden: Int,
    val iNeuronsPerHiddenLayer: Int,
    val iNumElite: Int,
    val iNumCopiesElite: Int,
    val iNumSweepers: Int,
    val dCrossoverRate: Double,
    val dMutationRate: Double,
    val dMaxPerturbation: Double,
    val iWindowWidth: Int,
    val iWindowHeight: Int,
    val iSweeperScale: Int,
    val dMaxTurnRate: Double,
    val iSpeedScale: Int,
    val iNumMines: Int,
    val dMineScale: Double,
    val iNumTicks: Int,
    val iFramesPerSecond: Int
) {

    companion object {
        lateinit var parameters: Parameters
        private val integer: Typed<Int> = "integer" to String::toIntOrNull
        private val floating: Typed<Double> = "float" to String::toDoubleOrNull
        private val positive: Passed<Int> = "must be positive" to { it > 0 }
        private val pos: Passed<Double> = "must be positive" to { it > 0.0 }
        private val nonNegative: Passed<Int> = "must not be negative" to { it >= 0 }
        private val nonNeg: Passed<Double> = "must not be negative" to { it >= 0.0 }
        private val inZeroOne: Passed<Double> = "must be between (inclusive): zero and one" to { it in 0.0..1.0 }

        fun loadInParameters(inputStream: InputStream) {
            val settings =
                inputStream.bufferedReader().readLines().mapNotNull { line ->
                    val splitBySpace = line.trim().split(' ')
                    if (splitBySpace.size == 2) Pair(splitBySpace[0], splitBySpace[1])
                    else null
                }.toMap()

            fun <T> String.checkFor(t: Typed<T>, p: Passed<T>): T {
                val unparsed = requireNotNull(settings[this]) { "$this: not found" }
                val untested = requireNotNull(t.second(unparsed)) { "$this: could not be parsed to ${t.first}" }
                require(p.second(untested)) { "$this: ${p.first}" }
                return untested
            }

            val noHidden: Boolean
            val elites: Int
            val copies: Int
            parameters = Parameters(
                // NeuralNet parameters ---------------------------------------------------------
                // are there any extra (hidden) layers?
                "iNumHidden".checkFor(integer, nonNegative).also { noHidden = it == 0 },
                // and if so, how many neurons do each of those have?
                "iNeuronsPerHiddenLayer".checkFor(integer, "must be positive if any hidden" to { noHidden || it > 0 }),
                // Genetic Algorithm parameters ---------------------------------------------------------
                // the number of fittest members that move on to the next generation unchanged.
                "iNumElite".checkFor(integer, nonNegative).also { elites = it },
                // how many copies of each elite are part of the next generation.
                "iNumCopiesElite".checkFor(integer, positive).also { copies = it },
                // population size (needs to be an even number after removing any copied elites from above)
                "iNumSweepers".checkFor(integer, "must be bigger than elites*copies by an even number" to {
                    val rm = it - elites * copies; rm >= 2 && rm % 2 == 0
                }),
                // of the genetic pairings made, what rate (1.0 = 100%) actually produce children with
                // mixed genes (i.e. weights in the neural nets) from parents
                "dCrossoverRate".checkFor(floating, inZeroOne),
                // per child-gene, what chance (1.0 = 100%) is there of it changing from inherited ...
                "dMutationRate".checkFor(floating, inZeroOne),
                // ... and by how much
                "dMaxPerturbation".checkFor(floating, nonNeg),
                // Testing Ground parameters ---------------------------------------------------------
                // size of testing ground
                "iWindowWidth".checkFor(integer, positive),
                "iWindowHeight".checkFor(integer, positive),
                // size of a sweeper within testing ground
                "iSweeperScale".checkFor(integer, positive),
                // at most, how fast can it turn
                "dMaxTurnRate".checkFor(floating, pos),
                // helpful if testing ground gets bigger
                "iSpeedScale".checkFor(integer, positive),
                // the mines to be swept
                "iNumMines".checkFor(integer, positive),
                // how big they are
                "dMineScale".checkFor(floating, pos),
                // more significant than for just animation, the product of these two numbers is how
                // many "moves" a sweeper gets per generation to get to and sweep mines
                "iNumTicks".checkFor(integer, positive), // is actually seconds, not 'ticks'
                "iFramesPerSecond".checkFor(integer, positive)
            )
        }
    }
}

typealias Typed<T> = Pair<String, (String) -> T?>
typealias Passed<T> = Pair<String, (T) -> Boolean>
