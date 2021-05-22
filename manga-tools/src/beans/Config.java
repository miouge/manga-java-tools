package beans;

import java.io.File;

public class Config {
		
	public static boolean initOK = false;
	
	public int volumeNo = 1;
	
	// use environment variable MGTW_ROOT_FOLDER to define this path
	public static String rootFolder;	
	
	public static String projetName = "default"; // current project name to easily switch between several project  
	
	public static String settingsFilePath; // settings.ini that need to be located into project folders (will hold custom settings about the project)
	
	// unpack archives related settings
	public String archiveFolder = rootFolder + "/" + projetName + "/archives"; // folder that contain original pdf, cbz, cbr files 		
	public boolean flatUnzip = true; // ask unpack all files of a single manga file to the same destination folder (without consideration of archive folders)
	public boolean resizeImg = false;
	public int wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
	
	// outlet for extraction/unpack of pdf, cbr, cbz
	// and/or also source of picture file for AutoCropper module
	public String originalImgFolder = rootFolder + "/" + projetName + "/original-img";
			
	// outlet for cropping operation
	// and/or also source of picture file for GeneratePDF module 
	public String croppedImgFolder = rootFolder + "/" + projetName + "/cropped-img";

	// outlet for pdf generation
	public String outletPdfFolder = rootFolder + "/" + projetName + "/outlet-pdf";
	
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
			initOK = true;
			
		} catch ( Exception e ) {

			String msg = "failed to initialize Config static object (" + e.toString() + ")";
			System.err.println( msg );
		}
	}
}
