# manga-java-tool

manga-java-tool is a set/collection of piece of JAVA code,
that i use to automate some tasks 
when i want to process MANGA PDF/CBZ/CBR files in order to crop out most of the useless pictures areas.
I finally produce cropped manga PDF to be use on my reader (a Vivlio 4)

I share it without any assistance (more as an entry point on the subject), to help anyone who could have the same needs as mine.

## modules

UnpackCbz : will be used to unpack a lot of .CBZ (or .CBR) archives to separate subfolders

UnpackPDF : will be used to unpack a lot of .PDF archives to separate subfolders

AutoCropper : will be used to crop image (ie remove any useless part of the original image like white margin, margin with scan artefact, page number ...).
			  using this module require a lot of customization especially if used with non-official source images.
			  
GeneratePDF : will be used to repack into one or multiple PDF files the cropped images 

Sequenceur : is just a loop to repeat the process on multiple manga volumes

### Dependencies

JAVA 8

xmpbox-2.0.21.jar
commons-vfs2-2.6.0.jar
commons-compress-1.20.jar
itextpdf-5.5.9.jar
pdfbox-app-2.0.21.jar
pdfbox-tools-2.0.21.jar
pdf-renderer-1.0.5.jar
com-sun-pdfview-1.0.5-201003191900.jar




