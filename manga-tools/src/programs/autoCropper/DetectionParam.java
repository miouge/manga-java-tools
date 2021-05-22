package programs.autoCropper;

public class DetectionParam {

	public double  nonWhiteNbRatio; // 0.25 = 25% = 1 sur 4
	public double  nonBlackNbRatio; // 0.25 = 25% = 1 sur 4
	public int nonWhiteLevel; // will considered as non white below this level
	public int nonBlackLevel; // will considered as non black above this level
	public int alsoCropBlackArea; // if > 0 : also try to crop black useless area
}
