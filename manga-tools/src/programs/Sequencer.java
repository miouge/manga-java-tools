package programs;

import beans.Config;

public class Sequencer {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		// [ firstVol - lastVol ] 
		int firstVol = 4; 
		int lastVol  = 5;
		
		// autocrop images
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			Config config = new Config( volumeNo );			
			//AutoCropper.autoCrop( config );
		}
		
		// package image into PDF
		for( int volumeNo = firstVol ; volumeNo <= lastVol ; volumeNo ++ ) {
			
			Config config = new Config( volumeNo );
			GeneratePDF.generatePDF( config );
		}
		
		System.out.format( "Sequenceur : complete\n");
	}
}
