package miouge.beans;

public class FileItem implements Comparable<FileItem> {

	// source
	public String name;
	public String fullpathname;
	public String folderOnly;
	public String extention;
	public Long rank;
	
	@Override
	public int compareTo(FileItem o) {
		
		return this.name.compareTo( o.name );
	}
}
