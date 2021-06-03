package programs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionFormat;

import beans.Config;
import beans.FileItem;
import beans.Tools;
import programs.autoCropper.Context;
import programs.autoCropper.CropDetectionResult;
import programs.autoCropper.DetectionParam;
import programs.autoCropper.FastRGB;
import programs.autoCropper.FileImg;
import programs.autoCropper.PixColor;
import programs.autoCropper.TypeDetected;

public class AutoCropper {
		
	static ArrayList<Integer> stdWs = new ArrayList<>();
	static ArrayList<Integer> stdHs = new ArrayList<>();
	static ArrayList<Integer> stdvCrops = new ArrayList<>();
	static ArrayList<Integer> stdhCrops = new ArrayList<>();
	
	static boolean folderStdCreated = false;
	static boolean folderCheckCreated = false;
	static boolean folderErrorCreated = false;
	static boolean folderUntouchedCreated = false;
	static boolean folderEmptyCreated = false;
	
	// loaded from settings.ini
	
	static Integer firstVol;
	static Integer lastVol;	
	
	static int borderMarginToIgnore = -1;

	static float fullHeight;
	static float pageNumbersUp;
	static float pageNumbersDown;
	static float fullWidth;
	static float pageNumbersLeft1;
	static float pageNumbersLeft2;
	static float pageNumbersRight1;
	static float pageNumbersRight2;
	
	static float nonWhiteNbRatio;
	static float nonBlackNbRatio;
	static int nonWhiteLevel;
	static int nonBlackLevel;
	static int alsoCropBlackArea;
	static int drawCroppingLine;
	
	static double toCheckCroppedFinalWidthRatio;
	static double toCheckCroppedFinalHeightRatio;
	
	static String subFolderFmt;

	static boolean isIgnoreBorderZone( int row, int col, int height, int width ) {
		
		// ignore these pixels close to the borders as it could include some scan artifacts
		// if borderMarginToIgnore = 5 -> ignore the 5 pixels zone close to the border
		// 0 mean : does not ignore anything
		
		if( borderMarginToIgnore <= 0 ) {
			return false;
		}
		
		final int borderLeft = borderMarginToIgnore;
		final int borderRight = width - borderMarginToIgnore; // 100 px - 5 = 95
		
		final int borderTop = borderMarginToIgnore;
		final int borderBottom = height - borderMarginToIgnore;
		
		if( row < borderTop     ) { return true; } 
		if( row >= borderBottom ) { return true; }
		if( col < borderLeft    ) { return true; } // 5 : ignore 0,1,2,3,4
		if( col >= borderRight  ) { return true; } // 5 : ignore 95 96 97 98 99
		
		return false;
	}
		
	static boolean isIgnoreZoneRelative( int row, int col, int height, int width ) {

		if( fullHeight        <= 0.0F ) { return false; }
		if( pageNumbersUp     <= 0.0F ) { return false; }
		if( pageNumbersDown   <= 0.0F ) { return false; }
		if( fullWidth         <= 0.0F ) { return false; }
		if( pageNumbersLeft1  <= 0.0F ) { return false; }
		if( pageNumbersLeft2  <= 0.0F ) { return false; }
		if( pageNumbersRight1 <= 0.0F ) { return false; }
		if( pageNumbersRight2 <= 0.0F ) { return false; }
		
		// expected page numbers position 
		
		final float fullH = fullHeight; // original image height on which the measurement has been done
		final float up      = pageNumbersUp / fullH;
		final float down    = pageNumbersDown / fullH;
		
		final float fullW = fullWidth; // original image width on which the measurement has been done
		final float left1   =  pageNumbersLeft1  / fullW;
		final float left2   =  pageNumbersLeft2  / fullW;
		final float right1  =  pageNumbersRight1 / fullW;
		final float right2  =  pageNumbersRight2 / fullW;

		//            left1         left2                 right1      right2   
		//              v             v                     v            v
		// up   ->
		//                page number                         page number
		// down ->
				
		if( (float)height*up < row && row < (float)height*down ) {
			
			// hauteur des numeros de page
			
			if(( (float)width*left1 < col && col < (float)width*left2 ) || ( (float)width*right1 < col && col < (float)width*right2 )) {
				
				// largeur des numero de page (Gauche ou Droite)
				
				return true; 
			}
		} 
		
		return false;
	}

