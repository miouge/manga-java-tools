package programs;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionFormat;

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

	// exclusion
	
	static int excludeWidthLessThan = -1;
	static int excludeWidthGreaterThan = -1;
	static int excludeHeightLessThan = -1;
	static int excludeHeightGreaterThan = -1;
	
	// rotation
	static double rotateImageBy = -90.0;
	

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
	
	static int forceSplitDoublePageImage = 0;
	static float splitOnlyIfRatioGreaterThan = 99F;
	static int firstPageIsLeftHalf = 1;	
	static double splitY1Ratio = 0.0;
	static double splitY2Ratio = 1.0;
	static double splitX1Ratio = 0.0;
	static double splitX2Ratio = 0.5;
	static double splitX3Ratio = 0.5;
	static double splitX4Ratio = 1.0;
	
	private static BufferedImage rotateImage( BufferedImage srcImage, double angle ) {
		
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

	static void splitImage( BufferedImage srcImage, FileItem fi, String destFolder ) throws Exception {
		
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		
		int leftX = 0;
		int rightX = width/2;
		
		int leftW = rightX - leftX;
		int rightW = width - rightX;
		
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
	}
	
	static boolean doSplitImage( BufferedImage srcImage ) throws Exception  {

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

	static boolean doRotageImage( BufferedImage srcImage ) throws Exception  {

		if(( -0.001 > rotateImageBy)||(rotateImageBy >= 0.001) ) {
			return true;
		}

		return false;
	}
	
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
	
	static void checkOriginalImage( Config config, int volumeNo, FileItem fi ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( fi.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );

		if( doExcludeImage( srcImage ) ) {

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
		
		boolean copyOriginal = true;
		
		if( doRotageImage( srcImage ) ) {
			
			srcImage = rotateImage( srcImage, rotateImageBy );
			copyOriginal = false;
			// TODO output srcImage to disk
		}
		
		if( doSplitImage( srcImage ) ) {
			
			String destinationPath = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo );
			splitImage( srcImage, fi, destinationPath );
			copyOriginal = false;
		}
		
		if( copyOriginal ) {
			
			String destinationPath = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo ) + "/" + fi.name;
			Files.copy(Paths.get( fi.fullpathname), Paths.get( destinationPath), StandardCopyOption.REPLACE_EXISTING );
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
		
		rotateImageBy            = Float.parseFloat( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "rotateImageBy", "0.0" ));
		
		forceSplitDoublePageImage   = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "forceSplitDoublePageImage"    , "0"  ));
		splitOnlyIfRatioGreaterThan = Float.parseFloat( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitOnlyIfRatioGreaterThan"  , "99" ));
		firstPageIsLeftHalf         = Integer.parseInt( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "firstPageIsLeftHalf"          , "0"  ));
		
		FractionFormat ff = new FractionFormat();
		Fraction fraction;

		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitY1Ratio", "0/100" ) );
		splitY1Ratio = fraction.doubleValue();
		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitY2Ratio", "100/100" ) );
		splitY2Ratio = fraction.doubleValue();
		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitX1Ratio", "0/100" ) );
		splitX1Ratio = fraction.doubleValue();
		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitX2Ratio", "50/100" ) );
		splitX2Ratio = fraction.doubleValue();
		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitX3Ratio", "50/100" ) );
		splitX3Ratio = fraction.doubleValue();
		fraction = ff.parse( Tools.getIniSetting( Config.settingsFilePath, "Analyse", "splitX4Ratio", "100/100" ) );
		splitX4Ratio = fraction.doubleValue();
	}	
	
	public static void checkOriginalImages() throws Exception {

		Config config = new Config();

		init( config );
		
		Tools.createFolder( config.analysedFolder, true, false );
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			widths.clear();
			heights.clear();
			excludedCount = 0;
			excludeFolderCreated = false;
		
			TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered
			
			String sourceFolder = config.originalImgFolder + "/" + String.format( subFolderFmt, volumeNo );
			String outputFolder = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo );
			
			Tools.createFolder( outputFolder, true, false );
			
			System.out.format( "[Analyse] will compute statistics about content of <%s> ...\n", sourceFolder );
			
			Tools.listInputFiles( sourceFolder, ".*\\.jpe?g", files, true, false ); // jpg or jpeg
			Tools.listInputFiles( sourceFolder, ".*\\.png"  , files, true, false );
			
			for( FileItem fi : files ) {
	
				checkOriginalImage( config, volumeNo, fi );
			}
			
			System.out.format( "   Total Images count : %d \n", files.size() );
			if( excludedCount > 0 ) {
				System.out.format( "   Excluded count : %d (moved apart to excludes/)\n", excludedCount );
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
