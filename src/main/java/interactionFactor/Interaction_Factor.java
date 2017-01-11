package interactionFactor;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.DialogListener;
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
import java.util.Vector;
import java.awt.*;
import ij.measure.Calibration;
import java.util.Arrays;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import java.awt.event.*;
import java.awt.Color;
import ij.gui.ProgressBar;

public class Interaction_Factor implements PlugIn, DialogListener {

	private String PREF_KEY = "IF_prefs.";
	private int nMaxSimulations = 50;
	private String[] channels = {"Red","Green","Blue"};
	private String[] thMethods;
	private AutoThresholder.Method[] methods;

	public Interaction_Factor() {
		thMethods = AutoThresholder.getMethods();

		methods = AutoThresholder.Method.values();

	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
	{
		if(e != null)
		{
			// REMOVED - We will no longer do it this way
			/*if(e.getID() == 701)
			{
				ItemEvent e_ = (ItemEvent) e;
				Object c = e_.getSource();
				if(c instanceof Choice)
				{
					Choice c_ = (Choice) c;
					if(c_.getItemCount() > 3)
					{
						String th_value = (String) e_.getItem();
						IJ.log("Run TH Overlay: " + th_value);

						//Execute TH code here
					}

				}

			}*/
			if(e.getID() == 1001)
			{
				ActionEvent e_ = (ActionEvent) e;
				String command = e_.getActionCommand();
				if(command == "Test IF")
				{
					//Execute IF code here
					//IJ.log("Running IF...");
					
					run_IF(gd);
				}
				if(command == "Apply Overlay")
				{
					//IJ.log("Apply Overlay");
					//IJ.run("Remove Overlay");
					//choices
					Choice choice0 = (Choice) gd.getChoices().get(0);
					int ch1Color = choice0.getSelectedIndex();
					
					Choice choice1 = (Choice) gd.getChoices().get(1);
					int ch2Color = choice1.getSelectedIndex();
					
					Choice choice2 = (Choice) gd.getChoices().get(2);
					int thMethodInt = choice2.getSelectedIndex();
					
					//checkbox
					Vector checkboxes = gd.getCheckboxes();
					Checkbox check0 = (Checkbox) checkboxes.get(0);
					boolean edgeOption = check0.getState();

					IfFunctions fs = new IfFunctions();

					ImagePlus im = IJ.getImage();

					if (im.getType() != ImagePlus.COLOR_RGB) {
						IJ.error("RGB image required");
						
					}

					if (ch1Color == ch2Color) {
						IJ.error("Channel Colors are the same. Choose another channel");
				
					}

					AutoThresholder.Method method = methods[thMethodInt];


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
					
					IJ.setThreshold(im, th_ch2, 255,"Red");
					im.updateAndDraw();
					
					if (edgeOption) {
						if (hasRoi) {
							fs.excludeEdgesRoi(roiSelection, ipMask, ipCh1Mask);
							fs.excludeEdgesRoi(roiSelection, ipMask, ipCh2Mask);
						} else {
							fs.excludeEdges(roi, ipMask, ipCh1Mask);
							fs.excludeEdges(roi, ipMask, ipCh2Mask);
						}
					}
					
					//fs.setClustersOverlay(im, ipCh1Mask,  ipCh2Mask);
					Overlay chsOverlays = fs.returnOverlay(ipCh1Mask, ipCh2Mask);
					Color stColor = Color.WHITE;
					Color fColor = new Color((float)1.0,(float)1.0,(float)1.0,(float)1.0);
					//chsOverlays.setFillColor(fColor);
					chsOverlays.setStrokeColor(stColor);
					im.setOverlay(chsOverlays);

					// im.setOverlay(ovCh1);
					//im.setOverlay(chsOverlays);
					//IJ.run(im, "Select None", "");
					//Overlay orig = im.getOverlay();
					//im.drawOverlay(chsOverlays);
					//ip.setOverlay(chsOverlays);
					//im.updateAndDraw();
					//im.draw();
					//ImageCanvas ic = im.getCanvas();
					//ic.setImageUpdated();
					//ic.resetDoubleBuffer();
					//im.setRoi(roi);
					//chsOverlays.drawBackgrounds(true);


				}
				if(command == "Clear Overlay")
				{
					//IJ.log("Clear Overlay");
					//IJ.run("Remove Overlay");
					ImagePlus im = IJ.getImage();
					//im.setHideOverlay(true);
					IJ.run("Remove Overlay");
				}
			}
		}
		else
		{

			gd.repaint();
			//IJ.log("Started IF Plugin...");
		}
		return true;
	}

	public void run(String arg) {
		

		
		int thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt", 11);
		int ch1Color = (int) Prefs.get(PREF_KEY + "ch1Color", 0);
		int ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
		boolean edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);
		boolean moveCh1Clusters = Prefs.get(PREF_KEY + "moveOption", true);

		
		
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
		GenericDialog gd = new NonBlockingGenericDialog("Interaction Factor");
		gd.addDialogListener((DialogListener)this);

		gd.addMessage("--------------- Segmentation ---------------\n");
		gd.addChoice("Channe1(Ch1)_Color:", channels, channels[ch1Color]);
		gd.addChoice("Channe2(Ch2)_Color:", channels, channels[ch2Color]);
		gd.addChoice("Threshold:", thMethods, thMethods[thMethodInt]);
		gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);
		gd.addCheckbox("Move_Ch1_Clusters", moveCh1Clusters);

