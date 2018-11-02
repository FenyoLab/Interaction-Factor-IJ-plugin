package interactionFactor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.Recorder;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
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

public class Interaction_Factor_Sims implements PlugIn, DialogListener
{
  private static String[] thMethods = AutoThresholder.getMethods();
  private static String[] channels = { "Red", "Green", "Blue" };
  private static String[] channelsLower = { "red", "green", "blue" };
  private String[] channelsAbb = { "R", "G", "B" };
  private static String[] simParametersCh1 = { "None", "Random" };
  private static String[] simParametersCh2 = { "Random", "NonRandom" };
  private String[] measurements = { "Clusters_Area", "ROI_Area", "Sum_Pixel_Inten", "Clusters_Sum_Inten", "Clusters_Mean_Inten", "Ch1_Stoichiometry", "Ch2_Stoichiometry", "Clusters_Overlaps", "%Clusters_Overlpas", "Overlap_Count", "Overlap_Area" };
  private boolean[] measurVals = { false, false, false, false, false, false, false, false, false, false, false, false };
  private String[] outputImg = { "Show_Simulations", "Show_Ch1_Mask", "Show_Ch2_Mask", "Show_ROI_Mask", "Show_Overlap_Mask", "Overlap_Locations_Table" };
  private boolean[] outputImgVals = { false, false, false, false, false, false };
  private int thMethodInt = 11;
  private int ch1Color = 0;
  private int ch2Color = 1;
  private boolean edgeOption = false;
  private boolean moveCh1Clusters = false;
  private boolean thManualOption = false;
  private int thManual_ch1Level = 0;
  private int thManual_ch2Level = 0;
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
  private boolean showSimsOption = false;
  private boolean ch1MaskOption = false;
  private boolean ch2MaskOption = false;
  private boolean roiMaskOption = false;
  private boolean overlapMaskOption = false;
  private boolean overlapLocations = false;
  private String ch1SimParam;
  private String ch2SimParam;
  private Integer ch1SimParamInt;
  private Integer ch2SimParamInt;
  private double interFactorCh2;
  private AutoThresholder.Method[] methods;
  private static int nMaxSimulations = 0;
  protected static final String PREF_KEY = "Interaction_Factor_Sims.";
  
