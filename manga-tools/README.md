# manga-java-tool

manga-java-tool is a set/collection of piece of code in order to automate some tasks while
processing manga Cbz to produce manga PDF to be use on a reader

## modules

UnpackCbz : will be use to unpack a lot of .CBZ (or .CBR) archives to separate subfolders

1- configure the Config::rootFolder
2- place .CBZ (or .CBR) into {Config::rootFolder}/archives folder
3- run UnpackCbz.java
4- images will be unzipped/unrared into {Config::rootFolder}/original-img/T{archiveNum}

UnpackPDF : will be use to unpack a lot of .PDF archives to separate subfolders

AutoCropper : will be use to crop image (ie remove any useless part of the original image like white margin, margin with scan artefact, page number ...)
			  this module use to require a lot of customization especially if used with non-official source images
			  
GeneratePDF : will be use to repack into one or multiple PDF files the processed images 

Sequenceur : will be use to repeat the process on multiple manga volumes

### Dependencies




