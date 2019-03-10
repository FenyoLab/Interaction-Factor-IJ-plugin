package interactionFactor;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.ProgressBar;
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.Recorder;
import ij.process.AutoThresholder;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Interaction_Factor implements PlugIn, DialogListener
{
  private String PREF_KEY = "IF_prefs.";
  private int nMaxSimulationsIF = 50;
  private int randomSimsMaxDisplay = 20;
  
  private String[] displayChannels = { "ch1/Red", "ch2/Green", "ch3/Blue", "ch4", "ch5" };
  private String[] channels = { "Red", "Green", "Blue", "ch4", "ch5" };
  private String[] channelsLower = { "red", "green", "blue", "ch4", "ch5" };
  private String[] channelsAbb = { "R", "G", "B" };
  private String[] stackedChannels = { "ch1", "ch2", "ch3", "ch4", "ch5" };
  
  private String[] thMethods;
  private AutoThresholder.Method[] methods;
  private String[] measurements = { "Clusters_Area", "ROI_Area", "Sum_Pixel_Inten", "Clusters_Sum_Inten", "Clusters_Mean_Inten", "Ch1_Stoichiometry", "Ch2_Stoichiometry", "Clusters_Overlaps", "%Clusters_Overlaps", "Overlap_Count", "Overlap_Area", "Random_Sims_Summary" };

  private boolean[] measurVals = { true, true, true, true, true, true, true, true, true, true, true, true };
  private String[] outputImg = { "Save_Random_Simulations", "Show_Ch1_Mask", "Show_Ch2_Mask", "Show_ROI_Mask", "Show_Overlap_Mask", "Overlap_Locations_Table" };
  private boolean[] outputImgVals = { false, false, false, false, false, false };

  private int thMethodInt = 11;
  private int compareCh1 = 0;
  private int compareCh2 = 1;
  private boolean edgeOption = false;
  private boolean moveCh1Clusters = true;
  private boolean thManualOption = false;
  private int thManual_ch1Level = 0;
  private int thManual_ch2Level = 0;
  private int minCh1AreaClusters = 0;
  private int minCh2AreaClusters = 0;
  private boolean calcIFOption = true;
  private boolean overlapsOpt = false;
  private boolean overlapsPercOpt = false;
  private boolean overlapsCountOpt = false;
  private boolean overlapsAreaOpt = false;
  private boolean sumIntOption = false;
  private boolean sumIntThOption = false;
  private boolean meanIntThOption = false;
  private boolean areaOption = false;
  private boolean areaRoiOption = false;
  private boolean ch1StoiOption = false;
  private boolean ch2StoiOption = false;
  private boolean randomSimsSummaryOption = false;
  private boolean simImageOption = false;
  private boolean ch1MaskOption = false;
  private boolean ch2MaskOption = false;
  private boolean roiMaskOption = false;
  private boolean overlapMaskOption = false;
  private boolean overlapLocations = false;
  
  public Interaction_Factor() {
    this.thMethods = AutoThresholder.getMethods();
    this.methods = AutoThresholder.Method.values();
  }
  
  public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
  {
    if (e != null)
    {
      if (e.getID() == 1001)
      {
        ActionEvent e_ = (ActionEvent)e;
        String command = e_.getActionCommand();
        if (command == "Test IF")
        {
          Choice choice0 = (Choice)gd.getChoices().get(0);
          this.compareCh1 = choice0.getSelectedIndex();
          Choice choice1 = (Choice)gd.getChoices().get(1);
          this.compareCh2 = choice1.getSelectedIndex();
          Choice choice2 = (Choice)gd.getChoices().get(2);
          this.thMethodInt = choice2.getSelectedIndex();
          
          Vector numFields = gd.getNumericFields();
          TextField numField0 = (TextField)numFields.get(0);
          TextField numField1 = (TextField)numFields.get(1);
          this.thManual_ch1Level = Integer.parseInt(numField0.getText());
          this.thManual_ch2Level = Integer.parseInt(numField1.getText());

          TextField numField2 = (TextField)numFields.get(2);
          TextField numField3 = (TextField)numFields.get(3);
          this.minCh1AreaClusters = Integer.parseInt(numField2.getText());
          this.minCh2AreaClusters = Integer.parseInt(numField3.getText());
          
          Vector checkboxes = gd.getCheckboxes();
          
          Checkbox check0 = (Checkbox)checkboxes.get(0);
          Checkbox check1 = (Checkbox)checkboxes.get(1);
          Checkbox check2 = (Checkbox)checkboxes.get(2);
          Checkbox check3 = (Checkbox)checkboxes.get(3);
          Checkbox check4 = (Checkbox)checkboxes.get(4);
          Checkbox check5 = (Checkbox)checkboxes.get(5);
          Checkbox check6 = (Checkbox)checkboxes.get(6);
          Checkbox check7 = (Checkbox)checkboxes.get(7);
          Checkbox check8 = (Checkbox)checkboxes.get(8);
          Checkbox check9 = (Checkbox)checkboxes.get(9);
          Checkbox check10 = (Checkbox)checkboxes.get(10);
          Checkbox check11 = (Checkbox)checkboxes.get(11);
          Checkbox check12 = (Checkbox)checkboxes.get(12);
          Checkbox check13 = (Checkbox)checkboxes.get(13);
          Checkbox check14 = (Checkbox)checkboxes.get(14);
          Checkbox check15 = (Checkbox)checkboxes.get(15);
          Checkbox check16 = (Checkbox)checkboxes.get(16);
          Checkbox check17 = (Checkbox)checkboxes.get(17);
          Checkbox check18 = (Checkbox)checkboxes.get(18);
          Checkbox check19 = (Checkbox)checkboxes.get(19);
          Checkbox check20 = (Checkbox)checkboxes.get(20);
          Checkbox check21 = (Checkbox)checkboxes.get(21);
          
          this.thManualOption = check0.getState();
          this.edgeOption = check1.getState();
          this.calcIFOption = check2.getState();
          this.moveCh1Clusters = check3.getState();

          this.areaOption = check4.getState();
          this.areaRoiOption = check5.getState();

          this.sumIntOption = check6.getState();
          this.sumIntThOption = check7.getState();
          this.meanIntThOption = check8.getState();

          this.ch1StoiOption = check9.getState();
          this.ch2StoiOption = check10.getState();

          this.overlapsOpt = check11.getState();
          this.overlapsPercOpt = check12.getState();
          this.overlapsCountOpt = check13.getState();
          this.overlapsAreaOpt = check14.getState();
          this.randomSimsSummaryOption = check15.getState();

          this.simImageOption = check16.getState();
          this.ch1MaskOption = check17.getState();
          this.ch2MaskOption = check18.getState();
          this.roiMaskOption = check19.getState();
          this.overlapMaskOption = check20.getState();
          this.overlapLocations = check21.getState();
          
          run_IF(this.compareCh1, this.compareCh2, this.moveCh1Clusters, 0, this.calcIFOption, this.thMethodInt, this.thManualOption, this.thManual_ch1Level, this.thManual_ch2Level, this.edgeOption, this.areaOption, this.areaRoiOption, this.overlapsOpt, this.overlapsPercOpt, this.overlapsCountOpt, this.overlapsAreaOpt, this.sumIntOption, this.sumIntThOption, this.meanIntThOption, this.ch1StoiOption, this.ch2StoiOption, this.simImageOption, this.ch1MaskOption, this.ch2MaskOption, this.roiMaskOption, this.overlapMaskOption, this.overlapLocations, this.randomSimsSummaryOption, this.minCh1AreaClusters, this.minCh2AreaClusters);
        }

        if (command == "Apply Overlay")
        {
          Choice choice0 = (Choice)gd.getChoices().get(0);
          int compareCh1 = choice0.getSelectedIndex();

          Choice choice1 = (Choice)gd.getChoices().get(1);
          int compareCh2 = choice1.getSelectedIndex();

          Choice choice2 = (Choice)gd.getChoices().get(2);
          int thMethodInt = choice2.getSelectedIndex();
          
          Vector numFields = gd.getNumericFields();
          TextField numField0 = (TextField)numFields.get(0);
          TextField numField1 = (TextField)numFields.get(1);
          int thManual_ch1Level = Integer.parseInt(numField0.getText());
          int thManual_ch2Level = Integer.parseInt(numField1.getText());

          TextField numField2 = (TextField)numFields.get(2);
          TextField numField3 = (TextField)numFields.get(3);
          int minCh1AreaClusters = Integer.parseInt(numField2.getText());
          int minCh2AreaClusters = Integer.parseInt(numField3.getText());

          Vector checkboxes = gd.getCheckboxes();
          Checkbox check0 = (Checkbox)checkboxes.get(0);
          Checkbox check1 = (Checkbox)checkboxes.get(1);
          boolean thManualOption = check0.getState();
          boolean edgeOption = check1.getState();
          
          IfFunctions fs = new IfFunctions();
          
          if (compareCh1 == compareCh2) {
            IJ.error("Channel Colors are the same. Choose another channel");
            return true;
          }
          
          ImagePlus imOrig = IJ.getImage();
          ImageProcessor ipOrig = imOrig.getProcessor();
          int M = ipOrig.getWidth();
          int N = ipOrig.getHeight();
          int size = M * N;
          
          ImageProcessor ipCh1 = new ByteProcessor(M, N);
          ImageProcessor ipCh2 = new ByteProcessor(M, N);
          ImageProcessor ipCh3 = new ByteProcessor(M, N);
          
          byte[] ch3;

          String imageType = "";

          ImagePlus im = imOrig;
          if (imOrig.getType() == 4)
          {
            imageType = "RGB";

            byte[] red = new byte[size];
            byte[] green = new byte[size];
            byte[] blue = new byte[size];

            ((ColorProcessor)ipOrig).getRGB(red, green, blue);

            if (compareCh1 == 0) {
              ipCh1.setPixels(red);
            } else if (compareCh1 == 1) {
              ipCh1.setPixels(green);
            } else {
              ipCh1.setPixels(blue);
            }

            if (compareCh2 == 0) {
              ipCh2.setPixels(red);
            } else if (compareCh2 == 1) {
              ipCh2.setPixels(green);
            } else {
              ipCh2.setPixels(blue);
            }

            if (compareCh1 + compareCh2 == 1) {
              ipCh3.setPixels(blue);
              ch3 = blue;
            } else if (compareCh1 + compareCh2 == 2) {
              ipCh3.setPixels(green);
              ch3 = green;
            } else {
              ipCh3.setPixels(red);
              ch3 = red;
            }
          } else {
            if ((((imOrig.getType() == 2) || (imOrig.getType() == 1) || (imOrig.getType() == 0) ? 1 : 0) & (imOrig.getDimensions()[2] >= 2 ? 1 : 0)) != 0)
            {
              compareCh1 += 1;
              compareCh2 += 1;
              
              int num_dimensions = imOrig.getDimensions()[2];
              if ((compareCh1 <= 0) || (compareCh1 > num_dimensions)) {
                IJ.error("Channel 1 is outside of image stack range.");
                return true;
              }
              if ((compareCh2 <= 0) || (compareCh2 > num_dimensions)) {
                IJ.error("Channel 2 is outside of image stack range.");
                return true;
              }
              if (imOrig.getType() != 0)
              {
                imageType = "STACKED";
                imOrig.setC(compareCh1);
                ipCh1.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
                ipCh1.setMask(imOrig.getChannelProcessor().getMask());
                ipCh1.setRoi(imOrig.getChannelProcessor().getRoi());
                imOrig.setC(compareCh2);
                ipCh2.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
                ipCh2.setMask(imOrig.getChannelProcessor().getMask());
                ipCh2.setRoi(imOrig.getChannelProcessor().getRoi());
                
                ImageStack new8bitStack = imOrig.createEmptyStack();
                new8bitStack.addSlice(ipCh1);
                new8bitStack.addSlice(ipCh2);
                im = new CompositeImage(imOrig);
                im.setStack(new8bitStack);
                im.setTitle(imOrig.getShortTitle() + "-8bit_ch" + compareCh1 + "-ch" + compareCh2);
                im.setRoi(imOrig.getRoi());
                im.setCalibration(imOrig.getCalibration());
                im.setDisplayMode(1);
                im.show();
              }
              else
              {
                imageType = "STACKED8";
                imOrig.setC(compareCh1);
                ipCh1.setPixels(imOrig.getChannelProcessor().getPixelsCopy());
                ipCh1.setMask(imOrig.getChannelProcessor().getMask());
                ipCh1.setRoi(imOrig.getChannelProcessor().getRoi());
                imOrig.setC(compareCh2);
                ipCh2.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
                ipCh2.setMask(imOrig.getChannelProcessor().getMask());
                ipCh2.setRoi(imOrig.getChannelProcessor().getRoi());
                im = imOrig;
              }
            }
            else
            {
              IJ.error("RGB image (8-bit) or Stacked GRAY image (8/16/32-bit) required.");
              return true;
            }
          }

          im.setC(compareCh1);
          
          String name = im.getShortTitle();
          AutoThresholder.Method method = this.methods[thMethodInt];

          Calibration cal = im.getCalibration();
          String unit = cal.getUnit();
          double pixelHeight = cal.pixelHeight;
          double pixelWidth = cal.pixelWidth;
          double calConvert = pixelHeight * pixelWidth;
          
          Rectangle roi = im.getProcessor().getRoi();
          Roi roiSelection = im.getRoi();

          ImageProcessor mask = im.getMask();

          ImageProcessor ipOverlaps = new ByteProcessor(M, N);
          ImageProcessor ipMask = new ByteProcessor(M, N);

          boolean hasMask = mask != null;
          boolean hasRoi = roiSelection != null;
          
          if (hasMask) {
            ipMask.insert(mask, roi.x, roi.y);
          }
          else
          {
            ipMask.setValue(255.0D);
            ipMask.setRoi(roi);
            ipMask.fill();
          }
          
          for (int u = 0; u < M; u++) {
            for (int v = 0; v < N; v++) {
              int p = ipMask.getPixel(u, v);
              if (p == 0) {
                ipCh1.putPixel(u, v, 0);
                ipCh2.putPixel(u, v, 0);
                if (imageType == "RGB") { ipCh3.putPixel(u, v, 0); }
              }
            }
          }
          ImageProcessor ipCh1Mask = ipCh1.duplicate();
          ImageProcessor ipCh2Mask = ipCh2.duplicate();

          int th_ch1 = thManual_ch1Level;
          int th_ch2 = thManual_ch2Level;
          ipCh1Mask.setMask(ipMask);
          ipCh2Mask.setMask(ipMask);
          AutoThresholder autoth;
          if (!thManualOption)
          {
            autoth = new AutoThresholder();

            int[] ch1_hist = ipCh1Mask.getHistogram();
            th_ch1 = autoth.getThreshold(method, ch1_hist);

            int[] ch2_hist = ipCh2Mask.getHistogram();
            th_ch2 = autoth.getThreshold(method, ch2_hist);
          }
          ipCh1Mask.threshold(th_ch1);
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
          int rem_count;
          if (minCh1AreaClusters > 0)
          {
            rem_count = fs.excludeByClusterArea(minCh1AreaClusters, cal, ipCh1Mask, ipCh1);
          }
          if (minCh2AreaClusters > 0)
          {
            rem_count = fs.excludeByClusterArea(minCh2AreaClusters, cal, ipCh2Mask, ipCh2);
          }
          
          fs.setClustersOverlay(im, ipCh1Mask, ipCh2Mask);
        }
        if (command == "Clear Overlay")
        {
          IJ.run("Remove Overlay");
        }
      }
    }
    else {
      gd.repaint();
    }
    return true;
  }
  
  public void run(String arg)
  {
    this.thMethodInt = ((int)Prefs.get(this.PREF_KEY + "thMethodInt", 11));
    this.compareCh1 = ((int)Prefs.get(this.PREF_KEY + "ch1Color", 0));
    this.compareCh2 = ((int)Prefs.get(this.PREF_KEY + "ch2Color", 1));
    this.edgeOption = Prefs.get(this.PREF_KEY + "edgeOption", true);
    this.moveCh1Clusters = Prefs.get(this.PREF_KEY + "moveOption", true);
    this.thManualOption = Prefs.get(this.PREF_KEY + "thManualOption", false);
    this.thManual_ch1Level = ((int)Prefs.get(this.PREF_KEY + "thManual_ch1Level", 0));
    this.thManual_ch2Level = ((int)Prefs.get(this.PREF_KEY + "thManual_ch2Level", 0));
    this.minCh1AreaClusters = ((int)Prefs.get(this.PREF_KEY + "minCh1AreaClusters", 0));
    this.minCh2AreaClusters = ((int)Prefs.get(this.PREF_KEY + "minCh2AreaClusters", 0));
    this.calcIFOption = Prefs.get(this.PREF_KEY + "calcIFOption", false);
    this.overlapsOpt = Prefs.get(this.PREF_KEY + "overlapsOpt", true);
    this.overlapsPercOpt = Prefs.get(this.PREF_KEY + "overlapsPercOpt", true);
    this.overlapsCountOpt = Prefs.get(this.PREF_KEY + "overlapsCountOpt", true);
    this.overlapsAreaOpt = Prefs.get(this.PREF_KEY + "overlapsAreaOpt", true);
    this.sumIntOption = Prefs.get(this.PREF_KEY + "sumIntOption", true);
    this.sumIntThOption = Prefs.get(this.PREF_KEY + "sumIntThOption", true);
    this.meanIntThOption = Prefs.get(this.PREF_KEY + "meanIntThOption", true);
    this.areaOption = Prefs.get(this.PREF_KEY + "areaOption", true);
    this.areaRoiOption = Prefs.get(this.PREF_KEY + "areaRoiOption", true);
    this.ch1StoiOption = Prefs.get(this.PREF_KEY + "ch1StoiOption", true);
    this.ch2StoiOption = Prefs.get(this.PREF_KEY + "ch2StoiOption", true);
    this.randomSimsSummaryOption = Prefs.get(this.PREF_KEY + "randomSimsSummaryOption", true);
    this.simImageOption = Prefs.get(this.PREF_KEY + "simImageOption", false);
    this.ch1MaskOption = Prefs.get(this.PREF_KEY + "ch1MaskOption", false);
    this.ch2MaskOption = Prefs.get(this.PREF_KEY + "ch2MaskOption", false);
    this.roiMaskOption = Prefs.get(this.PREF_KEY + "roiMaskOption", false);
    this.overlapMaskOption = Prefs.get(this.PREF_KEY + "overlapMaskOption", false);
    this.overlapLocations = Prefs.get(this.PREF_KEY + "overlapLocations", false);
    this.measurVals[0] = this.areaOption;
    this.measurVals[1] = this.areaRoiOption;
    this.measurVals[2] = this.sumIntOption;
    this.measurVals[3] = this.sumIntThOption;
    this.measurVals[4] = this.meanIntThOption;
    this.measurVals[5] = this.ch1StoiOption;
    this.measurVals[6] = this.ch2StoiOption;
    this.measurVals[7] = this.overlapsOpt;
    this.measurVals[8] = this.overlapsPercOpt;
    this.measurVals[9] = this.overlapsCountOpt;
    this.measurVals[10] = this.overlapsAreaOpt;
    this.measurVals[11] = this.randomSimsSummaryOption;
    this.outputImgVals[0] = this.simImageOption;
    this.outputImgVals[1] = this.ch1MaskOption;
    this.outputImgVals[2] = this.ch2MaskOption;
    this.outputImgVals[3] = this.roiMaskOption;
    this.outputImgVals[4] = this.overlapMaskOption;
    this.outputImgVals[5] = this.overlapLocations;
    GenericDialog gd = new NonBlockingGenericDialog("Interaction Factor");
    gd.addDialogListener(this);
    gd.setInsets(5, 20, 0);
    gd.addMessage("----------------- Segmentation ------------------\n");
    gd.setInsets(5, 0, 3);
    gd.addChoice("Channel_1:", this.displayChannels, this.displayChannels[this.compareCh1]);
    gd.setInsets(0, 0, 3);
    gd.addChoice("Channel_2:", this.displayChannels, this.displayChannels[this.compareCh2]);
    gd.setInsets(0, 0, 3);
    gd.addChoice("Threshold:", this.thMethods, this.thMethods[this.thMethodInt]);
    gd.setInsets(5, 20, 0);
    gd.addCheckbox("Use_Manual_Threshold", this.thManualOption);
    gd.setInsets(5, 35, 3);
    gd.addNumericField("Channel_1_Threshold", this.thManual_ch1Level, 0);
    gd.setInsets(0, 35, 3);
    gd.addNumericField("Channel_2_Threshold", this.thManual_ch2Level, 0);
    gd.setInsets(5, 20, 0);
    gd.addCheckbox("Exclude_Edge_Clusters", this.edgeOption);
    gd.setInsets(5, 20, 0);
    gd.addMessage("Area Cutoffs for Objects after Threshold:\n");
    gd.setInsets(0, 35, 3);
    gd.addNumericField("Min_Area_Channel_1", this.minCh1AreaClusters, 0);
    gd.setInsets(0, 35, 3);
    gd.addNumericField("Min_Area_Channel_2", this.minCh2AreaClusters, 0);
    Panel buttons = new Panel();
    buttons.setLayout(new FlowLayout(1, 5, 0));
    Button b1 = new Button("Apply Overlay");
    b1.addActionListener(gd);
    b1.addKeyListener(gd);
    buttons.add(b1);
    Button b2 = new Button("Clear Overlay");
    b2.addActionListener(gd);
    b2.addKeyListener(gd);
    buttons.add(b2);
    gd.addPanel(buttons, 10, new Insets(15, 0, 0, 0));
    
    gd.setInsets(5, 20, 0);
    gd.addMessage("----------------- IF Parameter ------------------\n");
    gd.addCheckbox("Calculate_the_IF", this.calcIFOption);
    gd.addCheckbox("Move_Ch1_Clusters", this.moveCh1Clusters);
    gd.setInsets(5, 20, 0);
    gd.addMessage("------------ Additional Measurements -------------\n");
    gd.setInsets(10, 20, 0);
    gd.addCheckboxGroup(6, 2, this.measurements, this.measurVals);
    gd.setInsets(5, 20, 0);
    gd.addMessage("----------------- Output Images -----------------\n");
    gd.setInsets(10, 20, 0);
    gd.addCheckboxGroup(3, 2, this.outputImg, this.outputImgVals);
    buttons = new Panel();
    buttons.setLayout(new FlowLayout(1, 5, 0));
    b1 = new Button("Test IF");
    b1.addActionListener(gd);
    b1.addKeyListener(gd);
    buttons.add(b1);
    gd.addPanel(buttons, 13, new Insets(15, 0, 0, 0));
    
    Recorder.recordInMacros = true;
    gd.showDialog();
    
    if (gd.wasCanceled()) {
      return;
    }
    ImageJ thisImageJ = IJ.getInstance();
    ProgressBar progressBar = thisImageJ.getProgressBar();
    progressBar.show(0.0D, true);
    IfFunctions fs = new IfFunctions();
    
    String ch1ColorStr = gd.getNextChoice();
    for (int i = 0; i < this.displayChannels.length; i++)
    {
      if ((ch1ColorStr.equals(this.displayChannels[i]) | ch1ColorStr.equals(this.channels[i]) | ch1ColorStr.equals(this.channelsLower[i]) | ch1ColorStr.equals(this.stackedChannels[i])))
      {
        ch1ColorStr = this.stackedChannels[i];
        this.compareCh1 = i;
      }
    }
    String ch2ColorStr = gd.getNextChoice();
    for (int i = 0; i < this.displayChannels.length; i++)
    {
      if ((ch2ColorStr.equals(this.displayChannels[i]) | ch2ColorStr.equals(this.channels[i]) | ch2ColorStr.equals(this.channelsLower[i]) | ch2ColorStr.equals(this.stackedChannels[i])))
      {
        ch2ColorStr = this.stackedChannels[i];
        this.compareCh2 = i;
      }
    }
    String thMethodIntStr = gd.getNextChoice();
    for (int i = 0; i < this.thMethods.length; i++) {
      if (thMethodIntStr.equals(this.thMethods[i])) {
        this.thMethodInt = i;
      }
    }
    
    this.thManualOption = gd.getNextBoolean();
    this.thManual_ch1Level = ((int)gd.getNextNumber());
    this.thManual_ch2Level = ((int)gd.getNextNumber());
    this.edgeOption = gd.getNextBoolean();
    this.minCh1AreaClusters = ((int)gd.getNextNumber());
    this.minCh2AreaClusters = ((int)gd.getNextNumber());
    this.calcIFOption = gd.getNextBoolean();
    this.moveCh1Clusters = gd.getNextBoolean();
    this.areaOption = gd.getNextBoolean();
    this.areaRoiOption = gd.getNextBoolean();
    this.sumIntOption = gd.getNextBoolean();
    this.sumIntThOption = gd.getNextBoolean();
    this.meanIntThOption = gd.getNextBoolean();
    this.ch1StoiOption = gd.getNextBoolean();
    this.ch2StoiOption = gd.getNextBoolean();
    this.overlapsOpt = gd.getNextBoolean();
    this.overlapsPercOpt = gd.getNextBoolean();
    this.overlapsCountOpt = gd.getNextBoolean();
    this.overlapsAreaOpt = gd.getNextBoolean();
    this.randomSimsSummaryOption = gd.getNextBoolean();
    this.simImageOption = gd.getNextBoolean();
    this.ch1MaskOption = gd.getNextBoolean();
    this.ch2MaskOption = gd.getNextBoolean();
    this.roiMaskOption = gd.getNextBoolean();
    this.overlapMaskOption = gd.getNextBoolean();
    this.overlapLocations = gd.getNextBoolean();
    
    run_IF(this.compareCh1, this.compareCh2, this.moveCh1Clusters, 0, this.calcIFOption, this.thMethodInt, this.thManualOption, this.thManual_ch1Level, this.thManual_ch2Level, this.edgeOption, this.areaOption, this.areaRoiOption, this.overlapsOpt, this.overlapsPercOpt, this.overlapsCountOpt, this.overlapsAreaOpt, this.sumIntOption, this.sumIntThOption, this.meanIntThOption, this.ch1StoiOption, this.ch2StoiOption, this.simImageOption, this.ch1MaskOption, this.ch2MaskOption, this.roiMaskOption, this.overlapMaskOption, this.overlapLocations, this.randomSimsSummaryOption, this.minCh1AreaClusters, this.minCh2AreaClusters);
    if (Recorder.record)
    {
      Recorder.recordOption("channel_1", ch1ColorStr);
      Recorder.recordOption("channel_2", ch2ColorStr);
      Recorder.recordOption("threshold", thMethodIntStr);
      Recorder.recordOption("Channel_1_Threshold", Integer.toString(this.thManual_ch1Level));
      Recorder.recordOption("Channel_2_Threshold", Integer.toString(this.thManual_ch2Level));
      Recorder.recordOption("Min_Area_Channel_1", Integer.toString(this.minCh1AreaClusters));
      Recorder.recordOption("Min_Area_Channel_2", Integer.toString(this.minCh2AreaClusters));

      if (this.thManualOption) {
        Recorder.recordOption("Use_Manual_Threshold");
      }
      if (this.edgeOption) {
        Recorder.recordOption("Exclude_Edge_Clusters");
      }
      if (this.calcIFOption) {
        Recorder.recordOption("Calculate_the_IF");
      }
      if (this.moveCh1Clusters) {
        Recorder.recordOption("Move_Ch1_Clusters");
      }
      if (this.areaOption) {
        Recorder.recordOption("Clusters_Area");
      }
      if (this.areaRoiOption) {
        Recorder.recordOption("ROI_Area");
      }
      if (this.sumIntOption) {
        Recorder.recordOption("Sum_Pixel_Inten");
      }
      if (this.sumIntThOption) {
        Recorder.recordOption("Clusters_Sum_Inten");
      }
      if (this.meanIntThOption) {
        Recorder.recordOption("Clusters_Mean_Inten");
      }
      if (this.overlapsOpt) {
        Recorder.recordOption("Clusters_Overlaps");
      }
      if (this.overlapsPercOpt) {
        Recorder.recordOption("%Clusters_Overlaps");
      }
      if (this.overlapsCountOpt) {
        Recorder.recordOption("Overlap_Count");
      }
      if (this.overlapsAreaOpt) {
        Recorder.recordOption("Overlap_Area");
      }
      if (this.randomSimsSummaryOption) {
        Recorder.recordOption("Random_Sims_Summary");
      }
      if (this.simImageOption) {
        Recorder.recordOption("Save_Random_Simulations");
      }
      if (this.ch1MaskOption) {
        Recorder.recordOption("Show_Ch1_Mask");
      }
      if (this.ch2MaskOption) {
        Recorder.recordOption("Show_Ch2_Mask");
      }
      if (this.overlapMaskOption) {
        Recorder.recordOption("Show_Overlap_Mask");
      }
      if (this.overlapLocations) {
        Recorder.recordOption("Overlap_Locations_Table");
      }
    }
  }

  public void run_IF(int compareCh1, int compareCh2, boolean moveRefClusters, int refChannel, boolean calcIFOption, int thMethodInt, boolean thManualOption, int thManual_ch1Level, int thManual_ch2Level, boolean edgeOption, boolean areaOption, boolean areaRoiOption, boolean overlapsOpt, boolean overlapsPercOpt, boolean overlapsCountOpt, boolean overlapsAreaOpt, boolean sumIntOption, boolean sumIntThOption, boolean meanIntThOption, boolean ch1StoiOption, boolean ch2StoiOption, boolean simImageOption, boolean ch1MaskOption, boolean ch2MaskOption, boolean roiMaskOption, boolean overlapMaskOption, boolean overlapLocations, boolean randomSimsSummaryOption, int minCh1AreaClusters, int minCh2AreaClusters)
  {
    IJ.showProgress(0.0);
    Prefs.set(this.PREF_KEY + "ch1Color", compareCh1);
    Prefs.set(this.PREF_KEY + "ch2Color", compareCh2);
    Prefs.set(this.PREF_KEY + "moveOption", moveRefClusters);
    Prefs.set(this.PREF_KEY + "refChannel", refChannel);
    Prefs.set(this.PREF_KEY + "thMethodInt", thMethodInt);
    Prefs.set(this.PREF_KEY + "edgeOption", edgeOption);
    Prefs.set(this.PREF_KEY + "areaOption", areaOption);
    Prefs.set(this.PREF_KEY + "areaRoiOption", areaRoiOption);
    Prefs.set(this.PREF_KEY + "thManualOption", thManualOption);
    Prefs.set(this.PREF_KEY + "thManual_ch1Level", thManual_ch1Level);
    Prefs.set(this.PREF_KEY + "thManual_ch2Level", thManual_ch2Level);
    Prefs.set(this.PREF_KEY + "minCh1AreaClusters", minCh1AreaClusters);
    Prefs.set(this.PREF_KEY + "minCh2AreaClusters", minCh2AreaClusters);
    Prefs.set(this.PREF_KEY + "calcIFOption", calcIFOption);
    Prefs.set(this.PREF_KEY + "overlapsOpt", overlapsOpt);
    Prefs.set(this.PREF_KEY + "overlapsPercOpt", overlapsPercOpt);
    Prefs.set(this.PREF_KEY + "overlapsCountOpt", overlapsCountOpt);
    Prefs.set(this.PREF_KEY + "overlapsAreaOpt", overlapsAreaOpt);
    Prefs.set(this.PREF_KEY + "sumIntOption", sumIntOption);
    Prefs.set(this.PREF_KEY + "sumIntThOption", sumIntThOption);
    Prefs.set(this.PREF_KEY + "meanIntThOption", meanIntThOption);
    Prefs.set(this.PREF_KEY + "ch1StoiOption", ch1StoiOption);
    Prefs.set(this.PREF_KEY + "ch2StoiOption", ch2StoiOption);
    Prefs.set(this.PREF_KEY + "randomSimsSummaryOption", randomSimsSummaryOption);
    Prefs.set(this.PREF_KEY + "simImageOption", simImageOption);
    Prefs.set(this.PREF_KEY + "ch1MaskOption", ch1MaskOption);
    Prefs.set(this.PREF_KEY + "ch2MaskOption", ch2MaskOption);
    Prefs.set(this.PREF_KEY + "roiMaskOption", roiMaskOption);
    Prefs.set(this.PREF_KEY + "overlapMaskOption", overlapMaskOption);
    Prefs.set(this.PREF_KEY + "overlapLocations", overlapLocations);

    String imagedir = ".";
    if (simImageOption) {
      DirectoryChooser chooser = new DirectoryChooser("Choose directory to process");
      imagedir = chooser.getDirectory();
    }
    IfFunctions fs = new IfFunctions();
    
    if (compareCh1 == compareCh2)
    {
      IJ.error("Channel Colors are the same. Choose another channel.");
      return;
    }
    
    ImagePlus imOrig = IJ.getImage();
    ImageProcessor ipOrig = imOrig.getProcessor();
    int M = ipOrig.getWidth();
    int N = ipOrig.getHeight();
    int size = M * N;
    
    ImageProcessor ipCh1 = new ByteProcessor(M, N);
    ImageProcessor ipCh2 = new ByteProcessor(M, N);
    ImageProcessor ipCh3 = new ByteProcessor(M, N);
    byte[] ch3 = new byte[1];
    String imageType = "";
    ImagePlus im = imOrig;
    if (imOrig.getType() == 4)
    {
      imageType = "RGB";

      byte[] red = new byte[size];
      byte[] green = new byte[size];
      byte[] blue = new byte[size];
      
      ((ColorProcessor)ipOrig).getRGB(red, green, blue);
      if (compareCh1 == 0) {
        ipCh1.setPixels(red);
      } else if (compareCh1 == 1) {
        ipCh1.setPixels(green);
      } else {
        ipCh1.setPixels(blue);
      }

      if (compareCh2 == 0) {
        ipCh2.setPixels(red);
      } else if (compareCh2 == 1) {
        ipCh2.setPixels(green);
      } else {
        ipCh2.setPixels(blue);
      }

      if (compareCh1 + compareCh2 == 1) {
        ipCh3.setPixels(blue);
        ch3 = blue;
      } else if (compareCh1 + compareCh2 == 2) {
        ipCh3.setPixels(green);
        ch3 = green;
      } else {
        ipCh3.setPixels(red);
        ch3 = red;
      }
    } else {
      if ((((imOrig.getType() == 2) || (imOrig.getType() == 1) || (imOrig.getType() == 0) ? 1 : 0) & (imOrig.getDimensions()[2] >= 2 ? 1 : 0)) != 0)
      {
        compareCh1 += 1;
        compareCh2 += 1;
        
        int num_dimensions = imOrig.getDimensions()[2];
        if ((compareCh1 <= 0) || (compareCh1 > num_dimensions)) {
          IJ.error("Channel 1 is outside of image stack range.");
          return;
        }
        if ((compareCh2 <= 0) || (compareCh2 > num_dimensions)) {
          IJ.error("Channel 2 is outside of image stack range.");
          return;
        }
        
        if (imOrig.getType() != 0)
        {
          imageType = "STACKED";
          imOrig.setC(compareCh1);
          ipCh1.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
          ipCh1.setMask(imOrig.getChannelProcessor().getMask());
          ipCh1.setRoi(imOrig.getChannelProcessor().getRoi());

          imOrig.setC(compareCh2);
          ipCh2.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
          ipCh2.setMask(imOrig.getChannelProcessor().getMask());
          ipCh2.setRoi(imOrig.getChannelProcessor().getRoi());

          ImageStack new8bitStack = imOrig.createEmptyStack();
          new8bitStack.addSlice(ipCh1);
          new8bitStack.addSlice(ipCh2);

          im = new CompositeImage(imOrig);
          im.setStack(new8bitStack);
          im.setTitle(imOrig.getShortTitle() + "-8bit_ch" + compareCh1 + "-ch" + compareCh2);
          im.setRoi(imOrig.getRoi());
          im.setCalibration(imOrig.getCalibration());
          im.setDisplayMode(1);
        }
        else
        {
          imageType = "STACKED8";
          imOrig.setC(compareCh1);
          ipCh1.setPixels(imOrig.getChannelProcessor().getPixelsCopy());
          ipCh1.setMask(imOrig.getChannelProcessor().getMask());
          ipCh1.setRoi(imOrig.getChannelProcessor().getRoi());

          imOrig.setC(compareCh2);
          ipCh2.setPixels(imOrig.getChannelProcessor().convertToByteProcessor().getPixelsCopy());
          ipCh2.setMask(imOrig.getChannelProcessor().getMask());
          ipCh2.setRoi(imOrig.getChannelProcessor().getRoi());

          im = imOrig;
        }
      }
      else
      {
        IJ.error("RGB image (8-bit) or Stacked GRAY image (8/16/32-bit) required."); return;
      }
    }
    im.setC(compareCh1);
    
    String name = im.getShortTitle();
    AutoThresholder.Method method = this.methods[thMethodInt];
    Calibration cal = im.getCalibration();
    String unit = cal.getUnit();
    double pixelHeight = cal.pixelHeight;
    double pixelWidth = cal.pixelWidth;
    double calConvert = pixelHeight * pixelWidth;
    Rectangle roi = im.getProcessor().getRoi();
    Roi roiSelection = im.getRoi();
    ImageProcessor mask = im.getMask();
    ImageProcessor ipOverlaps = new ByteProcessor(M, N);
    ImageProcessor ipMask = new ByteProcessor(M, N);
    boolean hasMask = mask != null;
    boolean hasRoi = roiSelection != null;
    
    if (hasRoi) {
      String nameRoi = roiSelection.getName();
      if (nameRoi != null) {
        name = name + '-' + nameRoi;
      }
    }
    
    if (hasMask) {
      ipMask.insert(mask, roi.x, roi.y);
    }
    else
    {
      ipMask.setValue(255.0D);
      ipMask.setRoi(roi);
      ipMask.fill();
    }
    
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipMask.getPixel(u, v);
        if (p == 0) {
          ipCh1.putPixel(u, v, 0);
          ipCh2.putPixel(u, v, 0);
          if (imageType.equals("RGB")) { ipCh3.putPixel(u, v, 0); }
        }
      }
    }
    
    double aRoi = 0;
    if ((hasMask) || (hasRoi)) {
      ipCh1.setMask(ipMask);
      ImageStatistics roiStats = ipCh1.getStatistics();
      aRoi = (double) roiStats.pixelCount * calConvert;
    }
    ImageProcessor ipCh1Mask = ipCh1.duplicate();
    ImageProcessor ipCh2Mask = ipCh2.duplicate();

    int th_ch1 = thManual_ch1Level;
    int th_ch2 = thManual_ch2Level;
    ipCh1Mask.setMask(ipMask);
    ipCh2Mask.setMask(ipMask);
    AutoThresholder autoth;
    if (!thManualOption)
    {
      autoth = new AutoThresholder();

      int[] ch1_hist = ipCh1Mask.getHistogram();
      th_ch1 = autoth.getThreshold(method, ch1_hist);

      int[] ch2_hist = ipCh2Mask.getHistogram();
      th_ch2 = autoth.getThreshold(method, ch2_hist);
    }
    ipCh1Mask.threshold(th_ch1);
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
    int num_rem;
    if (minCh1AreaClusters > 0)
    {
      num_rem = fs.excludeByClusterArea(minCh1AreaClusters, cal, ipCh1Mask, ipCh1);
    }
    if (minCh2AreaClusters > 0)
    {
      num_rem = fs.excludeByClusterArea(minCh2AreaClusters, cal, ipCh2Mask, ipCh2);
    }
    ResultsTable summary = Analyzer.getResultsTable();
    if (summary == null) {
      summary = new ResultsTable();
      Analyzer.setResultsTable(summary);
    }
    ResultsTable rTable = new ResultsTable();

    ipOverlaps.copyBits(ipCh1Mask, 0, 0, Blitter.COPY);
    ipOverlaps.copyBits(ipCh2Mask, 0, 0, Blitter.AND);

    ImageProcessor ipFlood = ipOverlaps.duplicate();
    List<ImageProcessor> overlaps = new ArrayList();
    List<Rectangle> overlapsRect = new ArrayList();
    
    int overlapCount = fs.clustersProcessing(name, true, rTable, cal, ipFlood, ipOverlaps, overlaps, overlapsRect);

    ImageProcessor ipCh1Flood = ipCh1Mask.duplicate();
    List<ImageProcessor> ch1Clusters = new ArrayList();
    List<Rectangle> ch1ClustersRect = new ArrayList();

    int ch1ClusterCount = fs.clustersProcessing(cal, rTable, ipCh1Flood, ipCh1, ch1Clusters, ch1ClustersRect);

    ImageProcessor ipCh2Flood = ipCh2Mask.duplicate();
    List<ImageProcessor> ch2Clusters = new ArrayList();
    List<Rectangle> ch2ClustersRect = new ArrayList();

    int ch2ClusterCount = fs.clustersProcessing(cal, rTable, ipCh2Flood, ipCh2, ch2Clusters, ch2ClustersRect);
    boolean clustersFound = true;

    if ((ch1ClusterCount == 0) || (ch2ClusterCount == 0)) {
      clustersFound = false;
    }

    ipCh2.setMask(ipCh2Mask);
    ImageStatistics ch2Stats = ipCh2.getStatistics();
    double aCh2Pixels = (double) ch2Stats.pixelCount * calConvert;

    ipCh1.setMask(ipCh1Mask);
    ImageStatistics ch1Stats = ipCh1.getStatistics();
    double aCh1Pixels = (double) ch1Stats.pixelCount * calConvert;

    ipCh1.setMask(ipOverlaps);
    ImageStatistics overlapStats = ipCh1.getStatistics();
    double aOverlapPixels = (double) overlapStats.pixelCount * calConvert;

    int ch1SumIntensity = fs.sumIntensities(ipCh1);
    int ch2SumIntensity = fs.sumIntensities(ipCh2);
    int ch1SumIntensityTh = fs.sumIntensitiesMask(ipCh1, ipCh1Mask);
    int ch2SumIntensityTh = fs.sumIntensitiesMask(ipCh2, ipCh2Mask);

    int ch2Overlaps = fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask);
    int ch1Overlaps = fs.ch2ClusterOverlaps(ipCh2Mask, ipCh1Mask);

    int[] ch1OverlapsStoich = fs.clusterStoichiometry(ipCh2Mask, ipCh1Mask);
    int[] ch2OverlapsStoich = fs.clusterStoichiometry(ipCh1Mask, ipCh2Mask);

    double ch2Percentage = (double) ch2Overlaps / (double) ch2ClusterCount;
    double ch1Percentage = (double) ch1Overlaps / (double) ch1ClusterCount;

    double ch1MeanInt = ch1Stats.mean;
    double ch2MeanInt = ch2Stats.mean;
    
    int minX = 0;
    int maxX = M;
    int minY = 0;
    int maxY = N;

    if (hasRoi) {
      minX = (int)roi.getMinX();
      maxX = (int)roi.getMaxX();
      minY = (int)roi.getMinY();
      maxY = (int)roi.getMaxY();
    }

    double pValCh1Ch2 = 0.0;
    double pValCh2Ch1 = 0.0;
    double IFCh1Ch2 = 0.0;
    double IFCh2Ch1 = 0.0;
    if ((calcIFOption) || (randomSimsSummaryOption) & (clustersFound))
    {
      double[] ch2ClustersProbs = new double[ch2Clusters.size()];
      Arrays.fill(ch2ClustersProbs, 0.0);
      double countForPvalCh2 = 0.0;

      double[] ch1ClustersProbs = new double[ch1Clusters.size()];
      Arrays.fill(ch1ClustersProbs, 0.0);
      double countForPvalCh1 = 0.0;
      int nMaxSims;
      if (calcIFOption)
      {
        nMaxSims = this.nMaxSimulationsIF;
      }
      else
      {
        nMaxSims = this.randomSimsMaxDisplay;
      }

      for (int i = 0; i < nMaxSims; i++)
      {
        IJ.showProgress(i, nMaxSims);
        String nSimulation = Integer.toString(i + 1);
        IJ.showStatus("Running IF..." + nSimulation + "/" + nMaxSims);
        ImageProcessor ipCh1Random;
        if (this.moveCh1Clusters) {
          ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
        }
        else {
          ipCh1Random = ipCh1.duplicate();
        }
        
        ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
        ipCh1RandomMask.threshold(th_ch1);

        ImageProcessor ipCh2Random = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh1RandomMask, ch2ClustersProbs, ch2Clusters, ch2ClustersRect);

        ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
        ipCh2RandomMask.threshold(th_ch2);
        
        int ch1RandomOverlaps = 0;
        double percOverlapsCh1 = 0.0;
        if (calcIFOption)
        {
          ImageProcessor ipCh1Random2;
          if (this.moveCh1Clusters) {
            ipCh1Random2 = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh2RandomMask, ch1ClustersProbs, ch1Clusters, ch1ClustersRect);
          }
          else {
            ipCh1Random2 = ipCh1.duplicate();
          }
          
          ImageProcessor ipCh1RandomMask2 = ipCh1Random2.duplicate();
          ipCh1RandomMask2.threshold(th_ch1);
          
          ch1RandomOverlaps = fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask2);
          percOverlapsCh1 = (double) ch1RandomOverlaps / (double) ch1Clusters.size();

          if (percOverlapsCh1 >= ch1Percentage) {
            countForPvalCh1 += 1;
          }
        }
        
        int ch2RandomOverlaps = fs.ch2ClusterOverlaps(ipCh1RandomMask, ipCh2RandomMask);
        double percOverlapsCh2 = (double) ch2RandomOverlaps / (double) ch2Clusters.size();
        if (percOverlapsCh2 >= ch2Percentage) {
          countForPvalCh2 += 1;
        }
        ImageProcessor ipOverlapsRandom = new ByteProcessor(M, N);
        ipOverlapsRandom.copyBits(ipCh1RandomMask, 0, 0, Blitter.COPY);
        ipOverlapsRandom.copyBits(ipCh2RandomMask, 0, 0, Blitter.AND);

        ImageProcessor ipOverlapFlood = ipOverlapsRandom.duplicate();
        List<ImageProcessor> oClustersRandom = new ArrayList();
        List<Rectangle> oClustersRectRandom = new ArrayList();

        int oRandomCount = fs.clustersProcessing(name + "_Sim_" + nSimulation, true, rTable, cal, ipOverlapFlood, ipOverlapsRandom, oClustersRandom, oClustersRectRandom);
        int ch1RandomOverlaps_first_sim = fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask);
        double percOverlapsCh1_first_sim = (double) ch1RandomOverlaps_first_sim / (double) ch1Clusters.size();

        int[] ch1RandomOverlapsStoich = fs.clusterStoichiometry(ipCh2RandomMask, ipCh1RandomMask);
        int[] ch2RandomOverlapsStoich = fs.clusterStoichiometry(ipCh1RandomMask, ipCh2RandomMask);

        ipCh1Random.setMask(ipOverlapsRandom);
        ImageStatistics overlapRandomStats = ipCh1Random.getStatistics();
        double aOverlapRandomPixels = (double) overlapRandomStats.pixelCount * calConvert;
        
        if ((randomSimsSummaryOption) && (i < this.randomSimsMaxDisplay))
        {
          summary.incrementCounter();
          summary.addValue("Image", name + "_Sim_" + nSimulation);
          summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);

          String ch12Str = "";
          String ch21Str = "";
          String ch1Str = "";
          String ch2Str = "";
          
          if (imageType == "RGB") {
            ch12Str = this.channelsAbb[compareCh1] + "-" + this.channelsAbb[compareCh2];
            ch21Str = this.channelsAbb[compareCh2] + "-" + this.channelsAbb[compareCh1];
            ch1Str = this.channelsAbb[compareCh1];
            ch2Str = this.channelsAbb[compareCh2];
          }
            else {
            ch12Str = "ch" + compareCh1 + "-ch" + compareCh2;
            ch21Str = "ch" + compareCh2 + "-ch" + compareCh1;
            ch1Str = "ch" + compareCh1;
            ch2Str = "ch" + compareCh2;
          }
          
          if (calcIFOption)
          {
            summary.addValue(ch12Str + " IF", "NT");
            summary.addValue(ch12Str + " p-val", "NT");
            summary.addValue(ch21Str + " IF", "NT");
            summary.addValue(ch21Str + " p-val", "NT");

            if (imageType == "RGB") {
              for (int c1 = 0; c1 < 3; c1++) {
                for (int c2 = 0; c2 < 3; c2++) {
                  if (c1 != c2) {
                    if (((c1 == compareCh1 ? 1 : 0) & (c2 == compareCh2 ? 1 : 0)) == 0) {
                      if (((c1 == compareCh2 ? 1 : 0) & (c2 == compareCh1 ? 1 : 0)) == 0)
                      {
                        summary.addValue(this.channelsAbb[c1] + "-" + this.channelsAbb[c2] + " IF", "NT");
                        summary.addValue(this.channelsAbb[c1] + "-" + this.channelsAbb[c2] + " p-val", "NT");
                        summary.addValue(this.channelsAbb[c2] + "-" + this.channelsAbb[c1] + " IF", "NT");
                        summary.addValue(this.channelsAbb[c2] + "-" + this.channelsAbb[c1] + " p-val", "NT");
                      }
                    }
                  }
                }
              }
            }
          }
          
          summary.addValue("Th Algorithm", this.thMethods[thMethodInt]);
          summary.addValue(ch1Str + " Th", th_ch1);
          summary.addValue(ch2Str + " Th", th_ch2);

          if (sumIntOption) {
            summary.addValue(ch1Str + " Sum Inten", "None");
          }

          if (sumIntThOption) {
            summary.addValue(ch1Str + " Clus Sum Inten", ch1SumIntensityTh);
          }

          if (meanIntThOption) {
            summary.addValue(ch1Str + " Clus Mean Inten", ch1MeanInt);
          }

          if (areaOption) {
            summary.addValue(ch1Str + " Clus Area", aCh1Pixels);
          }

          summary.addValue(ch1Str + " Clus Count", ch1ClusterCount);

          if (sumIntOption) {
            summary.addValue(ch2Str + " Sum Inten", "None");
          }

          if (sumIntThOption) {
            summary.addValue(ch2Str + " Clus Sum Inten", ch2SumIntensityTh);
          }

          if (meanIntThOption) {
            summary.addValue(ch2Str + " Clus Mean Inten", ch2MeanInt);
          }

          if (areaOption) {
            summary.addValue(ch2Str + " Clus Area", aCh2Pixels);
          }
          summary.addValue(ch2Str + " Clus Count", ch2ClusterCount);

          if (overlapsAreaOpt) {
            summary.addValue("Overlap Area", aOverlapRandomPixels);
          }

          if (overlapsCountOpt) {
            summary.addValue("Overlap Count", oRandomCount);
          }

          if (overlapsPercOpt) {
            summary.addValue(ch1Str + " %Clus Overlaps", String.format("%.1f", percOverlapsCh1_first_sim * 100.0 ));
          }

          if (overlapsOpt) {
            summary.addValue(ch1Str + " Clus Overlaps", ch1RandomOverlaps_first_sim);
          }

          if (ch1StoiOption)
          {
            summary.addValue(ch1Str + "1:1", String.format("%.1f", (double) ch1RandomOverlapsStoich[0] / (double) ch1RandomOverlaps_first_sim * 100.0 ));
            summary.addValue(ch1Str + "1:2", String.format("%.1f", (double) ch1RandomOverlapsStoich[1] / (double) ch1RandomOverlaps_first_sim * 100.0 ));
            summary.addValue(ch1Str + "1:3", String.format("%.1f", (double) ch1RandomOverlapsStoich[2] / (double) ch1RandomOverlaps_first_sim * 100.0 ));
            summary.addValue(ch1Str + "1:>3", String.format("%.1f", (double) ch1RandomOverlapsStoich[3] / (double) ch1RandomOverlaps_first_sim * 100.0 ));
          }

          if (overlapsPercOpt) {
            summary.addValue(ch2Str + " %Clus Overlaps", String.format("%.1f", percOverlapsCh2 * 100.0 ));
          }

          if (overlapsOpt) {
            summary.addValue(ch2Str + " Clus Overlaps", ch2RandomOverlaps);
          }

          if (ch2StoiOption)
          {
            summary.addValue(ch2Str + "1:1", String.format("%.1f", (double) ch2RandomOverlapsStoich[0] / (double) ch2RandomOverlaps * 100.0 ));
            summary.addValue(ch2Str + "1:2", String.format("%.1f", (double) ch2RandomOverlapsStoich[1] / (double) ch2RandomOverlaps * 100.0 ));
            summary.addValue(ch2Str + "1:3", String.format("%.1f", (double) ch2RandomOverlapsStoich[2] / (double) ch2RandomOverlaps * 100.0 ));
            summary.addValue(ch2Str + "1:>3", String.format("%.1f", (double) ch2RandomOverlapsStoich[3] / (double) ch2RandomOverlaps * 100.0 ));
          }

          if (areaRoiOption) {
            summary.addValue("ROI Area", aRoi);
          }
        }

        if (simImageOption) {
          ImagePlus randomSimImage;

          if (imageType == "RGB")
          {
            byte[] blueRandom;
            byte[] greenRandom;
            byte[] redRandom;
            if ((compareCh1 == 0) && (compareCh2 == 1)) {
              redRandom = (byte[])ipCh1Random.getPixels();
              greenRandom = (byte[])ipCh2Random.getPixels();
              blueRandom = ch3;
            } else {
              if ((compareCh1 == 1) && (compareCh2 == 0)) {
                greenRandom = (byte[])ipCh1Random.getPixels();
                redRandom = (byte[])ipCh2Random.getPixels();
                blueRandom = ch3;
              } else {

                if ((compareCh1 == 2) && (compareCh2 == 0)) {
                  blueRandom = (byte[])ipCh1Random.getPixels();
                  redRandom = (byte[])ipCh2Random.getPixels();
                  greenRandom = ch3;
                } else {

                  if ((compareCh1 == 0) && (compareCh2 == 2)) {
                    redRandom = (byte[])ipCh1Random.getPixels();
                    blueRandom = (byte[])ipCh2Random.getPixels();
                    greenRandom = ch3;
                  } else {

                    if ((compareCh1 == 2) && (compareCh2 == 1)) {
                      blueRandom = (byte[])ipCh1Random.getPixels();
                      greenRandom = (byte[])ipCh2Random.getPixels();
                      redRandom = ch3;
                    }
                    else
                    {
                      greenRandom = (byte[])ipCh1Random.getPixels();
                      blueRandom = (byte[])ipCh2Random.getPixels();
                      redRandom = ch3;
                    }
                  }
                }
              }
            }
            ColorProcessor ipSimulation = new ColorProcessor(M, N);
            ipSimulation.setRGB(redRandom, greenRandom, blueRandom);
            randomSimImage = new ImagePlus(name + "_Sim" + nSimulation, ipSimulation);
          }
          else {
            ColorProcessor ipSimulation = new ColorProcessor(M, N);
            byte[] blue = new byte[size];
            ipSimulation.setRGB((byte[])ipCh1Random.getPixels(), (byte[])ipCh2Random.getPixels(), blue);
            randomSimImage = new ImagePlus(name + "_Sim" + nSimulation, ipSimulation);
          }
          FileSaver fileSave = new FileSaver(randomSimImage);
          fileSave.saveAsTiff(imagedir + name + "_Sim" + nSimulation + ".tiff");
        }
      }
      
      pValCh1Ch2 = countForPvalCh2 / (double) this.nMaxSimulationsIF;
      double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, this.nMaxSimulationsIF);
      IFCh1Ch2 = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);

      pValCh2Ch1 = countForPvalCh1 / (double) this.nMaxSimulationsIF;
      double[] ch1ClustersProbsTest = fs.prob(ch1ClustersProbs, this.nMaxSimulationsIF);
      IFCh2Ch1 = fs.calcIF(ch1ClustersProbsTest, ch1Percentage);
    }

    summary.incrementCounter();
    summary.addValue("Image", name);
    summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);

    String ch12Str = "";
    String ch21Str = "";
    String ch1Str = "";
    String ch2Str = "";
    
    if(imageType.equals("RGB"))
    {
      ch12Str = this.channelsAbb[compareCh1] + "-" + this.channelsAbb[compareCh2];
      ch21Str = this.channelsAbb[compareCh2] + "-" + this.channelsAbb[compareCh1];
      ch1Str = this.channelsAbb[compareCh1];
      ch2Str = this.channelsAbb[compareCh2];
    }
    else
    {
      ch12Str = "ch" + compareCh1 + "-ch" + compareCh2;
      ch21Str = "ch" + compareCh2 + "-ch" + compareCh1;
      ch1Str = "ch" + compareCh1;
      ch2Str = "ch" + compareCh2;
    }
    
    if(calcIFOption) {
      String pValStrCh1Ch2;
      if (pValCh1Ch2 == 0.0) {
        pValStrCh1Ch2 = "p<0.02";
      }
      else {
        pValStrCh1Ch2 = "p=" + String.valueOf(pValCh1Ch2);
      }
      String pValStrCh2Ch1;

      if (pValCh2Ch1 == 0.0) {
        pValStrCh2Ch1 = "p<0.02";
      }
      else {
        pValStrCh2Ch1 = "p=" + String.valueOf(pValCh2Ch1);
      }
      
      summary.addValue(ch12Str + " IF", IFCh1Ch2);
      summary.addValue(ch12Str + " p-val", pValStrCh1Ch2);
      
      if (this.moveCh1Clusters) {
        summary.addValue(ch21Str + " IF", IFCh2Ch1);
        summary.addValue(ch21Str + " p-val", pValStrCh2Ch1);
      }
      else {
        summary.addValue(ch21Str + " IF", "NT");
        summary.addValue(ch21Str + " p-val", "NT");
      }

      if (imageType.equals("RGB"))
      {
        for (int c1 = 0; c1 < 3; c1++)
        {
          for (int c2 = 0; c2 < 3; c2++)
          {
            if (c1 != c2) {
              if (((c1 == compareCh1 ? 1 : 0) & (c2 == compareCh2 ? 1 : 0)) == 0) {
                if (((c1 == compareCh2 ? 1 : 0) & (c2 == compareCh1 ? 1 : 0)) == 0) {
                  summary.addValue(this.channelsAbb[c1] + "-" + this.channelsAbb[c2] + " IF", "NT");
                  summary.addValue(this.channelsAbb[c1] + "-" + this.channelsAbb[c2] + " p-val", "NT");
                  summary.addValue(this.channelsAbb[c2] + "-" + this.channelsAbb[c1] + " IF", "NT");
                  summary.addValue(this.channelsAbb[c2] + "-" + this.channelsAbb[c1] + " p-val", "NT");
                }
              }
            }
          }
        }
      }
    }

    summary.addValue("Th Algorithm", this.thMethods[thMethodInt]);
    summary.addValue(ch1Str + " Th", th_ch1);
    summary.addValue(ch2Str + " Th", th_ch2);

    if (sumIntOption) {
      summary.addValue(ch1Str + " Sum Inten", ch1SumIntensity);
    }

    if (sumIntThOption) {
      summary.addValue(ch1Str + " Clus Sum Inten", ch1SumIntensityTh);
    }

    if (meanIntThOption) {
      summary.addValue(ch1Str + " Clus Mean Inten", ch1MeanInt);
    }

    if (areaOption) {
      summary.addValue(ch1Str + " Clus Area", aCh1Pixels);
    }

    summary.addValue(ch1Str + " Clus Count", ch1ClusterCount);

    if (sumIntOption) {
      summary.addValue(ch2Str + " Sum Inten", ch2SumIntensity);
    }

    if (sumIntThOption) {
      summary.addValue(ch2Str + " Clus Sum Inten", ch2SumIntensityTh);
    }

    if (meanIntThOption) {
      summary.addValue(ch2Str + " Clus Mean Inten", ch2MeanInt);
    }

    if (areaOption) {
      summary.addValue(ch2Str + " Clus Area", aCh2Pixels);
    }

    summary.addValue(ch2Str + " Clus Count", ch2ClusterCount);

    if (overlapsAreaOpt) {
      summary.addValue("Overlap Area", aOverlapPixels);
    }

    if (overlapsCountOpt) {
      summary.addValue("Overlap Count", overlapCount);
    }

    if (overlapsPercOpt) {
      summary.addValue(ch1Str + " %Clus Overlaps", String.format("%.1f", ch1Percentage * 100.0));
    }

    if (overlapsOpt) {
      summary.addValue(ch1Str + " Clus Overlaps", ch1Overlaps);
    }

    if (ch1StoiOption) {
      summary.addValue(ch1Str + "1:1", String.format("%.1f", (double) ch1OverlapsStoich[0] / (double) ch1Overlaps * 100.0));
      summary.addValue(ch1Str + "1:2", String.format("%.1f", (double) ch1OverlapsStoich[1] / (double) ch1Overlaps * 100.0));
      summary.addValue(ch1Str + "1:3", String.format("%.1f", (double) ch1OverlapsStoich[2] / (double) ch1Overlaps * 100.0));
      summary.addValue(ch1Str + "1:>3", String.format("%.1f", (double) ch1OverlapsStoich[3] / (double) ch1Overlaps * 100.0));
    }

    if(overlapsPercOpt) {
      summary.addValue(ch2Str + " %Clus Overlaps", String.format("%.1f", ch2Percentage * 100.0));
    }

    if(overlapsOpt) {
      summary.addValue(ch2Str + " Clus Overlaps", ch2Overlaps);
    }

    if (ch2StoiOption) {
      summary.addValue(ch2Str + "1:1", String.format("%.1f", (double) ch2OverlapsStoich[0] / (double) ch2Overlaps * 100.0));
      summary.addValue(ch2Str + "1:2", String.format("%.1f", (double) ch2OverlapsStoich[1] / (double) ch2Overlaps * 100.0));
      summary.addValue(ch2Str + "1:3", String.format("%.1f", (double) ch2OverlapsStoich[2] / (double) ch2Overlaps * 100.0));
      summary.addValue(ch2Str + "1:>3", String.format("%.1f", (double) ch2OverlapsStoich[3] / (double) ch2Overlaps * 100.0));
    }
    
    if (areaRoiOption) {
      summary.addValue("ROI Area", aRoi);
    }
    
    summary.show("Results");

    if (overlapLocations) {
      rTable.show("Overlap Locations");
    }
    
    if (ch1MaskOption) {
      ImagePlus ch1Im = new ImagePlus(name + ch1Str + " Mask", ipCh1Mask);
      ch1Im.setCalibration(cal);
      ch1Im.show();
    }
    if (ch2MaskOption) {
      ImagePlus ch2Im = new ImagePlus(name + ch2Str + " Mask", ipCh2Mask);
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
    
//    if (((imageType != "STACKED8" ? 1 : 0) & (imageType != "RGB" ? 1 : 0)) != 0)
//    {
//      im.show();
//    } ??

//    if(imageType == "STACKED8" || imageType == "RGB")
//    {
//      im.show();
//    }

    if(imageType != "STACKED8" && imageType != "RGB")
    {
      im.show();
    }

    IJ.showProgress(1.0);
  }

  public static void main(String[] args)
  {
    Class<?> clazz = Interaction_Factor.class;
    String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
    String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
    System.setProperty("plugins.dir", pluginsDir);

    new ImageJ();
  }
}

