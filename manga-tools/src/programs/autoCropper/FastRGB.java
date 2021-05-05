package programs.autoCropper;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;

public class FastRGB
{

    private int width;
    // private int height;
    private boolean hasAlphaChannel;
    private int pixelLength;
    private byte[] pixels;
    private int pixelSize; // 8-24
    BufferedImage img;   

    public FastRGB( BufferedImage image )
    {
    	ColorModel model = image.getColorModel();
    	pixelSize = model.getPixelSize();
    	img = image; // save

    	if( pixelSize > 8 ) {
    		
	        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	        width = image.getWidth();
	        // height = image.getHeight();
	        hasAlphaChannel = image.getAlphaRaster() != null;
	        pixelLength = 3;
	        if( hasAlphaChannel )
	        {
	            pixelLength = 4;
	        }
    	}
    }
    
    public PixColor getColor( int x, int y )
    {
        PixColor color = new PixColor();        
        color.alpha = 255; 

        if( pixelSize == 8 ) {
            	
        	// 8 bits image
        	
        	int colorValue = img.getRGB( x, y ); // ARGB
	        color.B = (int)   colorValue & 0x000000ff;       // blue
	        color.G = (int) ((colorValue & 0x0000ff00) >>  8); // green
	        color.R = (int) ((colorValue & 0x00ff0000) >> 16); // red	                	
        }
        else {
        	
        	// fast access only for 24 bits image
        	
	        int pos = ( y * pixelLength * width ) + ( x * pixelLength );
		        
	        if( hasAlphaChannel )
	        {
	        	color.alpha = ((int) pixels[pos++] & 0xff); // alpha
	        }
	        
	        color.B = (int) pixels[pos++] & 0xff; // blue
	        color.G = (int) pixels[pos++] & 0xff; // green
	        color.R = (int) pixels[pos++] & 0xff; // red	        
    	}
        
        // New grayscale image = ( (0.3 * R) + (0.59 * G) + (0.11 * B) )
        color.grey = ( 30 * color.R + 59 * color.G + 11 * color.B ) / 100;
	        
        return color;
    }    
}