package beans;

public class Config {
	
	public int volumeNo = 1;
	public String srcSubFolderFmt = "T%02d";	
	public String imgPattern = "";
	public static TypeImgFile imgFileType = TypeImgFile.jpeg;	
		
	public String rootFolder = "C:/Users/pri/Documents/Archives Personnelles/manga";	
	//public String rootFolder = "D:/Scratch/manga";
	
	// archives related settings
	// pdf, cbr source folder, 	
	public String archiveFolder = rootFolder + "/archives";	
	public boolean flatUnzip = true; // unpack all files to the same destination folder
	public boolean resizeImg = false;
	public int wantedHeight = 2010; // vivlio inkpad3 screen resolution  h= 1872px w= 1404px
	
	// outlet for extraction of pdf, cbr, cbz
	// or source of jpeg file
	public String originalImgFolder = rootFolder + "/original-img";
	
		
	// outlet for cropping
	public String croppedImgFolder = rootFolder + "/cropped-img";
	public static int drawCroppingLine = 0; // cropping debug option
	
	// outlet for pdf generation
	public String outletPdfFolder = rootFolder + "/outlet-pdf";

	// jpg to pdf
	
	public String pdfnamefmt = "Les enfants de la baleine (Gl�nat) T%02d.pdf";
	public String titlefmt   = "Les enfants de la baleine No %d";
	public String author  = "Umeda Abi";
	
	public Config() {		
	}
	
	public Config( int volumeNo ) {				
		this.volumeNo = volumeNo;
	}
}
