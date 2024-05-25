import kotlin.random.*

@Suppress("ClassName")
object rand {
    fun randomFloat() = Random.Default.nextDouble()
    fun randomRadian() = randomFloat() * Math.PI * 2
    fun randomFloat(lower: Double, upper: Double) = Random.Default.nextDouble(lower, upper)

    /** *triangular* (__not__ linear) random number between -1.0 and 1.0, peaking at 0.0 */
    fun randomClamped() = randomFloat() - randomFloat()
    fun randomInt(inclusiveMin: Int, inclusiveMax: Int) = Random.Default.nextInt(inclusiveMin, inclusiveMax + 1)
    fun randomBoolean() = Random.Default.nextBoolean()
}
