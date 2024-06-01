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

class Matrix {
    private companion object {
        val identity = S2DMatrix().apply {
            this[1, 1] = 1.0
            this[2, 2] = 1.0
            this[3, 3] = 1.0
        }

        fun translation(x: Double, y: Double) = S2DMatrix().apply {
            this[1, 1] = 1.0
            this[2, 2] = 1.0
            this[3, 1] = x
            this[3, 2] = y
            this[3, 3] = 1.0
        }

        fun scaling(xScale: Double, yScale: Double) = S2DMatrix().apply {
            this[1, 1] = xScale
            this[2, 2] = yScale
            this[3, 3] = 1.0
        }

        fun rotation(rot: Double) = S2DMatrix().apply {
            val sine = sin(rot)
            val cosine = cos(rot)
            this[1, 1] = cosine
            this[1, 2] = sine
            this[2, 1] = -sine
            this[2, 2] = cosine
            this[3, 3] = 1.0
        }
    }

    private var matrix = identity

    fun translate(x: Double, y: Double) {
        matrix *= translation(x, y)
    }

    fun scale(xScale: Double, yScale: Double) {
        matrix *= scaling(xScale, yScale)
    }

    fun rotate(rot: Double) {
        matrix *= rotation(rot)
    }

    fun adjustPoint(p: Point) = Pair(
        (matrix[1, 1] * p.x) + (matrix[2, 1] * p.y) + (matrix[3, 1]),
        (matrix[1, 2] * p.x) + (matrix[2, 2] * p.y) + (matrix[3, 2])
    )

    fun transformPoints(points: List<Point>) = points.map { it * this }

    @Suppress("NOTHING_TO_INLINE")
    private class S2DMatrix {
        // @formatter:off
        private val mm = DoubleArray(9)
        // one-based indexing [1..3, 1..3] to matrix values
        private inline fun i(r: Int, c: Int) = (r - 1) * 3 + (c - 1)
        inline operator fun get(r: Int, c: Int) = mm[i(r, c)]
        inline operator fun set(r: Int, c: Int, v: Double) { mm[i(r, c)] = v }
        // @formatter:on

        override fun toString() =
            "${this[1, 1]}  ${this[1, 2]}  ${this[1, 3]}\n${this[2, 1]}  ${this[2, 2]}  ${this[2, 3]}\n${this[3, 1]}  ${this[3, 2]}  ${this[3, 3]}\n"

        operator fun times(b: S2DMatrix) = S2DMatrix().also { c ->
            val a = this@S2DMatrix
            for (i in 1..3)
                for (j in 1..3)
                    for (k in 1..3)
                        c[i, j] += a[i, k] * b[k, j]
        }
    }
}
