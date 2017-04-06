import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Subtract_Selected_Background implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {		
		double mean = measure();
		if (mean < 0) {
			IJ.log("Invalid selection.");
			return;
		}
		// IJ.log(Double.toString(mean)); //		
		IJ.run("Select None");
		ip.subtract(mean);
	}
	
	double measure() {
		Roi roi = imp.getRoi();
		if (roi == null) {
			return -1;
		}
		if (roi!=null && roi.getType()==Roi.POINT) {
			// show error
			return -1;
		}
		if (roi!=null && roi.isLine()) {
			// show error
			return -1;
		}
		if (roi!=null && roi.getType()==Roi.ANGLE) {
			// show error
			return -1;
		}
		String MEASUREMENTS = "measurements";
		int AREA=1;
		int MEAN=2;
		int MIN_MAX = 16;
		int systemMeasurements = Prefs.getInt(MEASUREMENTS,AREA+MEAN+MIN_MAX);
		ImageStatistics stats = imp.getStatistics(systemMeasurements);
		double mean = stats.mean;
		return mean;
	}

}
