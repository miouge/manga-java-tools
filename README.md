## manga-java-tool

manga-java-tool is a tool to crop out the useless pictures areas of manga picture's ebooks

I personnally designed it to automate some/any/most or all tasks when i want to process many MANGA files (PDF or CBZ or CBR) in order to crop out most of the useless pictures areas (white or black margins) then to repack the result into PDF or CBZ files.

I did this because i read my mangas on a Pocket Book 7'8 inch Reader that have options to stretch pictures either using full width or full height of the screen.
Doing such, i maximize the display of the device to ease the reading.

### Prerequisites

- [ ] You will need to have Java 8 + eclipse Java 2021-03 or greater installed
- [ ] You will need to have the jars dependencies installed (into ext/ of the jre)

### Development/Building/Running environnement : 

Java 8 + eclipse Java 2021-03 or greater

## Dependencies

- [commons-compress-1.9.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar)
- [commons-io-1.3.2.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar)
- [commons-math-2.2.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-math/2.2/commons-math-2.2.jar)
- [commons-cli-1.4.jar](https://repo1.maven.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4.jar)
- [commons-logging-1.2.jar](https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar)
- [ini4j-0.5.4.jar](https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar)
- [itextpdf-5.5.9.jar](https://repo1.maven.org/maven2/com/itextpdf/itextpdf/5.5.9/itextpdf-5.5.9.jar)
- [pdfbox-2.0.23.jar](https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.23/pdfbox-2.0.23.jar)
- [pdfbox-tools-2.0.23.jar](https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-tools/2.0.23/pdfbox-tools-2.0.23.jar)
- [fontbox-2.0.26.jar](https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.26/fontbox-2.0.26.jar)

for com.github.junrar (use as submodule)

- [slf4j-api-1.7.30.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar)
- [slf4j-simple-1.7.30.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar)

### Generate the binary :

**manga-tools.jar** : can be generated with ant using the ant-build.xml into \out.

## CLI

usage: manga-tools a helper to (auto) crop manga images of archives.

  command line : java -jar manga-tools.jar -p <foo-project> -op <operations>

  operations could be "create" or "all" or list of these "unpack" "analyze" "crop" "repack", separated by a space

 -d,--debug               switch Debug/Verbose mode on.

 -op,--operations <arg>   List of operations to perform CREATE or ALL or UNPACK ANALYZE CROP REPACK. (default is none).

 -p,--project <arg>       Project name (subfolder) to work on (default is "default").

 example : java -jar manga-tools.jar -p MaisonIKKOKU -op analyze crop repack

## Basic usages
 
1. set up the environmement variable MGTW_ROOT_FOLDER (that location will be used as the program workspace base folder)
2. create project subfolder by calling java -jar manga-java-tool.jar -p foo -op create
3. put all ebooks files (of a single story) ro process into the created folder %MGTW_ROOT_FOLDER%/foo/a-archives 
4. run java -jar manga-java-tool.jar -p foo -op all
5. retrieve the processed files into %MGTW_ROOT_FOLDER%/foo/e-outlet

Basic settings would be :
- to process archives files in basic files order
- to crop both white and black areas
- to not crop if cropped area would be more than 30% of the original picture
- to generate both PDF and CBZ as results

Advanced settings are available by customizing the file **settings.ini** present into the %MGTW_ROOT_FOLDER%/foo folder

## modules presentation

**Unpack** : this module will be used to unpack images from original MANGA files ( .CBZ or .CBR or .PDF), to unpack PDF, if winrar is installed and available in the path, then it will used preferably (as RAR5 format is correctly managed) rather than the junrar sub module that don't support RAR5.

**Analyse** : this module will be used to walk along the images of unpacked content then output statistics about theirs sizes and ratios. it can also be used to exclude images based on size consideration, to perform conditionnal split and/or rotate of original images.

**AutoCropper** : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).using this module usely could require iterative tries to set the correct detection parameters especially if used with non-official source images.
it can however lead to crop automatically and reproductively ~90%-100% of the manga content leaving 0-10% to be checked and cropped manually to do this step of work, if needed, i'm using the freeware program [BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)

**Repack** : will be used to repack into one or multiple PDF/CBZ files the cropped images 

### Processing flow & Folders structure : 

Processing flow :
- a-archives/ -> {UNPACK} -> b-original-img/ -> {ANALYZE} -> c-analysed-img/ -> {CROP} -> d-cropped-img/ -> {REPACK} -> e-outlet/

- %MGTW_ROOT_FOLDER% / default /
- %MGTW_ROOT_FOLDER% / default / setting.ini
- %MGTW_ROOT_FOLDER% / default / a-archives /
- %MGTW_ROOT_FOLDER% / default / b-original-img /
- %MGTW_ROOT_FOLDER% / default / c-analysed-img /
- %MGTW_ROOT_FOLDER% / default / c-analysed-img / excludes /
- %MGTW_ROOT_FOLDER% / default / d-cropped-img /
- %MGTW_ROOT_FOLDER% / default / d-cropped-img / tocheck /
- %MGTW_ROOT_FOLDER% / default / d-cropped-img / std /
- %MGTW_ROOT_FOLDER% / default / d-cropped-img / empty /
- %MGTW_ROOT_FOLDER% / default / d-cropped-img / errors /
- %MGTW_ROOT_FOLDER% / default / e-outlet /

## Other Referenced softwares

[BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)
WinRar
