package utils;

public class Timer {

	long currentTime;
	long lastTime;
	long nextTime;
	long frameTime;

	double timeElapsed;

	float fps;

	public Timer(float fps) {
		if (fps == 0) {
			throw new IllegalArgumentException("FPS of 0 is not allowed");
		}
		timeElapsed = 0;
		lastTime = 0;
		this.fps = fps;

		frameTime = (long) ((1 / fps) * 1_000_000_000);
	}

	public void start() {
		lastTime = System.nanoTime();

		nextTime = lastTime + frameTime;
	}

	public boolean readyForNextFrame() {
		currentTime = System.nanoTime();

		if (currentTime > nextTime) {
			timeElapsed = (currentTime - lastTime);
			lastTime = currentTime;

			nextTime = currentTime + frameTime;
			return true;
		}
		return false;
	}

	public double timeElapsed() {
		currentTime = System.nanoTime();

		timeElapsed = (currentTime - lastTime);
		lastTime = currentTime;

		return timeElapsed;
	}

	public double getTimeElapsed() {
		return timeElapsed;
	}
}
