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
import ij.plugin.frame.Recorder;

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
import ij.Macro;

public class Interaction_Factor implements PlugIn, DialogListener {

	private String PREF_KEY = "IF_prefs.";
	private int nMaxSimulationsIF = 50;
	private String[] channels = {"Red","Green","Blue"};
	private String[] channelsLower = {"red","green","blue"};
	private String[] channelsAbb = {"R","G","B"};
	private String[] thMethods;
	private AutoThresholder.Method[] methods;
	private String[] measurements = {"Clusters_Area","ROI_Area","Sum_Pixel_Inten","Sum_Pixel_Inten_>_Th","Mean_Pixel_Inten_>_Th","Ch1_Stoichiometry","Ch2_Stoichiometry",
			"Overlaps","%Overlaps","Overlaps_Count","Overlap_Area"};
	private boolean[] measurVals = {true,true,true,true,true,true,true,true,true,true,true,true};
	private String[] outputImg = {"Save_Random_Simulations","Show_Ch1_Mask","Show_Ch2_Mask","Show_ROI_Mask","Show_Overlap_Mask","Overlap_Locations"};
	private boolean[] outputImgVals = {false,false,false,false,false,false};
	
	private int thMethodInt = 11 ;
	private int ch1Color = 0 ;
	private int ch2Color = 1 ;
	private boolean edgeOption = false;
	private boolean moveCh1Clusters = false ;

	//Measurement Options
	private boolean overlapsOpt = false ;
	private boolean overlapsPercOpt = false ;
	private boolean overlapsCountOpt = false;
	private boolean overlapsAreaOpt = false  ;
	private boolean sumIntOption = false;
	private boolean sumIntThOption = false ;
	private boolean meanIntThOption = false;
	private boolean areaOption = false;
	private boolean areaRoiOption = false ;
	private boolean ch1StoiOption = false;
	private boolean ch2StoiOption = false ;
	
	//Output Options
	private boolean simImageOption = false ;
	private boolean ch1MaskOption = false;
	private boolean ch2MaskOption = false;
	private boolean roiMaskOption = false;
	private boolean overlapMaskOption = false ;
	private boolean overlapLocations = false; 

