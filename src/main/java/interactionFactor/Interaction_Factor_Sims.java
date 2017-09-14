package interactionFactor;

import ij.IJ;
import ij.gui.DialogListener;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.Recorder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionEvent;
import ij.measure.Calibration;


/*Copyright (C) 2017  Keria Bermudez-Hernandez and Sarah Keegan 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
This plugin provides a method for quantifying protein-protein interactions by using stochastic modeling of super-resolution fluorescence microscopy data (RGB images). 
The result is an unbiased measure of co-localization of protein clusters, independent of cluster density and comparable across images. 
Please refer to manuscript (REF) and documentation for a detailed description of the Interaction Factor. 
Additionally, it allows the user to generate any number of simulations for the image at a user-defined Interaction Factor for use in further analysis.
*/


public class Interaction_Factor_Sims implements PlugIn, DialogListener {
	
	private static String[] thMethods = AutoThresholder.getMethods();
    private static  String[] channels = {"Red", "Green", "Blue"};
    private static  String[] channelsLower = {"red", "green", "blue"};
    private String[] channelsAbb = {"R","G","B"};
    private static  String[] simParametersCh1 = {"None", "Random"};
    private static  String[] simParametersCh2 = {"Random", "NonRandom"};
    
    private String[] measurements = {"Clusters_Area","ROI_Area","Sum_Pixel_Inten","Clusters_Sum_Inten","Clusters_Mean_Inten","Ch1_Stoichiometry","Ch2_Stoichiometry",
			"Clusters_Overlaps","%Clusters_Overlaps","Overlap_Count","Overlap_Area"};
	private boolean[] measurVals = {false,false,false,false,false,false,false,false,false,false,false,false};
	private String[] outputImg = {"Show_Simulations","Show_Ch1_Mask","Show_Ch2_Mask","Show_ROI_Mask","Show_Overlap_Mask","Overlap_Locations_Table"};
	private boolean[] outputImgVals = {false,false,false,false,false,false};
	
	private int thMethodInt = 11 ;
	private int ch1Color = 0 ;
	private int ch2Color = 1 ;
	private boolean edgeOption = false;
	private boolean moveCh1Clusters = false ;

	private boolean thManualOption = false;
	private int thManual_ch1Level = 0;
	private int thManual_ch2Level = 0;
	private double minClusterArea = 0;

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
	private boolean showSimsOption = false;
	private boolean ch1MaskOption = false;
	private boolean ch2MaskOption = false;
	private boolean roiMaskOption = false;
	private boolean overlapMaskOption = false ;
	private boolean overlapLocations = false; 
	
	private String ch1SimParam;
    private String ch2SimParam;
    private Integer ch1SimParamInt;
    private Integer ch2SimParamInt;
    private double interFactorCh2;
	
    private AutoThresholder.Method[] methods;

    private static int nMaxSimulations = 0;
    protected final static String PREF_KEY = "Interaction_Factor_Sims.";
    
