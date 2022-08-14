package programs;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.imageio.ImageIO;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionFormat;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class Analyze {
	
	ArrayList<Integer> widths = new ArrayList<>();
	ArrayList<Integer> heights = new ArrayList<>();
	ArrayList<Double> ratios = new ArrayList<>();
			
	boolean excludeFolderCreated = false;
	int excludedCount = 0;
	int splittedCount = 0;
	int rotatedCount = 0;

	// loaded from settings.ini
	
	Integer firstVol;
	Integer lastVol;
	String subFolderFmt;
	boolean cleanupSubFolders = true;  // default behavior is to drop existing target subfolders then recreate it

	// exclusion
	
	int excludeWidthLessThan = -1;
	int excludeWidthGreaterThan = -1;
	int excludeHeightLessThan = -1;
	int excludeHeightGreaterThan = -1;
	
	// rotation
	double rotateImageBy = 0.0;
	float rotateOnlyIfRatioGreaterThan = 99F;
	

	// Spitting
	//  0     X1        X2      X3           X4   Width
	//        |          |       |            |
	//  Y1 ----------------------------------------
	//        |          |       |            |
	//        |          |       |            |
	//        |   left   |       |   right    |
	//        |   half   |       |   half     |
	//        |          |       |            |
	//        |          |       |            |
	//  Y2 ----------------------------------------
	//        |          |       |            |
	//  Height
	
	int forceSplitDoublePageImage = 0;
	float splitOnlyIfRatioGreaterThan = 99F;
	int firstPageIsLeftHalf = 1;	
	double splitY1Ratio = 0.0;
	double splitY2Ratio = 1.0;
	double splitX1Ratio = 0.0;
	double splitX2Ratio = 0.5;
	double splitX3Ratio = 0.5;
	double splitX4Ratio = 1.0;
	
	private BufferedImage rotateImage( BufferedImage srcImage, double angle ) {
		
		double radian = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(radian));
		double cos = Math.abs(Math.cos(radian));

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
		int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);

		BufferedImage rotatedImage = new BufferedImage( nWidth, nHeight, srcImage.getType() );
		Graphics2D graphics = rotatedImage.createGraphics();

		graphics.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );

		graphics.translate( (nWidth - width) / 2, (nHeight - height) / 2 );
		// rotation around the center point
		graphics.rotate(radian, (double) (width / 2), (double) (height / 2));
		graphics.drawImage(srcImage, 0, 0, null);
		graphics.dispose();

		return rotatedImage;
	}

	void splitImage( BufferedImage srcImage, FileItem fi, String destFolder ) throws Exception {
		
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		
		int leftX  = (int)Math.floor((double)width * splitX1Ratio);
		int rightX = (int)Math.floor((double)width * splitX3Ratio);
		
		int leftW  = (int)Math.ceil((double)width * splitX2Ratio) - leftX;
		int rightW = (int)Math.ceil((double)width * splitX4Ratio) - rightX;
		
		int leftY = (int)Math.floor((double)height * splitY1Ratio);
		int rightY = leftY;
		
		int leftH = (int)Math.ceil((double)height * splitY2Ratio) - leftY;
		int rightH = leftH;
				
		BufferedImage leftHalf  = srcImage.getSubimage( leftX, leftY, leftW, leftH );
		BufferedImage rightHalf = srcImage.getSubimage( rightX, rightY, rightW, rightH );
		
		// auto detect the image format from the file it's extension
		String format = Tools.getImageFormat(fi.name);
		
		String filenameL = "";
		String filenameR = "";
		
		String[] parts = fi.name.split("\\.");
		for( int i = 0 ; i < (parts.length-1) ; i++ ) {
			
			filenameL += parts[i];
			filenameR += parts[i];
		}
		
		if( firstPageIsLeftHalf == 1 ) {

			// first page is left half
			filenameL += "a" + "." + format;
			filenameR += "b" + "." + format;
			
		} else {

			
			filenameL += "b" + "." + format;
			filenameR += "a" + "." + format;
		}

		File outputfileL = new File( destFolder + "/" + filenameL );
		File outputfileR = new File( destFolder + "/" + filenameR );
		ImageIO.write( leftHalf , format, outputfileL );
		ImageIO.write( rightHalf, format, outputfileR );
		
		widths.add(leftHalf.getWidth());
		heights.add(leftHalf.getHeight());
		widths.add(rightHalf.getWidth());
		heights.add(rightHalf.getHeight());
	}
	
	boolean doSplitImage( BufferedImage srcImage ) throws Exception  {

		if( forceSplitDoublePageImage == 1 ) {
			return true;
		}
		
		float width = (float)srcImage.getWidth();
		float height = (float)srcImage.getHeight();
		
		float ratio = width / height;
		if( ratio > splitOnlyIfRatioGreaterThan ) {
			return true;
		}

		return false;
	}

	boolean doRotageImage( BufferedImage srcImage ) throws Exception  {

		if(( rotateImageBy < -0.0001 )||( 0.0001 < rotateImageBy ) ) {
			
			// rotateImageBy not equal to 0.0
			
			if( rotateOnlyIfRatioGreaterThan < 99.0F ) {

				// rotateOnlyIfRatioGreaterThan is defined (that is a condition on the rotation) 
				
				float width  = (float)srcImage.getWidth();
				float height = (float)srcImage.getHeight();
				
				float ratio = width / height;
				if( ratio > rotateOnlyIfRatioGreaterThan ) {
					return true;
				}
			}
			else {
			
				return true;
			}
		}

		return false;
	}
	
	boolean doExcludeImage( BufferedImage srcImage ) throws Exception  {

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
	
	void processOriginalImage( Config config, int volumeNo, FileItem fi ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( fi.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );
		
		if( doExcludeImage( srcImage ) ) {

			// copy original image to analysed-img/T.../excludes/ ...

			String excludeFolder = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo ) +  "/excludes";

			if( excludeFolderCreated == false ) {

				// drop output folders if already exist then re-create it
				Tools.createFolder( excludeFolder, true, false );
				excludeFolderCreated = true;
			}
			
			String destinationPath = excludeFolder + "/" + fi.name;
			Files.copy(Paths.get( fi.fullpathname), Paths.get( destinationPath), StandardCopyOption.REPLACE_EXISTING );			
			excludedCount++;
			return;
		}
		
		widths.add(srcImage.getWidth());
		heights.add(srcImage.getHeight());
		ratios.add( (srcImage.getWidth() * 1.0) / (srcImage.getHeight() * 1.0) );
		
		boolean copyOriginal = true;
		
		if( doRotageImage( srcImage ) ) {
			
			srcImage = rotateImage( srcImage, rotateImageBy );
			copyOriginal = false;
			rotatedCount++;
		}
		
		if( doSplitImage( srcImage ) ) {
			
			// split current srcImage and save the two half into analysed-img/Tn/ ...
			
			String destinationPath = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo );
			splitImage( srcImage, fi, destinationPath );
			splittedCount++;
			return;
		}
		
		String destinationPath = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo ) + "/" + fi.name;
		
		if( copyOriginal ) {
						
			Files.copy(Paths.get( fi.fullpathname), Paths.get( destinationPath), StandardCopyOption.REPLACE_EXISTING );
 		}
		else {
			// output srcImage to analysed-img/Tn/ ...
			// auto detect the image format from the file it's extension
			String format = Tools.getImageFormat(fi.name);
			File outputfile = new File( destinationPath );
			ImageIO.write( srcImage , format, outputfile );
		}		
	}
	
	void init( Config config ) throws Exception {
		
		FractionFormat ff = new FractionFormat();
		Fraction fraction;
		
		firstVol = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "firstVolume", "-1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "lastVolume" , "-1" ));
		subFolderFmt = Tools.getIniSetting( config.settingsFilePath, "General", "subFolderFmt", "T%02d" );
		cleanupSubFolders = Boolean.parseBoolean( Tools.getIniSetting( config.settingsFilePath, "General", "cleanupSubFolders", "true" ));		
		
		excludeWidthLessThan     = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "excludeWidthLessThan"     , "-1" ));
		excludeWidthGreaterThan  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "excludeWidthGreaterThan"  , "-1" ));
		excludeHeightLessThan    = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "excludeHeightLessThan"    , "-1" ));
		excludeHeightGreaterThan = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "excludeHeightGreaterThan" , "-1" ));

		rotateImageBy            = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "Analyse", "rotateImageBy", "0.0" ));
		
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "rotateOnlyIfRatioGreaterThan"  , "100/1" ) );
		rotateOnlyIfRatioGreaterThan = fraction.floatValue();
		
		forceSplitDoublePageImage   = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "forceSplitDoublePageImage", "0"  ));
		
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitOnlyIfRatioGreaterThan"  , "100/1" ) );		
		splitOnlyIfRatioGreaterThan = fraction.floatValue();		
		
		firstPageIsLeftHalf         = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "Analyse", "firstPageIsLeftHalf", "0"  ));		

		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitY1Ratio", "0/100" ) );
		splitY1Ratio = fraction.doubleValue();    
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitY2Ratio", "100/100" ) );
		splitY2Ratio = fraction.doubleValue();    
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitX1Ratio", "0/100" ) );
		splitX1Ratio = fraction.doubleValue();    
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitX2Ratio", "50/100" ) );
		splitX2Ratio = fraction.doubleValue();    
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitX3Ratio", "50/100" ) );
		splitX3Ratio = fraction.doubleValue();    
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "Analyse", "splitX4Ratio", "100/100" ) );
		splitX4Ratio = fraction.doubleValue();
	}	
	
	public void analyzeOriginalImages(Config config) throws Exception {
		
		init( config );
		
		Tools.createFolder( config.analysedFolder, cleanupSubFolders, false );
		
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
			
			widths.clear();
			heights.clear();
			ratios.clear();
			excludedCount = 0;
			splittedCount = 0;
			rotatedCount = 0;
			excludeFolderCreated = false;
		
			ArrayList<FileItem> files = new ArrayList<>();
			
			String sourceFolder = config.originalImgFolder + "/" + String.format( subFolderFmt, volumeNo );
			String outputFolder = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo );
						
			System.out.format( "[Analyze] will compute statistics about content of <%s> ...\n", sourceFolder );
			
			Path path = Paths.get(sourceFolder);			
			if( Files.exists(path) == false ) {
				
				if( lastVol > 0 ) {
					System.err.format( "error ! original img folder does not exist <%s>...\n", sourceFolder );
				}
				break;
			}
			
			Tools.createFolder( outputFolder, cleanupSubFolders, true );			
			
			Tools.listInputFiles( sourceFolder, ".*\\.jpe?g", files, true, false ); // jpg or jpeg
			Tools.listInputFiles( sourceFolder, ".*\\.png"  , files, true, false );
			
			for( FileItem fi : files ) {
	
				processOriginalImage( config, volumeNo, fi );
			}
			
			System.out.format( "   Total Images count : %d \n", files.size() );
			if( excludedCount > 0 ) {
				System.err.format( "   *** Excluded count : %d (moved apart to excludes/)\n", excludedCount );
			}
			
			// compute statistics
						
			if( widths.size() > 0 && heights.size() > 0 && ratios.size() > 0 ) {
				
				OptionalInt maxW = widths.stream().mapToInt(Integer::intValue).max();
				OptionalInt minW = widths.stream().mapToInt(Integer::intValue).min();
				// OptionalDouble avgW = widths.stream().mapToInt(Integer::intValue).average();
				
				OptionalInt maxH = heights.stream().mapToInt(Integer::intValue).max();
				OptionalInt minH = heights.stream().mapToInt(Integer::intValue).min();
				// OptionalDouble avgH = heights.stream().mapToInt(Integer::intValue).average();
				
				OptionalDouble maxR = ratios.stream().mapToDouble(Double::doubleValue).max();
				OptionalDouble minR = ratios.stream().mapToDouble(Double::doubleValue).min();
				// OptionalDouble avgR = ratios.stream().mapToDouble(Double::doubleValue).average();
				
		        // Variance
				// double mean = widths.stream().mapToInt(Integer::intValue).average().getAsDouble();
		        // double variance = widths.stream().map( i -> i - mean ).map( i -> i*i ).mapToDouble( i -> i ).average().getAsDouble();		        
		        // Standard Deviation 
		        // double standardDeviation = Math.sqrt(variance);
				
				String trace = "";
				
		        if( minW.getAsInt() == maxW.getAsInt() )
		        {
		        	trace += String.format( "   Width [ %d ]", minW.getAsInt() );		        	
		        }
		        else
		        {
		        	trace += String.format( "   Width [ %d - %d ]", minW.getAsInt(), maxW.getAsInt() );
		        }
		        if( minH.getAsInt() == maxH.getAsInt() )
		        {
		        	trace += String.format( " Height [ %d ]", minH.getAsInt() );		        	
		        }
		        else
		        {
		        	trace += String.format( " Height [ %d - %d ]", minH.getAsInt(), maxH.getAsInt() );
		        }
		        
	        	trace += String.format( " -> Ratio [ %.2f - %.2f ]", minR.getAsDouble(), maxR.getAsDouble() );
	        	System.out.println( trace );
			}
			if( splittedCount > 0 ) {
				System.err.format( "   *** Splitted count : %d\n", splittedCount );
			}
			if( rotatedCount > 0 ) {
				System.err.format( "   *** Rotated count : %d\n", rotatedCount );
			}
			
			volumeNo++;
		}
		while( true );
	}
	
	public static void main(String[] args) {
		
		try {
			
			Config config = new Config();
			Analyze analyze = new Analyze();
			analyze.analyzeOriginalImages(config);
			System.out.format( "complete\n" );
			
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
