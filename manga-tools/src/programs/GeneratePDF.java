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
		
	// TODO : customize image locations path list
	// each path will be checked to list all images (.jpg .jpeg or .png) it contains
	// then images will be ordered by names	before insertion into the PDF document
	public static void getImagesLocations( String imgpath, ArrayList<String> locations ) {

		
		locations.add( imgpath + "/std" );
		//locations.add( imgpath + "/std/_BIC" );
		locations.add( imgpath + "/tocheck" );
		//locations.add( imgpath + "/tocheck/_BIC" );
	}

	static void init( Config config ) throws Exception {
		
		subFolderFmt = Tools.getIniSetting( Config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		filenameFmt  = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "filenameFmt", "xxx T%02d.pdf" );
		titlefmt     = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "titleFmt", "xxx No %d" );
		author       = Tools.getIniSetting( Config.settingsFilePath, "GeneratePDF", "author", "NA" );
		
		if( Config.initOK == false ) {
			throw new Exception( "Config object not correctly initialized !" );
		}
	}	
	
	public static void generatePDF( Config config ) {
				
		try {
			
			init( config );
			
		} catch (Exception e1) {

			e1.printStackTrace();
			return;
		}
		
		String pdfname = String.format( filenameFmt, config.volumeNo );
		String title   = String.format( titlefmt  , config.volumeNo );
		
        String imgpath = config.croppedImgFolder + "/" + String.format( subFolderFmt, config.volumeNo );
		String destFilePath =  config.outletPdfFolder;
                       		
		TreeSet<FileItem> files = new TreeSet<>(); // Naturally ordered 

		try
		{
			// create output folders
			Files.createDirectories(Paths.get( config.outletPdfFolder ));					
			
			ArrayList<String> locations = new ArrayList<String>();
			getImagesLocations( imgpath, locations );

			// compile file list
			
			for(  String location : locations ) {

				Tools.listInputFiles( location, ".*\\.jpe?g", files, true, true );
				Tools.listInputFiles( location, ".*\\.png", files, true, true );			
			}

			System.out.format( "total file count : %d files\n", files.size() );
			
			generatePDF( files, destFilePath + "/" + pdfname, title, author );
			
		} catch ( Exception e ) {

			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		
		// [ firstVol - lastVol ] 
		int firstVol = 3; 
		int lastVol  = 3;
						
		// autocrop images
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			Config config = new Config( volumeNo );
			generatePDF( config );
		}
		
		System.out.format( "complete\n");
	}		
}