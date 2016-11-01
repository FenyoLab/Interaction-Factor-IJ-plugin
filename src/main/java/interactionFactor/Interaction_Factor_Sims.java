package interactionFactor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Interaction_Factor_Sims implements PlugIn {
	public void run(String arg) {
		
		
		
	}
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins
		// menu
		Class<?> clazz = Interaction_Factor.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open sample
		ImagePlus nucleus = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_ROI.tif");
		ImagePlus image = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_R_G.tif");
		image.show();
		nucleus.show();

	}

}
