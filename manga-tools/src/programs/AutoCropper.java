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

	// TODO : customize according to the page number position
	static boolean isIgnoreZoneRelative( int row, int col, int height, int width ) {

		// expected page number position 
		
		float fullH = 3956.0F;
		float up      = 2886 / fullH;
		float down    = 2958 / fullH;
		
		float fullW = 1952.0F;
		float left1   =  141  / fullW;
		float left2   =  276  / fullW;
		float right1  =  1668  / fullW;
		float right2  =  1776  / fullW;

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

	// TODO : customize according to the page number position
	static boolean isIgnoreZoneAbs( int row, int col, int height, int width ) {
		
		// return true is the position could be a page marker position
		// page number position :
		
		// v--- hauteur n° de page ---v       v--- en bas a gauche ---v         v--- en bas a droite ---v

		// Wakfu
		//if( 2900 < row && row < 2960 ) { if(( 100 < col && col < 252 ) || ( 1695 < col && col < 1832 )) { return true; }}		
		
		// Dragon Ball
		// if( 1920 < row && row < 1960 ) { if(( 100 < col && col < 150 ) || ( 1220 < col && col < 1290 )) { return true; }}
		
		// Nana to kaoru
		// if( 1635 < row && row < 1670 ) { if(( 60  < col && col < 120 ) || ( 1080 < col && col < 1134 )) { return true; }}
		
		 // Step up love story
		// if( 2540 < row && row < 2615 ) { if(( 156 < col && col < 244 ) || ( 1664 < col && col < 1764 )) { return true; }}
		
		// Dragon Ball
		// if( 1920 < row && row < 1960 ) { if(( 100 < col && col < 150 ) || ( 1220 < col && col < 1290 )) { return true; }}
		   		
		
		return false;
	}
	
	// TODO : customize according to the page number position
	static boolean isIgnoreZone( int row, int col, int height, int width ) {
		
		// TODO : should chain multiple ignore zone
		return isIgnoreZoneAbs( row, col, height, width );
		//return isIgnoreZoneRelative( row, col, height, width );
	}

	static void drawCroppingLineOnSource( Context context, FastRGB fastRGB, BufferedImage srcImage, CropDetectionResult cdr, int height, int width ) {
		
		int red  = new Color(255,0,0).getRGB();
		int green = new Color(0,255,0).getRGB();

		for( int row = 0 ; row < height ; row++ ) {
			
			srcImage.setRGB( cdr.firstCol, row, red  );
			srcImage.setRGB( cdr.lastCol , row, red );
		}
		
		for( int col = 0 ; col < width ; col++ ) {
			
			srcImage.setRGB( col, cdr.firstRow, green  );
			srcImage.setRGB( col, cdr.lastRow , green );
		}
	}
	
	// guess y & h
	static void findCroppingRow( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, DetectionParam param, CropDetectionResult cdr, int height, int width ) throws IOException {
				
		int borderTop = param.border;
		int borderBottom = height - param.border;
		
		// useless white area detection
		
		double nonWhiteNbMin = (double)width * param.nonWhiteNbRatio;
		int wfirstRow = -1;
		int wlastRow  = -1;
		
		// from the top ...

		for( int row = 0 ; row < height && wfirstRow == -1 ; row++ ) {

			if( row < borderTop ) continue; // ignore pixel on the border
			
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
			
			if( row > borderBottom ) continue; // ignore pixel on the border
			
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
		
		// useless black area detection
		
		int bfirstRow = -1;
		int blastRow  = -1;
		double nonBlackNbMin = (double)width * param.nonBlackNbRatio;
		
		// from the top ...

		for( int row = 0 ; row < height && bfirstRow == -1 ; row++ ) {

			if( row < borderTop ) continue; // ignore pixel on the border
			
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
		}
		
		// from the bottom ...
		
		for( int row = height - 1 ; row >= 0 && blastRow == -1 ; row-- ) {
			
			if( row > borderBottom ) continue; // ignore pixel on the border
			
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

		int borderLeft = param.border;
		int borderRight = width - param.border;
		 
		// useless white area detection
		
		int wfirstCol = -1;
		int wlastCol  = -1;
		double nonWhiteNbMin = (double) height * param.nonWhiteNbRatio;

		// from the left
		
		for( int col = 0; col < width && wfirstCol == -1 ; col++ ) {
			
			if( col < borderLeft ) continue;
			
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
			
			if( col > borderRight ) continue;
			
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
		
		// useless black area detection
		
		int bfirstCol = -1;
		int blastCol  = -1;
		double nonBlackNbMin = (double)width * param.nonBlackNbRatio;

		// from the left
		
		for( int col = 0; col < width && bfirstCol == -1 ; col++ ) {
			
			if( col < borderLeft ) continue;
			
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
			
			if( col > borderRight ) continue;
			
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
		
		if( Config.drawCroppingLine ) {
			drawCroppingLineOnSource( context, fastRGB, srcImage, cdr, height, width );
		}
		// cropping directives
		
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

	// used for maison IKKOKU
	static void findTypeScanned( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, BufferedImage srcImage ) throws IOException {
		
		// specification de la dimension standard du cadre attendue (dans la majorite des cas)

		int stdHeight =  1158;
		int stdWidth  =  1042;	
		int tolerancy =   35;
		
		int ymargin = 3; // height padding after cropping detection
		int xmargin = 3; // width padding after cropping detection
		
		log.append( String.format("--- %s ---\n", img.name ) );
		
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();
		// System.out.format( "%s : HxW %dx%d\n", img.name, height, width );
		
		int stdHeightMin = stdHeight - tolerancy;
		int stdHeightMax = stdHeight + tolerancy;
		
		int stdWidthMin = stdWidth - tolerancy;
		int stdWidthMax = stdWidth + tolerancy;
		
		// find standard drawing at first
		{	
			DetectionParam param = new DetectionParam();
			CropDetectionResult cdr = new CropDetectionResult();
			
			param.border = 20;            // ignore these pixels close to the borders	
			param.nonWhiteNbRatio = 0.10; // 0.25 = 25% = 1 sur 4
			param.nonWhiteLevel = 125;    // below this level 
			
			findCropping( context, log, img, fastRGB, srcImage, param, cdr );
					
			if( img.w >= stdWidthMin && img.h >= stdHeightMin && img.w <= stdWidthMax && img.h <= stdHeightMax ) {
				
				// standard case as previously defined
				
				stdWs.add( img.w );
				stdHs.add( img.h );			
				
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
				
				System.out.format( "%s : cropping ...", img.name );
				System.out.format( "rows (y=%d h=%d) columns (x=%d w=%d) \n", img.y, img.h, img.x, img.w );		
				
				img.typeDetected = TypeDetected.standard;
				return;
			}
			
			/*
			if( img.w < stdWidthMin  && img.h < stdHeightMin ) {
				
				// to small : cancel cropping
				img.typeDetected = TypeDetected.untouched;
				return;				
			}
			*/
		}
		
		// find small image to be left untouched
		
		{	
			DetectionParam param = new DetectionParam();
			CropDetectionResult cdr = new CropDetectionResult();
			
			param.border = 10;            // ignore these pixels close to the borders	
			param.nonWhiteNbRatio = 0.05; // 0.25 = 25% = 1 sur 4
			param.nonWhiteLevel = 170;    // below this level 
			
			findCropping( context, log, img, fastRGB, srcImage, param, cdr );

			if( img.w < (stdWidthMin*0.8)  && img.h < (stdHeightMin*0.8) ) {
				
				// to small : cancel cropping
				img.typeDetected = TypeDetected.untouched;
				return;				
			}
		}
		
		// find fullpage drawing to be left untouched		
		
		int borderUse = countBorderUse( context, log, img, fastRGB, srcImage, height, width );		
		if( borderUse > 5000 ) {
			
			img.typeDetected = TypeDetected.untouched;
			return;				
		}		

		/*
		// find fullpage drawing
		{	
			DetectionParam param = new DetectionParam(); 
			
			param.border = 5;             // ignore these pixels close to the borders	
			param.nonWhiteNbRatio = 0.05; // 0.25 = 25% = 1 sur 4
			param.nonWhiteLevel = 200;    // below this level 
			
			findCropping( context, log, img, fastRGB, srcImage, param );

			int littleCropCount = 0;
			if( img.firstRow < minCropFromBorder ) littleCropCount++;
			if( img.firstCol < minCropFromBorder ) littleCropCount++;
			if( (height - img.lastCol) < minCropFromBorder ) littleCropCount++;
			if( (width  - img.lastCol) < minCropFromBorder ) littleCropCount++;
			
			if( littleCropCount >= 3 ) {
				
				img.typeDetected = TypeDetected.untouched;
				return;				
			}
		}
	
		if( img.w > (width-40) && img.h > (heigth-30) ) {
			
			// plus rien a cropper
			// full page graphic
			img.typeDetected = TypeDetected.untouched;
			return;
		}
		
		if( img.w < stdWidthMin && img.h < stdHeightMin ) {
		
			// to small : cancel cropping
			img.typeDetected = TypeDetected.untouched;
			return;				
		}
		*/
		
		System.out.format( "%s : to check ...\n", img.name );
		img.typeDetected = TypeDetected.tocheck;

	}

	// find type for official clean Cbz
	// TODO : configure DetectionParam if needed
	
	static void findTypeOfficial( Context context, StringBuffer log, FileImg img, FastRGB fastRGB, BufferedImage srcImage ) throws IOException {
		
		log.append( String.format("--- %s ---\n", img.name ) );
		
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();		
		// System.out.format( "%s : HxW %dx%d\n", img.name, height, width );
		
		if( (double)height / (double)width < 1.0 ) { // vivlio screen ration = 4/3 (1.33)
			
			// h / w ratio is unusual
			img.typeDetected = TypeDetected.tocheck;
			return;
		}
		if( (double)height / (double)width > 2.0 ) { // vivlio screen ration = 4/3 (1.33)
			
			// h / w ratio is unusual
			img.typeDetected = TypeDetected.tocheck;
			return;
		}
		
		int ymargin = 1; // height padding after cropping detection
		int xmargin = 1; // width padding after cropping detection
			
		// find standard drawing at first
		{	
			DetectionParam param = new DetectionParam();
			CropDetectionResult cdr = new CropDetectionResult();
			
			param.border = 0;            // ignore these pixels close to the borders
			//param.nonWhiteNbRatio = 0.0; // 0.25 = 25% = 1 sur 4
			param.nonWhiteNbRatio = 0.005; // 0.25 = 25% = 1 sur 4
			param.nonBlackNbRatio = 0.005; // 0.25 = 25% = 1 sur 4
			param.nonWhiteLevel = 175;   // below this level
			param.nonBlackLevel = 80;    // below this level
			
			try {
				
				findCropping( context, log, img, fastRGB, srcImage, param, cdr );
			} 
			catch( Exception e )
			{
				e.printStackTrace();
				img.typeDetected = TypeDetected.tocheck;
				return;
			}
			
			if( cdr.isEmpty ) {
				img.typeDetected = TypeDetected.empty;
				return;				
			}
			
			if( cdr.firstCol == -1 || cdr.firstRow == -1 || cdr.lastCol == -1 || cdr.lastRow == -1 ) {
				
				img.typeDetected = TypeDetected.tocheck;
				return;
			}
			
			if( ( cdr.vCrop > ( 0.25 * (double)height )) || ( cdr.hCrop > 0.25 * (double)width )) {
				img.typeDetected = TypeDetected.tocheck;
				return;
			}
			
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
	
	// TODO : select function to find type
	static void processImg( Context context, FileImg img ) throws Exception  {

		// System.out.format( "processing %s ...\n", img.name );
		
		File file = new File( img.fullpathname );
		BufferedImage srcImage = ImageIO.read( file );
				
		FastRGB fastRGB = new FastRGB( srcImage );

		StringBuffer log = new StringBuffer();
		
		// configure what if the best detection function to use
		// findTypeScanned( context, log, img, fastRGB, srcImage );
		findTypeOfficial( context, log, img, fastRGB, srcImage );

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

				if( Config.drawCroppingLine ) {
					
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
	
	public static void autoCrop( Config config ) {
		
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
		context.srcpath = config.originalImgFolder + "/" + String.format( config.srcSubFolderFmt, config.volumeNo );
		context.outpath = config.croppedImgFolder  + "/" + String.format( config.srcSubFolderFmt, config.volumeNo );		
		String logfile = context.outpath + "/autoCropper.log";
		
		try {
			
			// drop output folders if already exist then re-create it
			Tools.createFolder( context.outpath, true );
		}
		catch( IOException e ) {
			
			System.out.format( "create output directories failed\n");
			System.exit( 1 );
		}

		try ( BufferedWriter writer = new BufferedWriter(new FileWriter(logfile)) )
		{
			context.writer = writer;
			
			TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered
			
			Tools.listInputFiles( context.srcpath, ".*\\.jpe?g", files, true ); // jpg or jpeg
			Tools.listInputFiles( context.srcpath, ".*\\.png", files, true );
			
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
				System.out.format( "std avg Width = %.1f\n", avgW.getAsDouble());
			}
			if( stdHs.size() > 0 ) {
				OptionalDouble avgH = stdHs.stream().mapToInt(Integer::intValue).average();
				System.out.format( "std avg Heigth = %.1f\n", avgH.getAsDouble());
			}
			if( stdvCrops.size() > 0 ) {
				OptionalDouble avgW = stdvCrops.stream().mapToInt(Integer::intValue).average();
				System.out.format( "std avg vertical crop = %.1f\n", avgW.getAsDouble());
			}
			if( stdhCrops.size() > 0 ) {
				OptionalDouble avgH = stdhCrops.stream().mapToInt(Integer::intValue).average();
				System.out.format( "std avg horizontal crop = %.1f\n", avgH.getAsDouble());
			}
			
		} catch ( Exception e) {

			e.printStackTrace();
		}

		System.out.format( "complete\n");
	}
	
	public static void main(String[] args) {
		
		Config config = new Config();
		
		autoCrop( config );
		
		System.out.format( "complete\n");
	}	
}
