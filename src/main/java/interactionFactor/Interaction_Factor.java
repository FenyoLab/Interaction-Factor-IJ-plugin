package interactionFactor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.plugin.filter.Analyzer;
import java.util.ArrayList;
import java.util.List;
import ij.process.AutoThresholder;
import java.awt.*;
import ij.measure.Calibration;
import java.util.Arrays;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.io.OpenDialog;

public class Interaction_Factor implements PlugIn {

	public void run(String arg) {
		
		String[] thMethods = AutoThresholder.getMethods();
		String[] channels = {"Red","Green","Blue"};
		String PREF_KEY = "IF_prefs.";
		int nMaxSimulations = 50;
		AutoThresholder.Method[] methods = AutoThresholder.Method.values();
		
		int thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt", 11);
		int ch1Color = (int) Prefs.get(PREF_KEY + "ch1Color", 0);
		int ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
		boolean edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);
		
		//Measurement Options
		boolean sumIntOption =  Prefs.get(PREF_KEY + "sumIntOption", true);
		boolean sumIntThOption = Prefs.get(PREF_KEY + "sumIntThOption", true);
		boolean meanIntThOption = Prefs.get(PREF_KEY + "meanIntThOption", true);
		boolean areaOption = Prefs.get(PREF_KEY + "areaOption", true);

		//Output Options
		boolean simImageOption = Prefs.get(PREF_KEY + "simImageOption", false);
		boolean ch1MaskOption =Prefs.get(PREF_KEY + "ch1MaskOption", false);
		boolean ch2MaskOption =Prefs.get(PREF_KEY + "ch2MaskOption", false);
		boolean roiMaskOption = Prefs.get(PREF_KEY + "roiMaskOption", false);
		boolean overlapMaskOption = Prefs.get(PREF_KEY + "overlapMaskOption", false);
		boolean overlapLocations = Prefs.get(PREF_KEY + "overlapLocations", false);
		String imagedir =  Prefs.get(PREF_KEY +"imagedir",Prefs.getDefaultDirectory());
		

		// Dialog
		GenericDialog gd = new GenericDialog("Interaction Factor");
		
