package programs;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.github.junrar.Junrar;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class Unpack {
	
	static int errorCount = 0 ;
	static String subFolderFmt;
		
	// Zip Unpack (2 functions possibles to try)
	
	// function #1 to unzip
	public static void apacheDecompressZip( boolean flatUnzip, FileItem fi, String destFolder ) throws FileNotFoundException, IOException {
		
		File fromFile = new File( fi.fullpathname);
		
    	int BUFFER_SIZE = 1024;
    	
    	int fileCount = 0;
    	try( FileInputStream fileInputStream = new FileInputStream(fromFile); ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(fileInputStream)) {
    		
    		byte[] buffer = new byte[BUFFER_SIZE];
    		
    		ArchiveEntry archiveEntry;
    		while( null != (archiveEntry = archiveInputStream.getNextEntry())) {
    			
    			String entryName = archiveEntry.getName();
    			   			    			
                if( archiveEntry.isDirectory()) {
                	
                	if( flatUnzip ) {
                		continue;
                	}
                	else {
	                	// directory
	                	File destFile = new File( destFolder, entryName );
	                	String destFilePath = destFile.getCanonicalPath();
	                	Files.createDirectories(Paths.get( destFilePath ));
                	}
                }
                else {
                	
                	if( flatUnzip ) {
	        			// remove any subfolders, only take the last part of the path (the filename and extension)
	                	// so flat unzip all the file to the same destination folder 
	        			String splitAround = "";
	        			if( entryName.contains("/") ) {
	        				splitAround = "/";
	        			}
	        			else if ( entryName.contains("\\")) {
	        				splitAround = "\\";
	        			}
	        			else {
	        				
	        			}
	        			if( splitAround != "" ) {
	        				
	        				String parts[] = entryName.split( splitAround );
	        				if( parts.length > 1 ) {
	        					
	        					entryName = parts[ parts.length -1 ]; 
	        				}
	        			}
                	}
        			
        			File destFile = new File( destFolder, entryName );
                
        			try( FileOutputStream fileOutputStream = new FileOutputStream(destFile) ) {
        				
        				int length = -1;
        				while ((length = archiveInputStream.read(buffer)) != -1) {
        					fileOutputStream.write(buffer, 0, length);
        				}
        				fileOutputStream.flush();
        			}
    	            fileCount++;
                }
    		}
    	}
    	System.out.format( "%d files saved\n", fileCount );
    }	
	
	// function #2 to unzip
    public static void javaUtilUnzipFile( Config config, FileItem fi, String destFolder ) throws IOException {
	       
    	System.out.format( "unzip content of %s ...", fi.name );
    	
        byte[] buffer = new byte[1024];
        
        ZipInputStream zis = new ZipInputStream(new FileInputStream( fi.fullpathname ));
        ZipEntry zipEntry = zis.getNextEntry();
        
        int fileCount = 0;
        while( zipEntry != null ) {
        	
        	File destFile = new File( destFolder, zipEntry.getName());
            
            if( zipEntry.isDirectory()) {
            	
            	// directory
            	String destFilePath = destFile.getCanonicalPath();
            	Files.createDirectories(Paths.get( destFilePath ));
            }
            else {
            
	            FileOutputStream fos = new FileOutputStream( destFile );
	            
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
	            fos.close();
	            fileCount++;
            }
            
            zipEntry = zis.getNextEntry();
        }
        
        zis.closeEntry();
        zis.close();
 
        System.out.format( "%d files saved\n", fileCount );
    }	
    
    public static void unzipFile( Config config, FileItem fi, String destFolder ) throws Exception {

    	boolean success = false;
    	
    	// try function #1
    	try {

    		apacheDecompressZip( config.flatUnzip, fi, destFolder );
    		success = true;
    	}
    	catch( Exception e ) {
    	}

    	if( success ) {
    		return;
    	}
    	
    	// else try function #2
    	try {

    		javaUtilUnzipFile( config, fi, destFolder );
    		success = true;
    	}
    	catch( Exception e ) {
    	}
    	
    	if( success != true ) {
    		throw new Exception( "fail to unzip" );
    	}
    }
    
	// PDF Unpack
	
	public static BufferedImage resizeImage( Config config, BufferedImage image ) {
	
		int currentHeight = image.getHeight();
		int currentWidth  = image.getWidth();
		
		Double ratio = null;
		
		if( currentHeight <= config.wantedHeight ) {
			return null;
		}
		else {
			ratio = config.wantedHeight / (double)(currentHeight);
		}
		
		int newHeight = (int)((double)currentHeight * ratio);
		int newWidth  = (int)((double)currentWidth  * ratio);
		
		System.out.format( "resize %dx%d -> %dx%d\n", currentHeight, currentWidth, newHeight, newWidth );
		
        Image originalImage= image.getScaledInstance( newWidth, newHeight, Image.SCALE_DEFAULT);

        int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, type);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        return resizedImage;
	}	
	
	public static void extractPdfContent( Config config, FileItem fi, String destFolder ) throws IOException {
		
		System.out.format( "extract content of %s ...\n", fi.name );
		
		File infile = new File( fi.fullpathname );	
	    PDDocument document = PDDocument.load( infile );
	    PDFRenderer pdfRenderer = new PDFRenderer(document);
	    
	    System.out.format( "page nb = %d\n", document.getNumberOfPages() );
	    
	    for( int page = 0; page < document.getNumberOfPages(); ++page ) {
	    	
	    	String dest = String.format( "%s/img%03d.jpg", destFolder, page+1 );
	    	
	        BufferedImage img = pdfRenderer.renderImageWithDPI( page, 300, ImageType.RGB );
	        BufferedImage outImg = img;
	        
	        if( config.resizeImg ) {
	        	BufferedImage resizedImage = resizeImage( config, img );
	        	if( resizedImage != null ) {
	        		outImg = resizedImage;
	        	}
	        }
	        
	        ImageIOUtil.writeImage( outImg, dest, 300 );
			
			System.out.format( "%d ", page + 1 );
	    }
	    document.close();
	    
	    System.out.format( "\n" );
	}

	// -----------------------
	
	public static void unPackFile( Config config, FileItem fi, String destFolder ) throws IOException {
		
		try
		{
			// System.out.format( "extract content of %s ...\n", fi.name );

			if( fi.extention.equals("cbr") ) {

				// rar file
				// using junrar (but however not working in all case)
				Junrar.extract( new File( fi.fullpathname), new File( destFolder ) );
			}
			else if( fi.extention.equals("cbz") ) {
				
				// zipped file
				unzipFile( config, fi, destFolder );
			}
			else if( fi.extention.equals("pdf") ) {

				// pdf document
				extractPdfContent( config, fi, destFolder );
			}
			
		} catch ( Exception e ) {
	
			errorCount++;
			e.printStackTrace();
		}
	}	
	
	static void init( Config config ) {
		
		subFolderFmt = Tools.getIniSetting( Config.settingsFilePath, "General", "subFolderFmt", "T%02d" );		
	}
	
	public static void unPackArchiveFolderContent() {
		
		Config config = new Config();
		
		TreeSet<FileItem> files = new TreeSet<>(); // Naturally ordered 

		init( config );
		
		try
		{
			// compile file list
			
			Tools.listInputFiles( config.archiveFolder, ".*\\.cb.?", files, true, false ); // cbr & cbz
			Tools.listInputFiles( config.archiveFolder, ".*\\.pdf", files, true, false ); // pdf
			
			System.out.format( "[Unpack] will unpack files of folder <%s> ...\n", config.archiveFolder );
			System.out.format( "total file count : %d files\n", files.size() );
			
			// drop output folders if already exist then re-create it 
			Tools.createFolder( config.originalImgFolder, false, false );
			
			int i = 0;
			for( FileItem fi : files ) {

				// for each file ...
				i++;
				
				System.out.format( "unpack to %s/ the archive <%s> ...\n", String.format( subFolderFmt, i ), fi.name );				
				
				String destFolder = config.originalImgFolder + "/" + String.format( subFolderFmt, i );
				Tools.createFolder( destFolder, true, false );
				
				// unpack
				unPackFile( config, fi, destFolder );
			}			
			
		} catch ( Exception e ) {

			e.printStackTrace();
		}
		
		if( errorCount > 0 ) {
			System.out.format( "%d error during the processing\n", errorCount );
		}
		else {
			System.out.format( "all was fine !\n" );
		}
	}
	
	public static void main(String[] args) {

		// This will list all .cbr or .cbz or .pdf from config.archiveFolder
		// then unpack all file found to config.originalImgFolder + config.srcSubFolderFmt
				
		unPackArchiveFolderContent();
		
		System.out.format( "complete\n");
	}		
}