		//gd.setInsets(5, 0, 0);

		// ***** Apply and Remove Overlay Buttons *****
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		Button b1 = new Button("Apply Overlay");
		b1.addActionListener(gd);
		b1.addKeyListener(gd);
		buttons.add(b1);
		Button b2 = new Button("Clear Overlay");
		b2.addActionListener(gd);
		b2.addKeyListener(gd);
		buttons.add(b2);
		gd.addPanel(buttons, GridBagConstraints.CENTER, new Insets(15,0,0,0));
		// *****

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

		// ***** Test IF Button *****
		buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		b1 = new Button("Test IF");
		b1.addActionListener(gd);
		b1.addKeyListener(gd);
		buttons.add(b1);
		gd.addPanel(buttons, GridBagConstraints.EAST, new Insets(15,0,0,0));
		// *****

		gd.showDialog();

		if (gd.wasCanceled())
			return;
		ImageJ thisImageJ = IJ.getInstance();
		ProgressBar progressBar = thisImageJ.getProgressBar();
		progressBar.show(0.0, true);
		run_IF(gd);
		
		//gd.centerDialog(true);
		

	}

	private void run_IF(GenericDialog gd)
	{
		
		IJ.showProgress(0.0);
		
		//get options	
		Choice choice0 = (Choice) gd.getChoices().get(0);
		int ch1Color = choice0.getSelectedIndex();
		Choice choice1 = (Choice) gd.getChoices().get(1);
		int ch2Color = choice1.getSelectedIndex();
		Choice choice2 = (Choice) gd.getChoices().get(2);
		int thMethodInt = choice2.getSelectedIndex();
		
		Vector checkboxes = gd.getCheckboxes();
		Checkbox check0 = (Checkbox) checkboxes.get(0);
		Checkbox check1 = (Checkbox) checkboxes.get(1);
		Checkbox check2 = (Checkbox) checkboxes.get(2);
		Checkbox check3 = (Checkbox) checkboxes.get(3);
		Checkbox check4 = (Checkbox) checkboxes.get(4);
		Checkbox check5 = (Checkbox) checkboxes.get(5);
		Checkbox check6 = (Checkbox) checkboxes.get(6);
		Checkbox check7 = (Checkbox) checkboxes.get(7);
		Checkbox check8 = (Checkbox) checkboxes.get(8);
		Checkbox check9 = (Checkbox) checkboxes.get(9);
		Checkbox check10 = (Checkbox) checkboxes.get(10);
		Checkbox check11 = (Checkbox) checkboxes.get(11);


		boolean edgeOption = check0.getState();
		boolean moveCh1Clusters = check1.getState();
		boolean sumIntOption = check2.getState();
		boolean sumIntThOption = check3.getState();
		boolean meanIntThOption = check4.getState();
		boolean areaOption = check5.getState();
		boolean simImageOption = check6.getState();
		boolean ch1MaskOption = check7.getState();
		boolean ch2MaskOption = check8.getState();
		boolean roiMaskOption = check9.getState();
		boolean overlapMaskOption = check10.getState();
		boolean overlapLocations = check11.getState();;
		
		// set options
		Prefs.set(PREF_KEY + "ch1Color", ch1Color);
		Prefs.set(PREF_KEY + "ch2Color", ch2Color);
		Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
		Prefs.set(PREF_KEY + "edgeOption", edgeOption);
		Prefs.set(PREF_KEY + "moveOption", moveCh1Clusters);

		Prefs.set(PREF_KEY + "sumIntOption", sumIntOption);
		Prefs.set(PREF_KEY + "sumIntThOption", sumIntThOption);
		Prefs.set(PREF_KEY + "meanIntThOption", meanIntThOption);
		Prefs.set(PREF_KEY + "areaOption", areaOption);
		Prefs.set(PREF_KEY + "simImageOption", simImageOption);
		Prefs.set(PREF_KEY + "ch1MaskOption", ch1MaskOption);
		Prefs.set(PREF_KEY + "ch2MaskOption", ch2MaskOption);
		Prefs.set(PREF_KEY + "roiMaskOption", roiMaskOption);
		Prefs.set(PREF_KEY + "overlapMaskOption", overlapMaskOption);
		Prefs.set(PREF_KEY + "overlapLocations", overlapLocations);

		String imagedir = ".";
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
		//Area ROI
		double aRoi  = 0;
		if (hasMask || hasRoi){
			ipCh1.setMask(ipMask);
			ImageStatistics roiStats = ipCh1.getStatistics();
			aRoi = (double) roiStats.pixelCount * calConvert;
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

		if (ch1ClusterCount == 0 || ch2ClusterCount == 0){
			IJ.error("Zero Clusters. Choose another color");
			
		}
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
		double ch1MeanInt = ch1Stats.mean;
		double ch2MeanInt = ch2Stats.mean;

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
			IJ.showProgress(i, 50);
			
			
			String nSimulation = Integer.toString(i+1);
			IJ.showStatus("Running IF..."+nSimulation+"/50");
			//IJ.log("Running IF..."+nSimulation+"/50");
			ImageProcessor ipCh1Random;
			if (moveCh1Clusters){
				 ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
			}
			else{
				 ipCh1Random = ipCh1.duplicate();
			}
			//generate ch1 channel mask
			ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
			ipCh1RandomMask.threshold(th_ch1);
			ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1RandomMask, ch2ClustersProbs,
					ch2Clusters, ch2ClustersRect);
			//generate ch2 channel mask
			ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
			ipCh2RandomMask.threshold(th_ch2);

			
			int ch2RandomOverlaps = fs.overlapCount(ipCh2RandomMask, ipCh1RandomMask); // check this maybe replace with  fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask)

			double percOverlaps = (double)ch2RandomOverlaps/(double)ch2Clusters.size();

			if (percOverlaps >= ch2Percentage){
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
		summary.addValue(channels[ch1Color]+" %Overlaps",ch1Percentage*100);

		summary.addValue(channels[ch2Color] + " Cluster Count", ch2ClusterCount);
		summary.addValue(channels[ch2Color] + " Overlaps", ch2Overlaps);
		summary.addValue(channels[ch2Color] + "% Overlaps", ch2Percentage*100);
		summary.addValue("Overlap Count", overlapCount);
		summary.addValue("Overlap Area", aOverlapPixels);
		summary.addValue("ROI area", aRoi);

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
		IJ.showProgress(1.0);

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
