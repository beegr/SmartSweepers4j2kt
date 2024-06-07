import Matrix.*
import kotlin.math.*

/** Rather than creating another type for Point, we'll just use an alias to an immutable pair of doubles */
typealias Point = Pair<Double, Double>

// But as it is still nice to refer to x,y for Points, we'll create some extension properties to provide them
val Point.x: Double
    inline get() = this.first
val Point.y: Double
    inline get() = this.second

operator fun Point.times(matrix: Matrix) = matrix.adjustPoint(this)
operator fun Point.times(scalar: Double) = Point(x * scalar, y * scalar)
operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)
operator fun Point.minus(other: Point) = Point(x - other.x, y - other.y)
fun Point.length() = sqrt(x * x + y * y)
fun Point.normalize() = length().let { Point(x / it, y / it) }

/** Turn an angle/rotation, where zero is up, into coordinates on a unit-circle.  */
fun rotationToPoint(angleFromUp: Double) = Point(-sin(angleFromUp), cos(angleFromUp))

@Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")
/**
 * Start with a Matrix created by [startWith.translation], [startWith.scaling], or [startWith.rotation], then continue with use of
 * [thenTranslate], [thenScale], and [thenRotate], as desired until the Matrix contains all transformations needed.
 */
class Matrix private constructor() {
    // @formatter:off
    private val mm = DoubleArray(9)
    // one-based indexing [1..3, 1..3] to matrix values
    private inline fun i(r: Int, c: Int) = (r - 1) * 3 + (c - 1)
    private inline fun rc(i: Int) = Pair((i / 3) + 1, (i % 3) + 1)
    private inline operator fun get(r: Int, c: Int) = mm[i(r, c)]
    private inline operator fun set(r: Int, c: Int, v: Double) { mm[i(r, c)] = v }
    // @formatter:on

    @Suppress("ClassName")
    object startWith {
        fun translation(x: Double, y: Double) = Matrix().apply {
            this[1, 1] = 1.0
            this[2, 2] = 1.0
            this[3, 1] = x
            this[3, 2] = y
            this[3, 3] = 1.0
        }

        fun scaling(xScale: Double, yScale: Double) = Matrix().apply {
            this[1, 1] = xScale
            this[2, 2] = yScale
            this[3, 3] = 1.0
        }

        fun rotation(rot: Double) = Matrix().apply {
            val sine = sin(rot)
            val cosine = cos(rot)
            this[1, 1] = cosine
            this[1, 2] = sine
            this[2, 1] = -sine
            this[2, 2] = cosine
            this[3, 3] = 1.0
        }
    }

    fun thenTranslate(x: Double, y: Double) = this.also { timesAssign(startWith.translation(x, y)) }
    fun thenScale(xScale: Double, yScale: Double) = this.also { timesAssign(startWith.scaling(xScale, yScale)) }
    fun thenRotate(rot: Double) = this.also { timesAssign(startWith.rotation(rot)) }

    fun adjustPoint(p: Point) = Pair(
        (this[1, 1] * p.x) + (this[2, 1] * p.y) + (this[3, 1]),
        (this[1, 2] * p.x) + (this[2, 2] * p.y) + (this[3, 2])
    )

    fun transformPoints(points: Iterable<Point>) = points.map { it * this }

    override fun toString() =
        "${this[1, 1]}  ${this[1, 2]}  ${this[1, 3]}\n${this[2, 1]}  ${this[2, 2]}  ${this[2, 3]}\n${this[3, 1]}  ${this[3, 2]}  ${this[3, 3]}\n"

    operator fun timesAssign(b: Matrix) {
        val a = this
        val c = DoubleArray(9) { idx ->
            val (i, j) = rc(idx)
            (1..3).sumOf { k -> a[i, k] * b[k, j] }
        }
        c.copyInto(mm)
    }
}
