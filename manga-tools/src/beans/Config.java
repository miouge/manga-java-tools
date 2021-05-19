package beans;

public class Config {
	
	public int volumeNo = 1;
	public String srcSubFolderFmt = "T%02d";	
	public String imgPattern = "";
	
	// TODO : use en environnement variable to define this path
	public String rootFolder = "C:/Users/pri/Documents/Archives Personnelles/scratch";	
	//public String rootFolder = "D:/Scratch/manga";
	
	// archives related settings
	public String archiveFolder = rootFolder + "/archives"; // folder that contain original pdf, cbz, cbr files 		
	public boolean flatUnzip = true; // ask unpack all files of a single manga file to the same destination folder (without consideration of archive folders)
	public boolean resizeImg = false;
	public int wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
	
	// outlet for extraction/unpack of pdf, cbr, cbz
	// or/also source of picture file for AutoCropper
	public String originalImgFolder = rootFolder + "/original-img";
			
	// outlet for cropping operation
	public String croppedImgFolder = rootFolder + "/cropped-img";	
	public static boolean drawCroppingLine = false;  // if true : just draw cropping the lines instead of cropping
	public static boolean alsoCropBlackArea = false;  // if true : also try to crop black useless area

	// PDF generation settings	
	// outlet for pdf generation
	public String outletPdfFolder = rootFolder + "/outlet-pdf";
	public String pdfnamefmt = "Wakfu T%02d.pdf"; // format of document using volumeNo
	public String titlefmt   = "Wakfu No %d"; // title of document using volumeNo
	public String author  = "Tot - Azea - Saïd Sassine"; // author
	
	public Config() {		
	}
	
	public Config( int volumeNo ) {
		this.volumeNo = volumeNo;
	}
}
