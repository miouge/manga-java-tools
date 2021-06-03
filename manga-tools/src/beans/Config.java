package beans;

import java.io.File;

public class Config {
	
	// use environment variable MGTW_ROOT_FOLDER to define this path
	public String rootFolder;
	
	// current project name to easily switch between several projects
	// then project folder will be %MGTW_ROOT_FOLDER%/<projetName>/
	
	public String projectName = "default";

	// settings.ini that can hold any override custom directives about the project
	public String settingsFilePath;
	
	// folder that contain original pdf, cbz, cbr files and/or also source of picture file for Analyse module
	// Unpack module source when looking for archives to unpack	
	public String archiveFolder;;

	// folder that contain analysed image and/or also source of picture file for AutoCropper module
	public String analysedFolder;
	
	// when extracting image from pdf, should we resize the images
	public boolean resizeImg = false;
	public int wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
	
	// outlet for extraction/unpack of pdf, cbr, cbz and/or also source of picture file for AutoCropper module
	public String originalImgFolder;
			
	// outlet for cropping operation and/or also source based location of picture file for GeneratePDF module
	public String croppedImgFolder;

	// outlet for PDF generation
	public String outletPdfFolder;

	private void init() throws Exception {

		String rootFolderEnv = System.getenv( "MGTW_ROOT_FOLDER" );
		if( rootFolderEnv == null ) {
			throw new Exception( "MGTW_ROOT_FOLDER environment variable is undefined !" );
		}
		this.rootFolder = rootFolderEnv;
		settingsFilePath = this.rootFolder + "/" + this.projectName + "/settings.ini";
		
		File settingFile = new File( settingsFilePath );
		if( settingFile.exists() == false ) {
			
			System.err.println( settingFile + " was not found : will use default settings." );
		}
		
		// folder that contain original pdf, cbz, cbr files and/or also source of picture file for Analyse module
		// Unpack module source when looking for archives to unpack	
		this.archiveFolder = this.rootFolder + "/" + this.projectName + "/archives";

		// folder that contain analysed image and/or also source of picture file for AutoCropper module
		this.analysedFolder = this.rootFolder + "/" + this.projectName + "/analysed-img";
		
		// when extracting image from pdf, should we resize the images
		this.resizeImg = false;
		this.wantedHeight = 1872; // vivlio inkpad3 screen resolution (300dpi) h= 1872px w= 1404px (ratio = 4/3)
		
		// outlet for extraction/unpack of pdf, cbr, cbz and/or also source of picture file for AutoCropper module
		this.originalImgFolder = rootFolder + "/" + this.projectName + "/original-img";
				
		// outlet for cropping operation and/or also source based location of picture file for GeneratePDF module
		this.croppedImgFolder = rootFolder + "/" + this.projectName + "/cropped-img";

		// outlet for PDF generation
		this.outletPdfFolder = rootFolder + "/" + this.projectName + "/outlet-pdf";
	}
	
	
	public Config() throws Exception {
		
		init();		
	}
	
	public Config( String projectName ) throws Exception {
		this.projectName = projectName;
		init();
	}
}
