macro "Interaction_Factor_Batch"{

dir = getDirectory("Select Direcotory with Images");
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
        IJ.setOptions("Exclude_Edge_Clusters Overlaps channe1(ch1)_color=Red channe2(ch2)_color=Green threshold=Otsu ")
		run("Interaction Factor", "Exclude_Edge_Clusters Overlaps channe1(ch1)_color=Red channe2(ch2)_color=Green threshold=Otsu ");
		name = substring(list[i], 0, lengthOf(list[i])-4);
		IJ.log(name);
		
		//selectWindow(name+"Red Mask");
		//file_name = name+"Red Mask.tif";
		//saveAs("Tiff", dir_results+"/"+file_name);
		//selectWindow(name+"Green Mask");
		//file_name = name+"Green Mask.tif";
		//saveAs("Tiff", dir_results+"/"+file_name);
		run("Close All");
         }
        }
      } 
      selectWindow("Results");
      saveAs("results",dir_results+"/"+"Results"+".xls");  
}