	public Interaction_Factor() {
		thMethods = AutoThresholder.getMethods();
		methods = AutoThresholder.Method.values();
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
	{
		if(e != null)
		{
		
			if(e.getID() == 1001)
			{
				ActionEvent e_ = (ActionEvent) e;
				String command = e_.getActionCommand();
				if(command == "Test IF")
				{
					//Execute IF code here
					//IJ.log("Running IF...");
					
					Choice choice0 = (Choice) gd.getChoices().get(0);
					 ch1Color = choice0.getSelectedIndex();
					Choice choice1 = (Choice) gd.getChoices().get(1);
					 ch2Color = choice1.getSelectedIndex();
					Choice choice2 = (Choice) gd.getChoices().get(2);
					 thMethodInt = choice2.getSelectedIndex();
					
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
					Checkbox check12 = (Checkbox) checkboxes.get(12);
					Checkbox check13 = (Checkbox) checkboxes.get(13);
					Checkbox check14 = (Checkbox) checkboxes.get(14);
					Checkbox check15 = (Checkbox) checkboxes.get(15);
					Checkbox check16 = (Checkbox) checkboxes.get(16);
					Checkbox check17 = (Checkbox) checkboxes.get(17);
					Checkbox check18 = (Checkbox) checkboxes.get(18);

					 edgeOption = check0.getState();
					 moveCh1Clusters = check1.getState();
					
					 areaOption = check2.getState();
					 areaRoiOption = check3.getState();

					 sumIntOption = check4.getState();
					 sumIntThOption = check5.getState();
					 meanIntThOption = check6.getState();
					
					 ch1StoiOption = check7.getState();
					 ch2StoiOption = check8.getState();
					
					 overlapsOpt = check9.getState();
					 overlapsPercOpt = check10.getState();
					 overlapsCountOpt = check11.getState();
					 overlapsAreaOpt = check12.getState();
					
					 simImageOption = check13.getState();
					 ch1MaskOption = check14.getState();
					 ch2MaskOption = check15.getState();
					 roiMaskOption = check16.getState();
					 overlapMaskOption = check17.getState();
					 overlapLocations = check18.getState();
					
					run_IF(ch1Color,ch2Color,thMethodInt,edgeOption,moveCh1Clusters,areaOption,areaRoiOption,overlapsOpt,overlapsPercOpt,overlapsCountOpt,
							overlapsAreaOpt,sumIntOption,sumIntThOption,meanIntThOption,ch1StoiOption,ch2StoiOption,simImageOption,ch1MaskOption,ch2MaskOption,roiMaskOption,
							overlapMaskOption,overlapLocations);
					
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
		 
		 thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt", 11);
		 ch1Color = (int) Prefs.get(PREF_KEY + "ch1Color", 0);
		 ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
		 edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);
		 moveCh1Clusters = Prefs.get(PREF_KEY + "moveOption", true);

		//Measurement Options
		 overlapsOpt = Prefs.get(PREF_KEY + "overlapsOpt", true);
		 overlapsPercOpt = Prefs.get(PREF_KEY + "overlapsPercOpt", true);
		 overlapsCountOpt = Prefs.get(PREF_KEY + "overlapsCountOpt", true);
		 overlapsAreaOpt = Prefs.get(PREF_KEY + "overlapsAreaOpt", true);
		 sumIntOption =  Prefs.get(PREF_KEY + "sumIntOption", true);
		 sumIntThOption = Prefs.get(PREF_KEY + "sumIntThOption", true);
		 meanIntThOption = Prefs.get(PREF_KEY + "meanIntThOption", true);
		 areaOption = Prefs.get(PREF_KEY + "areaOption", true);
		 areaRoiOption = Prefs.get(PREF_KEY + "areaRoiOption", true);
		 ch1StoiOption= Prefs.get(PREF_KEY + "ch1StoiOption", true);
		 ch2StoiOption =Prefs.get(PREF_KEY + "ch2StoiOption", true );
		
		//Output Options
		 simImageOption = Prefs.get(PREF_KEY + "simImageOption", false);
		 ch1MaskOption =Prefs.get(PREF_KEY + "ch1MaskOption", false);
		 ch2MaskOption =Prefs.get(PREF_KEY + "ch2MaskOption", false);
		 roiMaskOption = Prefs.get(PREF_KEY + "roiMaskOption", false);
		 overlapMaskOption = Prefs.get(PREF_KEY + "overlapMaskOption", false);
		 overlapLocations = Prefs.get(PREF_KEY + "overlapLocations", false);
	
		 
		measurVals[0] = areaOption;
		measurVals[1] = areaRoiOption;
		measurVals[2] = sumIntOption;
		measurVals[3] = sumIntThOption;
		measurVals[4] = meanIntThOption;
		measurVals[5] = ch1StoiOption;
		measurVals[6] = ch2StoiOption;
		
		measurVals[7]= overlapsOpt;
		measurVals[8] = overlapsPercOpt;
		measurVals[9]= overlapsCountOpt;
		measurVals[10]= overlapsAreaOpt;
		
		//Output options
		outputImgVals[0] = simImageOption;
		outputImgVals[1] = ch1MaskOption;
		outputImgVals[2] = ch2MaskOption;
		outputImgVals[3] = roiMaskOption;
		outputImgVals[4] = overlapMaskOption;
		outputImgVals[5] = overlapLocations;
		
		// Dialog
		GenericDialog gd = new NonBlockingGenericDialog("Interaction Factor");
		gd.addDialogListener((DialogListener)this);

		gd.addMessage("--------------- Segmentation ---------------\n");
		gd.addChoice("Channel_1(Ch1)_Color:", channels, channels[ch1Color]);
		gd.addChoice("Channel_2(Ch2)_Color:", channels, channels[ch2Color]);
		gd.addChoice("Threshold:", thMethods, thMethods[thMethodInt]);
		gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);

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
		
		gd.addMessage("--------------- IF Parameter ---------------\n");
		gd.addCheckbox("Move_Ch1_Clusters", moveCh1Clusters);
		
		gd.addMessage("----------- Additional Measurements --------\n");
		gd.addCheckboxGroup(6, 2, measurements, measurVals);
		
		gd.addMessage("-------------- Output Images ---------------\n");
		gd.addCheckboxGroup(3, 2, outputImg, outputImgVals);
		
		// ***** Test IF Button *****
		buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		b1 = new Button("Test IF");
		b1.addActionListener(gd);
		b1.addKeyListener(gd);
		buttons.add(b1);
		gd.addPanel(buttons, GridBagConstraints.EAST, new Insets(15,0,0,0));
		// *****
		Recorder.recordInMacros = true;
		gd.showDialog();
		
		
		if (gd.wasCanceled())
			return;
		
		
		ImageJ thisImageJ = IJ.getInstance();
		ProgressBar progressBar = thisImageJ.getProgressBar();
		progressBar.show(0.0, true);
		IfFunctions fs = new IfFunctions();
		
		
		String ch1ColorStr =gd.getNextChoice();
		for(int i=0;i<channels.length;i++){
			if (ch1ColorStr.equals(channels[i])| ch1ColorStr.equals(channelsLower[i])){
				ch1Color = i;
			}
		}
		String ch2ColorStr =gd.getNextChoice();
		for(int i=0;i<channels.length;i++){
			if (ch2ColorStr.equals(channels[i])| ch2ColorStr.equals(channelsLower[i])){
				ch2Color = i;
			}
		}
		String thMethodIntStr = gd.getNextChoice();
		for(int i=0;i<thMethods.length;i++){
			if (thMethodIntStr.equals(thMethods[i])){
				thMethodInt = i;
			}
		}
		
		
		 edgeOption = gd.getNextBoolean();
		 moveCh1Clusters = gd.getNextBoolean();
		
		 areaOption = gd.getNextBoolean();
		 areaRoiOption = gd.getNextBoolean();

		 sumIntOption = gd.getNextBoolean();
		 sumIntThOption = gd.getNextBoolean();
		 meanIntThOption = gd.getNextBoolean();
		
		 ch1StoiOption = gd.getNextBoolean();
		 ch2StoiOption = gd.getNextBoolean();
		
		 overlapsOpt = gd.getNextBoolean();
		 overlapsPercOpt = gd.getNextBoolean();
		 overlapsCountOpt = gd.getNextBoolean();
		 overlapsAreaOpt = gd.getNextBoolean();
		
		 simImageOption = gd.getNextBoolean();
		 ch1MaskOption = gd.getNextBoolean();
		 ch2MaskOption = gd.getNextBoolean();
		 roiMaskOption = gd.getNextBoolean();
		 overlapMaskOption = gd.getNextBoolean();
		 overlapLocations = gd.getNextBoolean();

		 run_IF(ch1Color,ch2Color,thMethodInt,edgeOption,moveCh1Clusters,areaOption,areaRoiOption,overlapsOpt,overlapsPercOpt,overlapsCountOpt,
				overlapsAreaOpt,sumIntOption,sumIntThOption,meanIntThOption,ch1StoiOption,ch2StoiOption,simImageOption,ch1MaskOption,ch2MaskOption,roiMaskOption,
				overlapMaskOption,overlapLocations);
		
		 
		  if (Recorder.record){
				//Recorder.recordCall("Interaction Factor",options);
				Recorder.recordOption("channel_1(ch1)_color",ch1ColorStr);
				Recorder.recordOption("channel_2(ch2)_color",ch2ColorStr);
				Recorder.recordOption("threshold",thMethodIntStr);
				if (edgeOption){
					Recorder.recordOption("Exclude_Edge_Clusters");
				}
				if (moveCh1Clusters){
					Recorder.recordOption("Move_Ch1_Clusters");
				}
				if(areaOption){
					Recorder.recordOption("Clusters_Area");
				}
				if(areaRoiOption){
					Recorder.recordOption("ROI_Area");
				}
				if(sumIntOption){
					Recorder.recordOption("Sum_Pixel_Inten");
				}
				if(sumIntThOption){
					Recorder.recordOption("Sum_Pixel_Inten_>_Th");
				}
				if(meanIntThOption){
					Recorder.recordOption("Mean_Pixel_Inten_>_Th");
				}
				if(overlapsOpt){
					Recorder.recordOption("Overlaps");
				}
				if(overlapsPercOpt){
					Recorder.recordOption("%Overlaps");
				}
				if(overlapsCountOpt){
					Recorder.recordOption("Overlaps_Count");
				}
				if(overlapsAreaOpt){
					Recorder.recordOption("Overlap_Area");
				}
				if(simImageOption){
					Recorder.recordOption("Save_Random_Simulations");
				}
				if(ch1MaskOption){
					Recorder.recordOption("Show_Ch1_Mask");
				}
				if(ch2MaskOption){
					Recorder.recordOption("Show_Ch2_Mask");
				}
				if(overlapMaskOption){
					Recorder.recordOption("Show_Overlap_Mask");
				}
				if(overlapLocations){
					Recorder.recordOption("Overlap_Locations");	
				}
			}

	}
	
	
	public void run_IF(int ch1Color,int ch2Color,int thMethodInt,boolean edgeOption,boolean moveCh1Clusters,boolean areaOption,boolean areaRoiOption,boolean overlapsOpt,boolean overlapsPercOpt,boolean overlapsCountOpt,
			boolean overlapsAreaOpt,boolean sumIntOption,boolean sumIntThOption,boolean meanIntThOption,boolean ch1StoiOption,boolean ch2StoiOption,boolean simImageOption,boolean ch1MaskOption,boolean ch2MaskOption,boolean roiMaskOption,
			boolean overlapMaskOption,boolean overlapLocations)
	{
		
		IJ.showProgress(0.0);
		
		//Set options
		Prefs.set(PREF_KEY + "ch1Color", ch1Color);
		Prefs.set(PREF_KEY + "ch2Color", ch2Color);
		Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
		Prefs.set(PREF_KEY + "edgeOption", edgeOption);
		Prefs.set(PREF_KEY + "moveOption", moveCh1Clusters);
		Prefs.set(PREF_KEY+"areaOption", areaOption);
		Prefs.set(PREF_KEY+"areaRoiOption", areaRoiOption);

		//Measurements
		Prefs.set(PREF_KEY + "overlapsOpt", overlapsOpt);
		Prefs.set(PREF_KEY + "overlapsPercOpt", overlapsPercOpt);
		Prefs.set(PREF_KEY + "overlapsCountOpt", overlapsCountOpt);
		Prefs.set(PREF_KEY + "overlapsAreaOpt", overlapsAreaOpt);
		Prefs.set(PREF_KEY + "sumIntOption", sumIntOption);
		Prefs.set(PREF_KEY + "sumIntThOption", sumIntThOption);
		Prefs.set(PREF_KEY + "meanIntThOption", meanIntThOption);
		Prefs.set(PREF_KEY + "ch1StoiOption", ch1StoiOption);
		Prefs.set(PREF_KEY + "ch2StoiOption", ch2StoiOption);
		
		//Output options
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
			return;
			
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
	
		int[] ch1OverlapsStoich = fs.clusterStoichiometry(ipCh2Mask, ipCh1Mask);
		int[] ch2OverlapsStoich = fs.clusterStoichiometry(ipCh1Mask, ipCh2Mask);

		//Percentage of Overlaps
		double ch2Percentage = (double) ch2Overlaps / (double) ch2ClusterCount;
		double ch1Percentage = (double) ch1Overlaps / (double) ch1ClusterCount;

		//Average Mean intensity
		double ch1MeanInt = ch1Stats.mean;
		double ch2MeanInt = ch2Stats.mean;

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
		
		// Calculating IF
		
		// Calculating IF ch1-ch2

		double[] ch2ClustersProbs = new double[ch2Clusters.size()];
		Arrays.fill(ch2ClustersProbs, 0);
		double countForPvalCh2 = 0;
		
		double[] ch1ClustersProbs = new double[ch1Clusters.size()];
		Arrays.fill(ch1ClustersProbs, 0);
		double countForPvalCh1 = 0;
		
		for (int i = 0; i < nMaxSimulationsIF; i++) {
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
			
			ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
			ipCh1RandomMask.threshold(th_ch1);
			
			//generate ch2 channel mask
			
			ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1RandomMask, ch2ClustersProbs,
					ch2Clusters, ch2ClustersRect);
            
			//generate ch2 channel mask
			
			ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
			ipCh2RandomMask.threshold(th_ch2);
			
			ImageProcessor ipCh1Random2;
			
			if (moveCh1Clusters){
				 ipCh1Random2 = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh2RandomMask, ch1ClustersProbs,
							ch1Clusters, ch1ClustersRect);
			}
			else{
				 ipCh1Random2 = ipCh1.duplicate();
			}
			
			ImageProcessor ipCh1RandomMask2 = ipCh1Random2.duplicate();
			ipCh1RandomMask2.threshold(th_ch1);
			
			int ch2RandomOverlaps =   fs.ch2ClusterOverlaps(ipCh1RandomMask, ipCh2RandomMask);
			int ch1RandomOverlaps =  fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask2);
			
			double percOverlapsCh2 = (double)ch2RandomOverlaps/(double)ch2Clusters.size();
			double percOverlapsCh1 = (double)ch1RandomOverlaps/(double)ch1Clusters.size();

			if (percOverlapsCh2 >= ch2Percentage){
				countForPvalCh2 +=1;
			}
			if (percOverlapsCh1 >= ch1Percentage){
				countForPvalCh1 +=1;
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
		
		double pValCh1Ch2 = countForPvalCh2/(double)nMaxSimulationsIF;
		double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, nMaxSimulationsIF);
		double IFCh1Ch2 = 0;
		IFCh1Ch2 = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);
		
