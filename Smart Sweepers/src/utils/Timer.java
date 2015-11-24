package utils;

public class Timer {

	long currentTime;
	long lastTime;
	long nextTime;
	long frameTime;
	long perfCountFrequency;

	double timeElapsed;
	double timeScale;

	float fps;

	public Timer() {
		this(0);
	}

	public Timer(float fps) {
		timeElapsed = 0;
		frameTime = 0;
		lastTime = 0;
		perfCountFrequency = System.nanoTime();
		this.fps = fps;

		timeScale = 1.0f / 1_000_000_000;
		frameTime = (long) ((1 / fps) * 1_000_000_000);
	}

	public void start() {
		lastTime = System.nanoTime();

		nextTime = lastTime + frameTime;
	}

	public boolean readyForNextFrame() {
		if (fps == 0) {
			System.out.println("No FPS set in timer");
			return false;
		}

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