    public Interaction_Factor_Sims() {
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
				if(command == "Apply Overlay")
				{
					
					//choices
					Choice choice0 = (Choice) gd.getChoices().get(0);
					int ch1Color = choice0.getSelectedIndex();
					
					Choice choice1 = (Choice) gd.getChoices().get(1);
					int ch2Color = choice1.getSelectedIndex();
					
					Choice choice2 = (Choice) gd.getChoices().get(2);
					int thMethodInt = choice2.getSelectedIndex();

					Vector numFields = gd.getNumericFields();
					TextField numField0 = (TextField) numFields.get(0);
					TextField numField1 = (TextField) numFields.get(1);
					TextField numField2 = (TextField) numFields.get(2);
					
					int thManual_ch1Level = Integer.parseInt(numField0.getText());
					int thManual_ch2Level = Integer.parseInt(numField1.getText());
					double minClusterArea = Double.parseDouble(numField2.getText());
					
					//checkbox
					Vector checkboxes = gd.getCheckboxes();
					Checkbox check0 = (Checkbox) checkboxes.get(0);
					Checkbox check1 = (Checkbox) checkboxes.get(1);
					boolean thManualOption = check0.getState();
					boolean edgeOption = check1.getState();

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

					int th_ch1 = thManual_ch1Level;
					int th_ch2 = thManual_ch2Level;
					if(!thManualOption)
					{
						AutoThresholder autoth = new AutoThresholder();

						// Threshold ch1 channel
						ipCh1Mask.setMask(ipMask);
						int[] ch1_hist = ipCh1Mask.getHistogram();
						th_ch1 = autoth.getThreshold(method, ch1_hist);


						// Threshold ch2 channel
						ipCh2Mask.setMask(ipMask);
						int[] ch2_hist = ipCh2Mask.getHistogram();
						th_ch2 = autoth.getThreshold(method, ch2_hist);

					}
					ipCh1Mask.threshold(th_ch1);
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
					//Calibration
			        Calibration cal =im.getCalibration();
			        double pixelHeight = cal.pixelHeight;
			        double pixelWidth = cal.pixelWidth;
			        double calConvert = pixelHeight*pixelWidth;
			        
					if (minClusterArea > 0){
						fs.removeClusters(ipCh1Mask, minClusterArea,calConvert);
						fs.removeClusters(ipCh2Mask, minClusterArea,calConvert);						
					}
					Overlay chsOverlays = fs.returnOverlay(ipCh1Mask, ipCh2Mask);
					Color stColor = Color.WHITE;
					chsOverlays.setStrokeColor(stColor);
					im.setOverlay(chsOverlays);

				}
				if(command == "Clear Overlay")
				{				
					IJ.run("Remove Overlay");
				}
			}
		}
		else
		{

			gd.repaint();
			
		}
			return true;
		}
		

	public void run(String arg) {
		
		 thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt",0);
         ch1Color =  (int) Prefs.get(PREF_KEY + "ch1Color", 0);
         ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
         edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);

		 thManualOption = Prefs.get(PREF_KEY + "thManualOption",false);
		 thManual_ch1Level = (int) Prefs.get(PREF_KEY + "thManual_ch1Level",0);
		 thManual_ch2Level = (int) Prefs.get(PREF_KEY + "thManual_ch2Level",0);
		 minClusterArea = (double) Prefs.get(PREF_KEY + "minClusterArea", 0);


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
		 showSimsOption = Prefs.get(PREF_KEY + "showSimsOption", false);
         ch1MaskOption = Prefs.get(PREF_KEY + "ch1MaskOption", false);
         ch2MaskOption =  Prefs.get(PREF_KEY + "ch2MaskOption", false);
         roiMaskOption = Prefs.get(PREF_KEY + "roiMaskOption", false);
         overlapMaskOption =  Prefs.get(PREF_KEY + "overlapMaskOption", false);
         overlapLocations =  Prefs.get(PREF_KEY + "overlapLocations", false);
		
        //Simulations
        ch1SimParamInt = (int) Prefs.get(PREF_KEY + "ch1SimParam", 0);
        ch2SimParamInt = (int) Prefs.get(PREF_KEY + "ch2SimParam", 0);
        interFactorCh2 =  Prefs.get(PREF_KEY + "interFactorCh2", 0);
        nMaxSimulations = (int) Prefs.get(PREF_KEY + "nMaxSimulations", nMaxSimulations);
       
        ch1SimParam = simParametersCh1[ch1SimParamInt];
        ch2SimParam = simParametersCh2[ch2SimParamInt];

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
		outputImgVals[0] = showSimsOption;
		outputImgVals[1] = ch1MaskOption;
		outputImgVals[2] = ch2MaskOption;
		outputImgVals[3] = roiMaskOption;
		outputImgVals[4] = overlapMaskOption;
		outputImgVals[5] = overlapLocations;
        
		//Dialog
        
        GenericDialog gd = new NonBlockingGenericDialog("Interaction Factor Simulations");
        gd.addDialogListener((DialogListener)this);
        
        gd.addMessage("--------------- Segmentation ---------------\n");
        gd.addChoice("Channel_1_(Ch1)_Color:", channels, channels[ch1Color]);
        gd.addChoice("Channel_2_(Ch2)_Color:", channels, channels[ch2Color]);
        gd.addChoice("Threshold:", thMethods, thMethods[thMethodInt]);
		gd.addCheckbox("Use_Manual_Threshold", thManualOption);
		gd.addNumericField("Channel_1_Threshold", thManual_ch1Level, 0);
		gd.addNumericField("Channel_2_Threshold", thManual_ch2Level, 0);
        gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);
		gd.addNumericField("Cluster_Minimum_Area", minClusterArea, 0);

        
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
 		gd.addMessage("---------- Simulation Parameters -----------\n");
        gd.addRadioButtonGroup("Ch1_Simulation:", simParametersCh1, 2, 1, simParametersCh1[ch1SimParamInt]);
        gd.addRadioButtonGroup("Ch2_Simulation:", simParametersCh2, 2, 1, simParametersCh2[ch2SimParamInt]);
        gd.addNumericField("Interaction_Factor", 0, 2);
        gd.addNumericField("Number_of_Simulations:", nMaxSimulations, 0);
        
 		gd.addMessage("----------- Additional Measurements --------\n");
		gd.addCheckboxGroup(6, 2, measurements, measurVals);
		
		gd.addMessage("-------------- Output Images ---------------\n");
		gd.addCheckboxGroup(3, 2, outputImg, outputImgVals);

        gd.showDialog();
        Recorder.recordInMacros = true;
        
        if (gd.wasCanceled())
            return;
        
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

		thManualOption = gd.getNextBoolean();
		thManual_ch1Level = (int) gd.getNextNumber();
		thManual_ch2Level = (int) gd.getNextNumber();
		minClusterArea = gd.getNextNumber();

        edgeOption = gd.getNextBoolean();
		
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

		showSimsOption = gd.getNextBoolean();
		ch1MaskOption = gd.getNextBoolean();
		ch2MaskOption = gd.getNextBoolean();
		roiMaskOption = gd.getNextBoolean();
		overlapMaskOption = gd.getNextBoolean();
		overlapLocations = gd.getNextBoolean();

        //Simulation Parameters
        
        ch1SimParam= gd.getNextRadioButton();
        ch2SimParam = gd.getNextRadioButton();
        interFactorCh2 =  gd.getNextNumber();
        nMaxSimulations = (int) gd.getNextNumber();
        
        if (ch1SimParam.equals("None") | ch1SimParam.equals("none")){
        	moveCh1Clusters = false;
        }
        else if (ch1SimParam.equals("Random") | ch1SimParam.equals("random")){
        	moveCh1Clusters = true;
        }
        if (Recorder.record){
        	
			Recorder.recordOption("channel_1(ch1)_color",ch1ColorStr);
			Recorder.recordOption("channel_2(ch2)_color",ch2ColorStr);
			Recorder.recordOption("threshold",thMethodIntStr);

			Recorder.recordOption("Channel_1_Threshold",Integer.toString(thManual_ch1Level));
			Recorder.recordOption("Channel_2_Threshold",Integer.toString(thManual_ch2Level));
		  	Recorder.recordOption("Cluster_Minimum_Area", Double.toString(minClusterArea));

			Recorder.recordOption("Ch1_Simulation",ch1SimParam);
			Recorder.recordOption("Ch2_Simulation",ch2SimParam);
			Recorder.recordOption("Interaction_Factor",Double.toString(interFactorCh2));
			Recorder.recordOption("Number_of_Simulations",Integer.toString(nMaxSimulations));

			if (thManualOption){
				Recorder.recordOption("Use_Manual_Threshold");
			}

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
				Recorder.recordOption("Clusters_Sum_Inten");
			}
			if(meanIntThOption){
				Recorder.recordOption("Clusters_Mean_Inten");
			}
			if(overlapsOpt){
				Recorder.recordOption("Clusters_Overlaps");
			}
			if(overlapsPercOpt){
				Recorder.recordOption("%Clusters_Overlaps");
			}
			if(overlapsCountOpt){
				Recorder.recordOption("Overlap_Count");
			}
			if(overlapsAreaOpt){
				Recorder.recordOption("Overlap_Area");
			}
			if(showSimsOption){
				Recorder.recordOption("Show_Simulations");
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
				Recorder.recordOption("Overlap_Locations_Table");	
			}
		}
        //Set Options
  		Prefs.set(PREF_KEY + "ch1Color", ch1Color);
  		Prefs.set(PREF_KEY + "ch2Color", ch2Color);
  		Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
  		Prefs.set(PREF_KEY + "edgeOption", edgeOption);
  		Prefs.set(PREF_KEY+"areaOption", areaOption);
  		Prefs.set(PREF_KEY+"areaRoiOption", areaRoiOption);

		Prefs.set(PREF_KEY+"thManualOption", thManualOption);
		Prefs.set(PREF_KEY + "thManual_ch1Level", thManual_ch1Level);
		Prefs.set(PREF_KEY + "thManual_ch2Level", thManual_ch2Level);
		Prefs.set(PREF_KEY + "minClusterArea", minClusterArea);

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
		Prefs.set(PREF_KEY + "showSimsOption", showSimsOption);
  		Prefs.set(PREF_KEY + "ch1MaskOption", ch1MaskOption);
  		Prefs.set(PREF_KEY + "ch2MaskOption", ch2MaskOption);
  		Prefs.set(PREF_KEY + "roiMaskOption", roiMaskOption);
  		Prefs.set(PREF_KEY + "overlapMaskOption", overlapMaskOption);
  		Prefs.set(PREF_KEY + "overlapLocations", overlapLocations);
  	    Prefs.set(PREF_KEY + "ch1SimParam", ch1SimParamInt);
        Prefs.set(PREF_KEY + "ch2SimParam", ch2SimParamInt);
        Prefs.set(PREF_KEY + "interFactorCh2", interFactorCh2);
        Prefs.set(PREF_KEY + "nMaxSimulations", nMaxSimulations);
        
        IfFunctions fs = new IfFunctions();
		
        AutoThresholder.Method[] methods = AutoThresholder.Method.values();
        ImagePlus im = IJ.getImage();

        if (im.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("RGB image required");
            return;
        }
        if (ch1Color == ch2Color){
            IJ.error("Channel Colors are the same. Choose another channel");
            return;
        }

		//TODO add check if (thManualOption is true, make sure thManual_ch1Level,thManual_ch2Level are in bounds
        
        if(ch2SimParam.equals("NonRandom")){
            if (interFactorCh2 >= 1.0){
                IJ.error("Interaction Factor has to be less than 1");
                return;
                }
            
            else if (interFactorCh2 == 0.0) {
            	 IJ.error("Interaction Factor has to be greater than 0");
                 return;
			}
         }
        
        if (nMaxSimulations == 0 ){
            IJ.error("Indicate the number of simulations");
            return;
        }
        
        String name = im.getShortTitle();
        AutoThresholder.Method method = methods[thMethodInt];

        //Calibration
        Calibration cal =im.getCalibration();
        String unit =cal.getUnit();
        double pixelHeight = cal.pixelHeight;
        double pixelWidth = cal.pixelWidth;
        double calConvert = pixelHeight*pixelWidth;

        ImageProcessor ip = im.getProcessor();
        Rectangle roi = ip.getRoi();
        Roi roiSelection = im.getRoi();

        ImageProcessor mask = im.getMask();// ip for the roi mask but only with surrounding box

        int M = ip.getWidth();
        int N = ip.getHeight();
        int size = M * N;

        byte[] red = new byte[size];
        byte[] green = new byte[size];
        byte[] blue = new byte[size];

        ((ColorProcessor) ip).getRGB(red, green, blue);

        ImageProcessor ipCh1 = new ByteProcessor(M, N); //ip for ch1 mask
        ImageProcessor ipCh2 = new ByteProcessor(M, N); //ip for ch2 mask
        ImageProcessor ipCh3 = new ByteProcessor(M, N); //ip for ch1 mask
        ImageProcessor ipOverlaps = new ByteProcessor(M, N); //ip for overlap mask
        ImageProcessor ipMask = new ByteProcessor(M, N); // ip for roi mask


        byte[] ch3;

        //Color of Ch1
        if (ch1Color == 0){
            ipCh1.setPixels(red);
        }
        else if (ch1Color == 1){
            ipCh1.setPixels(green);
        }
        else {
            ipCh1.setPixels(blue);
        }
        //Color of Ch2
        if (ch2Color == 0){
            ipCh2.setPixels(red);
        }
        else if (ch2Color == 1){
            ipCh2.setPixels(green);
        }
        else {
            ipCh2.setPixels(blue);
        }
        //Color of Ch3
        if (ch1Color + ch2Color == 1){
            ipCh3.setPixels(blue);
            ch3 = blue;
        }
        else if (ch1Color + ch2Color == 2){
            ipCh3.setPixels(green);
            ch3 = green;
        }
        else {
            ipCh3.setPixels(red);
            ch3 = red;
        }

        boolean hasMask = (mask != null);
        boolean hasRoi = (roiSelection != null);
        
        if (hasRoi){
			String nameRoi = roiSelection.getName();
			if (nameRoi != null){
			name = name +'-'+nameRoi;
			}
		}
        
        if (hasMask) {
            ipMask.insert(mask, roi.x, roi.y); //method to insert another ip inside an ip does not work with rectangular rois
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

		int th_ch1 = thManual_ch1Level;
		int th_ch2 = thManual_ch2Level;
		if(!thManualOption)
		{
			AutoThresholder autoth = new AutoThresholder();

			//Threshold ch1 channel
			ipCh1Mask.setMask(ipMask);
			int[] ch1_hist = ipCh1Mask.getHistogram();
			th_ch1 = autoth.getThreshold(method, ch1_hist);

			//Threshold ch2 channel
			ipCh2Mask.setMask(ipMask);
			int[] ch2_hist = ipCh2Mask.getHistogram();
			th_ch2 = autoth.getThreshold(method, ch2_hist);

		}
		ipCh1Mask.threshold(th_ch1);
		ipCh2Mask.threshold(th_ch2);

        if (edgeOption) {
            if (hasRoi){
                fs.excludeEdgesRoi(roiSelection,ipMask, ipCh1Mask);
                fs.excludeEdgesRoi(roiSelection,ipMask, ipCh2Mask);
            }
            else{
                fs.excludeEdges(roi,ipMask,ipCh1Mask);
                fs.excludeEdges(roi,ipMask,ipCh2Mask);
            }   
        }
        // Tables
        ResultsTable summary = Analyzer.getResultsTable();
        if (summary == null){
            summary = new ResultsTable();
            Analyzer.setResultsTable(summary);
        }
        ResultsTable rTable = new ResultsTable();
        //Remove Small Clusters
  		if (minClusterArea > 0){
  			fs.removeClusters(ipCh1Mask, minClusterArea,calConvert);
  			fs.removeClusters(ipCh2Mask, minClusterArea,calConvert);
  		}
        //Generate overlap mask
        ipOverlaps.copyBits(ipCh1Mask, 0, 0, Blitter.COPY);
        ipOverlaps.copyBits(ipCh2Mask, 0, 0, Blitter.AND);

        //Finding Overlaps
        ImageProcessor ipFlood = ipOverlaps.duplicate();
        List<ImageProcessor> overlaps = new ArrayList<ImageProcessor>();
        List<Rectangle> overlapsRect = new ArrayList<Rectangle>();

        int overlapCount = fs.clustersProcessing(name, true,rTable, cal, ipFlood, ipOverlaps, overlaps, overlapsRect);
		
        int ch1Overlaps = fs.ch2ClusterOverlaps(ipCh2Mask, ipCh1Mask);
        int ch2Overlaps = fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask);

        //Ch1 clusters
        ImageProcessor ipCh1Flood = ipCh1Mask.duplicate();
        List<ImageProcessor> ch1Clusters = new ArrayList<ImageProcessor>();
        List<Rectangle> ch1ClustersRect = new ArrayList<Rectangle>();

        int ch1ClusterCount = fs.clustersProcessing(cal,rTable,ipCh1Flood, ipCh1, ch1Clusters, ch1ClustersRect);

        //Ch2 clusters
        ImageProcessor ipCh2Flood = ipCh2Mask.duplicate();
        List<ImageProcessor> ch2Clusters = new ArrayList<ImageProcessor>();
        List<Rectangle> ch2ClustersRect = new ArrayList<Rectangle>();

        int ch2ClusterCount = fs.clustersProcessing(cal,rTable,ipCh2Flood, ipCh2, ch2Clusters, ch2ClustersRect);
        
        if (ch1ClusterCount == 0 || ch2ClusterCount == 0){
			IJ.error("Zero Clusters. Choose another color");
			return;
			
		}
        // Adding Overlays
     	ImageProcessor ipCh1FloodCopy = ipCh1Mask.duplicate();
     	ImageProcessor ipCh2FloodCopy = ipCh2Mask.duplicate();
     	fs.setClustersOverlay(im, ipCh1FloodCopy, ipCh2FloodCopy);
        
        //Summary
     	//Area Ch1
        ipCh1.setMask(ipCh1Mask);
        ImageStatistics ch1Stats = ipCh1.getStatistics();
        double aCh1Pixels = (double) ch1Stats.pixelCount *calConvert;
        
        //Area Ch2
        ipCh2.setMask(ipCh2Mask);
        ImageStatistics ch2Stats = ipCh2.getStatistics();
        double aCh2Pixels = (double) ch2Stats.pixelCount * calConvert;

        //Overlap
        ipCh1.setMask(ipOverlaps);
        ImageStatistics overlapStats = ipCh1.getStatistics();
        double aOverlapPixels = (double) overlapStats.pixelCount *calConvert;

        //Intensity values
        int ch1SumIntensity = fs.sumIntensities(ipCh1);
        int ch2SumIntensity = fs.sumIntensities(ipCh2);
        int ch1SumIntensityTh = fs.sumIntensitiesMask(ipCh1, ipCh1Mask);
        int ch2SumIntensityTh = fs.sumIntensitiesMask(ipCh2, ipCh2Mask);
        
        //Percentage of Overlaps
      	double ch1Percentage = (double) ch1Overlaps / (double) ch1ClusterCount;
      	double ch2Percentage = (double) ch2Overlaps / (double) ch2ClusterCount;
       
      	int[] ch1OverlapsStoich = fs.clusterStoichiometry(ipCh2Mask, ipCh1Mask);
		int[] ch2OverlapsStoich = fs.clusterStoichiometry(ipCh1Mask, ipCh2Mask);
		
      	//Average Mean intensity
      	double ch1MeanInt = ch1Stats.mean;
      	double ch2MeanInt = ch2Stats.mean;
      	
       //Calculating IF

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
 		
 	    // Calculating IF ch1-ch2

		double[] ch2ClustersProbs = new double[ch2Clusters.size()];
		Arrays.fill(ch2ClustersProbs, 0);
		double countForPvalCh2 = 0;
		
		double[] ch1ClustersProbs = new double[ch1Clusters.size()];
		Arrays.fill(ch1ClustersProbs, 0);
		double countForPvalCh1 = 0;
 		
 		for (int i = 0; i < 50; i++) {
 			IJ.showProgress(i, 50+nMaxSimulations);
 			
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
           
 			}
 		
		double pValCh1Ch2 = countForPvalCh2/(double)50;
		double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, 50);
		double IFCh1Ch2 = 0;
		IFCh1Ch2 = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);
		
		double pValCh2Ch1 = countForPvalCh1/(double)50;
		double[] ch1ClustersProbsTest = fs.prob(ch1ClustersProbs, 50);
		double IFCh2Ch1 = 0;
		IFCh2Ch1 = fs.calcIF(ch1ClustersProbsTest, ch1Percentage);     
 		
		//Adding Results to table
		summary.incrementCounter();
		summary.addValue("Image", name);
		summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
		summary.addValue(channelsAbb[ch1Color]  +" Sim", "None");
		summary.addValue(channelsAbb[ch2Color]  +" Sim", "None");

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

		//Segmentation
		if (thManualOption){
			summary.addValue("Th Algorithm", "Manual");
		}
		else{
			summary.addValue("Th Algorithm", thMethods[thMethodInt]);
		}
				
		summary.addValue(channelsAbb[ch1Color] + " Th", th_ch1);
		summary.addValue(channelsAbb[ch2Color] + " Th", th_ch2);
		summary.addValue(channelsAbb[ch1Color] +" Clus Count", ch1ClusterCount);
		summary.addValue(channelsAbb[ch2Color] + " Clus Count", ch2ClusterCount);
		
		//Optional Measurements
		
		if (areaOption){
			summary.addValue(channelsAbb[ch1Color] + " Clus Area", aCh1Pixels);
			summary.addValue(channelsAbb[ch2Color] + " Clus Area", aCh2Pixels);
		}
		if (areaRoiOption){
			summary.addValue("ROI Area", aRoi);
		}
		if (sumIntThOption){
			summary.addValue(channelsAbb[ch1Color] + " Clus Sum Inten", ch1SumIntensityTh);
			summary.addValue(channelsAbb[ch2Color] + " Clus Sum Inten", ch2SumIntensityTh);
		}
		if (meanIntThOption){
			summary.addValue(channelsAbb[ch1Color] + " Clus Mean Inten", ch1MeanInt);
			summary.addValue(channelsAbb[ch2Color] + " Clus Mean Inten", ch2MeanInt);
		}
		//Overlap Measurements
		if (overlapsOpt){
			summary.addValue(channelsAbb[ch1Color]+" Clus Overlaps",ch1Overlaps);
			summary.addValue(channelsAbb[ch2Color] + " Clus Overlaps", ch2Overlaps);
		}
		if(ch1StoiOption){
			//Stoichiometry
			summary.addValue(channelsAbb[ch1Color] + "1:1", String.format("%.1f", ((float)ch1OverlapsStoich[0]/ch1Overlaps)*100));//String.format("%.1f", ((float)ch2OverlapsStoich[0]/ch2Overlaps)*100)
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
			summary.addValue(channelsAbb[ch1Color]+" %Clus Overlaps",String.format("%.1f", (float)ch1Percentage*100));
			summary.addValue(channelsAbb[ch2Color] + " %Clus Overlaps", String.format("%.1f", (float)ch1Percentage*100));
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

        //Show images
        if (ch1MaskOption) {
            ImagePlus ch1Im = new ImagePlus(name +channelsAbb[ch1Color]+" Mask", ipCh1Mask);
            ch1Im.setCalibration(cal);
            ch1Im.show();
        }
        if (ch2MaskOption) {
            ImagePlus ch2Im = new ImagePlus(name +channels[ch2Color]+ " Mask", ipCh2Mask);
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
        
        //Generate simulations

        if (nMaxSimulations > 0) {

            ImageProcessor ipCh2Random = ipCh2.duplicate();
            ImageProcessor ipCh1Random = ipCh1.duplicate();

            for (int i = 0; i < nMaxSimulations; i++) {
            	IJ.showProgress(i+50, 50+nMaxSimulations);
                String nSimulation = Integer.toString(i+1);
                if (ch1SimParam.equals("Random")){ // if both are random
                    ipCh1Random = fs.simRandom(ipMask,minX,maxX,minY,maxY,ch1Clusters,ch1ClustersRect);
                } 
                if(ch2SimParam.equals("Random")) { //ch1 is random and ch2 is nonrandom
                	interFactorCh2 = 0;
                    ipCh2Random = fs.simRandom(ipMask,minX,maxX,minY,maxY,ch2Clusters,ch2ClustersRect);
                }
                else if(ch2SimParam.equals("NonRandom")){
                    ipCh2Random = fs.simNonRandom(ipMask,minX,maxX,minY,maxY,ipCh1Random,ch2Clusters,ch2ClustersRect,interFactorCh2,th_ch1);
                }
                if (ipCh2Random == null || ipCh1Random == null){
                    IJ.error("Error with Simulation 2");
                    return;
                }
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
                double interFactorCh2Int = interFactorCh2*100;
                
                


				if(showSimsOption)
				{
					ImagePlus colorRandIm = new ImagePlus(name + "_Sim_IF_"+Integer.toString((int)interFactorCh2Int) +"_"+nSimulation, ipSimulation);
					colorRandIm.setCalibration(cal);
					colorRandIm.show();
				}

                
                //generate ch2 channel mask
                ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
                ipCh2RandomMask.threshold(th_ch2);

                //generate ch1 channel mask
                ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
                ipCh1RandomMask.threshold(th_ch1);

                //generate overlap mask
                ImageProcessor ipOverlapsRandom = new ByteProcessor(M, N);
                ipOverlapsRandom.copyBits(ipCh1RandomMask, 0, 0, Blitter.COPY);
                ipOverlapsRandom.copyBits(ipCh2RandomMask, 0, 0, Blitter.AND);

                //count the objects of overlap and measurements
                ImageProcessor ipOverlapFlood = ipOverlapsRandom.duplicate();
                List<ImageProcessor> oClustersRandom = new ArrayList<ImageProcessor>();
                List<Rectangle> oClustersRectRandom = new ArrayList<Rectangle>();

                int oRandomCount = fs.clustersProcessing(name + "_Sim_" + nSimulation, true,rTable,cal ,ipOverlapFlood, ipOverlapsRandom, oClustersRandom, oClustersRectRandom);
                int ch1RandomOverlaps = fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask);
                int ch2RandomOverlaps = fs.ch2ClusterOverlaps(ipCh1RandomMask, ipCh2RandomMask);
                
                int[] ch1RandomOverlapsStoich = fs.clusterStoichiometry(ipCh2RandomMask, ipCh1RandomMask);
        		int[] ch2RandomOverlapsStoich = fs.clusterStoichiometry(ipCh1RandomMask, ipCh2RandomMask);
             
              	double ch1RandomPercentage = (double) ch1RandomOverlaps / (double) ch1ClusterCount;
              	double ch2RandomPercentage = (double) ch2RandomOverlaps / (double) ch2ClusterCount;
              	
                //Overlap
                ipCh1Random.setMask(ipOverlapsRandom);
                ImageStatistics overlapRandomStats = ipCh1Random.getStatistics();
                double aOverlapRandomPixels = (double) overlapRandomStats.pixelCount* calConvert;

                if (ch1MaskOption) {
                    ImagePlus ch1RandomIm = new ImagePlus(name + " Sim" + nSimulation + channelsAbb[ch1Color]+" Mask", ipCh1RandomMask);
                    ch1RandomIm.setCalibration(cal);
                    ch1RandomIm.show();
                }
                if (ch2MaskOption) {
                    ImagePlus ch2RandomIm = new ImagePlus(name + " Sim" + nSimulation+ channelsAbb[ch2Color]+" Mask", ipCh2RandomMask);
                    ch2RandomIm .setCalibration(cal);
                    ch2RandomIm .show();
                }
                if (overlapMaskOption) {
                    ImagePlus overlapRandomIm = new ImagePlus(name + " Sim" + nSimulation + " Overlap Mask", ipOverlapsRandom);
                    overlapRandomIm.setCalibration(cal);
                    overlapRandomIm.show();
                }

                summary.incrementCounter();
                summary.addValue("Image", name + "_Sim_" + nSimulation);
                summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
                
                if (ch1SimParam.equals("None")) {
                    summary.addValue(channelsAbb[ch1Color] + " Sim", "None");

                }
                else {
                    summary.addValue(channelsAbb[ch1Color]  +" Sim", "Random");}
                if (ch2SimParam.equals("Random")) {
                    summary.addValue(channelsAbb[ch2Color] + " Sim", "Random");
                    summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" IF", 0);
                    summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" p-val", "NT");

                }
                else {
                    summary.addValue(channelsAbb[ch2Color] + " Sim", "NonRandom");
                    summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" IF", interFactorCh2);
                    summary.addValue(channelsAbb[ch1Color]+ "-" +channelsAbb[ch2Color]+" p-val", "NT");
                    }
                
                summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" IF", "NT");
        		summary.addValue(channelsAbb[ch2Color]+ "-" +channelsAbb[ch1Color]+" p-val", "NT");
        		//Segmentation
        		if (thManualOption){
        			summary.addValue("Th Algorithm", "Manual");
        		}
        		else{
        			summary.addValue("Th Algorithm", thMethods[thMethodInt]);
        		}
        		summary.addValue(channelsAbb[ch1Color] + " Th", th_ch1);
        		summary.addValue(channelsAbb[ch2Color] + " Th", th_ch2);
        		summary.addValue(channelsAbb[ch1Color] +" Clus Count", ch1ClusterCount);
        		summary.addValue(channelsAbb[ch2Color] + " Clus Count", ch2ClusterCount);
        		//Optional Measurements
        		
        		if (areaOption){
        			summary.addValue(channelsAbb[ch1Color] + " Clus Area", aCh1Pixels);
        			summary.addValue(channelsAbb[ch2Color] + " Clus Area", aCh2Pixels);
        		}
        		if (areaRoiOption){
        			summary.addValue("ROI area", aRoi);
        		}
        		if (sumIntThOption){
        			summary.addValue(channelsAbb[ch1Color] + " Clus Sum Inten", ch1SumIntensityTh);
        			summary.addValue(channelsAbb[ch2Color] + " Clus Sum Inten", ch2SumIntensityTh);
        		}
        		if (meanIntThOption){
        			summary.addValue(channelsAbb[ch1Color] + " Clus Mean Inten", ch1MeanInt);
        			summary.addValue(channelsAbb[ch2Color] + " Clus Mean Inten", ch2MeanInt);
        		}
        		//Overlap Measurements
        		if (overlapsOpt){
        			summary.addValue(channelsAbb[ch1Color]+" Clus Overlaps",ch1RandomOverlaps);
        			summary.addValue(channelsAbb[ch2Color] + " Clus Overlaps", ch2RandomOverlaps);
        		}
        		if(ch1StoiOption){
        			//Stoichiometry
        			summary.addValue(channelsAbb[ch1Color] + "1:1", String.format("%.1f", ((float)ch1RandomOverlapsStoich[0]/ch1RandomOverlaps)*100));//String.format("%.1f", ((float)ch2OverlapsStoich[1]/ch2Overlaps)*100)
        			summary.addValue(channelsAbb[ch1Color] + "1:2", String.format("%.1f", ((float)ch1RandomOverlapsStoich[1]/ch1RandomOverlaps)*100));
        			summary.addValue(channelsAbb[ch1Color] + "1:3", String.format("%.1f", ((float)ch1RandomOverlapsStoich[2]/ch1RandomOverlaps)*100));
        			summary.addValue(channelsAbb[ch1Color] + "1:>3", String.format("%.1f", ((float)ch1RandomOverlapsStoich[3]/ch1RandomOverlaps)*100));
        		}
        		if(ch2StoiOption){
        			//Stoichiometry
        			summary.addValue(channelsAbb[ch2Color] + "1:1", String.format("%.1f", ((float)ch2RandomOverlapsStoich[0]/ch2RandomOverlaps)*100));
        			summary.addValue(channelsAbb[ch2Color] + "1:2", String.format("%.1f", ((float)ch2RandomOverlapsStoich[1]/ch2RandomOverlaps)*100));
        			summary.addValue(channelsAbb[ch2Color] + "1:3", String.format("%.1f", ((float)ch2RandomOverlapsStoich[2]/ch2RandomOverlaps)*100));
        			summary.addValue(channelsAbb[ch2Color] + "1:>3", String.format("%.1f", ((float)ch2RandomOverlapsStoich[3]/ch2RandomOverlaps)*100));
        		}
        		if (overlapsPercOpt){
        			summary.addValue(channelsAbb[ch1Color]+" %Clus Overlaps", String.format("%.1f", (float)ch1RandomPercentage*100));
        			summary.addValue(channelsAbb[ch2Color] + " %Clus Overlaps", String.format("%.1f", (float)ch2RandomPercentage*100));
        		}
        		if (overlapsCountOpt){
        			summary.addValue("Overlap Count", oRandomCount);
        		}
        		if (overlapsAreaOpt){
        			summary.addValue("Overlap Area", aOverlapRandomPixels);
        		}
        		if (sumIntOption){
        			summary.addValue(channelsAbb[ch1Color] + " Sum Inten", "None");
        			summary.addValue(channelsAbb[ch2Color] + " Sum Inten", "None");
        		}

            }
        }

        summary.show("Results");
        if (overlapLocations){
            rTable.show("Overlap Locations");
        }
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


	}

}
