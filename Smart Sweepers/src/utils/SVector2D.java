package utils;

public class SVector2D {
	private double x;
	private double y;

	public SVector2D(double x, double y) {
		super();
		this.setX(x);
		this.setY(y);
	}

	public SVector2D add(SVector2D other) {
		setX(getX() + other.getX());
		setY(getY() + other.getY());

		return this;
	}

	public SVector2D sub(SVector2D other) {
		setX(getX() - other.getX());
		setY(getY() - other.getY());

		return this;
	}

	public SVector2D subN(SVector2D other) {
		return new SVector2D(getX() - other.getX(), getY() - other.getY());
	}

	public SVector2D mul(SVector2D other) {
		setX(getX() * other.getX());
		setY(getY() * other.getY());

		return this;
	}

	public SVector2D div(SVector2D other) {
		setX(getX() / other.getX());
		setY(getY() / other.getY());

		return this;
	}

	public SVector2D mul(double scalar) {
		return new SVector2D(getX() * scalar, getY() * scalar);
	}

	public double length() {
		return Math.sqrt(getX() * getX() + getY() * getY());
	}

	public double distance(SVector2D other) {
		double x = this.x - other.x;
		double y = this.y - other.y;
		return sqrt(x * x + y * y);
	}

	public SVector2D normalize() {
		double length = length();

		return new SVector2D(getX() / length, getY() / length);
	}

	public double dotProduct(SVector2D other) {
		return getX() * other.getX() + getY() * other.getY();
	}

	public int sign(SVector2D other) {
		if (getY() * other.getX() > getX() * other.getY()) {
			return 1;
		} else {
			return -1;
		}
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	private double sqrt(double d) {
		double sqrt = Double.longBitsToDouble( ( ( Double.doubleToLongBits( d )-(1l<<52) )>>1 ) + ( 1l<<61 ) );
		
		return (sqrt + d/sqrt)/2.0;
	}
}
