package interactionFactor;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.plugin.filter.Analyzer;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import ij.measure.Calibration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import ij.gui.Overlay;

/**
 * Created by keriabermudez on 10/27/16.
 */
public class IfFunctions {
	void printTest() {
		IJ.log("Test");
	}

	void excludeEdgesRoi(Roi roi, ImageProcessor maskROI, ImageProcessor channelMask) {

		channelMask.setValue(255);
		roi.drawPixels(channelMask);

		FloodFiller flood = new FloodFiller(channelMask);
		Polygon pol = roi.getPolygon();
		int x = pol.xpoints[0];
		int y = pol.ypoints[0];

		channelMask.setValue(100);
		flood.fill8(x, y);

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

	void excludeEdges(Rectangle roiRect, ImageProcessor maskROI, ImageProcessor channelMask) {

		int rLeft = roiRect.x;
		int rTop = roiRect.y;
		int rRight = rLeft + roiRect.width;
		int rBottom = rTop + roiRect.height;

		for (int u = rLeft; u < rLeft + 1; u++) {
			for (int v = rTop; v < rBottom; v++) {
				channelMask.putPixel(u, v, 255);
				/*
				 * int pixel = channelMask.getPixel(u, v); if (pixel == 255){
				 * flood.fill(u, v); }
				 */
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
		channelMask.setValue(100);
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

	ImageProcessor simRandomProb(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY,
			ImageProcessor ipCh1Random, double[] ch2ClustersProbs, List<ImageProcessor> ch2Clusters,
			List<Rectangle> ch2ClustersRect) {
		
		int M = roiMask.getWidth();
		int N = roiMask.getHeight();
		ImageProcessor ipSimulation = new ByteProcessor(M, N); // ip for ch2
																// mask
		for (int i = 0; i < ch2Clusters.size(); i++) {

			Rectangle clusterRect = ch2ClustersRect.get(i);// ip for ch2 mask
			ImageProcessor cluster = ch2Clusters.get(i);
			int max = 100000;
			int randomIter = 0;

			while (randomIter < max) {
				// int randomLeft = minimumX + (int)(Math.random() * maximumX-
				// clusterRect.width);
				int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width + 1);
				// int randomLeft = (int) ((float) Math.random() * (M -
				// clusterRect.width));
				int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height + 1);
				// int randomTop = (int) ((float) Math.random() * (N -
				// clusterRect.height));
				int randomRight = randomLeft + clusterRect.width;
				int randomBottom = randomTop + clusterRect.height;

				boolean overlapSelf = true;
				double overlapOther = 0;
				int surrounding_pixels = 0;
				Outer: 
				for (int v = randomTop; v < randomBottom; v++) {
					for (int u = randomLeft; u < randomRight; u++) {
						if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
							if (roiMask.getPixel(u, v) != 255) {
								overlapSelf = true;
								break Outer;
							}
							surrounding_pixels += ipSimulation.getPixel(u, v); // N
																				// is
																				// height
																				// ,
																				// M
																				// is
																				// width
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
							// checking surrounding pixels
							if (surrounding_pixels == 0) {
								overlapSelf = false;
							} else {
								overlapSelf = true;
								break Outer;
							}
						}
					}
				}
				if (overlapSelf == false) {
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
									break Outer;// this could be changed
								}
							}
						}
					}
					ch2ClustersProbs[i] += overlapOther;
					randomIter = max;
				} // random factor is 0
				randomIter++;

			}
		}

		return ipSimulation;
	}
	ImageProcessor simNonRandom(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY,
			ImageProcessor ipCh1Random, List<ImageProcessor> ch2Clusters,
			List<Rectangle> ch2ClustersRect, double interFactor,int th_other) {
		
		int M = roiMask.getWidth();
		int N = roiMask.getHeight();
		ImageProcessor ipSimulation = new ByteProcessor(M, N); // ip for ch2
																// mask
		for (int i = 0; i < ch2Clusters.size(); i++) {

			Rectangle clusterRect = ch2ClustersRect.get(i);// ip for ch2 mask
			ImageProcessor cluster = ch2Clusters.get(i);
			int max = 100000;
			int randomIter = 0;

			while (randomIter < max) {
				// int randomLeft = minimumX + (int)(Math.random() * maximumX-
				// clusterRect.width);
				int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width + 1);
				// int randomLeft = (int) ((float) Math.random() * (M -
				// clusterRect.width));
				int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height + 1);
				// int randomTop = (int) ((float) Math.random() * (N -
				// clusterRect.height));
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
							surrounding_pixels += ipSimulation.getPixel(u, v); // N
																				// is
																				// height
																				// ,
																				// M
																				// is
																				// width
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
							// checking surrounding pixels
							if (surrounding_pixels == 0) {
								overlapSelf = false;
							} else {
								overlapSelf = true;
								break Outer;
							}
						}
					}
				}
				if (overlapSelf == false) {
					Outer:
					for (int v = randomTop; v < randomBottom; v++) {
						for (int u = randomLeft; u < randomRight; u++) {
							if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
								int pOther = ipCh1Random.getPixel(u, v);
								if (pOther > th_other) {
									overlapOther = true;
									break Outer;// this could be changed
								}
								else{
									overlapOther = false; // 
									}
							}
						}
					}
				}
				if (overlapSelf == false && overlapOther == true){
                    //change pixel
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
				if (overlapSelf == false && overlapOther == false){
                    double random =  Math.random();
                    if (random  > interFactor){                 
                        //change pixel
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

	ImageProcessor simRandom(ImageProcessor roiMask, int minimumX, int maximumX, int minimumY, int maximumY,
			List<ImageProcessor> clusters, List<Rectangle> clustersRect) {

		int M = roiMask.getWidth();
		int N = roiMask.getHeight();

		ImageProcessor ipSimulation = new ByteProcessor(M, N); // ip for ch2
																// mask

		for (int i = 0; i < clusters.size(); i++) {

			Rectangle clusterRect = clustersRect.get(i);// ip for ch2 mask
			ImageProcessor cluster = clusters.get(i);
			int max = 100000;
			int randomIter = 0;

			while (randomIter < max) {
				// int randomLeft = minimumX + (int)(Math.random() * maximumX-
				// clusterRect.width);
				int randomLeft = ThreadLocalRandom.current().nextInt(minimumX, maximumX - clusterRect.width + 1);
				// int randomLeft = (int) ((float) Math.random() * (M -
				// clusterRect.width));
				int randomTop = ThreadLocalRandom.current().nextInt(minimumY, maximumY - clusterRect.height + 1);
				// int randomTop = (int) ((float) Math.random() * (N -
				// clusterRect.height));
				int randomRight = randomLeft + clusterRect.width;
				int randomBottom = randomTop + clusterRect.height;

				boolean overlapSelf = true;
				int surrounding_pixels = 0;

				Outer: for (int v = randomTop; v < randomBottom; v++) {
					for (int u = randomLeft; u < randomRight; u++) {
						if (cluster.getPixel(u - randomLeft, v - randomTop) > 0) {
							if (roiMask.getPixel(u, v) != 255) {
								overlapSelf = true;
								break Outer;
							}
							surrounding_pixels += ipSimulation.getPixel(u, v); // N
																				// is
																				// height
																				// ,
																				// M
																				// is
																			// width
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
				if (overlapSelf == false) { // random factor is 0
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
					wand.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.POLYGON);
					// Then add the image processor of intensity to the list
					chMaskFlood.setRoi(roi_par);
					chMaskFlood.setValue(200);
					chMaskFlood.fill(roi_par);
					chMaskCounter.setRoi(roi_par);
					ImageStatistics stats = chMaskCounter.getStatistics();
					if (stats.max > 0) {
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

	int ch2ClusterOverlaps(ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask) {
		ImageProcessor ipFlood = ipCh2Mask.duplicate();
		Wand wand = new Wand(ipFlood);

		int count = 0;
		int M = ipCh1Mask.getWidth();
		int N = ipCh1Mask.getHeight();

		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int pCh2 = ipFlood.get(u, v);
				int pCh1 = ipCh1Mask.get(u, v);
				if ((pCh1 == 255) && (pCh2 == 255)) {

					wand.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.POLYGON);

					// Then add the image processor of intensity to the list
					ipFlood.setRoi(roi_par);
					ipFlood.setValue(200);
					ipFlood.fill(roi_par);
					count++;
				}
			}
		}
		return count;
	}

	void setClustersOverlay(ImagePlus im, ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask) {

		// Color red_color = new Color( 255, 0, 0);
		Overlay ovCh = new Overlay();

		int M = ipCh1Mask.getWidth();
		int N = ipCh1Mask.getHeight();
		Wand wandCh1 = new Wand(ipCh1Mask);
		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipCh1Mask.get(u, v);
				if (p == 255) {
					wandCh1.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wandCh1.xpoints, wandCh1.ypoints, wandCh1.npoints, Roi.POLYGON);
					ovCh.add(roi_par);
					// im.setOverlay(roi_par,blue_color,3,blue_color);
				}
			}
		}
		Wand wandCh2 = new Wand(ipCh2Mask);
		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipCh2Mask.get(u, v);
				if (p == 255) {
					wandCh2.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wandCh2.xpoints, wandCh2.ypoints, wandCh2.npoints, Roi.POLYGON);
					ovCh.add(roi_par);
					// im.setOverlay(roi_par,red_color,3,red_color);
				}
			}
		}
		Color test_color = Color.WHITE;
		ovCh.setStrokeColor(test_color);
		// im.setOverlay(ovCh1);
		// ovCh2.setStrokeColor(red_color);
		im.setOverlay(ovCh);

	}
	Overlay returnOverlay(ImageProcessor ipCh1Mask, ImageProcessor ipCh2Mask) {

		// Color red_color = new Color( 255, 0, 0);
		Overlay ovCh = new Overlay();

		int M = ipCh1Mask.getWidth();
		int N = ipCh1Mask.getHeight();
		Wand wandCh1 = new Wand(ipCh1Mask);
		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipCh1Mask.get(u, v);
				if (p == 255) {
					wandCh1.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wandCh1.xpoints, wandCh1.ypoints, wandCh1.npoints, Roi.POLYGON);
					ovCh.addElement(roi_par);
					
					// im.setOverlay(roi_par,blue_color,3,blue_color);
				}
			}
		}
		Wand wandCh2 = new Wand(ipCh2Mask);
		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipCh2Mask.get(u, v);
				if (p == 255) {
					wandCh2.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wandCh2.xpoints, wandCh2.ypoints, wandCh2.npoints, Roi.POLYGON);
					ovCh.addElement(roi_par);
					// im.setOverlay(roi_par,red_color,3,red_color);
				}
			}
		}
		return ovCh;

	}

	int clustersProcessing(String imageName, boolean results, ResultsTable rt, Calibration calibration,
			ImageProcessor ipFlood, ImageProcessor channel, List<ImageProcessor> clusters,
			List<Rectangle> clustersRect) {

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
				if (p == 255) {

					wand.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.POLYGON);

					ImageProcessor roi_parMask = roi_par.getMask();

					Rectangle region_r = roi_par.getBounds();
					clustersRect.add(region_r);// adding to list of rectangle
												// rois
					// new image processor of intensity

					// ROI corner coordinates:
					int rLeft = region_r.x;
					int rTop = region_r.y;
					int rRight = rLeft + region_r.width;
					int rBottom = rTop + region_r.height;

					// process all pixels inside the ROI
					for (int y = rTop; y < rBottom; y++) {
						for (int x = rLeft; x < rRight; x++) {
							if (roi_parMask.getPixel(x - rLeft, y - rTop) > 0) {
								int pixel = channel.getPixel(x, y);
								roi_parMask.putPixel(x - rLeft, y - rTop, pixel);

							}
						}
					}
					clusters.add(roi_parMask);

					// Then add the image processor of intensity to the list
					ipFlood.setRoi(roi_par);
					ipFlood.setValue(200);
					ipFlood.fill(roi_par);
					count++;

					// adding the results

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
		return count;
	}

	int clustersProcessing(Calibration calibration, ResultsTable rt, ImageProcessor ipFlood, ImageProcessor channel,
			List<ImageProcessor> clusters, List<Rectangle> clustersRect) {

		int count = clustersProcessing("Nothing", false, rt, calibration, ipFlood, channel, clusters, clustersRect);
		return count;

	}

	int clustersProcessingSimple(ImageProcessor channelMask) {
		ImageProcessor ipFlood = channelMask.duplicate();

		Wand wand = new Wand(ipFlood);

		int count = 0;
		int M = ipFlood.getWidth();
		int N = ipFlood.getHeight();

		for (int u = 0; u < M; u++) {
			for (int v = 0; v < N; v++) {
				int p = ipFlood.get(u, v);
				if (p == 255) {
					wand.autoOutline(u, v, 255, 255);
					PolygonRoi roi_par = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.POLYGON);
					ipFlood.setRoi(roi_par);
					ipFlood.setValue(200);
					ipFlood.fill(roi_par);
					count++;
				}
			}
		}
		return count;
	}

}