  public Interaction_Factor_Sims() {
    thMethods = AutoThresholder.getMethods();
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
        if (command == "Apply Overlay")
        {
          Choice choice0 = (Choice)gd.getChoices().get(0);
          int ch1Color = choice0.getSelectedIndex();

          Choice choice1 = (Choice)gd.getChoices().get(1);
          int ch2Color = choice1.getSelectedIndex();

          Choice choice2 = (Choice)gd.getChoices().get(2);
          int thMethodInt = choice2.getSelectedIndex();

          Vector numFields = gd.getNumericFields();
          TextField numField0 = (TextField)numFields.get(0);
          TextField numField1 = (TextField)numFields.get(1);
          int thManual_ch1Level = Integer.parseInt(numField0.getText());
          int thManual_ch2Level = Integer.parseInt(numField1.getText());

          Vector checkboxes = gd.getCheckboxes();
          Checkbox check0 = (Checkbox)checkboxes.get(0);
          Checkbox check1 = (Checkbox)checkboxes.get(1);
          boolean thManualOption = check0.getState();
          boolean edgeOption = check1.getState();

          IfFunctions fs = new IfFunctions();

          ImagePlus im = IJ.getImage();

          if (im.getType() != 4) {
            IJ.error("RGB image required");
          }

          if (ch1Color == ch2Color) {
            IJ.error("Channel Colors are the same. Choose another channel");
          }

          AutoThresholder.Method method = this.methods[thMethodInt];

          ImageProcessor ip = im.getProcessor();
          Rectangle roi = ip.getRoi();
          Roi roiSelection = im.getRoi();

          ImageProcessor mask = im.getMask();

          int M = ip.getWidth();
          int N = ip.getHeight();
          int size = M * N;

          byte[] red = new byte[size];
          byte[] green = new byte[size];
          byte[] blue = new byte[size];

          ((ColorProcessor)ip).getRGB(red, green, blue);

          ImageProcessor ipCh1 = new ByteProcessor(M, N);
          ImageProcessor ipCh2 = new ByteProcessor(M, N);
          ImageProcessor ipCh3 = new ByteProcessor(M, N);

          ImageProcessor ipMask = new ByteProcessor(M, N);

          if (ch1Color == 0) {
            ipCh1.setPixels(red);
          } else if (ch1Color == 1) {
            ipCh1.setPixels(green);
          } else {
            ipCh1.setPixels(blue);
          }

          if (ch2Color == 0) {
            ipCh2.setPixels(red);
          } else if (ch2Color == 1) {
            ipCh2.setPixels(green);
          } else {
            ipCh2.setPixels(blue);
          }
          byte[] ch3;
          if (ch1Color + ch2Color == 1) {
            ipCh3.setPixels(blue);
            ch3 = blue;
          }
          else {
            if (ch1Color + ch2Color == 2) {
              ipCh3.setPixels(green);
              ch3 = green;
            } else {
              ipCh3.setPixels(red);
              ch3 = red;
            }
          }
          boolean hasMask = mask != null;
          boolean hasRoi = roiSelection != null;

          if (hasMask) {
            ipMask.insert(mask, roi.x, roi.y);
          }
          else {
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
          if (!thManualOption)
          {
            AutoThresholder autoth = new AutoThresholder();

            ipCh1Mask.setMask(ipMask);
            int[] ch1_hist = ipCh1Mask.getHistogram();
            th_ch1 = autoth.getThreshold(method, ch1_hist);

            ipCh2Mask.setMask(ipMask);
            int[] ch2_hist = ipCh2Mask.getHistogram();
            th_ch2 = autoth.getThreshold(method, ch2_hist);
          }

          ipCh1Mask.threshold(th_ch1);
          ipCh2Mask.threshold(th_ch2);

          IJ.setThreshold(im, th_ch2, 255.0D, "Red");
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

          Overlay chsOverlays = fs.returnOverlay(ipCh1Mask, ipCh2Mask);
          Color stColor = Color.WHITE;
          chsOverlays.setStrokeColor(stColor);
          im.setOverlay(chsOverlays);
        }

        if (command == "Clear Overlay") {
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
    this.thMethodInt = ((int)Prefs.get("Interaction_Factor_Sims.thMethodInt", 0.0));
    this.ch1Color = ((int)Prefs.get("Interaction_Factor_Sims.ch1Color", 0.0));
    this.ch2Color = ((int)Prefs.get("Interaction_Factor_Sims.ch2Color", 1.0));
    this.edgeOption = Prefs.get("Interaction_Factor_Sims.edgeOption", true);
    this.thManualOption = Prefs.get("Interaction_Factor_Sims.thManualOption", false);
    this.thManual_ch1Level = ((int)Prefs.get("Interaction_Factor_Sims.thManual_ch1Level", 0.0));
    this.thManual_ch2Level = ((int)Prefs.get("Interaction_Factor_Sims.thManual_ch2Level", 0.0));
    this.overlapsOpt = Prefs.get("Interaction_Factor_Sims.overlapsOpt", true);
    this.overlapsPercOpt = Prefs.get("Interaction_Factor_Sims.overlapsPercOpt", true);
    this.overlapsCountOpt = Prefs.get("Interaction_Factor_Sims.overlapsCountOpt", true);
    this.overlapsAreaOpt = Prefs.get("Interaction_Factor_Sims.overlapsAreaOpt", true);
    this.sumIntOption = Prefs.get("Interaction_Factor_Sims.sumIntOption", true);
    this.sumIntThOption = Prefs.get("Interaction_Factor_Sims.sumIntThOption", true);
    this.meanIntThOption = Prefs.get("Interaction_Factor_Sims.meanIntThOption", true);
    this.areaOption = Prefs.get("Interaction_Factor_Sims.areaOption", true);
    this.areaRoiOption = Prefs.get("Interaction_Factor_Sims.areaRoiOption", true);
    this.ch1StoiOption = Prefs.get("Interaction_Factor_Sims.ch1StoiOption", true);
    this.ch2StoiOption = Prefs.get("Interaction_Factor_Sims.ch2StoiOption", true);
    this.showSimsOption = Prefs.get("Interaction_Factor_Sims.showSimsOption", false);
    this.ch1MaskOption = Prefs.get("Interaction_Factor_Sims.ch1MaskOption", false);
    this.ch2MaskOption = Prefs.get("Interaction_Factor_Sims.ch2MaskOption", false);
    this.roiMaskOption = Prefs.get("Interaction_Factor_Sims.roiMaskOption", false);
    this.overlapMaskOption = Prefs.get("Interaction_Factor_Sims.overlapMaskOption", false);
    this.overlapLocations = Prefs.get("Interaction_Factor_Sims.overlapLocations", false);
    this.ch1SimParamInt = (int)Prefs.get("Interaction_Factor_Sims.ch1SimParam", 0.0);
    this.ch2SimParamInt = (int)Prefs.get("Interaction_Factor_Sims.ch2SimParam", 0.0);
    this.interFactorCh2 = Prefs.get("Interaction_Factor_Sims.interFactorCh2", 0.0);
    nMaxSimulations = (int)Prefs.get("Interaction_Factor_Sims.nMaxSimulations", nMaxSimulations);
    this.ch1SimParam = simParametersCh1[this.ch1SimParamInt];
    this.ch2SimParam = simParametersCh2[this.ch2SimParamInt];
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
    this.outputImgVals[0] = this.showSimsOption;
    this.outputImgVals[1] = this.ch1MaskOption;
    this.outputImgVals[2] = this.ch2MaskOption;
    this.outputImgVals[3] = this.roiMaskOption;
    this.outputImgVals[4] = this.overlapMaskOption;
    this.outputImgVals[5] = this.overlapLocations;

    GenericDialog gd = new NonBlockingGenericDialog("Interaction Factor Simulations");
    gd.addDialogListener(this);

    gd.addMessage("--------------- Segmentation ---------------\n");
    gd.addChoice("Channel_1_(Ch1)_Color:", channels, channels[this.ch1Color]);
    gd.addChoice("Channel_2_(Ch2)_Color:", channels, channels[this.ch2Color]);
    gd.addChoice("Threshold:", thMethods, thMethods[this.thMethodInt]);
    gd.addCheckbox("Use_Manual_Threshold", this.thManualOption);
    gd.addNumericField("Channel_1_Threshold", this.thManual_ch1Level, 0);
    gd.addNumericField("Channel_2_Threshold", this.thManual_ch2Level, 0);
    gd.addCheckbox("Exclude_Edge_Clusters", this.edgeOption);

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

    gd.addMessage("---------- Simulation Parameters -----------\n");
    gd.addRadioButtonGroup("Ch1_Simulation:", simParametersCh1, 2, 1, simParametersCh1[this.ch1SimParamInt]);
    gd.addRadioButtonGroup("Ch2_Simulation:", simParametersCh2, 2, 1, simParametersCh2[this.ch2SimParamInt]);
    gd.addNumericField("Interaction_Factor", 0.0D, 2);
    gd.addNumericField("Number_of_Simulations:", nMaxSimulations, 0);

    gd.addMessage("----------- Additional Measurements --------\n");
    gd.addCheckboxGroup(6, 2, this.measurements, this.measurVals);

    gd.addMessage("-------------- Output Images ---------------\n");
    gd.addCheckboxGroup(3, 2, this.outputImg, this.outputImgVals);

    gd.showDialog();
    Recorder.recordInMacros = true;

    if (gd.wasCanceled()) {
      return;
    }
    String ch1ColorStr = gd.getNextChoice();
    for (int i = 0; i < channels.length; i++) {
      if ((ch1ColorStr.equals(channels[i]) | ch1ColorStr.equals(channelsLower[i]))) {
        this.ch1Color = i;
      }
    }
    String ch2ColorStr = gd.getNextChoice();
    for (int i = 0; i < channels.length; i++) {
      if ((ch2ColorStr.equals(channels[i]) | ch2ColorStr.equals(channelsLower[i]))) {
        this.ch2Color = i;
      }
    }
    String thMethodIntStr = gd.getNextChoice();
    for (int i = 0; i < thMethods.length; i++) {
      if (thMethodIntStr.equals(thMethods[i])) {
        this.thMethodInt = i;
      }
    }

    this.thManualOption = gd.getNextBoolean();
    this.thManual_ch1Level = ((int)gd.getNextNumber());
    this.thManual_ch2Level = ((int)gd.getNextNumber());
    this.edgeOption = gd.getNextBoolean();
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
    this.showSimsOption = gd.getNextBoolean();
    this.ch1MaskOption = gd.getNextBoolean();
    this.ch2MaskOption = gd.getNextBoolean();
    this.roiMaskOption = gd.getNextBoolean();
    this.overlapMaskOption = gd.getNextBoolean();
    this.overlapLocations = gd.getNextBoolean();
    this.ch1SimParam = gd.getNextRadioButton();
    this.ch2SimParam = gd.getNextRadioButton();
    this.interFactorCh2 = gd.getNextNumber();
    nMaxSimulations = (int)gd.getNextNumber();

    if ((this.ch1SimParam.equals("None") | this.ch1SimParam.equals("none"))) {
      this.moveCh1Clusters = false;
    }
    else if ((this.ch1SimParam.equals("Random") | this.ch1SimParam.equals("random"))) {
      this.moveCh1Clusters = true;
    }
    if (Recorder.record) {
      Recorder.recordOption("channel_1(ch1)_color", ch1ColorStr);
      Recorder.recordOption("channel_2(ch2)_color", ch2ColorStr);
      Recorder.recordOption("threshold", thMethodIntStr);

      Recorder.recordOption("Channel_1_Threshold", Integer.toString(this.thManual_ch1Level));
      Recorder.recordOption("Channel_2_Threshold", Integer.toString(this.thManual_ch2Level));

      Recorder.recordOption("Ch1_Simulation", this.ch1SimParam);
      Recorder.recordOption("Ch2_Simulation", this.ch2SimParam);
      Recorder.recordOption("Interaction_Factor", Double.toString(this.interFactorCh2));
      Recorder.recordOption("Number_of_Simulations", Integer.toString(nMaxSimulations));

      if (this.thManualOption) {
        Recorder.recordOption("Use_Manual_Threshold");
      }

      if (this.edgeOption) {
        Recorder.recordOption("Exclude_Edge_Clusters");
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
      if (this.showSimsOption) {
        Recorder.recordOption("Show_Simulations");
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

    Prefs.set("Interaction_Factor_Sims.ch1Color", this.ch1Color);
    Prefs.set("Interaction_Factor_Sims.ch2Color", this.ch2Color);
    Prefs.set("Interaction_Factor_Sims.thMethodInt", this.thMethodInt);
    Prefs.set("Interaction_Factor_Sims.edgeOption", this.edgeOption);
    Prefs.set("Interaction_Factor_Sims.areaOption", this.areaOption);
    Prefs.set("Interaction_Factor_Sims.areaRoiOption", this.areaRoiOption);
    Prefs.set("Interaction_Factor_Sims.thManualOption", this.thManualOption);
    Prefs.set("Interaction_Factor_Sims.thManual_ch1Level", this.thManual_ch1Level);
    Prefs.set("Interaction_Factor_Sims.thManual_ch2Level", this.thManual_ch2Level);
    Prefs.set("Interaction_Factor_Sims.overlapsOpt", this.overlapsOpt);
    Prefs.set("Interaction_Factor_Sims.overlapsPercOpt", this.overlapsPercOpt);
    Prefs.set("Interaction_Factor_Sims.overlapsCountOpt", this.overlapsCountOpt);
    Prefs.set("Interaction_Factor_Sims.overlapsAreaOpt", this.overlapsAreaOpt);
    Prefs.set("Interaction_Factor_Sims.sumIntOption", this.sumIntOption);
    Prefs.set("Interaction_Factor_Sims.sumIntThOption", this.sumIntThOption);
    Prefs.set("Interaction_Factor_Sims.meanIntThOption", this.meanIntThOption);
    Prefs.set("Interaction_Factor_Sims.ch1StoiOption", this.ch1StoiOption);
    Prefs.set("Interaction_Factor_Sims.ch2StoiOption", this.ch2StoiOption);
    Prefs.set("Interaction_Factor_Sims.showSimsOption", this.showSimsOption);
    Prefs.set("Interaction_Factor_Sims.ch1MaskOption", this.ch1MaskOption);
    Prefs.set("Interaction_Factor_Sims.ch2MaskOption", this.ch2MaskOption);
    Prefs.set("Interaction_Factor_Sims.roiMaskOption", this.roiMaskOption);
    Prefs.set("Interaction_Factor_Sims.overlapMaskOption", this.overlapMaskOption);
    Prefs.set("Interaction_Factor_Sims.overlapLocations", this.overlapLocations);
    Prefs.set("Interaction_Factor_Sims.ch1SimParam", this.ch1SimParamInt);
    Prefs.set("Interaction_Factor_Sims.ch2SimParam", this.ch2SimParamInt);
    Prefs.set("Interaction_Factor_Sims.interFactorCh2", this.interFactorCh2);
    Prefs.set("Interaction_Factor_Sims.nMaxSimulations", nMaxSimulations);

    IfFunctions fs = new IfFunctions();

    AutoThresholder.Method[] methods = AutoThresholder.Method.values();
    ImagePlus im = IJ.getImage();

    if (im.getType() != 4) {
      IJ.error("RGB image required");
      return;
    }
    if (this.ch1Color == this.ch2Color) {
      IJ.error("Channel Colors are the same. Choose another channel");
      return;
    }
    if (this.ch2SimParam.equals("NonRandom")) {
      if (this.interFactorCh2 >= 1.0D) {
        IJ.error("Interaction Factor has to be less than 1");
        return;
      }
      if (this.interFactorCh2 == 0.0D) {
        IJ.error("Interaction Factor has to be greater than 0");
        return;
      }
    }
    if (nMaxSimulations == 0) {
      IJ.error("Indicate the number of simulations");
      return;
    }

    String name = im.getShortTitle();
    AutoThresholder.Method method = methods[this.thMethodInt];
    Calibration cal = im.getCalibration();
    String unit = cal.getUnit();
    double pixelHeight = cal.pixelHeight;
    double pixelWidth = cal.pixelWidth;
    double calConvert = pixelHeight * pixelWidth;
    ImageProcessor ip = im.getProcessor();
    Rectangle roi = ip.getRoi();
    Roi roiSelection = im.getRoi();
    ImageProcessor mask = im.getMask();

    int M = ip.getWidth();
    int N = ip.getHeight();
    int size = M * N;

    byte[] red = new byte[size];
    byte[] green = new byte[size];
    byte[] blue = new byte[size];

    ((ColorProcessor)ip).getRGB(red, green, blue);

    ImageProcessor ipCh1 = new ByteProcessor(M, N);
    ImageProcessor ipCh2 = new ByteProcessor(M, N);
    ImageProcessor ipCh3 = new ByteProcessor(M, N);
    ImageProcessor ipOverlaps = new ByteProcessor(M, N);
    ImageProcessor ipMask = new ByteProcessor(M, N);

    if (this.ch1Color == 0) {
      ipCh1.setPixels(red);
    }
    else if (this.ch1Color == 1) {
      ipCh1.setPixels(green);
    }
    else {
      ipCh1.setPixels(blue);
    }

    if (this.ch2Color == 0) {
      ipCh2.setPixels(red);
    }
    else if (this.ch2Color == 1) {
      ipCh2.setPixels(green);
    }
    else {
      ipCh2.setPixels(blue);
    }

    byte[] ch3;
    if (this.ch1Color + this.ch2Color == 1) {
      ipCh3.setPixels(blue);
      ch3 = blue;
    } else {
      if (this.ch1Color + this.ch2Color == 2) {
        ipCh3.setPixels(green);
        ch3 = green;
      }
      else {
        ipCh3.setPixels(red);
        ch3 = red;
      }
    }
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
    } else {
      ipMask.setValue(255.0);
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

    double aRoi = 0.0;
    if ((hasMask) || (hasRoi)) {
      ipCh1.setMask(ipMask);
      ImageStatistics roiStats = ipCh1.getStatistics();
      aRoi = (double) roiStats.pixelCount * calConvert;
    }

    ImageProcessor ipCh1Mask = ipCh1.duplicate();
    ImageProcessor ipCh2Mask = ipCh2.duplicate();

    int th_ch1 = this.thManual_ch1Level;
    int th_ch2 = this.thManual_ch2Level;
    if (!this.thManualOption)
    {
      AutoThresholder autoth = new AutoThresholder();

      ipCh1Mask.setMask(ipMask);
      int[] ch1_hist = ipCh1Mask.getHistogram();
      th_ch1 = autoth.getThreshold(method, ch1_hist);

      ipCh2Mask.setMask(ipMask);
      int[] ch2_hist = ipCh2Mask.getHistogram();
      th_ch2 = autoth.getThreshold(method, ch2_hist);
    }

    ipCh1Mask.threshold(th_ch1);
    ipCh2Mask.threshold(th_ch2);

    if (this.edgeOption) {
      if (hasRoi) {
        fs.excludeEdgesRoi(roiSelection, ipMask, ipCh1Mask);
        fs.excludeEdgesRoi(roiSelection, ipMask, ipCh2Mask);
      }
      else {
        fs.excludeEdges(roi, ipMask, ipCh1Mask);
        fs.excludeEdges(roi, ipMask, ipCh2Mask);
      }
    }

    ResultsTable summary = Analyzer.getResultsTable();
    if (summary == null) {
      summary = new ResultsTable();
      Analyzer.setResultsTable(summary);
    }

    ResultsTable rTable = new ResultsTable();

    ipOverlaps.copyBits(ipCh1Mask, 0, 0, 0);
    ipOverlaps.copyBits(ipCh2Mask, 0, 0, 9);

    ImageProcessor ipFlood = ipOverlaps.duplicate();
    List<ImageProcessor> overlaps = new ArrayList();
    List<Rectangle> overlapsRect = new ArrayList();

    int overlapCount = fs.clustersProcessing(name, true, rTable, cal, ipFlood, ipOverlaps, overlaps, overlapsRect);

    int ch1Overlaps = fs.ch2ClusterOverlaps(ipCh2Mask, ipCh1Mask);
    int ch2Overlaps = fs.ch2ClusterOverlaps(ipCh1Mask, ipCh2Mask);

    ImageProcessor ipCh1Flood = ipCh1Mask.duplicate();
    List<ImageProcessor> ch1Clusters = new ArrayList();
    List<Rectangle> ch1ClustersRect = new ArrayList();

    int ch1ClusterCount = fs.clustersProcessing(cal, rTable, ipCh1Flood, ipCh1, ch1Clusters, ch1ClustersRect);

    ImageProcessor ipCh2Flood = ipCh2Mask.duplicate();
    List<ImageProcessor> ch2Clusters = new ArrayList();
    List<Rectangle> ch2ClustersRect = new ArrayList();

    int ch2ClusterCount = fs.clustersProcessing(cal, rTable, ipCh2Flood, ipCh2, ch2Clusters, ch2ClustersRect);

    if ((ch1ClusterCount == 0) || (ch2ClusterCount == 0)) {
      IJ.error("Zero Clusters. Choose another color");
      return;
    }

    ImageProcessor ipCh1FloodCopy = ipCh1Mask.duplicate();
    ImageProcessor ipCh2FloodCopy = ipCh2Mask.duplicate();
    fs.setClustersOverlay(im, ipCh1FloodCopy, ipCh2FloodCopy);

    ipCh1.setMask(ipCh1Mask);
    ImageStatistics ch1Stats = ipCh1.getStatistics();
    double aCh1Pixels = (double) ch1Stats.pixelCount * calConvert;

    ipCh2.setMask(ipCh2Mask);
    ImageStatistics ch2Stats = ipCh2.getStatistics();
    double aCh2Pixels = (double) ch2Stats.pixelCount * calConvert;

    ipCh1.setMask(ipOverlaps);
    ImageStatistics overlapStats = ipCh1.getStatistics();
    double aOverlapPixels = (double) overlapStats.pixelCount * calConvert;

    int ch1SumIntensity = fs.sumIntensities(ipCh1);
    int ch2SumIntensity = fs.sumIntensities(ipCh2);
    int ch1SumIntensityTh = fs.sumIntensitiesMask(ipCh1, ipCh1Mask);
    int ch2SumIntensityTh = fs.sumIntensitiesMask(ipCh2, ipCh2Mask);

    double ch1Percentage = (double) ch1Overlaps / (double) ch1ClusterCount;
    double ch2Percentage = (double) ch2Overlaps / (double) ch2ClusterCount;

    int[] ch1OverlapsStoich = fs.clusterStoichiometry(ipCh2Mask, ipCh1Mask);
    int[] ch2OverlapsStoich = fs.clusterStoichiometry(ipCh1Mask, ipCh2Mask);

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

    double[] ch2ClustersProbs = new double[ch2Clusters.size()];
    Arrays.fill(ch2ClustersProbs, 0.0);
    double countForPvalCh2 = 0.0;

    double[] ch1ClustersProbs = new double[ch1Clusters.size()];
    Arrays.fill(ch1ClustersProbs, 0.0);
    double countForPvalCh1 = 0.0;

    for (int i = 0; i < 50; i++)
    {
      IJ.showProgress(i, 50 + nMaxSimulations);

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
      ImageProcessor ipCh1Random2;
      if (this.moveCh1Clusters) {
        ipCh1Random2 = fs.simRandomProb(ipMask, minX, maxX, minY, maxY, ipCh2RandomMask, ch1ClustersProbs, ch1Clusters, ch1ClustersRect);
      }
      else {
        ipCh1Random2 = ipCh1.duplicate();
      }

      ImageProcessor ipCh1RandomMask2 = ipCh1Random2.duplicate();
      ipCh1RandomMask2.threshold(th_ch1);

      int ch2RandomOverlaps = fs.ch2ClusterOverlaps(ipCh1RandomMask, ipCh2RandomMask);
      int ch1RandomOverlaps = fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask2);

      double percOverlapsCh2 = (double) ch2RandomOverlaps / (double) ch2Clusters.size();
      double percOverlapsCh1 = (double) ch1RandomOverlaps / (double) ch1Clusters.size();

      if (percOverlapsCh2 >= ch2Percentage) {
        countForPvalCh2 += 1.0;
      }
      if (percOverlapsCh1 >= ch1Percentage) {
        countForPvalCh1 += 1.0;
      }
    }

    double pValCh1Ch2 = countForPvalCh2 / 50.0;
    double[] ch2ClustersProbsTest = fs.prob(ch2ClustersProbs, 50);
    double IFCh1Ch2 = 0.0;
    IFCh1Ch2 = fs.calcIF(ch2ClustersProbsTest, ch2Percentage);

    double pValCh2Ch1 = countForPvalCh1 / 50.0;
    double[] ch1ClustersProbsTest = fs.prob(ch1ClustersProbs, 50);
    double IFCh2Ch1 = 0.0;
    IFCh2Ch1 = fs.calcIF(ch1ClustersProbsTest, ch1Percentage);

    summary.incrementCounter();
    summary.addValue("Image", name);
    summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);
    summary.addValue(this.channelsAbb[this.ch1Color] + " Sim", "None");
    summary.addValue(this.channelsAbb[this.ch2Color] + " Sim", "None");

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

    summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " IF", IFCh1Ch2);
    summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " p-val", pValStrCh1Ch2);
    if (this.moveCh1Clusters) {
      summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " IF", IFCh2Ch1);
      summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " p-val", pValStrCh2Ch1);
    }
    else {
      summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " IF", "NT");
      summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " p-val", "NT");
    }

    summary.addValue("Th Algorithm", thMethods[this.thMethodInt]);
    summary.addValue(this.channelsAbb[this.ch1Color] + " Th", th_ch1);
    summary.addValue(this.channelsAbb[this.ch2Color] + " Th", th_ch2);
    summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Count", ch1ClusterCount);
    summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Count", ch2ClusterCount);

    if (this.areaOption) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Area", aCh1Pixels);
      summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Area", aCh2Pixels);
    }
    if (this.areaRoiOption) {
      summary.addValue("ROI Area", aRoi);
    }
    if (this.sumIntThOption) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Sum Inten", ch1SumIntensityTh);
      summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Sum Inten", ch2SumIntensityTh);
    }
    if (this.meanIntThOption) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Mean Inten", ch1MeanInt);
      summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Mean Inten", ch2MeanInt);
    }
    if (this.overlapsOpt) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Overlaps", ch1Overlaps);
      summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Overlaps", ch2Overlaps);
    }
    if (this.ch1StoiOption)
    {
      summary.addValue(this.channelsAbb[this.ch1Color] + "1:1", String.format("%.1f", (double) ch1OverlapsStoich[0] / (double) ch1Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch1Color] + "1:2", String.format("%.1f", (double) ch1OverlapsStoich[1] / (double) ch1Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch1Color] + "1:3", String.format("%.1f", (double) ch1OverlapsStoich[2] / (double) ch1Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch1Color] + "1:>3", String.format("%.1f", (double) ch1OverlapsStoich[3] / (double) ch1Overlaps * 100.0));
    }
    if (this.ch2StoiOption)
    {
      summary.addValue(this.channelsAbb[this.ch2Color] + "1:1", String.format("%.1f", (double) ch2OverlapsStoich[0] / (double) ch2Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch2Color] + "1:2", String.format("%.1f", (double) ch2OverlapsStoich[1] / (double) ch2Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch2Color] + "1:3", String.format("%.1f", (double) ch2OverlapsStoich[2] / (double) ch2Overlaps * 100.0));
      summary.addValue(this.channelsAbb[this.ch2Color] + "1:>3", String.format("%.1f", (double) ch2OverlapsStoich[3] / (double) ch2Overlaps * 100.0));
    }
    if (this.overlapsPercOpt) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " %Clus Overlaps", String.format("%.1f", ch1Percentage * 100.0));
      summary.addValue(this.channelsAbb[this.ch2Color] + " %Clus Overlaps", String.format("%.1f", ch2Percentage * 100.0));
    }
    if (this.overlapsCountOpt) {
      summary.addValue("Overlap Count", overlapCount);
    }
    if (this.overlapsAreaOpt) {
      summary.addValue("Overlap Area", aOverlapPixels);
    }
    if (this.sumIntOption) {
      summary.addValue(this.channelsAbb[this.ch1Color] + " Sum Inten", ch1SumIntensity);
      summary.addValue(this.channelsAbb[this.ch2Color] + " Sum Inten", ch2SumIntensity);
    }
    if (this.ch1MaskOption) {
      ImagePlus ch1Im = new ImagePlus(name + this.channelsAbb[this.ch1Color] + " Mask", ipCh1Mask);
      ch1Im.setCalibration(cal);
      ch1Im.show();
    }
    if (this.ch2MaskOption) {
      ImagePlus ch2Im = new ImagePlus(name + channels[this.ch2Color] + " Mask", ipCh2Mask);
      ch2Im.setCalibration(cal);
      ch2Im.show();
    }
    if (this.roiMaskOption) {
      ImagePlus roiIm = new ImagePlus(name + " ROI Mask", ipMask);
      roiIm.setCalibration(cal);
      roiIm.show();
    }
    if (this.overlapMaskOption) {
      ImagePlus overlapIm = new ImagePlus(name + " Overlap Mask", ipOverlaps);
      overlapIm.setCalibration(cal);
      overlapIm.show();
    }

    if (nMaxSimulations > 0)
    {
      ImageProcessor ipCh2Random = ipCh2.duplicate();
      ImageProcessor ipCh1Random = ipCh1.duplicate();

      for (int i = 0; i < nMaxSimulations; i++) {
        IJ.showProgress(i + 50, 50 + nMaxSimulations);
        String nSimulation = Integer.toString(i + 1);
        if (this.ch1SimParam.equals("Random")) {
          ipCh1Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch1Clusters, ch1ClustersRect);
        }
        if (this.ch2SimParam.equals("Random")) {
          this.interFactorCh2 = 0.0;
          ipCh2Random = fs.simRandom(ipMask, minX, maxX, minY, maxY, ch2Clusters, ch2ClustersRect);
        }
        else if (this.ch2SimParam.equals("NonRandom")) {
          ipCh2Random = fs.simNonRandom(ipMask, minX, maxX, minY, maxY, ipCh1Random, ch2Clusters, ch2ClustersRect, this.interFactorCh2, th_ch1);
        }
        if ((ipCh2Random == null) || (ipCh1Random == null)) {
          IJ.error("Error with Simulation 2"); return;
        }

        byte[] blueRandom;
        byte[] greenRandom;
        byte[] redRandom;

        if ((this.ch1Color == 0) && (this.ch2Color == 1)) {
          redRandom = (byte[])ipCh1Random.getPixels();
          greenRandom = (byte[])ipCh2Random.getPixels();
          blueRandom = ch3;
        } else {
          if ((this.ch1Color == 1) && (this.ch2Color == 0)) {
            greenRandom = (byte[])ipCh1Random.getPixels();
            redRandom = (byte[])ipCh2Random.getPixels();
            blueRandom = ch3;
          } else {
            if ((this.ch1Color == 2) && (this.ch2Color == 0)) {
              blueRandom = (byte[])ipCh1Random.getPixels();
              redRandom = (byte[])ipCh2Random.getPixels();
              greenRandom = ch3;
            } else {
              if ((this.ch1Color == 0) && (this.ch2Color == 2)) {
                redRandom = (byte[])ipCh1Random.getPixels();
                blueRandom = (byte[])ipCh2Random.getPixels();
                greenRandom = ch3;
              } else {
                if ((this.ch1Color == 2) && (this.ch2Color == 1)) {
                  blueRandom = (byte[])ipCh1Random.getPixels();
                  greenRandom = (byte[])ipCh2Random.getPixels();
                  redRandom = ch3;
                }
                else {
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
        double interFactorCh2Int = this.interFactorCh2 * 100.0;

        if (this.showSimsOption)
        {
          ImagePlus colorRandIm = new ImagePlus(name + "_Sim_IF_" + Integer.toString((int)interFactorCh2Int) + "_" + nSimulation, ipSimulation);
          colorRandIm.setCalibration(cal);
          colorRandIm.show();
        }

        ImageProcessor ipCh2RandomMask = ipCh2Random.duplicate();
        ipCh2RandomMask.threshold(th_ch2);

        ImageProcessor ipCh1RandomMask = ipCh1Random.duplicate();
        ipCh1RandomMask.threshold(th_ch1);

        ImageProcessor ipOverlapsRandom = new ByteProcessor(M, N);
        ipOverlapsRandom.copyBits(ipCh1RandomMask, 0, 0, 0);
        ipOverlapsRandom.copyBits(ipCh2RandomMask, 0, 0, 9);

        ImageProcessor ipOverlapFlood = ipOverlapsRandom.duplicate();
        List<ImageProcessor> oClustersRandom = new ArrayList();
        List<Rectangle> oClustersRectRandom = new ArrayList();

        int oRandomCount = fs.clustersProcessing(name + "_Sim_" + nSimulation, true, rTable, cal, ipOverlapFlood, ipOverlapsRandom, oClustersRandom, oClustersRectRandom);
        int ch1RandomOverlaps = fs.ch2ClusterOverlaps(ipCh2RandomMask, ipCh1RandomMask);
        int ch2RandomOverlaps = fs.ch2ClusterOverlaps(ipCh1RandomMask, ipCh2RandomMask);

        int[] ch1RandomOverlapsStoich = fs.clusterStoichiometry(ipCh2RandomMask, ipCh1RandomMask);
        int[] ch2RandomOverlapsStoich = fs.clusterStoichiometry(ipCh1RandomMask, ipCh2RandomMask);

        double ch1RandomPercentage = (double) ch1RandomOverlaps / (double) ch1ClusterCount;
        double ch2RandomPercentage = (double) ch2RandomOverlaps / (double) ch2ClusterCount;

        ipCh1Random.setMask(ipOverlapsRandom);
        ImageStatistics overlapRandomStats = ipCh1Random.getStatistics();
        double aOverlapRandomPixels = (double) overlapRandomStats.pixelCount * calConvert;

        if (this.ch1MaskOption) {
          ImagePlus ch1RandomIm = new ImagePlus(name + " Sim" + nSimulation + this.channelsAbb[this.ch1Color] + " Mask", ipCh1RandomMask);
          ch1RandomIm.setCalibration(cal);
          ch1RandomIm.show();
        }
        if (this.ch2MaskOption) {
          ImagePlus ch2RandomIm = new ImagePlus(name + " Sim" + nSimulation + this.channelsAbb[this.ch2Color] + " Mask", ipCh2RandomMask);
          ch2RandomIm.setCalibration(cal);
          ch2RandomIm.show();
        }
        if (this.overlapMaskOption) {
          ImagePlus overlapRandomIm = new ImagePlus(name + " Sim" + nSimulation + " Overlap Mask", ipOverlapsRandom);
          overlapRandomIm.setCalibration(cal);
          overlapRandomIm.show();
        }

        summary.incrementCounter();
        summary.addValue("Image", name + "_Sim_" + nSimulation);
        summary.addValue("Scale", Double.toString(pixelHeight) + " " + unit);

        if (this.ch1SimParam.equals("None")) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Sim", "None");
        }
        else {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Sim", "Random");
        }
        if (this.ch2SimParam.equals("Random")) {
          summary.addValue(this.channelsAbb[this.ch2Color] + " Sim", "Random");
          summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " IF", 0.0);
          summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " p-val", "NT");
        }
        else {
          summary.addValue(this.channelsAbb[this.ch2Color] + " Sim", "NonRandom");
          summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " IF", this.interFactorCh2);
          summary.addValue(this.channelsAbb[this.ch1Color] + "-" + this.channelsAbb[this.ch2Color] + " p-val", "NT");
        }

        summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " IF", "NT");
        summary.addValue(this.channelsAbb[this.ch2Color] + "-" + this.channelsAbb[this.ch1Color] + " p-val", "NT");
        summary.addValue("Th Algorithm", thMethods[this.thMethodInt]);
        summary.addValue(this.channelsAbb[this.ch1Color] + " Th", th_ch1);
        summary.addValue(this.channelsAbb[this.ch2Color] + " Th", th_ch2);
        summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Count", ch1ClusterCount);
        summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Count", ch2ClusterCount);

        if (this.areaOption) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Area", aCh1Pixels);
          summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Area", aCh2Pixels);
        }
        if (this.areaRoiOption) {
          summary.addValue("ROI area", aRoi);
        }
        if (this.sumIntThOption) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Sum Inten", ch1SumIntensityTh);
          summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Sum Inten", ch2SumIntensityTh);
        }
        if (this.meanIntThOption) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Mean Inten", ch1MeanInt);
          summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Mean Inten", ch2MeanInt);
        }
        if (this.overlapsOpt) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Clus Overlaps", ch1RandomOverlaps);
          summary.addValue(this.channelsAbb[this.ch2Color] + " Clus Overlaps", ch2RandomOverlaps);
        }
        if (this.ch1StoiOption)
        {
          summary.addValue(this.channelsAbb[this.ch1Color] + "1:1", String.format("%.1f", ch1RandomOverlapsStoich[0] / ch1RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch1Color] + "1:2", String.format("%.1f", ch1RandomOverlapsStoich[1] / ch1RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch1Color] + "1:3", String.format("%.1f", ch1RandomOverlapsStoich[2] / ch1RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch1Color] + "1:>3", String.format("%.1f", ch1RandomOverlapsStoich[3] / ch1RandomOverlaps * 100.0));
        }
        if (this.ch2StoiOption)
        {
          summary.addValue(this.channelsAbb[this.ch2Color] + "1:1", String.format("%.1f", ch2RandomOverlapsStoich[0] / ch2RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch2Color] + "1:2", String.format("%.1f", ch2RandomOverlapsStoich[1] / ch2RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch2Color] + "1:3", String.format("%.1f", ch2RandomOverlapsStoich[2] / ch2RandomOverlaps * 100.0));
          summary.addValue(this.channelsAbb[this.ch2Color] + "1:>3", String.format("%.1f", ch2RandomOverlapsStoich[3] / ch2RandomOverlaps * 100.0));
        }
        if (this.overlapsPercOpt) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " %Clus Overlaps", String.format("%.1f", ch1RandomPercentage * 100.0));
          summary.addValue(this.channelsAbb[this.ch2Color] + " %Clus Overlaps", String.format("%.1f", ch2RandomPercentage * 100.0));
        }
        if (this.overlapsCountOpt) {
          summary.addValue("Overlap Count", oRandomCount);
        }
        if (this.overlapsAreaOpt) {
          summary.addValue("Overlap Area", aOverlapRandomPixels);
        }
        if (this.sumIntOption) {
          summary.addValue(this.channelsAbb[this.ch1Color] + " Sum Inten", "None");
          summary.addValue(this.channelsAbb[this.ch2Color] + " Sum Inten", "None");
        }
      }
    }

    summary.show("Results");
    if (this.overlapLocations) {
      rTable.show("Overlap Locations");
    }
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
