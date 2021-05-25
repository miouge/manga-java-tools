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

public class Analyse {
	
	static ArrayList<Integer> widths = new ArrayList<>();
	static ArrayList<Integer> heights = new ArrayList<>();
	
	static boolean excludeFolderCreated = false;
	static int excludedCount = 0;

	// loaded from settings.ini
	
	static Integer firstVol;
	static Integer lastVol;
	static String subFolderFmt;

	static int excludeWidthLessThan = -1;
	static int excludeWidthGreaterThan = -1;
	static int excludeHeightLessThan = -1;
	static int excludeHeightGreaterThan = -1;
		 
	static boolean doExcludeImage( BufferedImage srcImage ) throws Exception  {

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		
		if( excludeWidthLessThan > 0 && ( width < excludeWidthLessThan )) {
			return true;
		}
		if( excludeWidthLessThan > 0 && ( excludeWidthGreaterThan < width )) {
			return true;
		}
		if( excludeWidthLessThan > 0 && ( height < excludeHeightLessThan )) {
			return true;
		}
		if( excludeWidthLessThan > 0 && ( excludeHeightGreaterThan < height )) {
			return true;
		}

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
				Tools.createFolder( excludeFolder, true, false );
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
	
	static void init( Config config ) throws Exception {
		
		firstVol = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "General", "firstVolume", "1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "General", "lastVolume" , "1" ));
		subFolderFmt = Tools.getIniSetting( Config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		
		excludeWidthLessThan     = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "excludeWidthLessThan"     , "-1" ));
		excludeWidthGreaterThan  = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "excludeWidthGreaterThan"  , "-1" ));
		excludeHeightLessThan    = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "excludeHeightLessThan"    , "-1" ));
		excludeHeightGreaterThan = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "excludeHeightGreaterThan" , "-1" ));				
	}	
	
	public static void checkOriginalImages() throws Exception {

		Config config = new Config();

		init( config );
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			widths.clear();
			heights.clear();
			excludedCount = 0;
			excludeFolderCreated = false;
		
			TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered
			
			String sourceFolder = config.originalImgFolder + "/" + String.format( subFolderFmt, volumeNo );
			
			System.out.format( "[Analyse] will compute statistics about content of <%s> ...\n", sourceFolder );
			
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
				
				System.out.format( "   Width  : [ %d - %d ] avg = %.1f deviation = %.1f\n", minW.getAsInt(), maxW.getAsInt(), avgW.getAsDouble(), standardDeviation );
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
				
				System.out.format( "   Height : [ %d - %d ] avg = %.1f deviation = %.1f\n", minH.getAsInt(), maxH.getAsInt(), avgH.getAsDouble(), standardDeviation );
			}
		}
	}
	
	public static void main(String[] args) {
		
		try {
			
			checkOriginalImages();
			System.out.format( "complete\n" );
			
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
