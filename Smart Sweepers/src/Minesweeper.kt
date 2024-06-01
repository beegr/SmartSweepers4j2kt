import rand.randomFloat
import kotlin.math.*

class Minesweeper {
    private val itsBrain = NeuralNet()
    private lateinit var position: SVector2D
    private val lookAt = SVector2D(0.0, 0.0)

    private var rotation = 0.0
    private var speed = 0.0
    private var lTrack = 0.16
    private var rTrack = 0.16
    var fitness = 0
        private set
    private val scale = Parameters.iSweeperScale.toDouble()

    private var closestMine: Index = 0

    init {
        reset()
    }

    fun reset() {
        fitness = 0
        rotation = rand.randomRadian()
        position = SVector2D(randomFloat() * Parameters.WindowWidth, randomFloat() * Parameters.WindowHeight)
    }

    fun worldTransformMatrix() =
        with(Matrix()) {
            scale(scale, scale)
            rotate(rotation)
            translate(position.x, position.y)
            this
        }

    fun update(mines: List<SVector2D>) {
        // NOTE: while the sweepers will wrap around to keep them in the testing ground should
        // they leave, the mines are not 'seen' wrapped around.  There's only enough Portal Guns
        // available for us to keep the sweepers from getting trapped in corners, etc., and we
        // got them in a ding-and-dent sale from somebody called CJ.

        val (between, found) = position.vectorToClosestOf(mines)
        closestMine = found
        val towardClosestMine = between.normalize()

        val output = itsBrain.update(listOf(towardClosestMine.x, towardClosestMine.y, lookAt.x, lookAt.y))

        lTrack = output[0]; rTrack = output[1]

        rotation += (lTrack - rTrack).boundedBy(-Parameters.dMaxTurnRate, Parameters.dMaxTurnRate)

        speed = lTrack + rTrack

        lookAt.x = -sin(rotation); lookAt.y = cos(rotation)

        position += lookAt * speed

        with(position) {
            x = x.wrappedTo(0.0, Parameters.WindowWidth.toDouble())
            y = y.wrappedTo(0.0, Parameters.WindowHeight.toDouble())
        }
    }

    fun checkForMine(mines: List<SVector2D>, size: Double) =
        if (position.closeEnoughForJazz(mines[closestMine], size)) closestMine
        else -1

    fun incrementFitness() {
        ++fitness
    }

    fun putWeights(fx: ReadWeight) =
        itsBrain.putWeights(fx)

    private companion object {
        /** if outside of bounds, returns nearest */
        fun <T : Comparable<T>> T.boundedBy(lower: T, upper: T) =
            if (this < lower) lower
            else if (this > upper) upper
            else this

        /** if outside of bounds, returns furthest */
        fun <T : Comparable<T>> T.wrappedTo(lower: T, upper: T) =
            if (this < lower) upper
            else if (this > upper) lower
            else this

        /** determines the nearest place in list, returns a vector *to* it, and its list-index */
        fun SVector2D.vectorToClosestOf(places: List<SVector2D>): Pair<SVector2D, Index> =
            places
                .mapIndexed { i, place ->
                    val vectorBetween = this - place
                    val distanceBetween = vectorBetween.length()
                    Triple(distanceBetween, vectorBetween, i)
                }
                .minBy { it.first }
                .let { it.second to it.third }

        /** So if you are "close enough for Jazz" are you thus "On the Jazz"? */
        fun SVector2D.closeEnoughForJazz(place: SVector2D, size: Double) =
            (this - place).length() < size + 5
    }
}
