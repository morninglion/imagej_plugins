import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.table.*;
import javax.swing.JList;
import javax.swing.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.tool.*;

public class Oval_Select extends PlugInTool implements DialogListener {
	private ImagePlus imp;

	private double width = 10;
	private double height = 10;
	int rx = 10;
	int ry = 10;
	private Vector fields;
	
	private static final int TABLE_WIDTH = 220;
	private static final int TABLE_ROWS = 18;
	private TableModel tableModel = null;
	JTable table = null;
	ImageCanvas ic;
	
	
	public void run(String arg) {
		super.run(arg);
		imp = IJ.getImage();
		if (imp == null) {
			return;
		}
		ic = imp.getCanvas();
		if (ic==null) {
			// TODO: error
			return;
		}
		
		showDlg();
	//	select();
	}
	
	private int[] getCursorLoc() {
		int[] arr = new int[4]; // x, y, z, flags
		ImageCanvas ic = imp.getCanvas();
		if (ic==null) {
			// TODO: error
			return arr;
		}
		Point p = ic.getCursorLoc();
		arr[0] = p.x;
		arr[1] = p.y;
		arr[2] = imp.getCurrentSlice()-1;
		Roi roi = imp.getRoi();
		arr[3] = ic.getModifiers()+((roi!=null)&&roi.contains(p.x,p.y)?32:0);
		return arr;
	}
	
	private void select() {
		int leftClick = 16;
		int x, y, z, flags;
		while (true) {			
			int[] arr = getCursorLoc();
			x = arr[0];
			y = arr[1];
			z = arr[2];
			flags = arr[3];
			
			if ((flags&leftClick) == 0) {
				// add t, TODO:
			}
			else {
				// if right click, end
			}
		}
	}
	
	private void showDlg() {
		int digits = 0;		
		Roi roi = imp.getRoi();		
		GenericDialog gd = new GenericDialog("Specify");
		gd.addNumericField("Width:", width, digits);
		gd.addNumericField("Height:", height, digits);
		fields = gd.getNumericFields();
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) {
			 if (roi!=null) {
				 imp.setRoi(roi);
			 }
		}
	}
	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(50);
		width = gd.getNextNumber();
		height = gd.getNextNumber();
		rx = (int)((double)(width + 0.5) / 2.0);
		ry = (int)((double)(height + 0.5) / 2.0);
		return true;
	}
	
	private void createRoi(MouseEvent e) {
		int x = ic.offScreenX(e.getX());
		int y = ic.offScreenY(e.getY());		
		Roi roi = new OvalRoi(x - rx, y - ry, width, height);
		
	//	IJ.log("w=" + Double.toString(width));
		
		imp.setRoi(roi);
	}
	public void mouseClicked(ImagePlus imp, MouseEvent e) {
		createRoi(e);
		IJ.doCommand("Add to Manager");
		
	}

	public void mouseMoved(ImagePlus imp, MouseEvent e) {
		createRoi(e);
	}
}
