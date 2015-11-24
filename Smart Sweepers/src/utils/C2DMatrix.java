package utils;

import java.util.List;

public class C2DMatrix {

	S2DMatrix matrix;

	public C2DMatrix() {
		matrix = new S2DMatrix();
		identity();
	}

	public void identity() {
		matrix._11 = 1;
		matrix._12 = 0;
		matrix._13 = 0;

		matrix._21 = 0;
		matrix._22 = 1;
		matrix._23 = 0;

		matrix._31 = 0;
		matrix._32 = 0;
		matrix._33 = 1;
	}

	public void translate(double x, double y) {
		S2DMatrix mat = new S2DMatrix();

		mat._11 = 1;
		mat._12 = 0;
		mat._13 = 0;

		mat._21 = 0;
		mat._22 = 1;
		mat._23 = 0;

		mat._31 = x;
		mat._32 = y;
		mat._33 = 1;

		// and multiply
		matrixMultiply(mat);
	}

	public void scale(double xScale, double yScale) {
		S2DMatrix mat = new S2DMatrix();

		mat._11 = xScale;
		mat._12 = 0;
		mat._13 = 0;

		mat._21 = 0;
		mat._22 = yScale;
		mat._23 = 0;

		mat._31 = 0;
		mat._32 = 0;
		mat._33 = 1;

		// and multiply
		matrixMultiply(mat);
	}

	public void rotate(double rot) {
		S2DMatrix mat = new S2DMatrix();

		double Sin = Math.sin(rot);
		double Cos = Math.cos(rot);

		mat._11 = Cos;
		mat._12 = Sin;
		mat._13 = 0;

		mat._21 = -Sin;
		mat._22 = Cos;
		mat._23 = 0;

		mat._31 = 0;
		mat._32 = 0;
		mat._33 = 1;

		// and multiply
		matrixMultiply(mat);
	}

	public void matrixMultiply(S2DMatrix mIn) {
		S2DMatrix mat_temp = new S2DMatrix();

		// first row
		mat_temp._11 = (matrix._11 * mIn._11) + (matrix._12 * mIn._21) + (matrix._13 * mIn._31);
		mat_temp._12 = (matrix._11 * mIn._12) + (matrix._12 * mIn._22) + (matrix._13 * mIn._32);
		mat_temp._13 = (matrix._11 * mIn._13) + (matrix._12 * mIn._23) + (matrix._13 * mIn._33);

		// second
		mat_temp._21 = (matrix._21 * mIn._11) + (matrix._22 * mIn._21) + (matrix._23 * mIn._31);
		mat_temp._22 = (matrix._21 * mIn._12) + (matrix._22 * mIn._22) + (matrix._23 * mIn._32);
		mat_temp._23 = (matrix._21 * mIn._13) + (matrix._22 * mIn._23) + (matrix._23 * mIn._33);

		// third
		mat_temp._31 = (matrix._31 * mIn._11) + (matrix._32 * mIn._21) + (matrix._33 * mIn._31);
		mat_temp._32 = (matrix._31 * mIn._12) + (matrix._32 * mIn._22) + (matrix._33 * mIn._32);
		mat_temp._33 = (matrix._31 * mIn._13) + (matrix._32 * mIn._23) + (matrix._33 * mIn._33);

		matrix = mat_temp;
	}

	public void transformSPoints(List<SPoint> vPoint) {
		for (int i = 0; i < vPoint.size(); ++i) {
			double tempX = (matrix._11 * vPoint.get(i).x) + (matrix._21 * vPoint.get(i).y) + (matrix._31);

			double tempY = (matrix._12 * vPoint.get(i).x) + (matrix._22 * vPoint.get(i).y) + (matrix._32);

			vPoint.get(i).x = tempX;

			vPoint.get(i).y = tempY;

		}
	}
}
