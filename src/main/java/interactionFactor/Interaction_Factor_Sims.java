package interactionFactor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.plugin.filter.Analyzer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import ij.measure.Calibration;


public class Interaction_Factor_Sims implements PlugIn {
	private static String[] thMethods = AutoThresholder.getMethods();
    private static  String[] channels = {"Red", "Green", "Blue"};
    private static  String[] simParametersCh1 = {"None", "Random"};
    private static  String[] simParametersCh2 = {"Random", "Non Random"};

    private static int nMaxSimulations = 0;
    //private static double interFactorCh1 = 0;
    //private static double interFactorCh2 = 0;
    protected final static String PREF_KEY = "Interaction_Factor_Sims.";

	public void run(String arg) {
		
		int thMethodInt = (int) Prefs.get(PREF_KEY + "thMethodInt",0);
        int ch1Color =  (int) Prefs.get(PREF_KEY + "ch1Color", 0);
        int ch2Color = (int) Prefs.get(PREF_KEY + "ch2Color", 1);
        String ch1SimParam= Prefs.get(PREF_KEY + "ch1SimParam", simParametersCh1[0]);;
        String ch2SimParam = Prefs.get(PREF_KEY + "ch2SimParam", simParametersCh2[0]);
        double interFactorCh2 =  Prefs.get(PREF_KEY + "interFactorCh2", 0);
        nMaxSimulations = (int) Prefs.get(PREF_KEY + "nMaxSimulations", nMaxSimulations);
        boolean edgeOption = Prefs.get(PREF_KEY + "edgeOption", true);
        boolean simImageOption = Prefs.get(PREF_KEY + "simImageOption", false);
        boolean ch1MaskOption = Prefs.get(PREF_KEY + "ch1MaskOption", false);
        boolean ch2MaskOption =  Prefs.get(PREF_KEY + "ch2MaskOption", false);
        boolean roiMaskOption = Prefs.get(PREF_KEY + "roiMaskOption", false);
        boolean overlapMaskOption =  Prefs.get(PREF_KEY + "overlapMaskOption", false);
        boolean overlapLocations =  Prefs.get(PREF_KEY + "overlapLocations", false);
		
        //Dialog
        GenericDialog gd = new GenericDialog("Interaction Factor");
        gd.addMessage("----------- Segmentation -----------");
        gd.addChoice("Threshold_Algorithm:", thMethods, thMethods[thMethodInt]);
        gd.addChoice("Channel_1_(Ch1)_Color:", channels, channels[ch1Color]);
        gd.addChoice("Channel_2_(Ch2)_Color:", channels, channels[ch2Color]);
        gd.addCheckbox("Exclude_Edge_Clusters", edgeOption);
        gd.addMessage("------- Simulation Parameters ------");
        gd.addRadioButtonGroup("Ch1 Simulation:", simParametersCh1, 3, 1, ch1SimParam);
        gd.addRadioButtonGroup("Ch2 Simulation:", simParametersCh2, 3, 1, ch2SimParam);
        gd.addNumericField("Ch2_Interaction_Factor", interFactorCh2, 0);
        gd.addNumericField("Number_of_Simulations:", nMaxSimulations, 0);
        gd.addMessage("-------------- Output --------------");
        gd.addCheckbox("Simulation", simImageOption);
        gd.addCheckbox("Ch1_Mask", ch1MaskOption);
        gd.addCheckbox("Ch2_Mask",ch2MaskOption);
        gd.addCheckbox("ROI_Mask",roiMaskOption);
        gd.addCheckbox("Overlap_Mask",overlapMaskOption);
        gd.addCheckbox("Overlap_Locations",overlapLocations);
        gd.showDialog();

        if (gd.wasCanceled())
            return;

        AutoThresholder.Method[] methods = AutoThresholder.Method.values();

        thMethodInt = gd.getNextChoiceIndex();
        ch1Color = gd.getNextChoiceIndex();
        ch2Color = gd.getNextChoiceIndex();
        ch1SimParam= gd.getNextRadioButton();
        ch2SimParam = gd.getNextRadioButton();
        interFactorCh2 =  gd.getNextNumber();
        nMaxSimulations = (int) gd.getNextNumber();
        edgeOption = gd.getNextBoolean();
        simImageOption = gd.getNextBoolean();
        ch1MaskOption =gd.getNextBoolean();
        ch2MaskOption = gd.getNextBoolean();
        roiMaskOption =gd.getNextBoolean();
        overlapMaskOption = gd.getNextBoolean();
        overlapLocations = gd.getNextBoolean();


        // save user preferences
        Prefs.set(PREF_KEY + "thMethodInt", thMethodInt);
        Prefs.set(PREF_KEY+"ch1Color", ch1Color);
        Prefs.set(PREF_KEY+"ch2Color", ch2Color);
        Prefs.set(PREF_KEY+"ch1SimParam", ch1SimParam);
        Prefs.set(PREF_KEY+"ch2SimParam", ch2SimParam);
        Prefs.set(PREF_KEY+"interFactorCh2", interFactorCh2);
        Prefs.set(PREF_KEY+"nMaxSimulations", nMaxSimulations);
        Prefs.set(PREF_KEY+"edgeOption", edgeOption);
        Prefs.set(PREF_KEY+"simImageOption", simImageOption);
        Prefs.set(PREF_KEY+"ch1MaskOption", ch1MaskOption);
        Prefs.set(PREF_KEY+"ch2MaskOption", ch2MaskOption);
        Prefs.set(PREF_KEY+"roiMaskOption", roiMaskOption);
        Prefs.set(PREF_KEY+"overlapMaskOption", overlapMaskOption);
        
        IfFunctions fs = new IfFunctions();
        
        ImagePlus im = IJ.getImage();

        if (im.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("RGB image required");
            return;
        }
        if (ch1Color == ch2Color){
            IJ.error("Channel Colors are the same. Choose another channel");
            return;
        }
        
        /*double interFactorCh1 = 2.0;
        //  number of simulations is greater than 1 then at least one simulation needs to be either random
        if (ch1SimParam == simParametersCh1[0]){ //if there are no random simulations
            interFactorCh1 = 2.0;
        }
        else if (ch1SimParam == simParametersCh1[1]){ // random simulations
            interFactorCh1 = 0;
        }
        
        if (ch2SimParam == simParametersCh2[0]){
            interFactorCh2 = 2.0;
        }
        else if (ch2SimParam == simParametersCh2[1]){
            interFactorCh2 = 0.0;
        }
        else if(ch2SimParam == simParametersCh2[2]){
            if (interFactorCh2 >= 1.0){
                IJ.error("Attraction Factor has to less than 1");
                return;
            }
        }*/

        //if attraction Factor for ch2 is greater than one then attraction factor for ch1 has to be <= to one, if not error
        /*if (interFactorCh2 > 0 && interFactorCh2 < 1 && interFactorCh1 > 0 && interFactorCh1 < 1){
            IJ.error("Change parameters. If one channel is Non Random the other cannot be Non Random");
            return;
        }
        if (nMaxSimulations == 0 && (interFactorCh2 + interFactorCh1) > 0){
            IJ.error("Indicate the number of simulations");
            return;
        }
        if (nMaxSimulations > 0 && (interFactorCh2 + interFactorCh1) == 0){
            IJ.error("Indicate Simulation Parameters");
            return;
        }*/

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

        ImageProcessor ipCh1Mask = ipCh1.duplicate();
        ImageProcessor ipCh2Mask = ipCh2.duplicate();

        AutoThresholder autoth = new AutoThresholder();

        //Threshold ch1 channel
        ipCh1Mask.setMask(ipMask);
        int[] ch1_hist = ipCh1Mask.getHistogram();
        int th_ch1 = autoth.getThreshold(method, ch1_hist);
        ipCh1Mask.threshold(th_ch1);

        //Threshold ch2 channel
        ipCh2Mask.setMask(ipMask);
        int[] ch2_hist = ipCh2Mask.getHistogram();
        int th_ch2 = autoth.getThreshold(method, ch2_hist);
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
            //eliminate edge clusters from ch1 ch2 and ch3 channels
            /*for (int u = 0; u < M; u++) {
                for (int v = 0; v < N; v++) {
                    int p = ipMask.getPixel(u, v);
                    if (p == 0) {
                        ipCh1.putPixel(u, v, 0);
                        ipCh2.putPixel(u, v, 0);
                        ipCh3.putPixel(u, v, 0);
                    }
                }
            }*/
        }
        // Tables
        ResultsTable summary = Analyzer.getResultsTable();
        if (summary == null){
            summary = new ResultsTable();
            Analyzer.setResultsTable(summary);
        }
        ResultsTable rTable = new ResultsTable();
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

