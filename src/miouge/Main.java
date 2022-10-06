package miouge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import miouge.beans.Config;
import miouge.beans.Tools;

public class Main {

	// need commons-cli-1.4.jar	
	
	// need to define
	// java -jar manga-tools.jar (will show the usage)
	// java -jar manga-tools.jar -p theProjectName -op UNPACK ANALYSE CROP REPACK
	
	boolean copyOutFile( String filename, String outputFolder, boolean replaceExisting ) throws IOException {
		
		boolean exitValue = false;
		
		//	To copy a file from your jar, to the outside, you need to use the following approach:
		//
		//	    Get a InputStream to a the file inside your jar file using getResourceAsStream()
		//	    We open our target file using a FileOutputStream
		//	    We copy bytes from the input to the output stream
		//	    We close our streams to prevent resource leaks
		
		try {
			
			File outputDirectory = new File( outputFolder );
			File out = new File( outputDirectory, filename );
		    if( replaceExisting == false && out.exists() ) {
		    
		    	System.out.format( "file %s is already present\n", out );
		    	return false;
		    }

			InputStream is = Main.class.getClassLoader().getResourceAsStream( filename );
		    if( is == null ) {
		    	throw new FileNotFoundException( filename + " (resource not found in jar)");
		    }

			try( FileOutputStream fos = new FileOutputStream( out ); ) {
				
			    byte[] buf = new byte[2048];
			    int r;
			    while( -1 != (r = is.read(buf)) ) {
			        fos.write(buf, 0, r);
			    }
			}
			
			exitValue = true;
		}
		catch( Exception e )
		{
			System.out.format( "copyOutFile failed\n" );
			System.out.format( "Exception : %s\n", e.getMessage() );
		}
		
		return exitValue;
	}	
	
	public static void main(String[] args) {
		
		boolean error = true;
		
		Options options = new Options();

        Option projectOpt = new Option("p", "project", true, "Project name (subfolder) to operate (default is \"default\"");
        projectOpt.setRequired(true);
        options.addOption(projectOpt);
        
        String operationsHelp = 
          "List of operations to perform (with spaces if many ; default will be NONE ) :\n"
        + "Could be CREATE or ALL or some selection among UNPACK ANALYZE CROP REPACK\n"
        + "create/CREATE : create the project subfolder, the intermediate subfolders and the default settings.ini file\n"
        + "all/ALL       : equivalent to the list of these operations UNPACK ANALYZE CROP REPACK";
                        
        Option operationsOpt = new Option("op", "operations", true, operationsHelp );
        operationsOpt.setRequired(true);
        operationsOpt.setArgs(Option.UNLIMITED_VALUES); // Set option c to take 1 to unlimited arguments
        options.addOption(operationsOpt);
        
//      Maximum of 4 arguments that can pass into option
//      operationsOpt.setArgs(4);
//      Comma as separator
//      operationsOpt.setValueSeparator(' ');

//      Option c = Option.builder("c")
//              .hasArgs() // sets that number of arguments is unlimited
//              .build();
//      Options options = new Options();
//      options.addOption(c);
                
        final Option debugOpt = Option.builder("d") 
                .longOpt("debug") 
                .desc("switch Debug/Verbose mode on") 
                .hasArg(false) 
                .required(false) 
                .build();
        options.addOption(debugOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
        	
            cmd = parser.parse(options, args);
            
        } catch (ParseException e) {
        	
            System.out.println(e.getMessage());
            formatter.printHelp("manga-tools a helper to (auto) crop manga images of archives", options);
            System.exit(1);
        }
        
        String project = cmd.getOptionValue("p", "default");
        String[] operations = cmd.getOptionValues("op");
        boolean debug = cmd.hasOption("debug");
        
        if( project == null || operations == null || operations.length == 0 ) {
        	
            formatter.printHelp("manga-tools a helper to (auto) crop manga images of archives", options);
            System.exit(1);        	
        }        
        
        System.out.format( "using project=%s\n", project );
    
        Set<String> operationsList = new HashSet<String>(); 
        
        if( operations != null ) {
	        for( int i = 0 ; i < operations.length ; i++ ) {
	        	System.out.format( "using operations #%d=%s\n", i+1, operations[i] );
	        	operationsList.add( operations[i].toLowerCase() );
	        }
        }
        if( debug ) {
        	System.out.format( "verbose/debug mode ON\n" );
        }
		
		try {
			
			Config config = new Config(project);
			
			if( operationsList.contains("create") )
			{
				// create tree folders

				Tools.createFolder( config.archiveFolder, false, true );
				Tools.createFolder( config.originalImgFolder, true, true );
				Tools.createFolder( config.analysedFolder, true, true );
				Tools.createFolder( config.croppedImgFolder, true, true );
				Tools.createFolder( config.outletFolder, true, true );
				
				if( new Main().copyOutFile( "settings.ini", config.projectFolder, false ) == true ) {
					System.out.format( "settings.ini copied out into %s\n", config.projectFolder );
				}
			}
			else {
				
				if( operationsList.contains("unpack") || operationsList.contains("all")) {
					Unpack unpack = new Unpack();
					unpack.unPackArchiveFolderContent( config );
				}
				if( operationsList.contains("analyse") || operationsList.contains("analyze") || operationsList.contains("all")) {
					Analyze analyze = new Analyze();
					analyze.analyzeOriginalImages(config);
				}
				if( operationsList.contains("crop") || operationsList.contains("all")) {
					AutoCropper autoCropper = new AutoCropper();
					autoCropper.autoCrop( config );
				}
				if( operationsList.contains("repack") || operationsList.contains("all")) {
					Repack repack = new Repack();
					repack.repack( config );
				}
			}
			
			error = false;
		}
		catch ( Exception e )
		{
			System.out.format( "Exception : %s\n", e.getMessage() );
		}
		
		if( error ) {
			System.out.format( "end of program (on error)\n" );
		}
		else {
			System.out.format( "end of program (good completion)\n" );
		}
	}	
}
