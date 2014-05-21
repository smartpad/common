package com.jinnova.smartpad;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageSupport {
	
	private static final int[] WIDTHS = new int[] {50, 100, 200};
	
	private static String rootInQueue = "imaging/in-queue/";
	private static String rootOut = "imaging/root/";

	public static void main(String[] args) throws IOException {
		File rootInQueueFile = new File(rootInQueue);
		File rootOutFile = new File(rootOut);
		System.out.println("Imaging from " + rootInQueueFile.getAbsolutePath() + " to " + rootOutFile.getAbsolutePath());
		new ImageSupport().resize(rootInQueueFile, rootOutFile);
	}
	
	public static void initialize(String rootInQueue, String rootOut) {
		if (!rootInQueue.endsWith("/")) {
			rootInQueue += "/";
		}
		if (!rootOut.endsWith("/")) {
			rootOut += "/";
		}
		ImageSupport.rootInQueue = rootInQueue;
		ImageSupport.rootOut = rootOut;
	}
	
	public void queueIn(String typeName, String subTypeName, String entityId, String imageId, InputStream inputStream) throws IOException {
		
		String subTypePath = subTypeName == null ? "" : "/" + subTypeName;
		File folder = new File(rootInQueue + typeName + subTypePath + "/" + entityId + "/");
		folder.mkdirs();
		FileOutputStream fos = new FileOutputStream(new File(folder, imageId + ".png"));
		byte[] bytes = new byte[1024];
		while (true) {
			int count = inputStream.read(bytes);
			if (count < 0) {
				break;
			}
			fos.write(bytes, 0, count);
		}
	}
	
	public BufferedImage getImage(String typeName, String subTypeName, String entityId, String imageId, int size) throws IOException {
		String subTypePath = subTypeName == null ? "" : "/" + subTypeName;
		File f = new File(rootOut + typeName + subTypePath + "/" + entityId + "/sizes/" + imageId + "_" + size + ".png");
		return ImageIO.read(f);
	}
	
	public void resize(String sourceRootFolder, String destRootFolder) throws IOException {
		resize(new File(sourceRootFolder), new File(destRootFolder));
	}
	
	private void resize(File sourceFolder, File destFolder) throws IOException {
		for (File f : sourceFolder.listFiles()) {
			if (f.isDirectory()) {
				File newDestRoot = new File(destFolder, f.getName());
				newDestRoot.mkdir();
				resize(f, newDestRoot);
				continue;
			}
			resize(sourceFolder, f, destFolder);
		}
	}
	
	private void resize(File sourceFolder, File sourceFile, File destFolder) throws IOException {
		
		File sizesFolder = new File(destFolder, "sizes");
		sizesFolder.mkdirs();
		BufferedImage sourceImage = ImageIO.read(sourceFile);
		for (int expectedWidth : WIDTHS) {
			System.out.println("Resizing " + sourceFile.getAbsolutePath() + " from " + sourceImage.getWidth() + " to " + expectedWidth);
			int expectedHight = sourceImage.getHeight() * expectedWidth / sourceImage.getWidth();
			BufferedImage scaled = resize(sourceImage, expectedWidth, expectedHight);
	
			String destFileName = sourceFile.getName();
			int index = destFileName.lastIndexOf('.');
			if (index > -1) {
				destFileName = destFileName.substring(0, index);
			}
			destFileName = destFileName + "_" + expectedWidth + ".png";
			ImageIO.write(scaled, "png", new File(sizesFolder, destFileName));
		}
		ImageIO.write(sourceImage, "png", new File(destFolder, sourceFile.getName()));
		sourceFile.delete();
	}
	
	private BufferedImage resize(Image originalImage, int scaledWidth, int scaledHeight) {
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
}
