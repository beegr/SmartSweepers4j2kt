import kotlin.math.*

class SVector2D(var x: Double, var y: Double) {

    operator fun plusAssign(other: SVector2D) {
        x += other.x; y += other.y
    }

    operator fun minus(other: SVector2D) =
        SVector2D(x - other.x, y - other.y)

    operator fun times(scalar: Double) =
        SVector2D(x * scalar, y * scalar)

    fun length() =
        sqrt(x * x + y * y)

    fun normalize() =
        length().let { SVector2D(x / it, y / it) }
}
