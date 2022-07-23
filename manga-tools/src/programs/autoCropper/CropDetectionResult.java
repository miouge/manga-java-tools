package programs.autoCropper;

public class CropDetectionResult {
	
	// cropping detected
	public int firstRow = -1; //  [ 0 - Height [
	public int lastRow  = -1; //  [ 0 - Height [
	public int firstCol = -1; //  [ 0 - Width [
	public int lastCol  = -1; //  [ 0 - Width [

	// sum of cropping pixels 
	public int vCrop  = 0;	
	public int hCrop  = 0;
	
	public boolean isEmpty  = false;
}
