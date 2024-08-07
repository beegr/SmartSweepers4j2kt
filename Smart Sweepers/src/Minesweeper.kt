import Parameters.Companion.parameters

class Minesweeper(private val fxPosition: () -> Point, val fxWeights: () -> Weights) {

    private lateinit var position: Point
    private lateinit var lookAt: Point

    private var rotation = 0.0
    var fitness = 0
        private set

    private var closestMine: Index = 0

    init {
        reset()
    }

    fun reset() {
        fitness = 0
        rotation = rand.randomRadian()
        lookAt = rotationToPoint(rotation)
        position = fxPosition()
    }

    fun worldTransformMatrix() =
        Matrix.startWith.rotation(rotation).thenTranslate(position.x, position.y)

    fun update(mines: List<Point>) {
        // NOTE: while the sweepers will wrap around to keep them in the testing ground should
        // they leave, the mines are not 'seen' wrapped around.  There's only enough Portal Guns
        // available for us to keep the sweepers from getting trapped in corners, etc., and we
        // got them in a ding-and-dent sale from somebody called CJ.

        val (between, found) = position.vectorToClosestOf(mines)
        closestMine = found
        val towardClosestMine = between.normalize()

        val output =
            fxWeights()
                .let { neuralNetMaker.toNetCompute(it) }
                .let { it(listOf(towardClosestMine.x, towardClosestMine.y, lookAt.x, lookAt.y)) }

        val lTrack = output[0]
        val rTrack = output[1]

        rotation += (lTrack - rTrack).boundedBy(-parameters.dMaxTurnRate, parameters.dMaxTurnRate)

        val speed = (lTrack + rTrack) * parameters.iSpeedScale

        lookAt = rotationToPoint(rotation)

        val (xx, yy) = position + lookAt * speed
        position = Point(
            xx.wrappedTo(0.0, parameters.iWindowWidth.toDouble()),
            yy.wrappedTo(0.0, parameters.iWindowHeight.toDouble())
        )
    }

    fun checkForMine(mines: List<Point>, howClose: Double) =
        if (position.closeEnoughForJazz(mines[closestMine], howClose)) closestMine
        else -1

    fun incrementFitness() {
        ++fitness
    }

    companion object {
        val neuralNetMaker by lazy {
            NeuralNet(
                4, // towardClosestMine (x,y) and lookAt (x,y)
                parameters.iNumHidden,
                parameters.iNeuronsPerHiddenLayer,
                2 // track-power (left, right)
                // we'll use the default sigmoid function
            )
        }

        /** if outside of bounds, returns nearest */
        private fun <T : Comparable<T>> T.boundedBy(lower: T, upper: T) =
            if (this < lower) lower
            else if (this > upper) upper
            else this

        /** if outside of bounds, returns furthest */
        private fun <T : Comparable<T>> T.wrappedTo(lower: T, upper: T) =
            if (this < lower) upper
            else if (this > upper) lower
            else this

        /** determines the nearest place in list, returns a vector *to* it, and its list-index */
        private fun Point.vectorToClosestOf(places: List<Point>): Pair<Point, Index> =
            places
                .mapIndexed { i, place ->
                    val vectorBetween = this - place
                    val distanceBetween = vectorBetween.length()
                    Triple(distanceBetween, vectorBetween, i)
                }
                .minBy { it.first }
                .let { it.second to it.third }

        /** So if you are "close enough for Jazz" are you thus "On the Jazz"? */
        private fun Point.closeEnoughForJazz(place: Point, howClose: Double) =
            (this - place).length() <= howClose
    }
}
