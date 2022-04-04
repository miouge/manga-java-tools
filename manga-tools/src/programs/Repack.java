package programs;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.TreeSet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import beans.Config;
import beans.FileItem;
import beans.Tools;
import beans.ZipFileCompressUtils;

public class Repack {

	// loaded from settings.ini
	
	static Integer firstVol;
	static Integer lastVol;
	static String subFolderFmt;
	static boolean cleanupSubFolders = true;  // default behavior is to drop existing target subfolders then recreate it
	static String format; // cbz, cbr or pdf	
	static String filenameFmt;
	static String titlefmt;
	static String author;	
	
	static void generatePDF( TreeSet<FileItem> files, String destFilePath, String title, String author )  throws Exception {

        Document document = new Document();
        
        document.addTitle( title );
        document.addAuthor( author );
        document.addSubject("cropped by fifou in 2022");
        
        PdfWriter.getInstance(document, new FileOutputStream(new File(destFilePath)));
        document.open();
        
        int pageCount = 0;
        for( FileItem f : files ) {
        
        	System.out.format( "add %s\n", f.fullpathname );
        	
            document.newPage();
            Image image = Image.getInstance( f.fullpathname );
            image.setAbsolutePosition(0, 0);
            image.setBorderWidth(0);
            image.scaleAbsolute(PageSize.A4);
            document.add( image );
            pageCount++;
        }
        
        document.setPageCount( pageCount );
        System.out.format( "page count = %d\n", pageCount );
        document.close();
    }
		
	// TODO : customize image locations path list if needed
	// each path will be checked to list all images (.jpg .jpeg or .png) it contains
	// then images will be ordered by names	before insertion into the PDF document
	public static void getImagesLocations( String baseSourcePath, ArrayList<String> locations ) {
				
		// select std/_BIC/ subfolder content (manually cropped) if exit instead of std/ content
		
		File stdBIC = new File( baseSourcePath + "/std/_BIC" );
		if( stdBIC.exists() && stdBIC.isDirectory() ) {
			
			locations.add( baseSourcePath + "/std/_BIC" );
		}
		else {
		
			locations.add( baseSourcePath + "/std" );
		}
		
		// select tocheck/_BIC/ subfolder content (manually cropped) if exit instead of tocheck/ content
		
		File tocheckBIC = new File( baseSourcePath + "/tocheck/_BIC" );
		if( tocheckBIC.exists() && tocheckBIC.isDirectory() ) {
			locations.add( baseSourcePath + "/tocheck/_BIC" );
		}
		else {
			locations.add( baseSourcePath + "/tocheck" );
		}
		
		locations.add( baseSourcePath );
	}

	static void init( Config config ) throws Exception {
		
		firstVol = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "firstVolume", "1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "lastVolume" , "1" ));		
		subFolderFmt = Tools.getIniSetting( config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		cleanupSubFolders = Boolean.parseBoolean( Tools.getIniSetting( config.settingsFilePath, "General", "cleanupSubFolders", "true" ));

		format   = Tools.getIniSetting( config.settingsFilePath, "Repack", "format", "cbz" );		
		filenameFmt  = Tools.getIniSetting( config.settingsFilePath, "Repack", "filenameFmt", config.projectName + " T%02d" );
		titlefmt     = Tools.getIniSetting( config.settingsFilePath, "Repack", "titleFmt", config.projectName + " No %d" );
		author       = Tools.getIniSetting( config.settingsFilePath, "Repack", "author", "NA" );		
	}	
	
	public static void createArchives( Config config ) throws Exception {

		init( config );
		
		// create folder (optionally drop output folders if already exist then re-create it)
		Tools.createFolder( config.outletFolder, cleanupSubFolders, false );
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {

			String archiveName = String.format( filenameFmt, volumeNo ) + "." + format;
			String archiveFile = config.outletFolder + "/" + archiveName;
			String title       = String.format( titlefmt   , volumeNo );			
			
	        String baseSourcePath = config.croppedImgFolder + "/" + String.format( subFolderFmt, volumeNo );

			TreeSet<FileItem> files = new TreeSet<>(); // is Naturally ordered 			
			ArrayList<String> locations = new ArrayList<String>();
			
			// get list of folder to browse to collect images to include in the PDF
			getImagesLocations( baseSourcePath, locations );

			// compile picture list
			for(  String location : locations ) {

				Tools.listInputFiles( location, ".*\\.jpe?g", files, false, true );
				Tools.listInputFiles( location, ".*\\.png", files, false, true );
			}
			
			System.out.format( "total images count : %d files\n", files.size() );
			
			if( files.size() == 0 ) {
				continue;
			}
			
			if( format.equalsIgnoreCase("pdf") ) {
				
				generatePDF( files, archiveFile, title, author );
			}
			else if( format.equalsIgnoreCase("cbz") ) {


		        ZipFileCompressUtils zipFileCompressUtils = new ZipFileCompressUtils();
		        zipFileCompressUtils.createZipFile( files, archiveFile );
			}
			else if( format.equalsIgnoreCase("cbr") ) {
				
				// TODO : implement
			}
		}
	}	
	
	public static void main(String[] args) {

		try {

			Config config = new Config();
			createArchives( config );
			System.out.format( "complete\n" );

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}