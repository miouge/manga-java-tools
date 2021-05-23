# manga-java-tool

manga-java-tool is a set/collection of piece of JAVA code,
that i personnaly use to automate some tasks
when i want to process PDF/CBZ/CBR MANGA files in order to crop out most of the useless pictures areas.
I then finally produce cropped manga PDF file to be use on my B&W Reader (a Vilio Ink Pad 3), maximizing the display of the device.

I share this code, without any assistance (more as a entry point on the subject), to help anyone who could have the same needs as mine.

## modules presentation

Unpack : this module will be used to unpack images from original MANGA files ( .CBZ or .CBR or .PDF) but not yet CBR with RAR5 format.

Analyse : this module will be used to walk along the images of unpacked content then output statistics about theirs sizes (min/max/average/standard deviation)
          it can also be used to exclude images based on size consideration

AutoCropper : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).
			  using this module require usely iterative try to set the correct detection parameters especially if used with non-official source images.
			  it can however lead to crop automatically and reproductively ~90%-95% of the manga content leaving 5-10% to be checked and cropped manually
			  to do this step of work, i'm using the freeware program "BIC – Batch-Image-Cropper" (https://funk.eu/bic-batch-image-cropper/)	  
			  			  
GeneratePDF : will be used to repack into one or multiple PDF files the cropped images 

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

Unpack Module :
- place .CBZ and/or .CBR and/or .PDF files into the archive folder (by default %MGTW_ROOT_FOLDER%/default/archives)
- run Unpack.main()
- for each manga file found then a subfolder will be created into the unpack destination (by default  %MGTW_ROOT_FOLDER%/default/original-img/T%02d )

### Dependencies

- https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar
- https://repo1.maven.org/maven2/org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar
- https://repo1.maven.org/maven2/org/apache/commons/commons-math/2.2/commons-math-2.2.jar
- https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar
- https://repo1.maven.org/maven2/com/itextpdf/itextpdf/5.5.9/itextpdf-5.5.9.jar
- https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.23/pdfbox-2.0.23.jar
- https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-tools/2.0.23/pdfbox-tools-2.0.23.jar

com.github.junrar (use as submodule)

- https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar
- https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar

### referenced softwares

BIC – Batch-Image-Cropper (https://funk.eu/bic-batch-image-cropper/)

