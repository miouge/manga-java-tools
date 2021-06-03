## manga-java-tool

manga-java-tool is a set/collection of JAVA code piece,
that i personnaly use to automate some tasks
when i want to process PDF/CBZ/CBR MANGA files in order to crop out most of the useless pictures areas (white or black margins) then repack the result into PDF files.
I do this because, i read my mangas on my 7'8 inch B&W Reader (a Vilio Ink Pad 3), and i like to maximize the display of the device to ease the reading.

I share this code, as an entry point on the subject, to help anyone who could have the same or close needs as mine.


## modules presentation

**Unpack** : this module will be used to unpack images from original MANGA files ( .CBZ or .CBR or .PDF) but not yet CBR with RAR5 format.

**Analyse** : this module will be used to walk along the images of unpacked content then output statistics about theirs sizes (min/max/average/standard deviation). it can also be used to exclude images based on size consideration.
it can also be used to rotate original images. it can also be used to split rotate original dual page images.

**AutoCropper** : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).
			  using this module usely require iterative try to set the correct detection parameters especially if used with non-official source images.
			  it can however lead to crop automatically and reproductively ~90%-95% of the manga content leaving 5-10% to be checked and cropped manually
			  to do this step of work, i'm using the freeware program [BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)

**GeneratePDF** : will be used to repack into one or multiple PDF files the cropped images 


### How to use this code : 

- [ ] You will need to have Java 8 + eclipse Java 2021-03 or greater installed
- [ ] You will need to have the jars dependencies installed (into ext/ of the jre)
- [ ] You will have to set up the environmement variable MGTW_ROOT_FOLDER (that the workspace base folder)
- [ ] Create the %MGTW_ROOT_FOLDER% and subfolder default/ inside ("default" is the default project name)
- [ ] Drop the sample file *settings.ini*(/manga-tools/template/settings.ini) you will found inside the template folder into your %MGTW_ROOT_FOLDER%/default/ folder
- [ ] Create the %MGTW_ROOT_FOLDER%/default/archives/ and put into the CBZ or CBR or PDF manga files

### Folders structure : 

- %MGTW_ROOT_FOLDER% / default /
- %MGTW_ROOT_FOLDER% / default / archives /
- %MGTW_ROOT_FOLDER% / default / original-img /
- %MGTW_ROOT_FOLDER% / default / analysed-img /
- %MGTW_ROOT_FOLDER% / default / analysed-img / excludes /
- %MGTW_ROOT_FOLDER% / default / cropped-img /
- %MGTW_ROOT_FOLDER% / default / cropped-img / tocheck /
- %MGTW_ROOT_FOLDER% / default / cropped-img / std /
- %MGTW_ROOT_FOLDER% / default / cropped-img / empty /
- %MGTW_ROOT_FOLDER% / default / cropped-img / errors /
- %MGTW_ROOT_FOLDER% / default / outlet-pdf /

- archives/ -> {Unpack} -> original-img/ -> {Analyse} -> analysed-img/ -> {AutoCropper} -> cropped-img/ -> {GeneratePDF} -> outlet-pdf/

### Development/Running environnement : 

Java 8 + eclipse Java 2021-03 or greater

## Dependencies

- [commons-compress-1.9.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar)
- [commons-io-1.3.2.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar)
- [commons-math-2.2.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-math/2.2/commons-math-2.2.jar)
- [ini4j-0.5.4.jar](https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar)
- [itextpdf-5.5.9.jar](https://repo1.maven.org/maven2/com/itextpdf/itextpdf/5.5.9/itextpdf-5.5.9.jar)
- [pdfbox-2.0.23.jar](https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.23/pdfbox-2.0.23.jar)
- [pdfbox-tools-2.0.23.jar](https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-tools/2.0.23/pdfbox-tools-2.0.23.jar)

com.github.junrar (use as submodule)

- [slf4j-api-1.7.30.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar)
- [slf4j-simple-1.7.30.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar)

## Referenced softwares

[BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)

