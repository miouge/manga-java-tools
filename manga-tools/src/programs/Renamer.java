package programs;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

import programs.renamer.FileItemComparable;


public class Renamer {
	
	static Integer findNum( FileItemComparable fi ) {

		int p1 = 0, p2 = 0;
		
		// System.out.format( "parsing %s", foldername );

		String foldername = fi.name;
		
		for( int i = foldername.length() - 1 ; i > 0 ; i-- ) {
			
			if( foldername.charAt(i) == ')' ) {
				p1 = i;
			}
			if( foldername.charAt(i) == '(' ) {				
				p2 = i+1;
				
				
				CharSequence num = foldername.subSequence( p2, p1 );
				Integer numVol = Integer.parseInt(  num.toString() );
				fi.num = numVol;
				
				CharSequence shortName = foldername.subSequence( 0,  p2-2 );
				fi.shortname = shortName.toString();
				
				
				System.out.format( "found %d --%s--\n", fi.num, fi.shortname );
				
				
				return numVol;
			}									
		}
		return null;
	}
	
	static void listInputFiles( String pathToSearch, TreeSet<FileItemComparable> folders ) throws Exception {
		
    	System.out.format( "browsing %s ...\n", pathToSearch );
		
	    try( DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(pathToSearch)) ) {
	    	
	        for( Path path : stream) {
	        	
	            if( Files.isDirectory(path) == false ) {
	            	continue;
	            }
	            
	            // is a directory
            	File file = path.toFile();            	
            	//System.out.format( "found %s\n", path.toAbsolutePath().toString() );
            	
            	String filename = file.getName();
            	
            	if( filename.contains("(") && filename.contains(")") ) {
            		
                	FileItemComparable fi = new FileItemComparable();
                	fi.name = file.getName();
                	fi.fullpathname = path.toAbsolutePath().toString();
                	fi.location = pathToSearch;
                	folders.add( fi );
                	
                	Integer num = findNum( fi );
                	if( num == null ) {
                		throw new Exception( "failed to discover num" );
                	}
                	
    				String newPathName = String.format("%s\\%04d - %s", fi.location, fi.num, fi.shortname );
    				System.out.format( "renaming to %s ...\n", newPathName );
    				
    				
    				//Path path2 = Paths.get( fi.fullpathname );
    				//File file2 = path.toFile();
    				file.renameTo(  Paths.get( newPathName ).toFile() );                	                
            	}
            	else {
            		
            		listInputFiles( path.toAbsolutePath().toString(), folders );
            	}
	        }
	    }	    
	}
	
	public static void main(String[] args) {
			
		TreeSet<FileItemComparable> folders = new TreeSet<>(); // naturaly ordered
		
		String pathToSearch  = "C:/Scratch/download/BitTorrent/Bibliotheque FNA propre";
		
		try
		{
			
			listInputFiles( pathToSearch, folders );
			
			/*
			for( FileItem fi : folders ) {

				String newPathName = String.format("%s\\%d - %s", fi.location, fi.num, fi.shortname );
				System.out.format( "renaming to %s ...\n", newPathName );
				
				
				Path path = Paths.get( fi.fullpathname );
				File file = path.toFile();
				file.renameTo(  Paths.get( newPathName ).toFile() );
			}
			*/
			
			
		} catch ( Exception e) {

			e.printStackTrace();
		}
				
		System.out.format( "complete\n");
	}
}
