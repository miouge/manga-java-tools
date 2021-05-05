package programs.autoCropper;

import java.io.BufferedWriter;
import java.util.TreeSet;

public class Context {
	
	public TreeSet<FileImg> files = new TreeSet<>();
	
	public String srcpath;
	public String outpath;
	public String extention;
	
	public BufferedWriter writer; // logfile
	
	public int std = 0;
	public int untouched = 0;
	public int empty = 0;
	public int tocheck = 0;
	public int error = 0;	
}
