package beans;

import java.io.File;

public class Config {
	
	public int volumeNo = 1;	
	public String srcSubFolderFmt = "T%02d"; // subfolder name for each manga file	
	
	// use environment variable MGTW_ROOT_FOLDER to define this path
	public static String rootFolder;	
	
	public static String projetName = "default"; // current project name to easily switch between several project  
	
	public static String settingsFilePath; // settings.ini that need to be located into project folders (will hold custom settings about the project)
	
	// archives related settings
	public String archiveFolder = rootFolder + "/" + projetName + "/archives"; // folder that contain original pdf, cbz, cbr files 		
	public boolean flatUnzip = true; // ask unpack all files of a single manga file to the same destination folder (without consideration of archive folders)
	public boolean resizeImg = false;
	public int wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
	
	// outlet for extraction/unpack of pdf, cbr, cbz
	// or/also source of picture file for AutoCropper
	public String originalImgFolder = rootFolder + "/" + projetName + "/original-img";
			
	// outlet for cropping operation
	public String croppedImgFolder = rootFolder + "/" + projetName + "/cropped-img";
	public static boolean drawCroppingLine = false;  // if true : just draw cropping the lines instead of cropping
	public static boolean alsoCropBlackArea = false;  // if true : also try to crop black useless area

	// PDF generation settings	
	// outlet for pdf generation
	public String outletPdfFolder = rootFolder + "/" + projetName + "/outlet-pdf";
	public String pdfnamefmt = "xxxx T%02d.pdf"; // format of document using volumeNo
	public String titlefmt   = "xxxx No %d"; // title of document using volumeNo
	public String author  = ""; // author
	
	public Config() {		
	}	
	
	public Config( int volumeNo ) {
		this.volumeNo = volumeNo;
	}
	
	private static void init() throws Exception {

		String rootFolderEnv = System.getenv( "MGTW_ROOT_FOLDER" );
		if( rootFolderEnv == null ) {
			throw new Exception( "MGTW_ROOT_FOLDER environment variable is undefined !" );
		}
		rootFolder = rootFolderEnv;
		settingsFilePath = rootFolder + "/" + projetName + "/settings.ini";
		
		File settingFile = new File( settingsFilePath );
		if( settingFile.exists() == false ) {
			throw new Exception( "file not found : " + settingFile.toString() );
		}
	}
	
	// initialization
	static {

		try {
			init();
			
		} catch ( Exception e ) {

			String msg = "failed to initialize Config static object (" + e.toString() + ")";
			System.err.println( msg );
		}
	}
}
