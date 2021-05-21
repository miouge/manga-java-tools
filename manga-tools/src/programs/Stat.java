package programs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class Stat {
	
	static ArrayList<Integer> widths = new ArrayList<>();
	static ArrayList<Integer> heights = new ArrayList<>();
	
	static boolean excludeFolderCreated = false;
	static int excludedCount = 0;

	// TODO : customize this function if you need to exclude some pictures (there will next be moved into a excludes/ subfolder ) 
	static boolean doExcludeImage( BufferedImage srcImage ) throws Exception  {

//		int width = srcImage.getWidth();
//		int height = srcImage.getHeight();
//		
//		if(( width < 1000 ) || ( 1200 < width )) {
//			return true;	
//		}
//		if(( height < 1500 ) || ( 1700 < height )) {
//			return true;	
//		}

		return false;
	}	

	static void checkOriginalImage( Config config, FileItem fi ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( fi.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );
		
		if( doExcludeImage( srcImage ) ) {

			if( excludeFolderCreated == false ) {
				String excludeFolder = fi.folderOnly + "/excludes";
				
				// drop output folders if already exist then re-create it
				Tools.createFolder( excludeFolder, true );
				excludeFolderCreated = true;
			}
			
			String excludedPath = fi.folderOnly + "/excludes/" + fi.name;
			Files.move(Paths.get( fi.fullpathname), Paths.get( excludedPath), StandardCopyOption.REPLACE_EXISTING);
			
			excludedCount++;
			return;
		}	
		
		widths.add(srcImage.getWidth());
		heights.add(srcImage.getHeight());
	}
	
	public static void checkOriginalImages( Config config ) {
			
		try
		{
			widths.clear();
			heights.clear();
			excludedCount = 0;
			excludeFolderCreated = false;
			
			TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered
			
			String sourceFolder = config.originalImgFolder + "/" + String.format( config.srcSubFolderFmt, config.volumeNo );
			
			System.out.format( "%s content statistics : \n", sourceFolder );
			
			Tools.listInputFiles( sourceFolder, ".*\\.jpe?g", files, false, false ); // jpg or jpeg
			Tools.listInputFiles( sourceFolder, ".*\\.png"  , files, false, false );						
			
			for( FileItem fi : files ) {

				checkOriginalImage( config, fi );
			}
			
			System.out.format( "   Total Images count : %d \n", files.size() );
			if( excludedCount > 0 ) {
				System.out.format( "   Excluded count : %d (moved into excludes/)\n", excludedCount );
			}
			
			// compute statistics  
			
			if( widths.size() > 0 ) {
				
				OptionalInt maxW = widths.stream().mapToInt(Integer::intValue).max();
				OptionalInt minW = widths.stream().mapToInt(Integer::intValue).min();				
				OptionalDouble avgW = widths.stream().mapToInt(Integer::intValue).average();
				
  		        // Variance
				double mean = widths.stream().mapToInt(Integer::intValue).average().getAsDouble();
		        double variance = widths.stream().map( i -> i - mean ).map( i -> i*i ).mapToDouble( i -> i ).average().getAsDouble();
		        
		        //Standard Deviation 
		        double standardDeviation = Math.sqrt(variance);
				
				System.out.format( "   Width  : ( %d - %d ) avg = %.1f deviation =%.1f\n", minW.getAsInt(), maxW.getAsInt(), avgW.getAsDouble(), standardDeviation );
			}
			if( heights.size() > 0 ) {

				OptionalInt maxH = heights.stream().mapToInt(Integer::intValue).max();
				OptionalInt minH = heights.stream().mapToInt(Integer::intValue).min();				
				OptionalDouble avgH = heights.stream().mapToInt(Integer::intValue).average();				

  		        // Variance
				double mean = heights.stream().mapToInt(Integer::intValue).average().getAsDouble();
		        double variance = heights.stream().map( i -> i - mean ).map( i -> i*i ).mapToDouble( i -> i ).average().getAsDouble();

		        //Standard Deviation
		        double standardDeviation = Math.sqrt(variance);
				
				System.out.format( "   Height : ( %d - %d ) avg = %.1f deviation =%.1f\n", minH.getAsInt(), maxH.getAsInt(), avgH.getAsDouble(), standardDeviation );
			}
			
		} catch ( Exception e) {

			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		Config config = new Config();
		
		checkOriginalImages( config );
		
		System.out.format( "complete\n");
	}
}