	static boolean isIgnoreZone( int row, int col, int height, int width ) {
		
		if( isIgnoreBorderZone  ( row, col, height, width )) { return true; }
		if( isIgnoreZoneRelative( row, col, height, width )) { return true; }

		return false;
	}

	static void drawCroppingLineOnSource( Context context, FastRGB fastRGB, BufferedImage srcImage, CropDetectionResult cdr, int height, int width ) {
		
		int red   = new Color(255,0,0).getRGB();
		int blue  = new Color(0,0,255).getRGB();
		int white = new Color(255,255,255).getRGB();
		int black = new Color(0,0,0).getRGB();

		for( int row = 0 ; row < height ; row++ ) {
			
			int color = red;
			if( row % 3 == 0 ) {
				color = black;
			}
			else if( row % 5 == 0 ) {
				color = white;
			}
			
			srcImage.setRGB( cdr.firstCol, row, color  );
			srcImage.setRGB( cdr.lastCol , row, color );
		}
		
		for( int col = 0 ; col < width ; col++ ) {
			
			int color = blue;
			if( col % 3 == 0 ) {
				color = black;
			}
			else if( col % 5 == 0 ) {
				color = white;
			}
			
			srcImage.setRGB( col, cdr.firstRow, color  );
			srcImage.setRGB( col, cdr.lastRow , color );
		}
	}
	
	// guess y & h
	static void findCroppingRow( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, DetectionParam param, CropDetectionResult cdr, int height, int width ) throws IOException {

		// useless white area detection
		
		double nonWhiteNbMin = (double)width * param.nonWhiteNbRatio;
		int wfirstRow = -1;
		int wlastRow  = -1;
		
		// from the top ...

		for( int row = 0 ; row < height && wfirstRow == -1 ; row++ ) {

			int rowNonWhiteNb = 0;
			
			for( int col = 0 ; col < width && wfirstRow == -1 ; col++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey > param.nonWhiteLevel ) {  continue; } // pixel is white-like

				rowNonWhiteNb++;
			}
			
			if( rowNonWhiteNb > nonWhiteNbMin ) {

				wfirstRow = row;
				//System.out.format( "first row %d non white pixel = %d\n", row, rowNonWhiteNb );
				log.append( String.format( "first row %d non white pixel nb = %d\n", row, rowNonWhiteNb ));
			}
			
			if( row == (height - 1) && wfirstRow == -1 ) {
			
				// image if nearly full of white like pixel
				cdr.isEmpty = true;
				return;
			}
		}
		
		// from the bottom ...
		
		for( int row = height - 1 ; row >= 0 && wlastRow == -1 ; row-- ) {
			
			
			int rowNonWhiteNb = 0;
			
			for( int col = 0 ; col < width && wlastRow == -1 ; col++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey > param.nonWhiteLevel ) {  continue; } // pixel is white-like

				rowNonWhiteNb++;
			}

