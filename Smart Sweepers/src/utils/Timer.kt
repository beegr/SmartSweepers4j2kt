package utils

class Timer(fps: Float) {
    init {
        require(!(fps <= 0.0)) { "FPS of 0 is not allowed" }
    }

    private var currentTime = 0L
    private var lastTime = 0L
    private var nextTime = 0L
    private val frameTime = ((1 / fps) * 1_000_000_000).toLong()
    private var timeElapsed = 0.0

    private fun resetLastAndNextTimesFrom(newLast: Long) {
        lastTime = newLast
        nextTime = newLast + frameTime
    }

    fun start() = resetLastAndNextTimesFrom(System.nanoTime())

    fun readyForNextFrame(): Boolean {
        val now = System.nanoTime().also { currentTime = it }
        return (now > nextTime).also { passedNext ->
            if (passedNext) {
                timeElapsed = (now - lastTime).toDouble()
                resetLastAndNextTimesFrom(now)
            }
        }
    }
}

