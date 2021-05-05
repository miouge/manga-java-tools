package programs;

import beans.Config;

public class Sequenceur {

	public static void main(String[] args) {
		
		// [ firstVol - lastVol ] 
		int firstVol = 1; 
		int lastVol  = 5;
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			Config config = new Config( volumeNo );			
			AutoCropper.autoCrop( config );
		}
		
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			Config config = new Config( volumeNo );			
			// package into PDF			
			GeneratePDF.generatePDF( config );			
		}		
		
		System.out.format( "Sequenceur : complete\n");
	}
}