		double pValCh2Ch1 = countForPvalCh1/(double)nMaxSimulationsIF;
		double[] ch1ClustersProbsTest = fs.prob(ch1ClustersProbs, nMaxSimulationsIF);
		double IFCh2Ch1 = 0;
		IFCh2Ch1 = fs.calcIF(ch1ClustersProbsTest, ch1Percentage);
		
		//Summary Measurements
		summary.incrementCounter();
		summary.addValue("Image", name);
		summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);

		//Overlap Measurements

		String pValStrCh1Ch2;
		
		if (pValCh1Ch2 == 0){
			pValStrCh1Ch2 = "p<0.02" ;
		}
		else{
			pValStrCh1Ch2 ="p="+ String.valueOf(pValCh1Ch2);
		}
		
		String pValStrCh2Ch1;
		
		if (pValCh2Ch1 == 0){
			pValStrCh2Ch1 = "p<0.02" ;
		}
		else{
			pValStrCh2Ch1 ="p="+ String.valueOf(pValCh2Ch1);
		}
		
		summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" IF", IFCh1Ch2);
		summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" p-val", pValStrCh1Ch2);
		
		if (moveCh1Clusters){
			summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" IF", IFCh2Ch1);
			summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" p-val", pValStrCh2Ch1);
		}
		else{
			summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" IF", "NT");
			summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" p-val", "NT");
		}
		
		for (int c1 = 0; c1 < 3; c1++){
			for(int c2 = 0; c2 < 3; c2++){
				if (c1 != c2){
					if (((c1 == ch1Color) & (c2  == ch2Color)) || ((c1 == ch2Color) & (c2  == ch1Color))){
						continue;	
					}	
					else{
						summary.addValue(channelsAbb[c1]+ "-" +channelsAbb[c2]+" IF", "NT");
						summary.addValue(channelsAbb[c1]+ "-" +channelsAbb[c2]+" p-val", "NT");
						summary.addValue(channelsAbb[c2]+ "-" +channelsAbb[c1]+" IF", "NT");
						summary.addValue(channelsAbb[c2]+ "-" +channelsAbb[c1]+" p-val", "NT");

					}
				}
				}
			}
		
		//Segmentation
		summary.addValue("Th Algorithm", thMethods[thMethodInt]);
		summary.addValue(channelsAbb[ch1Color] + " Th", th_ch1);
		summary.addValue(channelsAbb[ch2Color] + " Th", th_ch2);
		summary.addValue(channelsAbb[ch1Color] +" Count", ch1ClusterCount);
		summary.addValue(channelsAbb[ch2Color] + " Count", ch2ClusterCount);
		
		//Optional Measurements
		
		if (areaOption){
			summary.addValue(channelsAbb[ch1Color] + " Area", aCh1Pixels);
			summary.addValue(channelsAbb[ch2Color] + " Area", aCh2Pixels);
		}
		if (areaRoiOption){
			summary.addValue("ROI area", aRoi);
		}
		if (sumIntThOption){
			summary.addValue(channelsAbb[ch1Color] + " Sum Inten > th", ch1SumIntensityTh);
			summary.addValue(channelsAbb[ch2Color] + " Sum Inten > th", ch2SumIntensityTh);
		}
		if (meanIntThOption){
			summary.addValue(channelsAbb[ch1Color] + " Mean Inten > th", ch1MeanInt);
			summary.addValue(channelsAbb[ch2Color] + " Mean Inten > th", ch2MeanInt);
		}
		//Overlap Measurements
		if (overlapsOpt){
			summary.addValue(channelsAbb[ch1Color]+" Overlaps",ch1Overlaps);
			summary.addValue(channelsAbb[ch2Color] + " Overlaps", ch2Overlaps);
		}
		if(ch1StoiOption){
			//Stoichiometry
			summary.addValue(channelsAbb[ch1Color] + "1:1", String.format("%.1f", ((float)ch1OverlapsStoich[0]/ch1Overlaps)*100)) ;
			summary.addValue(channelsAbb[ch1Color] + "1:2", String.format("%.1f", ((float)ch1OverlapsStoich[1]/ch1Overlaps)*100));
			summary.addValue(channelsAbb[ch1Color] + "1:3", String.format("%.1f", ((float)ch1OverlapsStoich[2]/ch1Overlaps)*100));
			summary.addValue(channelsAbb[ch1Color] + "1:>3", String.format("%.1f", ((float)ch1OverlapsStoich[3]/ch1Overlaps)*100));
		}
		if(ch2StoiOption){
			//Stoichiometry
			summary.addValue(channelsAbb[ch2Color] + "1:1", String.format("%.1f", ((float)ch2OverlapsStoich[0]/ch2Overlaps)*100));
			summary.addValue(channelsAbb[ch2Color] + "1:2", String.format("%.1f", ((float)ch2OverlapsStoich[1]/ch2Overlaps)*100));
			summary.addValue(channelsAbb[ch2Color] + "1:3", String.format("%.1f", ((float)ch2OverlapsStoich[2]/ch2Overlaps)*100));
			summary.addValue(channelsAbb[ch2Color] + "1:>3", String.format("%.1f", ((float)ch2OverlapsStoich[3]/ch2Overlaps)*100));
		}
		if (overlapsPercOpt){
			summary.addValue(channelsAbb[ch1Color]+" %Overlaps",String.format("%.1f", (float)ch1Percentage*100));
			summary.addValue(channelsAbb[ch2Color] + " %Overlaps", String.format("%.1f", (float)ch2Percentage*100));
		}
		if (overlapsCountOpt){
			summary.addValue("Overlap Count", overlapCount);
		}
		if (overlapsAreaOpt){
			summary.addValue("Overlap Area", aOverlapPixels);
		}
		if (sumIntOption){
			summary.addValue(channelsAbb[ch1Color] + " Sum Inten", (int)ch1SumIntensity);
			summary.addValue(channelsAbb[ch2Color] + " Sum Inten", (int)ch2SumIntensity);
		}
		summary.show("Results");

		if (overlapLocations) {
			rTable.show("Overlap Locations");
		}
		// Show images
		if (ch1MaskOption) {
			ImagePlus ch1Im = new ImagePlus(name + channelsAbb[ch1Color] + " Mask", ipCh1Mask);
			ch1Im.setCalibration(cal);
			ch1Im.show();
		}
		if (ch2MaskOption) {
			ImagePlus ch2Im = new ImagePlus(name + channelsAbb[ch2Color] + " Mask", ipCh2Mask);
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
		//ImagePlus image = IJ.openImage(
				//"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_R_G.tif");
		ImagePlus image = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/ellipse_sims_Sept_2016/ellipses_sims/04_IF_90_c1n_200_c1a_100_c2n_100_c2a_100_c12_sim_mask.tif");
		image.show();
		nucleus.show();

	}

}
