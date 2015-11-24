package utils;

public class S2DMatrix {

	double _11, _12, _13;
	double _21, _22, _23;
	double _31, _32, _33;

	S2DMatrix() {
		_11 = 0;
		_12 = 0;
		_13 = 0;
		_21 = 0;
		_22 = 0;
		_23 = 0;
		_31 = 0;
		_32 = 0;
		_33 = 0;
	}

	public String toString() {
		String returnString = "\n" + _11 + "  " + _12 + "  " + _13 + "\n" + _21 + "  " + _22 + "  " + _23 + "\n" + _31
				+ "  " + _32 + "  " + _33;

		return returnString;
	}
}
