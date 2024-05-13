package utils

import kotlin.random.*

@Suppress("ClassName")
object rand {
    @JvmStatic
    fun randomFloat() = Random.Default.nextDouble()

    @JvmStatic
    fun randomRadian() = randomFloat() * Math.PI * 2

    @JvmStatic
    fun randomFloat(lower: Double, upper: Double) = Random.Default.nextDouble(lower, upper)

    /** *triangular* (__not__ linear) random number between -1.0 and 1.0, peaking at 0.0 */
    @JvmStatic
    fun randomClamped() = randomFloat() - randomFloat()

    @JvmStatic
    fun randomInt(inclusiveMin: Int, inclusiveMax: Int) = Random.Default.nextInt(inclusiveMin, inclusiveMax + 1)

    @JvmStatic
    fun randomBoolean() = Random.Default.nextBoolean()
}
