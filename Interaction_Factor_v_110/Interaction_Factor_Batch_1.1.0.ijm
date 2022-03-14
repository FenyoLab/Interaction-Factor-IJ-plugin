macro "Interaction_Factor_Batch"{

dir = getDirectory("Select Directory with Images");
dir_roi = getDirectory("Select Directory with ROIs");
dir_results = getDirectory("Select Directory for Results");

parent = File.getParent(dir);
list = getFileList(dir);

numberFiles = 0;
numberMasks = 0;


for (i=0; i <list.length;i++){
    if (endsWith(list[i], ".tif")){
        numberFiles ++;
         }
for (i=0; i <list.length;i++){
    if (endsWith(list[i], ".tif")){
        open(dir+list[i]);
        open(dir_roi+list[i]);
        run("8-bit");
        setAutoThreshold("Default dark");
		run("Convert to Mask");
		run("Create Selection");
		roiManager("Add");
		selectWindow(list[i]);
		roiManager("Select", i);
	 	run("Interaction Factor", "channel_1(ch1)_color=Red channel_2(ch2)_color=Green use_manual_threshold channel_1_threshold=70 channel_2_threshold=100 cluster_minimum_area=0.0 move_ch1_clusters clusters_area roi_area sum_pixel_inten clusters_sum_inten clusters_mean_inten clusters_overlaps %clusters_overlaps overlap_count overlap_area");
		name = substring(list[i], 0, lengthOf(list[i])-4);
		IJ.log(name);
		run("Close All");
         }
        }
      } 
      selectWindow("Results");
      saveAs("results",dir_results+"/"+"Results"+".xls");
      run("Close");  
}