        //int ch1Overlaps = fs.overlapCount(ipCh2Mask, ipCh1Mask);
        //int ch2Overlaps = fs.overlapCount(ipCh1Mask, ipCh2Mask);

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
        // Adding Overlays
     	ImageProcessor ipCh1FloodCopy = ipCh1Mask.duplicate();
     	ImageProcessor ipCh2FloodCopy = ipCh2Mask.duplicate();
     	fs.setClustersOverlay(im, ipCh1FloodCopy, ipCh2FloodCopy);
        
        //Summary

        //Area Ch2
        ipCh2.setMask(ipCh2Mask);
        ImageStatistics ch2Stats = ipCh2.getStatistics();
        double aCh2Pixels = (double) ch2Stats.pixelCount * calConvert;

        //Ch1
        ipCh1.setMask(ipCh1Mask);
        ImageStatistics ch1Stats = ipCh1.getStatistics();
        double aCh1Pixels = (double) ch1Stats.pixelCount *calConvert;

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
 		double[] ch2ClustersProbs = new double[ch2Clusters.size()];
 		Arrays.fill(ch2ClustersProbs, 0);
 		double countForPval = 0;
 		
 		for (int i = 0; i < 50; i++) {
 			ImageProcessor ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
 			ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1Random, ch2ClustersProbs,
 					ch2Clusters, ch2ClustersRect);
 			 //generate ch2 channel mask
             ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
             ipCh2RandomMask.threshold(th_ch2);

