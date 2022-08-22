## manga-java-tool

manga-java-tool is a command line tool that can be use to crop out the useless pictures areas of manga picture's ebooks

I personnally designed it to automate some/any/most or all tasks when i want to process many MANGA files (PDF or CBZ or CBR) in order to crop out most of the useless pictures areas (white or black margins) then to repack the result into PDF or CBZ files.

I did this because i read my mangas on a Pocket Book 7'8 inch Reader that have options to stretch pictures either using full width or full height of the screen.
Doing such, i maximize the display use of the device and ease the reading.

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

### To Generate the binary :

**manga-tools.jar** : can be generated into \out with ant using the ant-build.xml

## CLI

usage: manga-tools a helper to (auto) crop manga images of archives.

  command line : java -jar manga-tools.jar -p {foo-project} -op {operations}

  operations could be "create" or "all" or list of these "unpack" "analyze" "crop" "repack", separated by a space

 -d,--debug               switch Debug/Verbose mode on.

 -op,--operations {arg1 arg2 arg3}   List of operations to perform CREATE or ALL or UNPACK ANALYZE CROP REPACK, separated by a space (default is NONE).

 -p,--project {arg}       Project name (subfolder) to work on (default is "default").

 example : ``` java -jar manga-tools.jar -p MaisonIKKOKU -op unpack analyze crop repack ```

## Basic usages
 
1. set up the environmement variable **MGTW_ROOT_FOLDER** (that location will be used as the program workspace base folder)
2. create project subfolder by calling ``` java -jar manga-java-tool.jar -p foo -op create ```
3. put all ebooks files (of a single story) to process into the folder that was created in step 2 : *%MGTW_ROOT_FOLDER%/foo/a-archives* 
4. run ``` java -jar manga-java-tool.jar -p foo -op all ```
5. retrieve the processed files into *%MGTW_ROOT_FOLDER%/foo/e-outlet*

Basic (default) settings are :
- to process archives files in basic files order (file01.pdf, file02.pdf ...)
- to crop both white and black areas
- to not crop if cropped area would be more than 30% of the original picture (such case are put in the tocheck/ subfolder)
- to generate both PDF and CBZ files as results

Many Advanced settings are available (and described) by customizing the file **settings.ini** that should be present into the %MGTW_ROOT_FOLDER%/foo folder

## Processing flow & Folders structure : 

- %MGTW_ROOT_FOLDER% / {projetName} /
- %MGTW_ROOT_FOLDER% / {projetName} / setting.ini
- %MGTW_ROOT_FOLDER% / {projetName} / a-archives /

-> {UNPACK} ->

- %MGTW_ROOT_FOLDER% / {projetName} / b-original-img / {ebookNum} /

-> {ANALYZE} ->

- %MGTW_ROOT_FOLDER% / {projetName} / c-analysed-img / {ebookNum} /
- %MGTW_ROOT_FOLDER% / {projetName} / c-analysed-img / {ebookNum} / excludes /

-> {CROP} ->

- %MGTW_ROOT_FOLDER% / {projetName} / d-cropped-img / {ebookNum} / std /
- %MGTW_ROOT_FOLDER% / {projetName} / d-cropped-img / {ebookNum} / tocheck /
- %MGTW_ROOT_FOLDER% / {projetName} / d-cropped-img / {ebookNum} / empty /
- %MGTW_ROOT_FOLDER% / {projetName} / d-cropped-img / {ebookNum} / errors /

-> {REPACK} ->

- %MGTW_ROOT_FOLDER% / {projetName} / e-outlet /

## modules presentation

**Unpack** : this module will be used to unpack images from original MANGA files (supported input format and extentions are CBZ CBR or .PDF), to unpack RAR if winrar is installed and available in the path, then it will used preferably rather than the junrar[^1] sub module.

[^1]: So far from now, junrar sub module don't support RAR5.

**Analyze** : this module will be used to walk along the images of unpacked content then output statistics about theirs sizes and ratios. it can also be used to exclude images based on size consideration, to perform conditionnal split and/or rotate of original images.

**AutoCropper** : will be used to crop the images (ie remove any useless part of the original image like white margin, margin with scan artefact, page number, useless white or black areas ...).using this module usely could require iterative tries to set the correct detection parameters especially if used with non-official source images.
it can however lead to crop automatically and reproductively ~90%-100% of the manga content leaving 0-10% to be checked and cropped manually to do this step of work, if needed, i'm using the freeware program [BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)

**Repack** : will be used to repack into one or multiple PDF/CBZ files the cropped images 

## Other Referenced softwares

[BIC – Batch-Image-Cropper](https://funk.eu/bic-batch-image-cropper/)
WinRar
