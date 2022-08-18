package programs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class Repack {

	// loaded from settings.ini ...
	
	Integer firstVol;
	Integer lastVol;
	String subFolderFmt;
	boolean cleanupSubFolders = true;  // default behavior is to drop existing target subfolders then recreate it
	String format; // cbz, cbr or pdf	
	String filenameFmt;
	String titlefmt;
	String author;	
	
    void generateZipArchive( List<FileItem> files, String zipFileName ) {
    	    	
        try {
        	
            Path zipFilePath = Paths.get(zipFileName);
            
            OutputStream outputStream = Files.newOutputStream(zipFilePath);            
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);            
            ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream);
            
            for( FileItem file : files ) {
            
            	String prefix = "";
            	File fileToZip = new File( file.fullpathname );
            	
            	// addFileToZipStream(zipArchiveOutputStream, fileToZip, "");            	
                
            	String entryName = prefix + fileToZip.getName();
            	
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry( fileToZip, entryName );
                zipArchiveOutputStream.putArchiveEntry( zipArchiveEntry );
                
                FileInputStream fileInputStream = new FileInputStream( fileToZip );
                IOUtils.copy( fileInputStream, zipArchiveOutputStream );
                IOUtils.closeQuietly(fileInputStream);
                
                zipArchiveOutputStream.closeArchiveEntry();                
            }

            zipArchiveOutputStream.close();
            bufferedOutputStream.close();
            outputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }        
	
	void generatePDF( List<FileItem> files, String destFilePath, String title, String author )  throws Exception {

        Document document = new Document();
        
        document.addTitle( title );
        document.addAuthor( author );
        document.addSubject("cropped by fifou in 2022");
        
        PdfWriter.getInstance(document, new FileOutputStream(new File(destFilePath)));
        document.open();
        
        int pageCount = 0;
        for( FileItem f : files ) {
        
        	// System.out.format( "add %s\n", f.fullpathname );
        	
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
	public void getImagesLocations( String baseSourcePath, ArrayList<String> locations ) {
				
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

	void init( Config config ) throws Exception {
		
		firstVol = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "firstVolume", "-1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "lastVolume" , "-1" ));
		subFolderFmt = Tools.getIniSetting( config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		cleanupSubFolders = Boolean.parseBoolean( Tools.getIniSetting( config.settingsFilePath, "General", "cleanupSubFolders", "true" ));

		format       = Tools.getIniSetting( config.settingsFilePath, "Repack", "format", "both" );		
		filenameFmt  = Tools.getIniSetting( config.settingsFilePath, "Repack", "filenameFmt", config.projectName + " T%02d" );
		titlefmt     = Tools.getIniSetting( config.settingsFilePath, "Repack", "titleFmt", config.projectName + " No %d" );
		author       = Tools.getIniSetting( config.settingsFilePath, "Repack", "author", "NA" );		
	}	
	
	public void createArchives( Config config, String activeFormat ) throws Exception {
		
		int volumeNo = 1;
		
		if( firstVol > 0 ) { // can be = -1
			volumeNo = firstVol;
		} 
			
		do {
			
			if( lastVol > 0 ) { // can be = -1 
				if( volumeNo > lastVol ) {
					break;
				}
			}		

			String archiveName = String.format( filenameFmt, volumeNo ) + "." + activeFormat;
			String archiveFile = config.outletFolder + "/" + archiveName;
			String title       = String.format( titlefmt   , volumeNo );			
				        
			ArrayList<FileItem> files = new ArrayList<>(); // is Naturally ordered 			
			ArrayList<String> locations = new ArrayList<String>();			
			locations.add( config.croppedImgFolder );
			
			// get list of folder to browse to collect images to include in the PDF
			String baseSourcePath = config.croppedImgFolder + "/" + String.format( subFolderFmt, volumeNo );			
			getImagesLocations( baseSourcePath, locations );

			// compile picture list
			for(  String location : locations ) {

				Tools.listInputFiles( location, ".*\\.jpe?g", files, false, false );
				Tools.listInputFiles( location, ".*\\.png", files, false, false );
			}
			
			System.out.format( "total images count : %d files ... ", files.size() );

			if( files.size() == 0 ) {
				
				if( lastVol <= 0 ) {
					break;
				}
				
				continue;
			}
			
			boolean success = false;
			if( activeFormat.equalsIgnoreCase("pdf") ) {
				
				generatePDF( files, archiveFile, title, author );
				success = true;
			}
			else if( activeFormat.equalsIgnoreCase("cbz") ) {

				generateZipArchive( files, archiveFile );
				success = true;
			}
			else {
				
			}
			
			if( success ) {
				System.out.format( "-> %s OK\n", archiveName );
			}
			else {
				System.out.format( "-> %s FAILED\n", archiveName );
			}

			volumeNo++;
		}
		while( true );			
	}
	
	public void repack( Config config ) throws Exception {

		init( config );

		// create folder (optionally drop output folders if already exist then re-create it)
		Tools.createFolder( config.outletFolder, cleanupSubFolders, false );

		if( format.equalsIgnoreCase("pdf") || format.equalsIgnoreCase("both") ) {

			createArchives( config, "pdf" );
		}
		if( format.equalsIgnoreCase("cbz") || format.equalsIgnoreCase("both") ) {

			createArchives( config, "cbz" );
		}
	}
	
	public static void main(String[] args) {

		try {

			Config config = new Config();
			Repack repack = new Repack();
			repack.repack( config );
			System.out.format( "complete\n" );

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}