package interactionFactor;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IfFunctions
{
  void excludeEdgesRoi(Roi roi, ImageProcessor maskROI, ImageProcessor channelMask)
  {
    channelMask.setValue(255.0);
    roi.drawPixels(channelMask);
    
    FloodFiller flood = new FloodFiller(channelMask);
    Polygon pol = roi.getPolygon();
    
    for (int i = 0; i < pol.xpoints.length; i++) {
      int x = pol.xpoints[i];
      int y = pol.ypoints[i];
      int pixel = channelMask.getPixel(x, y);
      if (pixel != 100) {
        channelMask.setValue(100.0D);
        flood.fill8(x, y);
      }
    }

    int width = channelMask.getWidth();
    int height = channelMask.getHeight();
    
    for (int u = 0; u < width; u++) {
      for (int v = 0; v < height; v++) {
        int pixel = channelMask.getPixel(u, v);
        if (pixel == 100) {
          channelMask.putPixel(u, v, 0);
          maskROI.putPixel(u, v, 0);
        }
      }
    }
  }
  
  void excludeEdges(Rectangle roiRect, ImageProcessor maskROI, ImageProcessor channelMask)
  {
    int rLeft = roiRect.x;
    int rTop = roiRect.y;
    int rRight = rLeft + roiRect.width;
    int rBottom = rTop + roiRect.height;
    
    for (int u = rLeft; u < rLeft + 1; u++) {
      for (int v = rTop; v < rBottom; v++) {
        channelMask.putPixel(u, v, 255);
      }
    }
    

    for (int u = rRight - 1; u < rRight; u++) {
      for (int v = rTop; v < rBottom; v++) {
        channelMask.putPixel(u, v, 255);
      }
    }
    for (int u = rLeft; u < rRight; u++) {
      for (int v = rTop; v < rTop + 1; v++) {
        channelMask.putPixel(u, v, 255);
      }
    }
    for (int u = rLeft; u < rRight; u++) {
      for (int v = rBottom - 1; v < rBottom; v++) {
        channelMask.putPixel(u, v, 255);
      }
    }
    channelMask.setValue(100.0);
    FloodFiller flood = new FloodFiller(channelMask);
    flood.fill8(rLeft, rTop);
    
    for (int u = 0; u < roiRect.width; u++) {
      for (int v = 0; v < roiRect.height; v++) {
        int pixel = channelMask.getPixel(u, v);
        if (pixel == 100) {
          channelMask.putPixel(u, v, 0);
          maskROI.putPixel(u, v, 0);
        }
      }
    }
  }

  ImageProcessor simRandomProb(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY, ImageProcessor ipCh1Random, double[] ch2ClustersProbs, List<ImageProcessor> ch2Clusters, List<Rectangle> ch2ClustersRect)
  {
    int M = roiMask.getWidth();
    int N = roiMask.getHeight();
    ImageProcessor ipSimulation = new ByteProcessor(M, N);

    for (int i = 0; i < ch2Clusters.size(); i++)
    {

      Rectangle clusterRect = ch2ClustersRect.get(i);
      ImageProcessor cluster = ch2Clusters.get(i);
      int max = 100000;
      int randomIter = 0;

      while (randomIter < max) {
        int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width);
        int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height);
        int randomRight = randomLeft + clusterRect.width;
        int randomBottom = randomTop + clusterRect.height;
        
        boolean overlapSelf = true;
        double overlapOther = 0.0;
        int surrounding_pixels = 0;
        Outer:
        for (int v = randomTop; v < randomBottom; v++) {
          for (int u = randomLeft; u < randomRight; u++) {
            if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
              if (roiMask.getPixel(u, v) != 255) {
                overlapSelf = true;
                break Outer;
              }
              surrounding_pixels += ipSimulation.getPixel(u, v);

              if (u + 1 < maximumX) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v);
              }
              if (u - 1 > minimumX) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v);
              }
              if (v + 1 < maximumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v + 1);
              }
              if (v - 1 > minimumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v - 1);
              }
              if ((u + 1 < maximumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v + 1);
              }
              if ((u - 1 > minimumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v - 1);
              }
              if ((u - 1 > minimumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v + 1);
              }
              if ((u + 1 < maximumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v - 1);
              }
              if (surrounding_pixels == 0) {
                overlapSelf = false;
              } else {
                overlapSelf = true;
                break Outer;
              }
            }
          }
        }

        if (!overlapSelf) {
          for (int v = randomTop; v < randomBottom; v++) {
            for (int u = randomLeft; u < randomRight; u++) {
              if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                int p = cluster.getPixel(u - randomLeft, v - randomTop);
                ipSimulation.putPixel(u, v, p);
              }
            }
          }
          Outer: for (int v = randomTop; v < randomBottom; v++) {
            for (int u = randomLeft; u < randomRight; u++) {
              if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                int pOther = ipCh1Random.getPixel(u, v);
                if (pOther > 0) {
                  overlapOther = 1;
                  break Outer;
                }
              }
            }
          }

          ch2ClustersProbs[i] += overlapOther;
          randomIter = max;
        }
        randomIter++;
      }
    }
    return ipSimulation;
  }

  ImageProcessor simNonRandom(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY, ImageProcessor ipCh1Random, List<ImageProcessor> ch2Clusters, List<Rectangle> ch2ClustersRect, double interFactor, int th_other)
  {
    int M = roiMask.getWidth();
    int N = roiMask.getHeight();
    ImageProcessor ipSimulation = new ByteProcessor(M, N);
    
    for (int i = 0; i < ch2Clusters.size(); i++)
    {
      Rectangle clusterRect = ch2ClustersRect.get(i);
      ImageProcessor cluster = ch2Clusters.get(i);
      int max = 100000;
      int randomIter = 0;
      
      while (randomIter < max) {
        int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width);
        int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height);
        int randomRight = randomLeft + clusterRect.width;
        int randomBottom = randomTop + clusterRect.height;
        
        boolean overlapSelf = true;
        boolean overlapOther = false;
        int surrounding_pixels = 0;
        Outer:
        for (int v = randomTop; v < randomBottom; v++) {
          for (int u = randomLeft; u < randomRight; u++) {
            if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
              if (roiMask.getPixel(u, v) != 255) {
                overlapSelf = true;
                break Outer;
              }
              surrounding_pixels += ipSimulation.getPixel(u, v);

              if (u + 1 < maximumX) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v);
              }
              if (u - 1 > minimumX) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v);
              }
              if (v + 1 < maximumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v + 1);
              }
              if (v - 1 > minimumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v - 1);
              }
              if ((u + 1 < maximumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v + 1);
              }
              if ((u - 1 > minimumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v - 1);
              }
              if ((u - 1 > minimumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v + 1);
              }
              if ((u + 1 < maximumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v - 1);
              }

              if (surrounding_pixels == 0) {
                overlapSelf = false;
              } else {
                overlapSelf = true;
                break Outer;
              }
            }
          }
        }
        if (!overlapSelf) {
          Outer:
          for (int v = randomTop; v < randomBottom; v++) {
            for (int u = randomLeft; u < randomRight; u++) {
              if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                int pOther = ipCh1Random.getPixel(u, v);
                if (pOther > th_other) {
                  overlapOther = true;
                  break Outer;
                }
                overlapOther = false;
              }
            }
          }
        }
        if ((!overlapSelf) && overlapOther)
        {
          for (int v = randomTop; v < randomBottom; v++) {
            for (int u = randomLeft; u < randomRight; u++) {
              if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                int p = cluster.getPixel(u - randomLeft, v - randomTop);
                ipSimulation.putPixel(u, v, p);
              }
            }
          }
          randomIter = max;
        }
        if ((!overlapSelf) && (!overlapOther)) {
          double random = Math.random();
          if (random > interFactor)
          {
            for (int v = randomTop; v < randomBottom; v++) {
              for (int u = randomLeft; u < randomRight; u++) {
                if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                  int p = cluster.getPixel(u - randomLeft, v - randomTop);
                  ipSimulation.putPixel(u, v, p);
                }
              }
            }
            randomIter = max;
          }
        }
        randomIter++;
      }
    }
    return ipSimulation;
  }

  ImageProcessor simRandom(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY, List<ImageProcessor> clusters, List<Rectangle> clustersRect)
  {
    int M = roiMask.getWidth();
    int N = roiMask.getHeight();
    
    ImageProcessor ipSimulation = new ByteProcessor(M, N);

    for (int i = 0; i < clusters.size(); i++)
    {
      Rectangle clusterRect = clustersRect.get(i);
      ImageProcessor cluster = clusters.get(i);
      int max = 100000;
      int randomIter = 0;

      while (randomIter < max)
      {
        int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width);
        int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height);
        int randomRight = randomLeft + clusterRect.width;
        int randomBottom = randomTop + clusterRect.height;
        
        boolean overlapSelf = true;
        int surrounding_pixels = 0;
        Outer:
        for (int v = randomTop; v < randomBottom; v++) {
          for (int u = randomLeft; u < randomRight; u++) {
            if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
              if (roiMask.getPixel(u, v) != 255) {
                overlapSelf = true;
                break Outer;
              }
              surrounding_pixels += ipSimulation.getPixel(u, v);

              if (u + 1 < maximumX) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v);
              }
              if (u - 1 > minimumX) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v);
              }
              if (v + 1 < maximumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v + 1);
              }
              if (v - 1 > minimumY) {
                surrounding_pixels += ipSimulation.getPixel(u, v - 1);
              }
              if ((u + 1 < maximumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v + 1);
              }
              if ((u - 1 > minimumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v - 1);
              }
              if ((u - 1 > minimumX) && (v + 1 < maximumY)) {
                surrounding_pixels += ipSimulation.getPixel(u - 1, v + 1);
              }
              if ((u + 1 < maximumX) && (v - 1 > minimumY)) {
                surrounding_pixels += ipSimulation.getPixel(u + 1, v - 1);
              }
              if (surrounding_pixels == 0) {
                overlapSelf = false;
              } else {
                overlapSelf = true;
                break Outer;
              }
            }
          }
        }

        if (!overlapSelf)
        {
          for (int v = randomTop; v < randomBottom; v++) {
            for (int u = randomLeft; u < randomRight; u++) {
              if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
                int p = cluster.getPixel(u - randomLeft, v - randomTop);
                ipSimulation.putPixel(u, v, p);
              }
            }
          }
          randomIter = max;
        }
        randomIter++;
      }
    }
    return ipSimulation;
  }
  
  int sumIntensitiesMask(ImageProcessor channel, ImageProcessor mask) {
    int width = channel.getWidth();
    int height = channel.getHeight();
    
    int sum = 0;
    
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (mask.getPixel(x, y) > 0) {
          int pixel = channel.getPixel(x, y);
          sum += pixel;
        }
      }
    }
    return sum;
  }
  
  int sumIntensities(ImageProcessor channel) {
    int width = channel.getWidth();
    int height = channel.getHeight();
    int sum = 0;
    
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (channel.getPixel(x, y) > 0) {
          int pixel = channel.getPixel(x, y);
          sum += pixel;
        }
      }
    }
    return sum;
  }
  
  int overlapCount(ImageProcessor chMask, ImageProcessor chMaskCounter) {
    ImageProcessor chMaskFlood = chMask.duplicate();
    Wand wand = new Wand(chMaskFlood);
    int chMaskOverlapCount = 0;
    
    int M = chMaskFlood.getWidth();
    int N = chMaskFlood.getHeight();
    
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = chMaskFlood.get(u, v);
        if (p == 255) {
          wand.autoOutline(u, v, 255.0D, 255.0D, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
          
          chMaskFlood.setRoi(roi_par);
          chMaskFlood.setValue(200.0D);
          chMaskFlood.fill(roi_par);
          chMaskCounter.setRoi(roi_par);
          ImageStatistics stats = chMaskCounter.getStatistics();
          if (stats.max > 0.0D) {
            chMaskOverlapCount++;
          }
        }
      }
    }
    return chMaskOverlapCount;
  }
  double[] prob(double[] ch2ClustersProbs, int nSimulations) {
    double[] ch2ClustersProbs2 = new double[ch2ClustersProbs.length];
    for (int i = 0; i < ch2ClustersProbs.length; i++) {
      ch2ClustersProbs2[i] = ch2ClustersProbs[i] / (double) nSimulations;
    }

    return ch2ClustersProbs2;
  }

  double functIfProb(double[] ch2ClustersProbs, double IF) {
    double p = 0;
    if (IF == 1) {
      p = 1;
    } else {
      for (int i = 0; i < ch2ClustersProbs.length; i++) {
        double p0 = ch2ClustersProbs[i];
        double pK = p0 / (1 - (1 - p0) * IF);
        p += pK;
      }
      p = p / ch2ClustersProbs.length;
    }
    return p;
  }

  double calcIF(double[] ch2ClustersProbs, double origM) {
    double minIF = 0;
    double IF = 0;
    double diff = 0;
    // create array of ranges of IFs
    double[] array = new double[100];
    double k = 0;
    int i = 0;
    while (i < array.length) {
      array[i] = k;
      k = k + 0.01;
      i++;
    }
    for (int c = 0; c < array.length; c++) {
      double curM = functIfProb(ch2ClustersProbs, array[c]);
      double curDiff = Math.abs(curM - origM);
      if ((array[c] == minIF) || (curDiff < diff)) {
        IF = array[c];
        diff = curDiff;
      }
    }
    return IF;
  }

  int ch2ClusterOverlaps(ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask)
  {
    int M = ipCh1Mask.getWidth();
    int N = ipCh1Mask.getHeight();
    
    ImageProcessor ipFlood = ipCh2Mask.duplicate();
    Wand wand = new Wand(ipFlood);
    
    int count = 0;

    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int pCh2 = ipFlood.get(u, v);
        int pCh1 = ipCh1Mask.get(u, v);
        if ((pCh1 == 255) && (pCh2 == 255)) {
          wand.autoOutline(u, v, 255.0, 255.0, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
          ipFlood.setRoi(roi_par);
          ipFlood.setValue(200.0);
          ipFlood.fill(roi_par);
          count++;
        }
      }
    }
    return count;
  }
  
  int[] clusterStoichiometry(ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask) {
    ImageProcessor ipCh1MaskTest = ipCh1Mask.duplicate();
    int[] clusterStoich = new int[4];
    
    int oneToOne = 0;
    int oneToTwo = 0;
    int oneToThree = 0;
    int oneToMore = 0;
    
    int M = ipCh1Mask.getWidth();
    int N = ipCh1Mask.getHeight();

    ShortProcessor ch1MaskLabeled = new ShortProcessor(M, N);
    
    Wand wand1 = new Wand(ipCh1MaskTest);
    int ch1Clusters = 1;
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int pCh1 = ipCh1MaskTest.get(u, v);
        int pCh1MaskLabeled = ch1MaskLabeled.get(u, v);
        if (((pCh1 == 255 ? 1 : 0) & (pCh1MaskLabeled == 0 ? 1 : 0)) != 0)
        {
          wand1.autoOutline(u, v, 255, 255);
          PolygonRoi roi_par = new PolygonRoi(wand1.xpoints, wand1.ypoints, wand1.npoints, 2);

          ch1MaskLabeled.setRoi(roi_par);
          ch1MaskLabeled.setValue(ch1Clusters);
          ch1MaskLabeled.fill(roi_par);
          
          ipCh1MaskTest.setRoi(roi_par);
          ipCh1MaskTest.setValue(200.0);
          ipCh1MaskTest.fill(roi_par);
          ch1Clusters++;
        }
      }
    }

    ImageProcessor ipFloodCh2 = ipCh2Mask.duplicate();
    Wand wand = new Wand(ipFloodCh2);
    
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int pCh2 = ipFloodCh2.get(u, v);
        if (pCh2 == 255)
        {
          wand.autoOutline(u, v, 255.0, 255.0, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);

          ch1MaskLabeled.setRoi(roi_par);
          ImageStatistics stats = ch1MaskLabeled.getStatistics();
          double maximumVal = stats.max;
          if (maximumVal > 0.0) {
            int[] histogram = stats.histogram16;
            int countCluster = 0;
            
            for (int in = 1; in < histogram.length; in++) {
              int values = histogram[in];
              if (values > 0) {
                countCluster++;
              }
            }
            ipFloodCh2.setRoi(roi_par);
            ipFloodCh2.setValue(100.0);
            ipFloodCh2.fill(roi_par);
            
            if (countCluster == 1) {
              oneToOne++;
            }
            else if (countCluster == 2) {
              oneToTwo++;
            }
            else if (countCluster == 3) {
              oneToThree++;
            }
            else if (countCluster > 3) {
              oneToMore++;
            }
          }
        }
      }
    }
    

    clusterStoich[0] = oneToOne;
    clusterStoich[1] = oneToTwo;
    clusterStoich[2] = oneToThree;
    clusterStoich[3] = oneToMore;
    return clusterStoich;
  }
  
  void setClustersOverlay(ImagePlus im, ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask)
  {
    Overlay ovCh = new Overlay();

    int M = ipCh1Mask.getWidth();
    int N = ipCh1Mask.getHeight();
    Wand wandCh1 = new Wand(ipCh1Mask);
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipCh1Mask.get(u, v);
        if (p == 255) {
          wandCh1.autoOutline(u, v, 255, 255);
          PolygonRoi roi_par = new PolygonRoi(wandCh1.xpoints, wandCh1.ypoints, wandCh1.npoints, 2);
          ovCh.add(roi_par);
        }
      }
    }
    
    Wand wandCh2 = new Wand(ipCh2Mask);
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipCh2Mask.get(u, v);
        if (p == 255) {
          wandCh2.autoOutline(u, v, 255, 255);
          PolygonRoi roi_par = new PolygonRoi(wandCh2.xpoints, wandCh2.ypoints, wandCh2.npoints, 2);
          ovCh.add(roi_par);
        }
      }
    }
    
    Color stColor = Color.WHITE;
    ovCh.setStrokeColor(stColor);
    im.setOverlay(ovCh);
  }
  
  Overlay returnOverlay(ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask)
  {
    Overlay ovCh = new Overlay();
    
    int M = ipCh1Mask.getWidth();
    int N = ipCh1Mask.getHeight();
    Wand wandCh1 = new Wand(ipCh1Mask);
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipCh1Mask.get(u, v);
        if (p == 255) {
          wandCh1.autoOutline(u, v, 255, 255);
          PolygonRoi roi_par = new PolygonRoi(wandCh1.xpoints, wandCh1.ypoints, wandCh1.npoints, 2);
          ovCh.addElement(roi_par);
        }
      }
    }
    Wand wandCh2 = new Wand(ipCh2Mask);
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipCh2Mask.get(u, v);
        if (p == 255) {
          wandCh2.autoOutline(u, v, 255, 255);
          PolygonRoi roi_par = new PolygonRoi(wandCh2.xpoints, wandCh2.ypoints, wandCh2.npoints, 2);
          ovCh.addElement(roi_par);
        }
      }
    }
    return ovCh;
  }

  int clustersProcessing(String imageName, boolean results, ResultsTable rt, Calibration calibration, ImageProcessor ipFlood, ImageProcessor channel, List<ImageProcessor> clusters, List<Rectangle> clustersRect)
  {
    double pixelHeight = calibration.pixelHeight;
    double pixelWidth = calibration.pixelWidth;
    double calConvert = pixelHeight * pixelWidth;
    Wand wand = new Wand(ipFlood);

    int count = 0;
    
    int M = ipFlood.getWidth();
    int N = ipFlood.getHeight();
    
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipFlood.get(u, v);
        if (p == 255)
        {
          wand.autoOutline(u, v, 255.0, 255.0, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
          ipFlood.setRoi(roi_par);
          
          ImageProcessor roi_parMask = roi_par.getMask();
          Rectangle region_r = roi_par.getBounds();

          int rLeft = region_r.x;
          int rTop = region_r.y;
          int rRight = rLeft + region_r.width;
          int rBottom = rTop + region_r.height;

          int num_nonzero = 0;
          for (int y = rTop; y < rBottom; y++)
          {
            for (int x = rLeft; x < rRight; x++) {
              if (roi_parMask.getPixel(x - rLeft, y - rTop) > 0)
              {
                int pixel = channel.getPixel(x, y);
                if (pixel > 0) num_nonzero += 1;
                roi_parMask.putPixel(x - rLeft, y - rTop, pixel);
              }
            }
          }
          
          if (num_nonzero > 0)
          {
            clusters.add(roi_parMask);
            clustersRect.add(region_r);
            ipFlood.setRoi(roi_par);
            ipFlood.setValue(200.0);
            ipFlood.fill(roi_par);
            count++;

            if (results) {
              ImageStatistics stats = ipFlood.getStatistics();
              rt.incrementCounter();
              rt.addValue("Image", imageName);
              rt.addValue("Number Pixels", stats.area);
              rt.addValue("Area", stats.area * calConvert);
              rt.addValue("CentroidX", stats.xCentroid * pixelWidth);
              rt.addValue("CentroidY", stats.yCentroid * pixelHeight);
            }
          }
        }
      }
    }
    return count;
  }
  

  int clustersProcessing(Calibration calibration, ResultsTable rt, ImageProcessor ipFlood, ImageProcessor channel, List<ImageProcessor> clusters, List<Rectangle> clustersRect)
  {
    int count = clustersProcessing("Nothing", false, rt, calibration, ipFlood, channel, clusters, clustersRect);
    return count;
  }
  
  int clustersProcessingSimple(ImageProcessor channelMask)
  {
    ImageProcessor ipFlood = channelMask.duplicate();
    
    Wand wand = new Wand(ipFlood);
    
    int count = 0;
    int M = ipFlood.getWidth();
    int N = ipFlood.getHeight();
    
    for (int u = 0; u < M; u++) {
      for (int v = 0; v < N; v++) {
        int p = ipFlood.get(u, v);
        if (p == 255) {
          wand.autoOutline(u, v, 255.0, 255.0, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
          ipFlood.setRoi(roi_par);
          ipFlood.setValue(200.0);
          ipFlood.fill(roi_par);
          count++;
        }
      }
    }
    return count;
  }
  
  int excludeByClusterArea(double areaMin, Calibration calibration, ImageProcessor channelMask, ImageProcessor channel)
  {
    double pixelHeight = calibration.pixelHeight;
    double pixelWidth = calibration.pixelWidth;
    double calConvert = pixelHeight * pixelWidth;
    
    ImageProcessor ipFlood = channelMask.duplicate();
    Wand wand = new Wand(ipFlood);
    
    int count = 0;
    int remove_count = 0;
    int M = ipFlood.getWidth();
    int N = ipFlood.getHeight();
    
    for (int u = 0; u < M; u++)
    {
      for (int v = 0; v < N; v++)
      {
        int p = ipFlood.get(u, v);
        if (p == 255)
        {
          wand.autoOutline(u, v, 255.0, 255.0, 8);
          PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, 2);
          ipFlood.setRoi(roi_par);
          
          ImageProcessor roi_parMask = roi_par.getMask();
          Rectangle region_r = roi_par.getBounds();

          int rLeft = region_r.x;
          int rTop = region_r.y;
          int rRight = rLeft + region_r.width;
          int rBottom = rTop + region_r.height;
          int num_nonzero = 0;

          for (int y = rTop; y < rBottom; y++) {
            for (int x = rLeft; x < rRight; x++) {
              if (roi_parMask.getPixel(x - rLeft, y - rTop) > 0) {
                int pixel = channel.getPixel(x, y);
                if (pixel > 0) num_nonzero += 1;
                roi_parMask.putPixel(x - rLeft, y - rTop, pixel);
              }
            }
          }
          
          if (num_nonzero > 0)
          {
            ipFlood.setRoi(roi_par);
            ipFlood.setValue(200.0);
            ipFlood.fill(roi_par);
            count++;
            
            ImageStatistics stats = ipFlood.getStatistics();
            double clusterArea = stats.area * calConvert;

            if (clusterArea < areaMin)
            {
              channelMask.setRoi(roi_par);
              channelMask.setValue(0.0);
              channelMask.fill(roi_par);
              remove_count++;
            }
          }
        }
      }
    }
    return remove_count;
  }
}
