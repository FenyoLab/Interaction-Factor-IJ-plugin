macro "Interaction_Factor_Batch"{

dir = getDirectory("Select Direcotory with Images");
dir_roi = getDirectory("Select Direcotory with ROIs");
dir_results = getDirectory("Select Direcotory for Results");

parent = File.getParent(dir);
list = getFileList(dir);

numberFiles = 0;
numberMasks = 0;

//Set options
//call("ij.Prefs.set", "IF_prefs.ch1Color", "0");
//call("ij.Prefs.set", "IF_prefs.ch2Color", "1");
//call("ij.Prefs.set", "IF_prefs.thMethodInt", "11");
//call("ij.Prefs.set", "IF_prefs.areaRoiOption", "true");
//call("ij.Prefs.set", "IF_prefs.ch1StoiOption", "true");

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
		run("Interaction Factor", "channel1(ch1)_color=Green channel2(ch2)_color=Red  threshold=Otsu ");
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