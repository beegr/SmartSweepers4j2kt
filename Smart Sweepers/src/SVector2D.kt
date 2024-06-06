import java.lang.Double.*
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

    private fun mbSqrt(d: Double): Double {
        val sqrt = longBitsToDouble(((doubleToLongBits(d) - (1L shl 52)) shr 1) + (1L shl 61))
        return (sqrt + d / sqrt) / 2.0
    }

    fun distance(other: SVector2D): Double {
        val x = this.x - other.x
        val y = this.y - other.y
        return mbSqrt(x * x + y * y)
    }
}
