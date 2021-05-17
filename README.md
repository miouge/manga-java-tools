# manga-java-tool

manga-java-tool is a set/collection of piece of JAVA code,
that i personnaly use to automate some tasks
when i want to process PDF/CBZ/CBR MANGA files in order to crop out most of the useless pictures areas.
I then finally produce cropped manga PDF file to be use on my B&W Reader (a Vilio Ink Pad 3), maximizing the display of the device.

I share this code, without any assistance (more as a entry point on the subject), to help anyone who could have the same needs as mine.

## modules presentation

Unpack : will be used to unpack images of original Manga (.CBZ or .CBR or .PDF) files
- customize the Config object {rootFolder} & {archiveFolder} & {originalImgFolder}
- place .CBZ or .CBR or .PDF files into the archive folder
- run Unpack.main()
- a subfolder T<volume Num>/ will be created for each manga file into the destination (original image folder}

Stat : will be used to walk along the images of unpacked content then outpout statistics about theirs size (min/max/average/standard deviation)

AutoCropper : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).
			  using this module require iterative try to set the customization parameter especially if used with non-official source images.
			  it can however lead to crop automatically and reproductively ~90%-95% of the manga content leaving 5-10% to be checked and cropped manually
			  to do this step of work, i'm using the freeware program "BIC – Batch-Image-Cropper" (https://funk.eu/bic-batch-image-cropper/)	  
			  			  
GeneratePDF : will be used to repack into one or multiple PDF files the cropped images 
- customize the config object about title template to use and author
- customize getImagesLocations() to specify where to pickup the pictures

Sequencer : is just a loop to repeat a process on multiple manga volumes
( like running Stat module on every volume)

### Development/Running environnement : 

Java 8 + eclipse Java 2020-12

### Dependencies

com.github.junrar (use as submodule)
need slf4j-api-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.9/)
need slf4j-ext-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-ext/1.7.9/)
need slf4j-simple-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.9/)

commons-vfs2-2.6.0.jar
commons-compress-1.20.jar
xmpbox-2.0.21.jar
itextpdf-5.5.9.jar
pdfbox-app-2.0.21.jar
pdfbox-tools-2.0.21.jar
pdf-renderer-1.0.5.jar
com-sun-pdfview-1.0.5-201003191900.jar

### referenced softwares

BIC – Batch-Image-Cropper (https://funk.eu/bic-batch-image-cropper/)

