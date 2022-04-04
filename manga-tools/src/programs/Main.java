package programs;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import beans.Config;

public class Main {


	// need commons-cli-1.4.jar	
	// java -jar manga-tools.jar
	// java -jar manga-tools.jar -p myProject -op UNPACK ANALYSE CROP REPACK
	
	public static void main(String[] args) {

		boolean error = true;
		
		Options options = new Options();

        Option projectOpt = new Option("p", "project", true, "Project name (subfolder) to operate (default is \"default\"");
        options.addOption(projectOpt);
        projectOpt.setRequired(true);        

        Option operationsOpt = new Option("op", "operations", true, "Operations list to perform {UNPACK/ANALYSE/CROP/REPACK/NONE} (default is NONE)");
        options.addOption(operationsOpt);
        operationsOpt.setRequired(true);
        operationsOpt.setArgs(Option.UNLIMITED_VALUES); // Set option c to take 1 to oo arguments
        
        // Maximum of 4 arguments that can pass into option
        // operationsOpt.setArgs(4);
        // Comma as separator
        // operationsOpt.setValueSeparator(' ');

        /*
        Option c = Option.builder("c")
                .hasArgs() // sets that number of arguments is unlimited
                .build();
        Options options = new Options();
        options.addOption(c);
        */        
        
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
            formatter.printHelp("manga-tools a helper to crop manga archives", options);
            System.exit(1);
        }

        String project = cmd.getOptionValue("p", "default");
        String[] operations = cmd.getOptionValues("op");
        boolean debug = cmd.hasOption("debug");
        
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
			
			if( operationsList.contains("unpack")) {
				Unpack.unPackArchiveFolderContent(config);
			}
			if( operationsList.contains("analyse")) {
				Analyse.processOriginalImages(config);
			}
			if( operationsList.contains("crop")) {
				AutoCropper.autoCrop(config);
			}
			if( operationsList.contains("repack")) {
				Repack.createArchives(config);
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
