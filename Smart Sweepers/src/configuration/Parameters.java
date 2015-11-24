package configuration;

import java.io.InputStream;
import java.util.Scanner;

public class Parameters {
	public static double dPi = 3.14159265358979;
	public static double dHalfPi = dPi / 2;
	public static double dTwoPi = dPi * 2;
	public static int WindowWidth = 400;
	public static int WindowHeight = 400;
	public static int iFramesPerSecond = 0;
	public static int iNumInputs = 0;
	public static int iNumHidden = 0;
	public static int iNeuronsPerHiddenLayer = 0;
	public static int iNumOutputs = 0;
	public static double dActivationResponse = 0;
	public static double dBias = 0;
	public static double dMaxTurnRate = 0;
	public static double dMaxSpeed = 0;
	public static int iSweeperScale = 0;
	public static int iNumSweepers = 5;
	public static int iNumMines = 0;
	public static int iNumTicks = 0;
	public static double dMineScale = 0;
	public static double dCrossoverRate = 0;
	public static double dMutationRate = 0;
	public static double dMaxPerturbation = 0;
	public static int iNumElite = 0;
	public static int iNumCopiesElite = 0;

	public static boolean loadInParameters(InputStream inputStream) {

		Scanner s = new Scanner(inputStream);

		s.next();
		iFramesPerSecond = getInt(s);
		s.next();
		iNumInputs = getInt(s);
		s.next();
		iNumHidden = getInt(s);
		s.next();
		iNeuronsPerHiddenLayer = getInt(s);
		s.next();
		iNumOutputs = getInt(s);
		s.next();
		dActivationResponse = getDouble(s);
		s.next();
		dBias = getDouble(s);
		s.next();
		dMaxTurnRate = getDouble(s);
		s.next();
		dMaxSpeed = getDouble(s);
		s.next();
		iSweeperScale = getInt(s);
		s.next();
		iNumMines = getInt(s);
		s.next();
		iNumSweepers = getInt(s);
		s.next();
		iNumTicks = getInt(s);
		s.next();
		dMineScale = getDouble(s);
		s.next();
		dCrossoverRate = getDouble(s);
		s.next();
		dMutationRate = getDouble(s);
		s.next();
		dMaxPerturbation = getDouble(s);
		s.next();
		iNumElite = getInt(s);
		s.next();
		iNumCopiesElite = getInt(s);

		return true;
	}

	private static int getInt(Scanner s) {
		return Integer.parseInt(s.next());
	}

	private static double getDouble(Scanner s) {
		return Double.parseDouble(s.next());
	}
}
