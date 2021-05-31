package programs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class GeneratePDF {

	// loaded from settings.ini
	
	static Integer firstVol;
	static Integer lastVol;
	static String subFolderFmt;
	static String filenameFmt;
	static String titlefmt;
	static String author;
	
	static void generatePDF( TreeSet<FileItem> files, String destFilePath, String title, String author )  throws Exception {

        Document document = new Document();
        
        document.addTitle( title );
        document.addAuthor( author );
        document.addSubject("cropped by fifou in 2020");
        
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
	}

	static void init( Config config ) throws Exception {
		
		firstVol = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "General", "firstVolume", "1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "General", "lastVolume" , "1" ));
		
		subFolderFmt = Tools.getIniSetting( Config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		filenameFmt  = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "filenameFmt", Config.projetName + " T%02d.pdf" );
		titlefmt     = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "titleFmt", Config.projetName + " No %d" );
		author       = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "author", "NA" );		
	}	
	
	public static void generatePDF() throws Exception {
		
		Config config = new Config();

		init( config );
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {

			String pdfname = String.format( filenameFmt, volumeNo );
			String title   = String.format( titlefmt   , volumeNo );
			
	        String baseSourcePath = config.croppedImgFolder + "/" + String.format( subFolderFmt, volumeNo );
			String destFilePath =  config.outletPdfFolder;

			TreeSet<FileItem> files = new TreeSet<>(); // Naturally ordered 
	
			// create output folders
			Files.createDirectories(Paths.get( config.outletPdfFolder ));
			
			ArrayList<String> locations = new ArrayList<String>();
			
			// get list of folder to browse to collect images to include in the PDF
			getImagesLocations( baseSourcePath, locations );

			// compile picture list
			for(  String location : locations ) {

				Tools.listInputFiles( location, ".*\\.jpe?g", files, true, true );
				Tools.listInputFiles( location, ".*\\.png", files, true, true );
			}

			System.out.format( "total images count : %d files\n", files.size() );
			
			generatePDF( files, destFilePath + "/" + pdfname, title, author );
		}
	}	
	
	public static void main(String[] args) {

		try {

			generatePDF();
			System.out.format( "complete\n" );

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}