# manga-java-tool

manga-java-tool is a set/collection of piece of JAVA code,
that i personnaly use to automate some tasks
when i want to process PDF/CBZ/CBR MANGA files in order to crop out most of the useless pictures areas.
I then finally produce cropped manga PDF file to be use on my B&W Reader (a Vilio Ink Pad 3), maximizing the display of the device.

I share this code, without any assistance (more as a entry point on the subject), to help anyone who could have the same needs as mine.

## modules presentation

Unpack : this module will be used to unpack images from original MANGA files ( .CBZ or .CBR or .PDF)
- concerning CBR (rar 5 is not supported as we rely to com.github.junrar to uncompress such file)

Stat : will be used to walk along the images of unpacked content then output statistics about theirs sizes (min/max/average/standard deviation)
       can also be used to exclude images based on size consideration

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

Java 8 + eclipse Java 2020-12 or greater


### How to use these code : 

- You will need to have Java 8 + eclipse Java 2020-12 or greater installed
- You will need to have the jars dependencies installed (into ext/ of the jre)
- You will have to set up the environmement variable MGTW_ROOT_FOLDER
- Optionaly, customize the {Config.projectName} or use "default" as project name

default folder tree is : (for default project name = "default")

- %MGTW_ROOT_FOLDER% /
- %MGTW_ROOT_FOLDER% / default / settings.ini
- %MGTW_ROOT_FOLDER% / default / archives /
- %MGTW_ROOT_FOLDER% / default / original-img /
- %MGTW_ROOT_FOLDER% / default / original-img / excludes /
- %MGTW_ROOT_FOLDER% / default / cropped-img /
- %MGTW_ROOT_FOLDER% / default / cropped-img / tocheck /
- %MGTW_ROOT_FOLDER% / default / cropped-img / std /
- %MGTW_ROOT_FOLDER% / default / cropped-img / empty /
- %MGTW_ROOT_FOLDER% / default / cropped-img / errors /
- %MGTW_ROOT_FOLDER% / default / outlet-pdf /

Unpack :
- place .CBZ and/or .CBR and/or .PDF files into the archive folder (by default %MGTW_ROOT_FOLDER%/default/archives)
- run Unpack.main()
- a subfolder will be created for each manga file into the destination (by default %MGTW_ROOT_FOLDER%/default/original-img)  using the pattern {Config.srcSubFolderFmt}

### Dependencies

com.github.junrar (use as submodule)
- will need slf4j-api-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.9/)
- will need slf4j-ext-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-ext/1.7.9/)
- will need slf4j-simple-1.7.9.jar (https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.9/)

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

