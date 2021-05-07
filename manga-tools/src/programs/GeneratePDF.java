package programs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import beans.Config;
import beans.FileItem;
import beans.Tools;

public class GeneratePDF {
	
	static void generatePDF( TreeSet<FileItem> files, String destFilePath, String title, String author )  throws Exception {

		/*
		String FILE = "D:/sampleiTextexample.pdf";
		Document document = new Document();
		PdfWriter.getInstance(document, 
			new FileOutputStream(FILE));
		document.open();
		//To add the title to PDF
		document.addTitle("iText Pdf");//
		//To add the Author for the PDF
		document.addAuthor("Selenium Easy");
		//To add the subject to the PDF document
		document.addSubject("iText Tutorial");
		//To add the Keywords for the document
		document.addKeywords("keyword1,keyword2,keyword3....etc");
		document.add(new Paragraph("Hello iText advanced example "));
		document.add(new Paragraph("Please check the properties of the PDF"));
		document.close();
		*/	        
	        
        Document document = new Document();
        
        document.addTitle( title );
        document.addAuthor( author );
        document.addSubject("cropped by fifou in 2020");
        
        PdfWriter.getInstance(document, new FileOutputStream(new File(destFilePath)));
        document.open();
        
        int pageCount = 0;
        for( FileItem f : files ) {
        
        	System.out.format( "add %s\n", f.fullpathname );
        	
            document.newPage();
            Image image = Image.getInstance( f.fullpathname );            
            image.setAbsolutePosition(0, 0);
            image.setBorderWidth(0);
            image.scaleAbsolute(PageSize.A4);            
            document.add( image );
            pageCount++;
        }
        
        document.setPageCount( pageCount );
        System.out.format( "page count = %d\n", pageCount );
        document.close();
    }
		
	@SuppressWarnings("unused")
	public static void generatePDF( Config config ) {
		
		
		String pdfname = String.format( config.pdfnamefmt, config.volumeNo );
		String title   = String.format( config.titlefmt  , config.volumeNo );
		
        String imgpath = config.croppedImgFolder + "/" + String.format( config.srcSubFolderFmt, config.volumeNo );
		String destFilePath =  config.outletPdfFolder;
                       		
		TreeSet<FileItem> files = new TreeSet<>(); // naturaly ordered 

		try
		{
			// create output folders
			Files.createDirectories(Paths.get( config.outletPdfFolder ));					
			
			// compile file list
			
			if( true ) {
			
				Tools.listInputFiles( imgpath + "/", ".*\\.jpe?g", files, true );
			}
			else {
				
				Tools.listInputFiles( imgpath + "/std"       , ".*\\.jpe?g", files, true );
				Tools.listInputFiles( imgpath + "/untouched" , ".*\\.jpe?g", files, true );
				Tools.listInputFiles( imgpath + "/tocheck"   , ".*\\.jpe?g", files, true );
			}

			//listInputFiles( outpath + "/untouched/_BIC" , Config.imgExtention, files );
			//listInputFiles( outpath + "/tocheck/_BIC"   , Config.imgExtention, files );
			
			System.out.format( "total file count : %d files\n", files.size() );
			
			generatePDF( files, destFilePath + "/" + pdfname, title, config.author );
			
		} catch ( Exception e ) {

			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		
		Config config = new Config();
		
		generatePDF( config );
		
		System.out.format( "complete\n");
	}	
}