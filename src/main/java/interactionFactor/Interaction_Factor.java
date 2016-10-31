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

public class Interaction_Factor implements PlugIn {

	private static String[] thMethods = AutoThresholder.getMethods();
	private static String[] channels = { "Red", "Green", "Blue" };
	protected final static String PREF_KEY = "Interaction_Factor.";

	public void run(String arg) {
		// String[] thMethods = AutoThresholder.getMethods();

		int ch1Color = (int) Prefs.get(PREF_KEY + "ch1Color", 0);
		int ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
		int thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt", 11);
		boolean edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);
		
		//Measurement Options
		boolean sumIntOption = Prefs.get(PREF_KEY + "sumIntOption", true);
		boolean sumIntThOption = Prefs.get(PREF_KEY + "sumIntThOption", true);
		boolean meanIntThOption = Prefs.get(PREF_KEY + "meanIntThOption", true);
		boolean areaOption = Prefs.get(PREF_KEY + "areaOption", true);

		//
		boolean simImageOption = Prefs.get(PREF_KEY + "simImageOption", false);
		boolean ch1MaskOption = Prefs.get(PREF_KEY + "ch1MaskOption", false);
		boolean ch2MaskOption = Prefs.get(PREF_KEY + "ch2MaskOption", false);
		boolean roiMaskOption = Prefs.get(PREF_KEY + "roiMaskOption", false);
		boolean overlapMaskOption = Prefs.get(PREF_KEY + "overlapMaskOption", false);
		boolean overlapLocations = Prefs.get(PREF_KEY + "overlapLocations", false);

		// Dialog
		GenericDialog gd = new GenericDialog("Interaction Factor");

		// gd.setInsets(15, 5, 5);
		gd.addMessage("----------- Segmentation -----------");
		gd.addChoice("Channel_1_(Ch1)_Color:", channels, channels[ch1Color]);
		gd.addChoice("Channel_2_(Ch2)_Color:", channels, channels[ch2Color]);
		gd.addChoice("Threshold_Algorithm:", thMethods, thMethods[thMethodInt]);
		gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);
		// gd.setInsets(15, 5, 5);
		gd.addMessage("------Additional Measuremnts ---------");
		gd.addCheckbox("Sum_Pixel_Intensities", sumIntOption);
		gd.addCheckbox("Sum_Pixel_Intensities_>_Th (Clusters Sum Intesities)", sumIntThOption);
		gd.addCheckbox("Mean_Pixel_Intensities_>_Th (Clusters Mean Intesities)", meanIntThOption);
		gd.addCheckbox("Clusters Area", meanIntThOption);


		
		gd.addMessage("----------- Output Images -----------");
		gd.addCheckbox("Random Simulations", simImageOption);
		gd.addCheckbox("Ch1_Mask", ch1MaskOption);
		gd.addCheckbox("Ch2_Mask", ch2MaskOption);
		gd.addCheckbox("ROI_Mask", roiMaskOption);
		gd.addCheckbox("Overlap_Mask", overlapMaskOption);
		gd.addCheckbox("Overlap_Locations", overlapLocations);
		gd.showDialog();

		if (gd.wasCanceled())
			return;

		AutoThresholder.Method[] methods = AutoThresholder.Method.values();

		ch1Color = gd.getNextChoiceIndex();
		ch2Color = gd.getNextChoiceIndex();
		thMethodInt = gd.getNextChoiceIndex();
		edgeOption = gd.getNextBoolean();
		simImageOption = gd.getNextBoolean();
		ch1MaskOption = gd.getNextBoolean();
		ch2MaskOption = gd.getNextBoolean();
		roiMaskOption = gd.getNextBoolean();
		overlapMaskOption = gd.getNextBoolean();
		overlapLocations = gd.getNextBoolean();

		// save user preferences

		Prefs.set(PREF_KEY + "ch1Color", ch1Color);
		Prefs.set(PREF_KEY + "ch2Color", ch2Color);
		Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
		Prefs.set(PREF_KEY + "edgeOption", edgeOption);
		Prefs.set(PREF_KEY + "simImageOption", simImageOption);
		Prefs.set(PREF_KEY + "ch1MaskOption", ch1MaskOption);
		Prefs.set(PREF_KEY + "ch2MaskOption", ch2MaskOption);
		Prefs.set(PREF_KEY + "roiMaskOption", roiMaskOption);
		Prefs.set(PREF_KEY + "overlapMaskOption", overlapMaskOption);

		IfFunctions fs = new IfFunctions();

		int nMaxSimulations = 50;
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
		double origM = (double) ch2Overlaps / (double) ch2ClusterCount;
		
		//Average Mean intensity
		double ch1MeanInt = (double)ch1SumIntensity/ch1Stats.pixelCount;
		double ch2MeanInt = (double)ch2SumIntensity/ch2Stats.pixelCount;


		// Calculating IF

		double[] ch2ClustersProbs = new double[ch2Clusters.size()];
		List<ImageProcessor> ch1RandomImgs = new ArrayList<ImageProcessor>();
		List<ImageProcessor> ch2RandomImgs = new ArrayList<ImageProcessor>();
		
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
		for (int i = 0; i < nMaxSimulations; i++) {
			ImageProcessor ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
			ch1RandomImgs.add(ipCh1Random);
			ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1Random, ch2ClustersProbs,
					ch2Clusters, ch2ClustersRect);
			ch2RandomImgs.add(ipCh2Random);
		}
		double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, nMaxSimulations);
		double IF = 0;

		IF = fs.calcIF(ch2ClustersProbsTest, origM);
		
		//Summary Measurements
		
		summary.incrementCounter();
		summary.addValue("Image", name);
		summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
	    
		//Overlap Measurements
		summary.addValue(channels[ch1Color] + " Cluster Count", ch1ClusterCount);
		summary.addValue(channels[ch2Color] + " Cluster Count", ch2ClusterCount);
		summary.addValue("IF", IF);
		summary.addValue(channels[ch2Color] + "% Overlaps", origM);
		summary.addValue("Overlap Area", aOverlapPixels);
		summary.addValue("Overlap Count", overlapCount);
		summary.addValue(channels[ch2Color] + " Overlaps", ch2Overlaps);
		
		//Segmentation
		summary.addValue("Th Algorithm", thMethods[thMethodInt]);
		summary.addValue(channels[ch1Color] + " Th", th_ch1);
		summary.addValue(channels[ch2Color] + " Th", th_ch2);
		
		//Channel 1 Measurements 
		summary.addValue(channels[ch1Color] + " Sum Intensities", ch1SumIntensity);
		summary.addValue(channels[ch1Color] + " Sum Intensities > th", ch1SumIntensityTh);
		summary.addValue(channels[ch1Color] + " Mean Intensities > th", ch1MeanInt);
		summary.addValue(channels[ch1Color] + " Area", aCh1Pixels);
		
		//Channel 2 Measurements
		summary.addValue(channels[ch2Color] + " Sum Intensities", ch2SumIntensity);
		summary.addValue(channels[ch2Color] + " Sum Intensities > th", ch2SumIntensityTh);
		summary.addValue(channels[ch2Color] + " Mean Intensities > th", ch2MeanInt);
		summary.addValue(channels[ch2Color] + " Area", aCh2Pixels);
		
		
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
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_ROI.tif");
		ImagePlus image = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_R_G.tif");
		image.show();
		nucleus.show();

	}

}