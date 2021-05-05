package programs;

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

import com.github.junrar.testutil.ExtractArchive;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class UnpackCbz {

	static int errorCount = 0 ;
	
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
	
    public static void unzipFile( Config config, FileItem fi, String destFolder ) throws IOException {
	       
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
		
	public static void unPackFile( Config config, FileItem fi, String destFolder ) throws IOException {
				
		try
		{
			if( fi.extention.equals("cbr") ) {
				
				System.out.format( "extract content of %s ...\n", fi.name );
				
				// rar file
				ExtractArchive.extractArchive( new File( fi.fullpathname) , new File( destFolder ) );				
			}
			else if( fi.extention.equals("cbz") ) {
				
				System.out.format( "extract content of %s ...\n", fi.name );
				
				//unzipFile( config, fi, destFolder );
				apacheDecompressZip( config.flatUnzip, fi, destFolder );
			}
			
		} catch ( Exception e ) {
	
			errorCount++;
			e.printStackTrace();
		}		
	}	
	
	public static void unPackAll( Config config ) {
		
		TreeSet<FileItem> files = new TreeSet<>(); // Naturally ordered 

		try
		{
			// compile file list
			
			Tools.listInputFiles( config.archiveFolder, ".*\\.cb.?", files, true ); // cbr & cbz
			
			System.out.format( "total file count : %d files\n", files.size() );
			
			// drop output folders if already exist then re-create it 
			Tools.createFolder( config.originalImgFolder, true );
			
			int i = 0;
			for( FileItem fi : files ) {

				// for each file ...
				i++;
				
				System.out.format( "dest %s/ archive %s\n", String.format( config.srcSubFolderFmt, i ), fi.name );				
				
				String destFolder = config.originalImgFolder + "/" + String.format( config.srcSubFolderFmt, i );
				Tools.createFolder( destFolder, true );
				
				// unpack
				unPackFile( config, fi, destFolder );
			}			
			
		} catch ( Exception e ) {

			e.printStackTrace();
		}
		
		if( errorCount > 0 ) {
			System.out.format( "%d error while processing\n", errorCount );
		}		
	}
	
	public static void main(String[] args) {

		// This will list all .cbr & .cbz from config.archiveFolder
		// then unpack all file found to config.originalImgFolder + config.srcSubFolderFmt		
		
		Config config = new Config();
				
		unPackAll( config );
		
		System.out.format( "complete\n");
	}
}
