package beans;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

public class Tools {

/* 

    public static void main(String[] args) {

        final File folder = new File("C:\\projects");

        List<String> result = new ArrayList<>();

        search(".*\\.java", folder, result);

        for (String s : result) {
            System.out.println(s);
        }

    }

    public static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }

        }
    }
 */
	
	static Optional<String> getExtension(String filename) {
		
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}	
	
	public static int listInputFiles( String pathToSearch, String pattern, TreeSet<FileItem> files, boolean verbose ) throws IOException {
		
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
	            	
	                if( file.getName().matches(pattern) == false ) {
	                	continue;
	                }

	            	// System.out.format( "found %s\n", path.toAbsolutePath().toString() );
	            	
	            	FileItem fi = new FileItem();
	            	fi.name = file.getName();
	            	fi.fullpathname = path.toAbsolutePath().toString();
	            	Optional<String > ext = getExtension( file.getName() );
	            	fi.extention = ext.get(); 
	            	files.add( fi );
	            	foundNb++;
	            }
	            else {
	            	
	            	// recursive search
	            	String newPathToSearch = path.toString();
	            	foundNb += listInputFiles( newPathToSearch, pattern, files, false );
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

	public static void createFolder( String folderpath, boolean dropExisting ) throws IOException {
		
		if( dropExisting ) {
			System.out.format( "drop & recreate folder %s\n", folderpath );
		}
		else {
			System.out.format( "create folder %s\n", folderpath );
		}
		
		File file = new File( folderpath );
		if( dropExisting ) {
			
			FileUtils.deleteDirectory( file );
		}
		Files.createDirectories(Paths.get( folderpath ));
	}
}
