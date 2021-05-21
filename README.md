# manga-java-tool

manga-java-tool is a set/collection of piece of JAVA code,
that i personnaly use to automate some tasks
when i want to process PDF/CBZ/CBR MANGA files in order to crop out most of the useless pictures areas.
I then finally produce cropped manga PDF file to be use on my B&W Reader (a Vilio Ink Pad 3), maximizing the display of the device.

I share this code, without any assistance (more as a entry point on the subject), to help anyone who could have the same needs as mine.

## modules presentation

Unpack : this module will be used to unpack images from original MANGA files ( .CBZ or .CBR or .PDF)
- concerning CBR (rar 5 is not supported as we rely to com.github.junrar to uncompress such file)

Stat : this module will be used to walk along the images of unpacked content then output statistics about theirs sizes (min/max/average/standard deviation)
       can also be used to exclude images based on size consideration

AutoCropper : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).
			  using this module require iterative try to set the customization parameter especially if used with non-official source images.
			  it can however lead to crop automatically and reproductively ~90%-95% of the manga content leaving 5-10% to be checked and cropped manually
			  to do this step of work, i'm using the freeware program "BIC � Batch-Image-Cropper" (https://funk.eu/bic-batch-image-cropper/)	  
			  			  
GeneratePDF : will be used to repack into one or multiple PDF files the cropped images 

Sequencer : is just a loop to repeat a process on multiple manga volumes
( like running Stat module on every volume)

### Development/Running environnement : 

Java 8 + eclipse Java 2021-03 or greater

### How to use these code : 

- You will need to have Java 8 + eclipse Java 2021-03 or greater installed
- You will need to have the jars dependencies installed (into ext/ of the jre)
- You will have to set up the environmement variable MGTW_ROOT_FOLDER
- Optionaly, customize the {Config.projectName} or use "default" as project name

default folder tree is : (for default project name = "default")
just create the %MGTW_ROOT_FOLDER% and default/
drop the sample settings.ini into the %MGTW_ROOT_FOLDER% / default folder

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

- https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar
- https://repo1.maven.org/maven2/org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar
- https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar
- https://repo1.maven.org/maven2/com/itextpdf/itextpdf/5.5.9/itextpdf-5.5.9.jar
- https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.23/pdfbox-2.0.23.jar
- https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-tools/2.0.23/pdfbox-tools-2.0.23.jar

com.github.junrar (use as submodule)

- https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.9/slf4j-api-1.7.9.jar

### referenced softwares

BIC � Batch-Image-Cropper (https://funk.eu/bic-batch-image-cropper/)

