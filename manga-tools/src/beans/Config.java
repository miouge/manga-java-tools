package beans;

import java.io.File;

public class Config {
	
	// use environment variable MGTW_ROOT_FOLDER to define this path
	public static String rootFolder;
	
	// current project name to easily switch between several projects
	// then project folder will be %MGTW_ROOT_FOLDER%/<projetName>/
	
	public static String projetName = "default";

	// settings.ini that can hold any override custom directives about the project
	public static String settingsFilePath;
	
	// folder that contain original pdf, cbz, cbr files
	// Unpack module source when looking for archives to unpack	
	public String archiveFolder = rootFolder + "/" + projetName + "/archives";
	
	// when extracting image from pdf, should we resize the images
	public boolean resizeImg = false;
	public int wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
	
	// outlet for extraction/unpack of pdf, cbr, cbz and/or also source of picture file for AutoCropper module
	public String originalImgFolder = rootFolder + "/" + projetName + "/original-img";
			
	// outlet for cropping operation and/or also source based location of picture file for GeneratePDF module
	public String croppedImgFolder = rootFolder + "/" + projetName + "/cropped-img";

	// outlet for PDF generation
	public String outletPdfFolder = rootFolder + "/" + projetName + "/outlet-pdf";
	
	public Config() {
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
			
			System.err.println( settingFile + " was not found : will use default settings." );
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
