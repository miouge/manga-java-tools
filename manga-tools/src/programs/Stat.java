package programs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import beans.Config;
import beans.FileItem;
import beans.Tools;
import programs.autoCropper.FileImg;

public class Stat {
	
	static ArrayList<Integer> widths = new ArrayList<>();	
	static ArrayList<Integer> heights = new ArrayList<>();
	
	// TODO : select function to find type
	static void processImg( FileImg img ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( img.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );
		
		widths.add(srcImage.getWidth());
		heights.add(srcImage.getHeight());
	}
	
	public static void computeStatistics( Config config ) {
			
		try
		{
			widths.clear();
			heights.clear();
			
			TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered
			
			String sourceFolder = config.originalImgFolder + "/" + String.format( config.srcSubFolderFmt, config.volumeNo );
			
			System.out.format( "%s content statistics : \n", sourceFolder );			
			
			Tools.listInputFiles( sourceFolder, ".*\\.jpe?g", files, false ); // jpg or jpeg
			Tools.listInputFiles( sourceFolder, ".*\\.png"  , files, false );			
			
			System.out.format( "   Image count : %d \n", files.size() );
			
			for( FileItem fi : files ) {
				
				FileImg img = new FileImg();
				img.fullpathname = fi.fullpathname;
				img.name = fi.name;
				
				processImg( img );
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
		
		computeStatistics( config );
		
		System.out.format( "complete\n");
	}
}
