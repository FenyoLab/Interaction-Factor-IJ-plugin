macro "Interaction_Factor_Batch"{

dir = getDirectory("Select Direcotory with Images");
dirMask = getDirectory("Select Direcotory with ROI masks");
parent = File.getParent(dir);
list = getFileList(dir);
roiMasks = getFileList(dirMask);

numberFiles = 0;
numberMasks = 0;

for (i=0; i <list.length;i++){
    if (endsWith(list[i], ".tif")){
        numberFiles ++;
         }
      } 
for (i=0; i <roiMasks.length;i++){
    if (endsWith(list[i], ".tif")){
        numberMasks ++;
         }
      }        

if ( numberFiles != numberMasks){
	exit("The number of images is not the same as the number of masks slices")
}
for (i=0; i <list.length;i++){
    if (endsWith(list[i], ".tif")){
        open(dir+list[i]);
        getDimensions(widthImage, heightImage, channels, slices, frames);
        open(dirMask+roiMasks[i]);
        getDimensions(widthROI, heightROI, channels, slices, frames);
         if (widthImage == widthROI && heightImage == heightROI){
         	setAutoThreshold("Default dark");
			run("Convert to Mask");
			run("Create Selection");
			roiManager("Add");
			selectWindow(list[i]);
			roiManager("Select", 0);
			run("Interaction Factor", "channe1(ch1)_color=Green channe2(ch2)_color=Blue threshold=Otsu sum_pixel_intensities sum_pixel_intensities_>_th mean_pixel_intensities_>_th clusters_area");
			run("Close All");
			selectWindow("ROI Manager");
			run("Close");
         }
        }
      } 
      selectWindow("Results");
      saveAs("results",parent+"/"+"Results"+".xls");  

}