package miouge.autoCropper;

public class DetectionParam {

	public double  nonWhiteNbRatio; // 0.25 = 25% = 1 sur 4
	public double  nonBlackNbRatio; // 0.25 = 25% = 1 sur 4
	public int nonWhiteLevel; // will considered as non white below this level
	public int nonBlackLevel; // will considered as non black above this level
	public int cropWhiteArea; // if > 0 : try to crop white useless area	
	public int cropBlackArea; // if > 0 : try to crop black useless area	
}
