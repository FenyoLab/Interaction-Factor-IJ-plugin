macro "Interaction_Factor_Batch"{

dir = getDirectory("Select Direcotory with Images");
dir_roi = getDirectory("Select Direcotory with ROIs");
dir_results = getDirectory("Select Direcotory for Results");

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
		run("Interaction Factor Sims", "channel_1(ch1)_color=Green channel_2(ch2)_color=Red threshold=Otsu ch1_simulation=None ch2_simulation=Random ch2_interaction_factor=0.0 number_of_simulations=1");

		///call(Interaction_Factor.test_run()
		name = substring(list[i], 0, lengthOf(list[i])-4);
		IJ.log(name);
		run("Close All");
         }
        }
      } 
      selectWindow("Results");
      saveAs("results",dir_results+"/"+"Results"+".xls");  
}
