package beans;

import java.io.File;

public class Config {
	
	// use environment variable MGTW_ROOT_FOLDER to define this path
	public String rootFolder;
	
	// current project name to easily switch between several projects
	// then project folder will be %MGT_SCRATCH_FOLDER%/<projetName>/
	
	public String projectName = "default";

	// settings.ini that can hold any override custom directives about the project
	public String settingsFilePath;
	
	// folder that contain original pdf, cbz, cbr files and/or also source of picture file for Analyse module
	// Unpack module source when looking for archives to unpack	
	public String archiveFolder;
		
	// outlet for extraction/unpack of pdf, cbr, cbz and/or also source of picture file for Analyse module
	public String originalImgFolder;
	
	// folder that contain analysed image and/or also source of picture file for AutoCropper module
	public String analysedFolder;	
			
	// outlet for cropping operation and/or also source based location of picture file for Repack module
	public String croppedImgFolder;

	// outlet for archive generation (pdf/cbz/cbr)
	public String outletFolder;

	private void init() throws Exception {

		String rootFolderEnv = System.getenv( "MGT_SCRATCH_FOLDER" );
		if( rootFolderEnv == null ) {
			throw new Exception( "MGT_SCRATCH_FOLDER environment variable is undefined !" );
		}
		this.rootFolder = rootFolderEnv;
		settingsFilePath = this.rootFolder + "/" + this.projectName + "/settings.ini";
		
		File settingFile = new File( settingsFilePath );
		if( settingFile.exists() == false ) {
			
			System.err.println( settingFile + " was not found : will use default settings." );
		}
		
		// folder that contain original pdf, cbz, cbr files and/or also source of picture file for Analyse module
		// Unpack module source when looking for archives to unpack	
		this.archiveFolder = this.rootFolder + "/" + this.projectName + "/a-archives";

		// outlet for extraction/unpack of pdf, cbr, cbz and/or also source of picture file for AutoCropper module
		this.originalImgFolder = rootFolder + "/" + this.projectName + "/b-original-img";
		
		// folder that contain analysed image and/or also source of picture file for AutoCropper module
		this.analysedFolder = this.rootFolder + "/" + this.projectName + "/c-analysed-img";		
				
		// outlet for cropping operation and/or also source based location of picture file for GeneratePDF module
		this.croppedImgFolder = rootFolder + "/" + this.projectName + "/e-cropped-img";

		// outlet for PDF generation
		this.outletFolder = rootFolder + "/" + this.projectName + "/e-outlet";
	}
	
	
	public Config() throws Exception {
		
		init();		
	}
	
	public Config( String projectName ) throws Exception {
		this.projectName = projectName;
		init();
	}
}
