package beans;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class Tools {

	public static String getImageFormat( String fileName ) throws Exception {
		
		// auto detect the image format from the file it's extension
		String format = null;
		
		String[] parts = fileName.split("\\.");
		
		if( parts.length >= 2 ) {

			String ext = parts[ parts.length - 1 ].toLowerCase();
			switch( ext ) {
				case "jpeg" :
				case "jpg" : { format = "jpg"; break; }
				case "png" : { format = "png"; break; }
			}
		}
		
		if( format == null ) {
			throw new Exception( "unable to detect the image format from filename");
		}

 		return format;
	}
	
	static Optional<String> getExtension(String filename) {
		
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}	
	
	public static int listInputFiles( String pathToSearch, String pattern, List<FileItem> files, boolean recursive, boolean verbose ) throws IOException {
		
		int foundNb = 0;
		
		if( verbose ) {
			System.out.format( "browsing %s (%s) ...\n", pathToSearch, pattern );
		}
		
	    try( DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get(pathToSearch) ) ) {
	    	
	        for( Path path : stream) {
	        		        	
	            if( Files.isDirectory(path) == false ) {
	            	
	            	// is a file
	            	
	            	File file = path.toFile();
	            	
	            	// System.out.format( "item %s\n", file.getName() );
	            		            	
	            	// (?i) = case-insensitive mode ON
	            	// (?-i) = case-insensitive mode OFF	            	
	                if( file.getName().matches("(?i).*" + pattern + ".*") == false ) 
	                {
	                	continue;
	                }

	            	// System.out.format( "found %s\n", path.toAbsolutePath().toString() );
	            	
	            	FileItem fi = new FileItem();
	            	fi.name = file.getName();
	            	fi.fullpathname = path.toAbsolutePath().toString();
	            	fi.folderOnly = path.getParent().toString(); 
	            	Optional<String > ext = getExtension( file.getName() );
	            	fi.extention = ext.get(); 
	            	files.add( fi );
	            	foundNb++;
	            }
	            else {
	            	
	            	// recursive search
	            	if( recursive ) {
		            	String newPathToSearch = path.toString();
		            	foundNb += listInputFiles( newPathToSearch, pattern, files, recursive, false );
	            	}
	            }
	        }
	    }
	    catch( java.nio.file.NoSuchFileException e ) {
	    	// juste ignore
	    }
	    
	    if( verbose && foundNb > 0 ) {
	    	System.out.format( "found %d matching files\n", foundNb );
	    }
	    
	    return foundNb; 
	}

	public static void createFolder( String folderpath, boolean dropExisting, boolean verbose ) throws IOException {
		
		if( dropExisting ) {
			
			if( verbose ) {
				System.out.format( "drop & recreate folder %s\n", folderpath );
			}
		}
		else {
			if( verbose ) {
				System.out.format( "create folder %s\n", folderpath );
			}
		}
		
		File file = new File( folderpath );
		if( dropExisting ) {
			
			FileUtils.deleteDirectory( file );
		}
		Files.createDirectories(Paths.get( folderpath ));
	}

	public static String getIniSetting( String path, String section, String keyName, String defaultValue ) {

		String keyValue = null;

		try {

			Ini iniFile = new Ini( new File( path ) );
			org.ini4j.Config.getGlobal().setEscape(false);

			Section iniSection = iniFile.get( section );
			if( iniSection == null ) {
				return defaultValue;
			}

			keyValue = iniSection.get( keyName );
			if( keyValue == null ) {
		
				return defaultValue;
			}

			if( keyValue.length() == 0 ) {

				return defaultValue;
			}

						
		} catch ( java.io.FileNotFoundException e ) {

			// if ini file not found just return the default value specified
			keyValue = defaultValue;

		} catch ( Exception e ) {

			keyValue = defaultValue;
			e.printStackTrace();
		}

		return keyValue;
	}
}