		gd.addMessage("--------------- Segmentation ---------------\n");
		gd.addChoice("Channe1(Ch1)_Color:", channels, channels[ch1Color]);
		gd.addChoice("Channe2(Ch2)_Color:", channels, channels[ch2Color]);
		gd.addChoice("Threshold:", thMethods, thMethods[thMethodInt]);
		gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);
		//gd.setInsets(5, 0, 0);

		gd.addMessage("----------- Additional Measurements --------\n");
		gd.addCheckbox("Sum_Pixel_Intensities", sumIntOption);
		gd.addCheckbox("Sum_Pixel_Intensities_>_Th", sumIntThOption);
		gd.addCheckbox("Mean_Pixel_Intensities_>_Th", meanIntThOption);
		gd.addCheckbox("Clusters_Area", areaOption);
		//gd.setInsets(5, 0, 0);

		gd.addMessage("-------------- Output Images ---------------\n");
		gd.addCheckbox("Save_Random_Simulations", simImageOption);
		gd.addCheckbox("Show_Ch1_Mask", ch1MaskOption);
		gd.addCheckbox("Show_Ch2_Mask", ch2MaskOption);
		gd.addCheckbox("Show_ROI_Mask", roiMaskOption);
		gd.addCheckbox("Show_Overlap_Mask", overlapMaskOption);
		gd.addCheckbox("Overlap_Locations", overlapLocations);
		gd.showDialog();
		

		//gd.centerDialog(true);
		
		if (gd.wasCanceled())
			return;

		ch1Color = gd.getNextChoiceIndex();
		Prefs.set(PREF_KEY + "ch1Color", ch1Color);
		ch2Color = gd.getNextChoiceIndex();
		Prefs.set(PREF_KEY + "ch2Color", ch2Color);
		thMethodInt = gd.getNextChoiceIndex();
		Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
		edgeOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "edgeOption", edgeOption);
		sumIntOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "sumIntOption", sumIntOption);
		sumIntThOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "sumIntThOption", sumIntThOption);
		meanIntThOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "meanIntThOption", meanIntThOption);
		areaOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "areaOption", areaOption);
		simImageOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "simImageOption", simImageOption);
		ch1MaskOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "ch1MaskOption", ch1MaskOption);
		ch2MaskOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "ch2MaskOption", ch2MaskOption);
		roiMaskOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "roiMaskOption", roiMaskOption);
		overlapMaskOption = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "overlapMaskOption", overlapMaskOption);
		overlapLocations = gd.getNextBoolean();
		Prefs.set(PREF_KEY + "overlapLocations", overlapLocations);
		
		if (simImageOption){
			DirectoryChooser chooser = new DirectoryChooser("Choose directory to process");
			//chooser.setDefaultDirectory(imagedir);
			imagedir = chooser.getDirectory();
			//OpenDialog chooser = new OpenDialog("Choose directory for Saved Simulations", imagedir);
			//imagedir= IJ.getDirectory("Choose directory for Saved Simulations");//chooser.getDirectory();
					//IJ.getDirectory("Choose directory for Saved Simulations");
			
		}
		IfFunctions fs = new IfFunctions();

		ImagePlus im = IJ.getImage();

		if (im.getType() != ImagePlus.COLOR_RGB) {
			IJ.error("RGB image required");
			return;
		}

		if (ch1Color == ch2Color) {
			IJ.error("Channel Colors are the same. Choose another channel");
			return;
		}

		String name = im.getShortTitle();
		AutoThresholder.Method method = methods[thMethodInt];

		// Calibration
		Calibration cal = im.getCalibration();
		String unit = cal.getUnit();
		double pixelHeight = cal.pixelHeight;
		double pixelWidth = cal.pixelWidth;
		double calConvert = pixelHeight * pixelWidth;

		ImageProcessor ip = im.getProcessor();
		Rectangle roi = ip.getRoi();
		Roi roiSelection = im.getRoi();

		ImageProcessor mask = im.getMask();// ip for the roi mask but only with
											// surrounding box

		int M = ip.getWidth();
		int N = ip.getHeight();
		int size = M * N;

		byte[] red = new byte[size];
		byte[] green = new byte[size];
		byte[] blue = new byte[size];

		((ColorProcessor) ip).getRGB(red, green, blue);

		ImageProcessor ipCh1 = new ByteProcessor(M, N); // ip for ch1 mask
		ImageProcessor ipCh2 = new ByteProcessor(M, N); // ip for ch2 mask
		ImageProcessor ipCh3 = new ByteProcessor(M, N); // ip for ch1 mask
		ImageProcessor ipOverlaps = new ByteProcessor(M, N); // ip for overlap
																// mask
		ImageProcessor ipMask = new ByteProcessor(M, N); // ip for roi mask

		byte[] ch3;

		// Color of Ch1
		if (ch1Color == 0) {
			ipCh1.setPixels(red);
		} else if (ch1Color == 1) {
			ipCh1.setPixels(green);
		} else {
			ipCh1.setPixels(blue);
		}
		// Color of Ch2
		if (ch2Color == 0) {
			ipCh2.setPixels(red);
		} else if (ch2Color == 1) {
			ipCh2.setPixels(green);
		} else {
			ipCh2.setPixels(blue);
		}
		// Color of Ch3
		if (ch1Color + ch2Color == 1) {
			ipCh3.setPixels(blue);
			ch3 = blue;
		} else if (ch1Color + ch2Color == 2) {
			ipCh3.setPixels(green);
			ch3 = green;
		} else {
			ipCh3.setPixels(red);
			ch3 = red;
		}

		boolean hasMask = (mask != null);
		boolean hasRoi = (roiSelection != null);

		if (hasMask) {
			ipMask.insert(mask, roi.x, roi.y);
			// method to insert another ip inside an ip does not work with
			// rectangular rois
		} else {
			ipMask.setValue(255);
			ipMask.setRoi(roi);
			ipMask.fill();
		}

		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipMask.getPixel(u, v);
				if (p == 0) {
					ipCh1.putPixel(u, v, 0);
					ipCh2.putPixel(u, v, 0);
					ipCh3.putPixel(u, v, 0);
				}

			}
		}

		ImageProcessor ipCh1Mask = ipCh1.duplicate();
		ImageProcessor ipCh2Mask = ipCh2.duplicate();

		AutoThresholder autoth = new AutoThresholder();

		// Threshold ch1 channel
		ipCh1Mask.setMask(ipMask);
		int[] ch1_hist = ipCh1Mask.getHistogram();
		int th_ch1 = autoth.getThreshold(method, ch1_hist);
		ipCh1Mask.threshold(th_ch1);

		// Threshold ch2 channel

		ipCh2Mask.setMask(ipMask);
		int[] ch2_hist = ipCh2Mask.getHistogram();
		int th_ch2 = autoth.getThreshold(method, ch2_hist);
		ipCh2Mask.threshold(th_ch2);

		if (edgeOption) {
			if (hasRoi) {
				fs.excludeEdgesRoi(roiSelection, ipMask, ipCh1Mask);
				fs.excludeEdgesRoi(roiSelection, ipMask, ipCh2Mask);
			} else {
				fs.excludeEdges(roi, ipMask, ipCh1Mask);
				fs.excludeEdges(roi, ipMask, ipCh2Mask);
			}
		}
		// Tables
		ResultsTable summary = Analyzer.getResultsTable();
		if (summary == null) {
			summary = new ResultsTable();
			Analyzer.setResultsTable(summary);
		}
		ResultsTable rTable = new ResultsTable();
		// Generate overlap mask

		ipOverlaps.copyBits(ipCh1Mask, 0, 0, Blitter.COPY);
		ipOverlaps.copyBits(ipCh2Mask, 0, 0, Blitter.AND);

		// Finding Overlaps
		ImageProcessor ipFlood = ipOverlaps.duplicate();
		List<ImageProcessor> overlaps = new ArrayList<ImageProcessor>();
		List<Rectangle> overlapsRect = new ArrayList<Rectangle>();

		int overlapCount = fs.clustersProcessing(name, true, rTable, cal, ipFlood, ipOverlaps, overlaps, overlapsRect);
		/*
		 * int ch1Overlaps = overlapCount(ipCh1Mask, ipCh2Mask); int ch2Overlaps
		 * = overlapCount(ipCh2Mask, ipCh1Mask);
		 */

		// Ch1 clusters
		ImageProcessor ipCh1Flood = ipCh1Mask.duplicate();
		List<ImageProcessor> ch1Clusters = new ArrayList<ImageProcessor>();
		List<Rectangle> ch1ClustersRect = new ArrayList<Rectangle>();

		int ch1ClusterCount = fs.clustersProcessing(cal, rTable, ipCh1Flood, ipCh1, ch1Clusters, ch1ClustersRect);

		// Ch2 clusters
		ImageProcessor ipCh2Flood = ipCh2Mask.duplicate();
		List<ImageProcessor> ch2Clusters = new ArrayList<ImageProcessor>();
		List<Rectangle> ch2ClustersRect = new ArrayList<Rectangle>();

		int ch2ClusterCount = fs.clustersProcessing(cal, rTable, ipCh2Flood, ipCh2, ch2Clusters, ch2ClustersRect);

		// Adding Overlays
		ImageProcessor ipCh1FloodCopy = ipCh1Mask.duplicate();
		ImageProcessor ipCh2FloodCopy = ipCh2Mask.duplicate();
		fs.setClustersOverlay(im, ipCh1FloodCopy, ipCh2FloodCopy);

		// Summary

		// Area Ch2
		ipCh2.setMask(ipCh2Mask);
		ImageStatistics ch2Stats = ipCh2.getStatistics();
		double aCh2Pixels = (double) ch2Stats.pixelCount * calConvert;

		// Area Ch1
		ipCh1.setMask(ipCh1Mask);
		ImageStatistics ch1Stats = ipCh1.getStatistics();
		double aCh1Pixels = (double) ch1Stats.pixelCount * calConvert;

		// Overlap
		ipCh1.setMask(ipOverlaps);
		ImageStatistics overlapStats = ipCh1.getStatistics();
		double aOverlapPixels = (double) overlapStats.pixelCount * calConvert;

		// Intensity values
		int ch1SumIntensity = fs.sumIntensities(ipCh1);
		int ch2SumIntensity = fs.sumIntensities(ipCh2);
		int ch1SumIntensityTh = fs.sumIntensitiesMask(ipCh1, ipCh1Mask);
		int ch2SumIntensityTh = fs.sumIntensitiesMask(ipCh2, ipCh2Mask);
		
		int ch2Overlaps = fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask);
		int ch1Overlaps = fs.ch2ClusterOverlaps(ipCh2Mask, ipCh1Mask);
		
		//Percentage of Overlaps
		double ch2Percentage = (double) ch2Overlaps / (double) ch2ClusterCount;
		double ch1Percentage = (double) ch1Overlaps / (double) ch1ClusterCount;

		//Average Mean intensity
		double ch1MeanInt = (double)ch1SumIntensity/ch1Stats.pixelCount;
		double ch2MeanInt = (double)ch2SumIntensity/ch2Stats.pixelCount;

		// Calculating IF

		double[] ch2ClustersProbs = new double[ch2Clusters.size()];
		
		int minX = 0;
		int maxX = M;
		int minY = 0;
		int maxY = N;

		if (hasRoi) {
			minX = (int) roi.getMinX();
			maxX = (int) roi.getMaxX();
			minY = (int) roi.getMinY();
			maxY = (int) roi.getMaxY();
		}
		Arrays.fill(ch2ClustersProbs, 0);
		double countForPval = 0;
		
		for (int i = 0; i < nMaxSimulations; i++) {
			String nSimulation = Integer.toString(i+1);
			ImageProcessor ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
			
			ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1Random, ch2ClustersProbs,
					ch2Clusters, ch2ClustersRect);
			 //generate ch2 channel mask
            ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
            ipCh2RandomMask.threshold(th_ch2);

            //generate ch1 channel mask
            ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
            ipCh1RandomMask.threshold(th_ch1);
            int ch2RandomOverlaps = fs.overlapCount(ipCh2RandomMask, ipCh1RandomMask); // check this maybe replace with  fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask)
           
			double percOverlaps = (double)ch2RandomOverlaps/(double)ch2Clusters.size();
			
			if (percOverlaps > ch2Percentage){
				countForPval +=1;
			}
			if (simImageOption){
				byte[] redRandom;
	            byte[] greenRandom;
	            byte[] blueRandom;
	            //Red Green
	            if (ch1Color == 0 && ch2Color == 1){
	                redRandom = (byte[]) ipCh1Random.getPixels();
	                greenRandom =(byte[]) ipCh2Random.getPixels();
	                blueRandom = ch3;
	            }
	            //Green Blue
	            else if (ch1Color == 1 && ch2Color == 0){
	                greenRandom = (byte[]) ipCh1Random.getPixels();
	                redRandom = (byte[]) ipCh2Random.getPixels();
	                blueRandom = ch3;
	            }
	            //Blue Red
	            else if(ch1Color == 2 && ch2Color == 0) {
	                blueRandom= (byte[]) ipCh1Random.getPixels();
	                redRandom = (byte[]) ipCh2Random.getPixels();
	                greenRandom = ch3;
	            }
	            //Red Blue
	            else if(ch1Color == 0 && ch2Color == 2) {
	                redRandom= (byte[]) ipCh1Random.getPixels();
	                blueRandom = (byte[]) ipCh2Random.getPixels();
	                greenRandom = ch3;
	            }
	            //Blue Green
	            else if (ch1Color == 2 && ch2Color == 1 ){
	                blueRandom= (byte[]) ipCh1Random.getPixels();
	                greenRandom = (byte[]) ipCh2Random.getPixels();
	                redRandom = ch3;
	            }
	            //Green Blue
	            else{
	                greenRandom= (byte[]) ipCh1Random.getPixels();
	                blueRandom = (byte[]) ipCh2Random.getPixels();
	                redRandom = ch3;
            }
            //Color Simulation
            ColorProcessor ipSimulation = new ColorProcessor(M, N);

            ipSimulation.setRGB(redRandom, greenRandom, blueRandom);
            ImagePlus colorRandIm = new ImagePlus(name + "_Sim" + nSimulation, ipSimulation);
            FileSaver fileSave = new FileSaver(colorRandIm);
            fileSave.saveAsTiff(imagedir+name+"_Sim" + nSimulation+".tiff");
            
			}
            
		}
		double pVal = countForPval/(double)nMaxSimulations;
		double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, nMaxSimulations);
		double IF = 0;

		IF = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);
		
		//Summary Measurements
		summary.incrementCounter();
		summary.addValue("Image", name);
		summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
	    
		//Overlap Measurements
		
		summary.addValue(channels[ch1Color]+ "-" +channels[ch2Color]+" IF", IF);
		String pValStr;
		if (pVal == 0){
			 pValStr = "p<0.02" ;
		}
		else{
			pValStr ="p="+ String.valueOf(pVal);
		}
		summary.addValue("p-val", pValStr);
		summary.addValue(channels[ch1Color] + " Cluster Count", ch1ClusterCount);
		summary.addValue(channels[ch1Color]+" Overlaps",ch1Overlaps);
        summary.addValue(channels[ch1Color]+" %Overlaps",ch1Percentage);
        
		summary.addValue(channels[ch2Color] + " Cluster Count", ch2ClusterCount);
		summary.addValue(channels[ch2Color] + " Overlaps", ch2Overlaps);
		summary.addValue(channels[ch2Color] + "% Overlaps", ch2Percentage*100);
		summary.addValue("Overlap Count", overlapCount);
		summary.addValue("Overlap Area", aOverlapPixels);

		//Segmentation
		summary.addValue("Th Algorithm", thMethods[thMethodInt]);
		summary.addValue(channels[ch1Color] + " Th", th_ch1);
		summary.addValue(channels[ch2Color] + " Th", th_ch2);
		
		//Optional Measurements
		
		if (sumIntOption){
			summary.addValue(channels[ch1Color] + " Sum Intensities", ch1SumIntensity);
			summary.addValue(channels[ch2Color] + " Sum Intensities", ch2SumIntensity);
		}
		if (sumIntThOption){
			summary.addValue(channels[ch1Color] + " Sum Intensities > th", ch1SumIntensityTh);
			summary.addValue(channels[ch2Color] + " Sum Intensities > th", ch2SumIntensityTh);
		}
		if (meanIntThOption){
			summary.addValue(channels[ch1Color] + " Mean Intensities > th", ch1MeanInt);
			summary.addValue(channels[ch2Color] + " Mean Intensities > th", ch2MeanInt);

		}
		if (areaOption){
			summary.addValue(channels[ch1Color] + " Area", aCh1Pixels);
			summary.addValue(channels[ch2Color] + " Area", aCh2Pixels);
		}
		
		summary.show("Results");
		
		if (overlapLocations) {
			rTable.show("Overlap Locations");
		}
		// Show images
		if (ch1MaskOption) {
			ImagePlus ch1Im = new ImagePlus(name + channels[ch1Color] + " Mask", ipCh1Mask);
			ch1Im.setCalibration(cal);
			ch1Im.show();
		}
		if (ch2MaskOption) {
			ImagePlus ch2Im = new ImagePlus(name + channels[ch2Color] + " Mask", ipCh2Mask);
			ch2Im.setCalibration(cal);
			ch2Im.show();
		}
		if (roiMaskOption) {
			ImagePlus roiIm = new ImagePlus(name + " ROI Mask", ipMask);
			roiIm.setCalibration(cal);
			roiIm.show();
		}
		if (overlapMaskOption) {
			ImagePlus overlapIm = new ImagePlus(name + " Overlap Mask", ipOverlaps);
			overlapIm.setCalibration(cal);
			overlapIm.show();
		}
		

	}


	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ,
	 * loads an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args
	 *            unused
	 */

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
				"/Users/keriabermudez/Dropbox/Projects/Dylans/Dylan_NEW/raw images and masks/pDNA-PKcs+LigIV+Ku80/0D/ROIs/Result of 1 Reconstruction-1.tif");
		ImagePlus image = IJ.openImage(
				"/Users/keriabermudez/Dropbox/Projects/Dylans/Dylan_NEW/raw images and masks/pDNA-PKcs+LigIV+Ku80/0D/images/Result of 1 Reconstruction.tif");
		image.show();
		nucleus.show();

	}

}
