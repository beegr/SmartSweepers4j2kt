class Timer(fps: Float) {
    init {
        require(fps > 0.0) { "FPS must be positive" }
    }

    private var pausedAt: Long? = null
    val paused: Boolean
        get() = pausedAt != null

    private var currentTime = 0L
    private var lastTime = 0L
    private var nextTime = 0L
    private val frameTime = ((1 / fps) * 1_000_000_000).toLong()
    private var timeElapsed = 0.0

    private fun resetLastAndNextTimesFrom(newLast: Long) {
        lastTime = newLast
        nextTime = newLast + frameTime
    }

    fun start() {
        pausedAt = null
        resetLastAndNextTimesFrom(System.nanoTime())
    }

    fun togglePause() {
        val timePaused = pausedAt?.let { currentTime - it }
        pausedAt =
            if (timePaused == null)
                currentTime
            else
                null.also { nextTime += timePaused }
    }

    fun readyForNextFrame(): Boolean {
        val now = System.nanoTime().also { currentTime = it }
        return (!paused && now > nextTime).also { passedNext ->
            if (passedNext) {
                timeElapsed = (now - lastTime).toDouble()
                resetLastAndNextTimesFrom(now)
            }
        }
    }
}
