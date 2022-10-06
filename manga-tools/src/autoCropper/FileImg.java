package programs.autoCropper;

import beans.FileItem;

public class FileImg extends FileItem {

	// cropping directives, sub image to keep
	public int x; // - the X coordinate of the upper-left corner of the specified rectangular region 
	public int y; // - the Y coordinate of the upper-left corner of the specified rectangular region 
	public int w; // - the width of the specified rectangular region 
	public int h; // - the height of the specified rectangular region
		
	public TypeDetected typeDetected = TypeDetected.undefined; 
}
