package programs.autoCropper;

public class DetectionParam {

	public double  nonWhiteNbRatio = 0.10; // 0.25 = 25% = 1 sur 4
	public double  nonBlackNbRatio = 0.10; // 0.25 = 25% = 1 sur 4
	public int nonWhiteLevel = 125; // will considered as non white below this level
	public int nonBlackLevel = 125; // will considered as non black above this level	
}