             //generate ch1 channel mask
             ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
             ipCh1RandomMask.threshold(th_ch1);
             int ch2RandomOverlaps = fs.overlapCount(ipCh2RandomMask, ipCh1RandomMask);
             
 			double percOverlaps = (double)ch2RandomOverlaps/(double)ch2Clusters.size();
 			
 			if (percOverlaps > ch2Percentage){
 				countForPval +=1;
 			}
 		}
             
 		double pVal = countForPval/ (double) 50;
 		double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs,  50);
 		double IF = 0;

 		IF = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);
       
 		summary.incrementCounter();
        summary.addValue("Image", name);
        summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
        summary.addValue(channels[ch1Color] + " Sim", "None");
        summary.addValue(channels[ch2Color] + " Sim", "None");

        
        summary.addValue(channels[ch1Color]+ "-" +channels[ch2Color]+" IF", IF);
    	String pValStr;
		if (pVal == 0){
			 pValStr = "p<0.02" ;
		}
		else{
			pValStr ="p="+ String.valueOf(pVal);
		}
        //Overlap measurements
		summary.addValue("p-val", pValStr);
        summary.addValue(channels[ch1Color]+ " Cluster Count",ch1ClusterCount);
		summary.addValue(channels[ch1Color]+" Overlaps",ch1Overlaps);
        summary.addValue(channels[ch1Color]+" %Overlaps",ch1Percentage);
        summary.addValue(channels[ch2Color]+" Cluster Count", ch2ClusterCount);
        summary.addValue(channels[ch2Color]+" Overlaps",ch2Overlaps);
        summary.addValue(channels[ch2Color]+" %Overlaps",ch2Percentage);
        summary.addValue("Overlap Count",overlapCount);
        summary.addValue("Overlap Area",aOverlapPixels);
        
        //Segmentation
        summary.addValue("Th Algorithm",thMethods[thMethodInt]);
        summary.addValue(channels[ch1Color]  +" Th",th_ch1);
        summary.addValue(channels[ch2Color] + " Th",th_ch2);
        summary.addValue(channels[ch1Color]  +" Sum Intensities",ch1SumIntensity);
        summary.addValue(channels[ch1Color]  +" Sum Intensities > th",ch1SumIntensityTh);
        summary.addValue(channels[ch2Color]  +" Sum Intensities",ch2SumIntensity);
        summary.addValue(channels[ch2Color]  +" Sum Intensities > th",ch2SumIntensityTh);
        summary.addValue(channels[ch1Color]+" Area",aCh1Pixels);
        summary.addValue(channels[ch2Color]+" Area",aCh2Pixels);

        //Show images
        if (ch1MaskOption) {
            ImagePlus ch1Im = new ImagePlus(name +channels[ch1Color]+" Mask", ipCh1Mask);
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
                String nSimulation = Integer.toString(i+1);
                if (ch1SimParam == "Random"){ // if both are random
                    ipCh1Random = fs.simRandom(ipMask,minX,maxX,minY,maxY,ch1Clusters,ch1ClustersRect);
                } 
                if(ch2SimParam == "Random") { //ch1 is random and ch2 is nonrandom
                    ipCh2Random = fs.simRandom(ipMask,minX,maxX,minY,maxY,ch2Clusters,ch2ClustersRect);
                }
                else if(ch2SimParam == "Non Random"){
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

                ImagePlus colorRandIm = new ImagePlus(name + "_Sim_IF_"+Double.toString(interFactorCh2) + nSimulation, ipSimulation);
                colorRandIm.setCalibration(cal);

                if (simImageOption){
                    colorRandIm.show();
                }

                //generate ch2 channel mask
                ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
                ipCh2RandomMask.threshold(th_ch2);

                //generate ch1 channel mask
                ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
                ipCh1RandomMask.threshold(th_ch1);

                /*//EXCLUDE EDGES
                if (edgeOption) {
                    if (hasRoi){
                        excludeEdgesRoi(roiSelection,ipMask, ipCh1RandomMask);
                        excludeEdgesRoi(roiSelection,ipMask, ipCh2RandomMask);
                    }
                    else{
                        excludeEdges(roi,ipMask,ipCh1RandomMask);
                        excludeEdges(roi,ipMask,ipCh2RandomMask);
                    }
                }*/

                //Ch1 clusters

               //int ch1RandomClusterCount = fs.clustersProcessingSimple(ipCh1RandomMask);

                //Ch2 clusters

                //int ch2RandomClusterCount = fs.clustersProcessingSimple(ipCh2RandomMask);

                //generate overlap mask
                ImageProcessor ipOverlapsRandom = new ByteProcessor(M, N);
                ipOverlapsRandom.copyBits(ipCh1RandomMask, 0, 0, Blitter.COPY);
                ipOverlapsRandom.copyBits(ipCh2RandomMask, 0, 0, Blitter.AND);

                //count the objects of overlap and measurements
                ImageProcessor ipOverlapFlood = ipOverlapsRandom.duplicate();
                List<ImageProcessor> oClustersRandom = new ArrayList<ImageProcessor>();
                List<Rectangle> oClustersRectRandom = new ArrayList<Rectangle>();

                int oRandomCount = fs.clustersProcessing(name + "_Sim_" + nSimulation, true,rTable,cal ,ipOverlapFlood, ipOverlapsRandom, oClustersRandom, oClustersRectRandom);
                int ch1RandomOverlaps = fs.overlapCount(ipCh1RandomMask, ipCh2RandomMask);
                int ch2RandomOverlaps = fs.overlapCount(ipCh2RandomMask, ipCh1RandomMask);


                //Overlap
                ipCh1Random.setMask(ipOverlapsRandom);
                ImageStatistics overlapRandomStats = ipCh1Random.getStatistics();
                double aOverlapRandomPixels = (double) overlapRandomStats.pixelCount* calConvert;

                if (ch1MaskOption) {
                    ImagePlus ch1RandomIm = new ImagePlus(name + " Sim" + nSimulation + channels[ch1Color]+" Mask", ipCh1RandomMask);
                    ch1RandomIm.setCalibration(cal);
                    ch1RandomIm.show();
                }
                if (ch2MaskOption) {
                    ImagePlus ch2RandomIm = new ImagePlus(name + " Sim" + nSimulation+ channels[ch2Color]+" Mask", ipCh2RandomMask);
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
                if (ch1SimParam == "None") {
                    summary.addValue(channels[ch1Color] + " Sim", "None");

                }
                else {
                    summary.addValue(channels[ch1Color]  +" Sim", "Random");}
                if (ch2SimParam == "Random") {
                    summary.addValue(channels[ch2Color] + " Sim", "Random");
                    summary.addValue(channels[ch1Color]+ "-" +channels[ch2Color]+" IF", 0);

                }
                else {
                    summary.addValue(channels[ch2Color] + " Sim", "Non Random");
                    summary.addValue(channels[ch1Color]+ "-" +channels[ch2Color]+" IF", interFactorCh2);
                    //summary.addValue(channels[ch2Color]  +" IF", interFactorCh2);
                    }
                summary.addValue("Th Algorithm",thMethods[thMethodInt]);
                summary.addValue(channels[ch1Color]  +" Th",th_ch1);
                summary.addValue(channels[ch2Color] + " Th", th_ch2);
                summary.addValue(channels[ch1Color]  +" Sum Intensities",ch1SumIntensity);
                summary.addValue(channels[ch1Color]  +" Sum Intensities > th",ch1SumIntensityTh);
                summary.addValue(channels[ch1Color]+" Area", aCh1Pixels);
                summary.addValue(channels[ch1Color]+ " Cluster Count",ch1ClusterCount);
                summary.addValue(channels[ch1Color]  +" Sum Intensities",ch2SumIntensity);
                summary.addValue(channels[ch2Color]  +" Sum Intensities > th",ch2SumIntensityTh);
                summary.addValue(channels[ch2Color]+" Area",aCh2Pixels);
                summary.addValue(channels[ch2Color]+" Cluster Count", ch2ClusterCount);
                //Overlap Measurement
                summary.addValue("Overlap Area", aOverlapRandomPixels);
                summary.addValue("Overlap Count", oRandomCount);
                summary.addValue(channels[ch1Color]+" Overlaps",ch1RandomOverlaps);
                summary.addValue(channels[ch2Color]+" Overlaps",ch2RandomOverlaps);
                
            
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

		// open sample
		ImagePlus nucleus = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_ROI.tif");
		ImagePlus image = IJ.openImage(
				"/Users/keriabermudez/Dropbox/David_Fenyos_Lab/Image_Analysis/Testing_random_py/Test/Yandongs/Untreated/images/Cells/cell-1_1/cell-1_1_R_G.tif");
		image.show();
		nucleus.show();

	}

}
