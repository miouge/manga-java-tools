package programs;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class UnpackPDF {	
	
	public static BufferedImage resizeImage( Config config, BufferedImage image ) {
	
		int currentHeight = image.getHeight();
		int currentWidth  = image.getWidth();
		
		Double ratio = null;
		
		if( currentHeight <= config.wantedHeight ) {			
			return null;
		}
		else {			
			ratio = config.wantedHeight / (double)(currentHeight);
		}
		
		int newHeight = (int)((double)currentHeight * ratio);
		int newWidth  = (int)((double)currentWidth  * ratio);
		
		System.out.format( "resize %dx%d -> %dx%d\n", currentHeight, currentWidth, newHeight, newWidth );
		
        Image originalImage= image.getScaledInstance( newWidth, newHeight, Image.SCALE_DEFAULT);

        int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, type);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        return resizedImage;
	}	
	
	public static void extractPdfContent( Config config, FileItem fi, String destFolder ) throws IOException {
		
		System.out.format( "extract content of %s ...\n", fi.name );
		
		File infile = new File( fi.fullpathname );	
	    PDDocument document = PDDocument.load( infile );
	    PDFRenderer pdfRenderer = new PDFRenderer(document);
	    
	    System.out.format( "page nb = %d\n", document.getNumberOfPages() );
	    
	    for( int page = 0; page < document.getNumberOfPages(); ++page ) {
	    	
	    	String dest = String.format( "%s/img%03d.jpg", destFolder, page+1 );	    	
	    	
	        BufferedImage img = pdfRenderer.renderImageWithDPI( page, 300, ImageType.RGB );
	        BufferedImage outImg = img;
	        
	        if( config.resizeImg ) {
	        	BufferedImage resizedImage = resizeImage( config, img );
	        	if( resizedImage != null ) {
	        		outImg = resizedImage;
	        	}
	        }
	        
	        ImageIOUtil.writeImage( outImg, dest, 300 );
			
			System.out.format( "%d ", page + 1 );			
	    }
	    document.close();
	    
	    System.out.format( "\n" );
	}

	public static void extractFromPdf( Config config ) {
		
		String path = config.archiveFolder;
		                       
		TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered 

		try
		{
			// compile file list
			
			Tools.listInputFiles( path + "/", ".*\\.pdf", files, true );
			
			System.out.format( "total file count : %d files\n", files.size() );
			
			// delete output folders if already exist 			
			File file = new File( config.originalImgFolder );
			FileUtils.deleteDirectory( file );
			
			// create output folders
			Files.createDirectories(Paths.get( config.originalImgFolder ));			
			
			int i = 1;
			for( FileItem fi : files ) {
			
				String subFolder = String.format( config.srcSubFolderFmt, i );
				String destFolder = config.originalImgFolder + "/" + subFolder;
				
				// for each pdf file ...
				// create output folders
				Files.createDirectories(Paths.get( destFolder ));
				
				extractPdfContent( config, fi, destFolder );
				i++;
			}
			
		} catch ( Exception e ) {

			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		Config config = new Config();
		
		extractFromPdf( config );
		
		System.out.format( "complete\n");
	}
}
