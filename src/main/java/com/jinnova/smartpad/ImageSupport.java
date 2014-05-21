package com.jinnova.smartpad;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageSupport {
	
	private static final int[] WIDTHS = new int[] {50, 100, 200};

	public static void main(String[] args) throws IOException {
		new ImageSupport().resize(new File("imaging/in-queue"), new File("imaging/root"));
	}
	
	public void resize(String sourceRootFolder, String destRootFolder) {
		
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
			destFileName = destFileName + "_" + expectedWidth + ".jpg";
			ImageIO.write(scaled, "jpg", new File(sizesFolder, destFileName));
		}
		ImageIO.write(sourceImage, "jpg", new File(destFolder, sourceFile.getName()));
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
