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
        val identity = S2DMatrix(_11 = 1.0, _22 = 1.0, _33 = 1.0)
    }

    private var matrix = identity

    fun translate(x: Double, y: Double) {
        matrix *= S2DMatrix(_11 = 1.0, _22 = 1.0, _31 = x, _32 = y, _33 = 1.0)
    }

    fun scale(xScale: Double, yScale: Double) {
        matrix *= S2DMatrix(_11 = xScale, _22 = yScale, _33 = 1.0)
    }

    fun rotate(rot: Double) {
        val sine = sin(rot)
        val cosine = cos(rot)
        matrix *= S2DMatrix(_11 = cosine, _12 = sine, _21 = -sine, _22 = cosine, _33 = 1.0)
    }

    fun adjustPoint(p: Point) = Pair(
        (matrix._11 * p.x) + (matrix._21 * p.y) + (matrix._31),
        (matrix._12 * p.x) + (matrix._22 * p.y) + (matrix._32)
    )

    fun transformPoints(points: List<Point>) = points.map { it * this }

    @Suppress("PropertyName")
    private class S2DMatrix(
        val _11: Double = 0.0, val _12: Double = 0.0, val _13: Double = 0.0,
        val _21: Double = 0.0, val _22: Double = 0.0, val _23: Double = 0.0,
        val _31: Double = 0.0, val _32: Double = 0.0, val _33: Double = 0.0
    ) {
        private val show by lazy { "$_11  $_12  $_13\n$_21  $_22  $_23\n$_31  $_32  $_33\n" }
        override fun toString() = show

        operator fun times(o: S2DMatrix) = S2DMatrix(
            _11 = (_11 * o._11) + (_12 * o._21) + (_13 * o._31),
            _12 = (_11 * o._12) + (_12 * o._22) + (_13 * o._32),
            _13 = (_11 * o._13) + (_12 * o._23) + (_13 * o._33),

            _21 = (_21 * o._11) + (_22 * o._21) + (_23 * o._31),
            _22 = (_21 * o._12) + (_22 * o._22) + (_23 * o._32),
            _23 = (_21 * o._13) + (_22 * o._23) + (_23 * o._33),

            _31 = (_31 * o._11) + (_32 * o._21) + (_33 * o._31),
            _32 = (_31 * o._12) + (_32 * o._22) + (_33 * o._32),
            _33 = (_31 * o._13) + (_32 * o._23) + (_33 * o._33)
        )
    }
}
