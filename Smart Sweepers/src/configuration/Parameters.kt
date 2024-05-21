package configuration

import java.io.*

object Parameters {
    // NeuralNet parameters ---------------------------------------------------------
    var iNumInputs = 4 // how many pieces of (orthogonal?) information the net has to 'think on' ...
        private set
    var iNumOutputs = 2 // .. to produce this many data points to 'act on'.
        private set
    // ^^^ ︎THOUGHT: I mean, ideally, the 'owning' algorithm that provided and used
    // the above would know this itself to request the NeuralNet I/O it needed.

    // each input has its own weight, and there's usually another amount, a bias or
    // threshold to be subtracted, so its multiple should always be -1 and not really
    // configurable
    var dBias = -1.0
        private set
    var iNumHidden = 0 // are there any extra (hidden) layers?
        private set
    var iNeuronsPerHiddenLayer = 0 // and if so, how many neurons do each of those have?
        private set
    var dActivationResponse = 1.0 // lastly, a parameter for the activation function, but normally always 1
        private set

    // Genetic Algorithm parameters ---------------------------------------------------------
    var iNumSweepers = 6 // population size (needs to be an even number after removing the copied elites below)
        private set
    var iNumElite = 0 // the number of fittest members that move on to the next generation unchanged.
        private set
    var iNumCopiesElite = 1 // how many copies of each elite are part of the next generation.
        private set

    // of the genetic pairings made, what rate (1.0 = 100%) actually produce children with
    // mixed genes (i.e. weights in the neural nets) from parents
    var dCrossoverRate = 0.0
        private set
    var dMutationRate = 0.0 // per child's gene, what chance (1.0 = 100%) is there of it changing from inherited ...
        private set
    var dMaxPerturbation = 0.0 // ... and by how much
        private set

    // Testing Ground parameters ---------------------------------------------------------
    // size of testing ground
    var WindowWidth = 400
        private set
    var WindowHeight = 400
        private set
    var iSweeperScale = 5 // size of a sweeper within testing ground
        private set
    var dMaxTurnRate = 0.3 // at most, how fast can it turn
        private set
    var dMaxSpeed = 0.0 // IGNORED currently; at most, how fast can it move
        private set

    var iNumMines = 0 // the mines to be swept
        private set
    var dMineScale = 2.0 // how big they are
        private set

    // more significant than for just animation, the product of these two numbers is how
    // many "moves" a sweeper gets per generation to get to and sweep mines
    var iNumTicks = 1_800 // is actually seconds, not 'ticks'
        private set
    var iFramesPerSecond = 60
        private set

    fun loadInParameters(inputStream: InputStream) {
        val settings =
            inputStream.bufferedReader().readLines().mapNotNull { line ->
                val splitBySpace = line.trim().split(' ')
                if (splitBySpace.size == 2) Pair(splitBySpace[0], splitBySpace[1])
                else null
            }.toMap()

        fun <T> findAndParse(n: String, t: String, fxParse: (String) -> T?): T =
            fxParse(
                settings[n] ?: throw IllegalStateException("setting '$n' not found")
            ) ?: throw IllegalStateException("setting '$n' could not be parsed to: $t")

        fun String.getInt() = findAndParse(this, "integer") { it.toIntOrNull() }
        fun String.getDouble() = findAndParse(this, "float") { it.toDoubleOrNull() }

        iFramesPerSecond = "iFramesPerSecond".getInt()
        iNumInputs = "iNumInputs".getInt()
        iNumHidden = "iNumHidden".getInt()
        iNeuronsPerHiddenLayer = "iNeuronsPerHiddenLayer".getInt()
        iNumOutputs = "iNumOutputs".getInt()
        dActivationResponse = "dActivationResponse".getDouble()
        dBias = "dBias".getDouble()
        dMaxTurnRate = "dMaxTurnRate".getDouble()
        dMaxSpeed = "dMaxSpeed".getDouble()
        iSweeperScale = "iSweeperScale".getInt()
        iNumMines = "iNumMines".getInt()
        iNumSweepers = "iNumSweepers".getInt()
        iNumTicks = "iNumTicks".getInt()
        dMineScale = "dMineScale".getDouble()
        dCrossoverRate = "dCrossoverRate".getDouble()
        dMutationRate = "dMutationRate".getDouble()
        dMaxPerturbation = "dMaxPerturbation".getDouble()
        iNumElite = "iNumElite".getInt()
        iNumCopiesElite = "iNumCopiesElite".getInt()
    }
}