			if( rowNonWhiteNb > nonWhiteNbMin ) {

				wlastRow = row;
				//System.out.format( "last row %d non white pixel = %d\n", row, rowNonWhiteNb );
				log.append(String.format( "last row %d non white pixel = %d\n", row, rowNonWhiteNb ));
			}
		}
		
		if( param.alsoCropBlackArea <= 0 ) {

			cdr.firstRow = wfirstRow;
			cdr.lastRow = wlastRow;

			log.append(String.format("delta Row =%d\n", cdr.lastRow - cdr.firstRow ));
			cdr.vCrop = height - (( cdr.lastRow - cdr.firstRow )+1);
			return;
		}
		
		// useless black area detection
		
		int bfirstRow = -1;
		int blastRow  = -1;
		double nonBlackNbMin = (double)width * param.nonBlackNbRatio;
		
		// from the top ...

		for( int row = 0 ; row < height && bfirstRow == -1 ; row++ ) {

			
			int rowNonBlackNb = 0;
			
			for( int col = 0 ; col < width && bfirstRow == -1 ; col++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey < param.nonBlackLevel ) {  continue; } // pixel is black-like

				rowNonBlackNb++;
			}
			
			if( rowNonBlackNb > nonBlackNbMin ) {

				bfirstRow = row;
				//System.out.format( "first row %d non black pixel nb = %d\n", row, rowNonBlackNb );
				log.append( String.format( "first row %d non black pixel nb = %d\n", row, rowNonBlackNb ));
			}
			
			if( row == (height - 1) && bfirstRow == -1 ) {
				
				// image if nearly full of black like pixel
				cdr.isEmpty = true;
				return;
			}			
		}
		
		// from the bottom ...
		
		for( int row = height - 1 ; row >= 0 && blastRow == -1 ; row-- ) {
			
			int rowNonBlackNb = 0;
			
			for( int col = 0 ; col < width && blastRow == -1 ; col++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey < param.nonBlackLevel ) {  continue; } // pixel is black-like

				rowNonBlackNb++;
			}

			if( rowNonBlackNb > nonBlackNbMin ) {

				blastRow = row;
				//System.out.format( "last row %d non black pixel = %d\n", row, rowNonBlackNb );
				log.append(String.format( "last row %d non black pixel = %d\n", row, rowNonBlackNb ));
			}
		}
		
		if( wfirstRow != -1 && bfirstRow != -1 ) {
			cdr.firstRow = ( wfirstRow > bfirstRow ) ? wfirstRow : bfirstRow;
		}
		if( wlastRow != -1 && blastRow != -1 ) {
			cdr.lastRow = ( wlastRow < blastRow ) ? wlastRow : blastRow;
		}
		
		log.append(String.format("delta Row =%d\n", cdr.lastRow - cdr.firstRow ));
		
		cdr.vCrop = height - (( cdr.lastRow - cdr.firstRow )+1);
	}
	
	// guess x & w	
	static void findCroppingCol( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, DetectionParam param, CropDetectionResult cdr, int height, int width ) throws IOException {
		 
		// useless white area detection
		
		int wfirstCol = -1;
		int wlastCol  = -1;
		double nonWhiteNbMin = (double) height * param.nonWhiteNbRatio;

		// from the left
		
		for( int col = 0; col < width && wfirstCol == -1 ; col++ ) {
			
			int colNonWhiteNb = 0;
			
			for( int row = 0; row < height && wfirstCol == -1 ; row++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey > param.nonWhiteLevel ) {  continue; } // pixel is white-like
				
				colNonWhiteNb++;
			}
			
			if( colNonWhiteNb > nonWhiteNbMin ) {

				wfirstCol = col;
				// System.out.format( "first col %d non white pixel = %d\n", col, colNonWhiteNb );
				log.append(String.format("first col %d non white pixel = %d\n", col, colNonWhiteNb ));
			}
		}
		
		// from the right
		
		for( int col = width - 1; col >= 0 && wlastCol == -1 ; col-- ) {
			

			
			int colNonWhiteNb = 0;
			
			for( int row = 0; row < height && wlastCol == -1 ; row++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey > param.nonWhiteLevel ) {  continue; } // pixel is white-like
				
				colNonWhiteNb++;
			}

			if( colNonWhiteNb > nonWhiteNbMin ) {

				wlastCol = col;
				// System.out.format( "last col %d non white pixel = %d\n", col, colNonWhiteNb );
				log.append(String.format("last col %d non white pixel = %d\n", col, colNonWhiteNb ));
			}
		}
		
		if( param.alsoCropBlackArea <= 0 ) {

			cdr.firstCol = wfirstCol;
			cdr.lastCol = wlastCol;

			log.append(String.format("delta col=%d\n", cdr.lastCol - cdr.firstCol ));
			cdr.hCrop = width - (( cdr.lastCol - cdr.firstCol )+1);
			return;
		}
		
		// useless black area detection
		
		int bfirstCol = -1;
		int blastCol  = -1;
		double nonBlackNbMin = (double)width * param.nonBlackNbRatio;

		// from the left
		
		for( int col = 0; col < width && bfirstCol == -1 ; col++ ) {
						
			int colNonBlackNb = 0;
			
			for( int row = 0; row < height && bfirstCol == -1 ; row++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey < param.nonBlackLevel ) {  continue; } // pixel is black-like
				
				colNonBlackNb++;
			}
			
			if( colNonBlackNb > nonBlackNbMin ) {

				bfirstCol = col;
				// System.out.format( "first col %d non black pixel = %d\n", col, colNonWhiteNb );
				log.append(String.format("first col %d non black pixel = %d\n", col, colNonBlackNb ));
			}
		}
		
		// from the right
		
		for( int col = width - 1; col >= 0 && blastCol == -1 ; col-- ) {
						
			int colNonBlackNb = 0;
			
			for( int row = 0; row < height && blastCol == -1 ; row++ ) {
				
				if( isIgnoreZone( row, col, height, width )) { continue; }
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey < param.nonBlackLevel ) {  continue; } // pixel is black-like
				
				colNonBlackNb++;
			}

			if( colNonBlackNb > nonBlackNbMin ) {

				blastCol = col;
				// System.out.format( "last col %d non black pixel = %d\n", col, colNonWhiteNb );
				log.append(String.format("last col %d non black pixel = %d\n", col, colNonBlackNb ));
			}
		}
		
		if( wfirstCol != -1 && bfirstCol != -1 ) {
			cdr.firstCol = ( wfirstCol > bfirstCol ) ? wfirstCol : bfirstCol;
		}
		if( wlastCol != -1 && blastCol != -1 ) {
			cdr.lastCol = ( wlastCol < blastCol ) ? wlastCol : blastCol;
		}
			
		log.append(String.format("delta col=%d\n", cdr.lastCol - cdr.firstCol ));

		cdr.hCrop = width - (( cdr.lastCol - cdr.firstCol )+1);
	}
	
	static void findCropping( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, BufferedImage srcImage, DetectionParam param, CropDetectionResult cdr ) throws IOException {

		int height = srcImage.getHeight();
		int width = srcImage.getWidth();
		
		findCroppingRow( context, log, img, fastRGB, param, cdr, height, width );
		
		if( cdr.isEmpty ) { 
			return;
		}
		
		findCroppingCol( context, log, img, fastRGB, param, cdr, height, width );
				
		// System.out.format( "%s : Row [%d - %d] vCrop=%d\n", img.name, cdr.firstRow, cdr.lastRow, cdr.vCrop );
		// System.out.format( "%s : Col [%d - %d] hCrop=%d\n", img.name, cdr.firstCol, cdr.lastCol, cdr.hCrop );

		if( cdr.firstCol == -1 || cdr.firstRow == -1 || cdr.lastCol == -1 || cdr.lastRow == -1 ) {			
			return;
		}		
		
		if( drawCroppingLine > 0 ) {
			drawCroppingLineOnSource( context, fastRGB, srcImage, cdr, height, width );
		}
		// record cropping directives
		
		img.x = cdr.firstCol;
		img.w = cdr.lastCol - cdr.firstCol; 

		img.y = cdr.firstRow;
		img.h = cdr.lastRow - cdr.firstRow;
		
	}
	
	static int countBorderUse( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, BufferedImage srcImage, int height, int width ) throws IOException {
		
		int nonWhiteNb = 0;
		
		for( int row = 0 ; row < height ; row++ ) {

			if( row > 70 && row < (height - 70)) continue; // ignore center area
			
			for( int col = 0 ; col < width ; col++ ) {
			
				if( col > 60 && col < (width - 60)) continue; // ignore center area

				// in border clore area ...
				
				PixColor pxColor = fastRGB.getColor( col, row );
				if( pxColor.grey > 200 ) {  continue; } // pixel is white-like
				
				nonWhiteNb++;
			}			
		}
		
		log.append(String.format("%s countBorderUse : on border close nonWhiteNb=%d\n", img.name, nonWhiteNb ));
		return nonWhiteNb;
	}	

	static void findImageType( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, BufferedImage srcImage ) throws IOException {
		
		log.append( String.format("--- %s ---\n", img.name ) );
		
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();		
		// System.out.format( "%s : HxW %dx%d\n", img.name, height, width );
		
		if( (double)height / (double)width < 1.0 ) { // vivlio screen ration = 4/3 (1.33)
			
			// h / w ratio is unusual
			img.typeDetected = TypeDetected.tocheck;
			System.out.format("%s -> tocheck (h/w ratio is unusual)\n", img.name );
			return;
		}
		if( (double)height / (double)width > 2.0 ) { // vivlio screen ration = 4/3 (1.33)
			
			// h / w ratio is unusual
			img.typeDetected = TypeDetected.tocheck;
			System.out.format("%s -> tocheck (h/w ratio is unusual)\n", img.name );
			return;
		}
		
		int ymargin = 1; // height padding after cropping detection
		int xmargin = 1; // width padding after cropping detection
			
		// find standard drawing at first
		{	
			DetectionParam param = new DetectionParam();
			CropDetectionResult cdr = new CropDetectionResult();
			
			param.nonWhiteNbRatio = nonWhiteNbRatio; // 0.25 = 25% = 1 sur 4
			param.nonBlackNbRatio = nonBlackNbRatio; // 0.25 = 25% = 1 sur 4
			param.nonWhiteLevel = nonWhiteLevel; // below this grey level
			param.nonBlackLevel = nonBlackLevel; // greater than this grey level
			param.alsoCropBlackArea = alsoCropBlackArea;
			
			try {
				
				findCropping( context, log, img, fastRGB, srcImage, param, cdr );
			} 
			catch( Exception e )
			{
				e.printStackTrace();
				System.out.format("%s -> tocheck (findCropping() throw an exception)\n", img.name );
				img.typeDetected = TypeDetected.tocheck;
				return;
			}
			
			if( cdr.isEmpty ) {
				img.typeDetected = TypeDetected.empty;
				return;				
			}
			
			if( cdr.firstCol == -1 || cdr.firstRow == -1 || cdr.lastCol == -1 || cdr.lastRow == -1 ) {
				
				System.out.format("%s -> tocheck (no crop directives)\n", img.name );
				img.typeDetected = TypeDetected.tocheck;
				return;
			}
			
			if( ((double)(height - cdr.vCrop)/(double)height) < toCheckCroppedFinalHeightRatio ) {

				img.typeDetected = TypeDetected.tocheck;
				System.out.format("%s -> tocheck (final image height ratio would have been %.3f (< %.3f) of original\n", img.name, ((double)(height - cdr.vCrop)/(double)height), toCheckCroppedFinalHeightRatio );
				return;				
			}

			if( ((double)(width - cdr.hCrop)/(double)width) < toCheckCroppedFinalWidthRatio ) {

				img.typeDetected = TypeDetected.tocheck;
				System.out.format("%s -> tocheck (final image width ratio would have been %.3f (< %.3f) of original\n", img.name, ((double)(width - cdr.hCrop)/(double)width), toCheckCroppedFinalWidthRatio );
				return;				
			}
			
			// find fullpage drawing to be left untouched
			
			/*
			 * int borderUse = countBorderUse( context, log, img, fastRGB, srcImage, height,
			 * width ); if( borderUse > 5000 ) {
			 * 
			 * img.typeDetected = TypeDetected.untouched; return; }
			 */
			
			// standard case as previously defined
			
			stdWs.add( img.w );
			stdHs.add( img.h );
			stdvCrops.add( cdr.vCrop );
			stdhCrops.add( cdr.hCrop );
			
			// add margin if possible
			if(( img.x - xmargin ) > 0 ) {
				img.x -= xmargin;
			}
			if(( img.y - ymargin ) > 0 ) {
				img.y -= ymargin;
			}			
			if(( img.x + img.w + xmargin*2) < width ) {
				img.w += xmargin*2;
			}			
			if((img.y + img.h + ymargin*2) < height) {
				img.h += ymargin*2;
			}
					
			// System.out.format( "%s : cropping ...", img.name );
			// System.out.format( "rows (y=%d h=%d) columns (x=%d w=%d) \n", img.y, img.h, img.x, img.w );
				
			img.typeDetected = TypeDetected.standard;
			return;
		}
	}
		
	static void processImg( Context context, FileImg img ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( img.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );
				
		FastRGB fastRGB = new FastRGB( srcImage );

		StringBuffer log = new StringBuffer();
		
		findImageType( context, log, img, fastRGB, srcImage );

		context.writer.write(log.toString());
		
		// auto detect the image format from the file it's extension
		String format = null;
		String fileName = file.getName();	
		
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
			throw new Exception( " unable to detect the image format ");
		}

		switch( img.typeDetected ) {
		
			case standard: {
				
				if( folderStdCreated == false ) {
					Files.createDirectories(Paths.get( context.outpath + "/std"));
					folderStdCreated = true;
				}

				File outputfile = new File( context.outpath + "/std/" + img.name );
				BufferedImage croppedImage = srcImage.getSubimage( img.x, img.y, img.w, img.h );

				if( drawCroppingLine > 0 ) { 
					
					// instead of cropped image write the source image with the cropping lines
					ImageIO.write( srcImage, format, outputfile );
				}
				else {
					ImageIO.write( croppedImage, format, outputfile );
				
				}
				context.std++;

				break;
			}
			case tocheck: {

				if( folderCheckCreated == false ) {
					Files.createDirectories(Paths.get( context.outpath + "/tocheck"));
					folderCheckCreated = true;
				}
				
				File outputfile = new File( context.outpath + "/tocheck/" + img.name );
				ImageIO.write( srcImage, format, outputfile );
				context.tocheck++;
				
				break;
			}
			case untouched: {
				
				if( folderUntouchedCreated == false ) {
					Files.createDirectories(Paths.get( context.outpath + "/untouched"));
					folderUntouchedCreated = true;
				}
				
				File outputfile = new File( context.outpath + "/untouched/" + img.name );
				ImageIO.write( srcImage, format, outputfile );
				context.untouched++;
				
				break;
			}
			case error : {
				
				if( folderErrorCreated == false ) {
					Files.createDirectories(Paths.get( context.outpath + "/error"));
					folderErrorCreated = true;
				}
				
				File outputfile = new File( context.outpath + "/error/" + img.name );
				ImageIO.write( srcImage, format, outputfile );
				context.error++;
				
				break;
			}
			
			case empty: {
				
				if( folderEmptyCreated == false ) {
					Files.createDirectories(Paths.get( context.outpath + "/empty"));
					folderEmptyCreated = true;
				}

				File outputfile = new File( context.outpath + "/empty/" + img.name );
				ImageIO.write( srcImage, format, outputfile );
				context.empty++;
				
				break;
			}
			
			default:
			case undefined: {
				throw new Exception( " unexpected case ");
			}
		}
	}
	
	static void init( Config config ) throws Exception {
		
		firstVol = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "firstVolume", "1" ));
		lastVol  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "General", "lastVolume" , "1" ));
		
		borderMarginToIgnore = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "borderMarginToIgnore"     , "0" ));
		
		fullHeight         = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "fullHeight"       , "-1" ));
		pageNumbersUp      = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersUp"    , "-1" ));
		pageNumbersDown    = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersDown"  , "-1" ));
		fullWidth          = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "fullWidth"        , "-1" ));
		pageNumbersLeft1   = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersLeft1" , "-1" ));
		pageNumbersLeft2   = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersLeft2" , "-1" ));
		pageNumbersRight1  = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersRight1", "-1" ));
		pageNumbersRight2  = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "pageNumbersRight2", "-1" ));
		
		nonWhiteNbRatio    = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "nonWhiteNbRatio"   , "0.005" ));
		nonBlackNbRatio    = Float.parseFloat( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "nonBlackNbRatio"   , "0.005" ));
		nonWhiteLevel      = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "nonWhiteLevel"     , "175" ));
		nonBlackLevel      = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "nonBlackLevel"     , "80" ));
		alsoCropBlackArea  = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "alsoCropBlackArea" , "0" ));
		drawCroppingLine   = Integer.parseInt( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "drawCroppingLine"  , "0" ));
		
		FractionFormat ff = new FractionFormat();
		Fraction fraction;				
		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "toCheckCroppedFinalWidthRatio"   , "0/100" ) );
		toCheckCroppedFinalWidthRatio = (float) fraction.doubleValue();

		fraction = ff.parse( Tools.getIniSetting( config.settingsFilePath, "AutoCropper", "toCheckCroppedFinalHeightRatio"  , "0/100" ) );
		toCheckCroppedFinalHeightRatio = (float) fraction.doubleValue();
		
		subFolderFmt = Tools.getIniSetting( config.settingsFilePath, "General", "subFolderFmt", "T%02d" );		
	}	
	
	public static void autoCrop() throws Exception {

		Config config = new Config();
		
		init( config );
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
		
			folderStdCreated = false;
			folderCheckCreated = false;
			folderErrorCreated = false;
			folderUntouchedCreated = false;
			folderEmptyCreated = false;
			
			stdWs.clear();
			stdHs.clear();
			stdvCrops.clear();
			stdhCrops.clear();
			
			Context context = new Context();
			context.srcpath = config.analysedFolder + "/" + String.format( subFolderFmt, volumeNo );
			context.outpath = config.croppedImgFolder  + "/" + String.format( subFolderFmt, volumeNo );
			String logfile = context.outpath + "/autoCropper.log";
			
			System.out.format( "[AutoCropper] will crop images of <%s> ...\n", context.srcpath );
			
			// drop output folders if already exist then re-create it
			Tools.createFolder( context.outpath, true, false );
	
			try ( BufferedWriter writer = new BufferedWriter(new FileWriter(logfile)) )
			{
				context.writer = writer;
				
				TreeSet<FileItem> files = new TreeSet<>(); // Naturally ordered
				
				// list but not recursive to not process the excludes/ subfolder if this one exist
				Tools.listInputFiles( context.srcpath, ".*\\.jpe?g", files, false, false ); // jpg or jpeg
				Tools.listInputFiles( context.srcpath, ".*\\.png", files, false, false );
				
				for( FileItem fi : files ) {
					
					FileImg img = new FileImg();
					img.fullpathname = fi.fullpathname;
					img.name = fi.name;
					
					processImg( context, img );
				}
				
				// compute statistics
				
				System.out.format( "std=%d untouched=%d empty =%d tocheck=%d error=%d [total=%d]\n", context.std, context.untouched, context.empty, context.tocheck, context.error, (context.std+context.untouched+context.tocheck+context.error+context.empty) );
				
				if( stdWs.size() > 0 ) {
					OptionalDouble avgW = stdWs.stream().mapToInt(Integer::intValue).average();
					System.out.format( "std final avg Width  = %.1f ", avgW.getAsDouble());
					
					if( stdvCrops.size() > 0 ) {
						OptionalDouble avgCropW = stdvCrops.stream().mapToInt(Integer::intValue).average();
						System.out.format( "avg horizontal crop = %.1f (%.2f%%)\n", avgCropW.getAsDouble(), avgCropW.getAsDouble()/avgW.getAsDouble()*100.0 );
					}
					else {
						System.out.format( "\n" );
					}
				}
				if( stdHs.size() > 0 ) {
					
					OptionalDouble avgH = stdHs.stream().mapToInt(Integer::intValue).average();
					System.out.format( "std final avg Heigth = %.1f ", avgH.getAsDouble());
					
					if( stdhCrops.size() > 0 ) {
						OptionalDouble avgCropH = stdhCrops.stream().mapToInt(Integer::intValue).average();
						System.out.format( "avg vertical crop = %.1f (%.2f%%)\n", avgCropH.getAsDouble(), avgCropH.getAsDouble()/avgH.getAsDouble()*100.0 );
					}	
					else {
						System.out.format( "\n" );
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		try {

			autoCrop();
			System.out.format( "complete\n" );
			
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
