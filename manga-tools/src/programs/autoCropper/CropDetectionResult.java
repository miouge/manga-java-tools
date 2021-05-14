package programs.autoCropper;

public class CropDetectionResult {
	
	// cropping detected
	public int firstRow = -1;
	public int lastRow  = -1;
	public int firstCol = -1;
	public int lastCol  = -1;

	// sum of cropping pixels 
	public int vCrop  = 0;	
	public int hCrop  = 0;
	
	public boolean isEmpty  = false;
}
