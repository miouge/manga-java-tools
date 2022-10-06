package miouge.renamer;

public class FileItemComparable implements Comparable<FileItemComparable> {

	// source
	public String name;
	public String fullpathname;
	public String location;
	
	public Integer num;
	public String shortname;
	
	@Override
	public int compareTo(FileItemComparable o) {
		
		return this.name.compareTo( o.name );		
	}
}
