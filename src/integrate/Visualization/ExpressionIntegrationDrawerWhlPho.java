package integrate.Visualization;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import misc.CommandLine;
import misc.RunRScript;
import statistics.general.MathTools;

/**
 * Draw gene images with expression boxes on the side
 * @author tshaw
 *
 */
public class ExpressionIntegrationDrawerWhlPho {

	public static String type() {
		return "INTEGRATION";
	}
	public static String description() {
		return "Draw gene images with expression boxes on the side";
	}
	public static String parameter_info() {
		return "[whl_inputFile] [pho_inputFile] [groups] [ZscoreBorder] [ouputPath]";
	}
	
	public static void execute(String[] args) {
		
		try {
			
			String whl_inputFile = args[0]; // whl level
			String pho_inputFile = args[1]; // site level
			String[] groups = args[2].split(":");
			double range = new Double(args[3]);
			String outputPath = args[4];
			HashMap whl_values_map = new HashMap();
			HashMap pho_values_map = new HashMap();
			HashMap kinase_values_map = new HashMap();
			
			HashMap whl_zscores_map = new HashMap();
			HashMap pho_zscores_map = new HashMap();
			HashMap kinase_zscores_map = new HashMap();
			
			HashMap geneName_map = new HashMap();
			FileInputStream fstream = new FileInputStream(whl_inputFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				geneName_map.put(split[0], split[0]);
				double[] whl_values = new double[groups.length];
				for (int i = 0; i < groups.length; i++) {
					double mean = 0;
					String[] idxes = groups[i].split(",");
					for (int j = 0; j < idxes.length; j++) {
						int idx = new Integer(idxes[j]);
						mean += MathTools.log2(new Double(split[idx]));
					}
					mean = mean / idxes.length;
					whl_values[i] = mean;
				}
				double[] whl_zscores = MathTools.zscores(whl_values);
				whl_zscores_map.put(split[0], whl_zscores);
				whl_values_map.put(split[0], whl_values);
			}
			in.close();
			
			fstream = new FileInputStream(pho_inputFile);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			in.readLine();
			while (in.ready()) {
				String str = in.readLine();				
				String[] split = str.split("\t");
				geneName_map.put(split[0].split("_")[0], split[0].split("_")[0]);
				double[] pho_values = new double[groups.length];
				for (int i = 0; i < groups.length; i++) {
					double mean = 0;
					String[] idxes = groups[i].split(",");
					for (int j = 0; j < idxes.length; j++) {
						int idx = new Integer(idxes[j]);
						mean += MathTools.log2(new Double(split[idx]));
					}
					mean = mean / idxes.length;
					pho_values[i] = mean;
				}
				
				if (pho_values_map.containsKey(split[0])) {
					double[] pho_values_tmp = (double[])pho_values_map.get(split[0]);
					double old_mean = MathTools.mean(pho_values_tmp);
					double new_mean = MathTools.mean(pho_values);
					if (new_mean > old_mean) {						
						pho_values_map.put(split[0], pho_values);
						
					}
				} else {
					pho_values_map.put(split[0], pho_values);
					double new_mean = MathTools.mean(pho_values);
					
				}
				
				
				double[] pho_zscores = MathTools.zscores(pho_values);				
				if (!pho_zscores_map.containsKey(split[0].split("_")[0])) {
					pho_zscores_map.put(split[0].split("_")[0], pho_zscores);
					
				}
				
				Iterator itr = pho_values_map.keySet().iterator();
				while (itr.hasNext()) {
					String name = (String)itr.next();
					if (name.split("_")[0].equals(split[0].split("_")[0])) {
						double[] zvalues = (double[])pho_zscores_map.get(name.split("_")[0]);
						if (MathTools.mean(pho_zscores) > MathTools.mean(zvalues)) {
							pho_zscores_map.put(split[0].split("_")[0], pho_zscores);
							
						}
					}
				}
				
				
			}
			in.close();
						
			Iterator itr = geneName_map.keySet().iterator();
			while (itr.hasNext()) {
				String geneName = (String)itr.next();
				double[] whl_zscores = new double[groups.length];
				double[] pho_zscores = new double[groups.length];
				double[] kinase_zscores = new double[groups.length];
				for (int i = 0; i < groups.length; i++) {
					whl_zscores[i] = Double.NaN;
					pho_zscores[i] = Double.NaN;
					kinase_zscores[i] = Double.NaN;
				}
				if (kinase_zscores_map.containsKey(geneName)) {										
					kinase_zscores = (double[])kinase_zscores_map.get(geneName);
					
				}
				if (whl_zscores_map.containsKey(geneName)) {
					whl_zscores = (double[])whl_zscores_map.get(geneName);
				}
				if (pho_zscores_map.containsKey(geneName)) {
					pho_zscores = (double[])pho_zscores_map.get(geneName);
				}
				
				String script = drawboxes(outputPath, geneName, whl_zscores, pho_zscores, range);
				CommandLine.writeFile("temp.r", script);
				/*if (geneName.equals("AURKA")) {
					CommandLine.writeFile("AURKA.r", script);
				}*/
				RunRScript.runRScript("temp.r");
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String drawboxes(String outputPath, String kinaseName, double[] whl_zscores, double[] pho_zscores, double range) {
		String script = "";
		script += "png(file = \"" + outputPath + "/" + kinaseName + ".png\", width=400,height=400)\n";
		if (kinaseName.length() <= 4) {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=24, labels = \"" + kinaseName + "\", cex=5, adj = c(1,0))\n";
		} else if (kinaseName.length() <= 5) {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=25, labels = \"" + kinaseName + "\", cex=4.5, adj = c(1,0))\n";			
		} else if (kinaseName.length() <= 6) {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=25, labels = \"" + kinaseName + "\", cex=4, adj = c(1,0))\n";
		} else if (kinaseName.length() <= 7) {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=26, labels = \"" + kinaseName + "\", cex=3.5, adj = c(1,0))\n";
		} else if (kinaseName.length() <= 8) {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=27, labels = \"" + kinaseName + "\", cex=3, adj = c(1,0))\n";
		} else {
			script += "plot(1:100, 1:100, type=\"n\", axes=F, ylim=c(0, 100), xlim=c(-70, 100), xlab=\"\", ylab=\"\")\n";
			script += "text(x = 50, y=28, labels = \"" + kinaseName + "\", cex=2.5, adj = c(1,0))\n";
		}
		double buffer = 40.0 / whl_zscores.length;
		
		int addBuffer = -3;
		int addBuffer2 = -6;

		for (int i = 0; i < whl_zscores.length; i++) {
			double x1axis = 55 + i * buffer;
			double x2axis = (55 + buffer) + i * buffer;
			double x1axis_p1 = 55 + i * buffer + (10.0 / 3);
			double x1axis_p2 = 55 + i * buffer + (20.0 / 3);
			if (Double.isNaN(whl_zscores[i])) {
				script += "rect(" + x1axis + ", 38 + " + addBuffer + ", " + x2axis + ", 44 + " + addBuffer + ", lwd=2, col = rgb(1, 1, 1))\n";
				//script += "segments(" + x1axis + ", 40, " + x1axis_p1 + ", 38, lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis + ", 42, " + x1axis_p2 + ", 38, lwd=1, col = rgb(0, 0, 0));\n";
				script += "segments(" + x1axis + ", 44 + " + addBuffer + ", " + x2axis + ", 38 + " + addBuffer + ", lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis_p1 + ", 44, " + x2axis + ", 40, lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis_p2 + ", 44, " + x2axis + ", 42, lwd=1, col = rgb(0, 0, 0));\n";
			} else {
				if (whl_zscores[i] < -range) {
					whl_zscores[i] = -range;
				}
				if (whl_zscores[i] > range) {
					whl_zscores[i] = range;
				}
				
				//double val = 1 - Math.abs(whl_zscores[i]) / range;
				double val = 1 - Math.abs(whl_zscores[i] + range) / (range * 2);
				script += "rect(" + x1axis + ", 38 + " + addBuffer + ", " + x2axis + ", 44 + " + addBuffer + ", lwd=2, col = rgb(" + val + ", 1," + val + "))\n";
				/*if (whl_zscores[i] < 0) {				
					script += "rect(" + x1axis + ", 38, " + x2axis + ", 44, lwd=2, col = rgb(" + val + "," + val + ", 1))\n";
				} else if (whl_zscores[i] > 0) {
					script += "rect(" + x1axis + ", 38, " + x2axis + ", 44, lwd=2, col = rgb(1, " + val + "," + val + "))\n";
				} else if (whl_zscores[i] == 0) {
					script += "rect(" + x1axis + ", 38, " + x2axis + ", 44, lwd=2, col = rgb(1, 1, 1))\n";
				}*/
			}
		}
		
		for (int i = 0; i < pho_zscores.length; i++) {
			double x1axis = 55 + i * buffer;
			double x2axis = (55 + buffer) + i * buffer;
			double x1axis_p1 = 55 + i * buffer + (10.0 / 3);
			double x1axis_p2 = 55 + i * buffer + (20.0 / 3);			
			if (Double.isNaN(pho_zscores[i])) {
				script += "rect(" + x1axis + ", 30 + " + addBuffer2 + ", " + x2axis + ", 36 + " + addBuffer2 + ", lwd=2, col = rgb(1, 1, 1))\n";
				//script += "segments(" + x1axis + ", 32, " + x1axis_p1 + ", 30, lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis + ", 34, " + x1axis_p2 + ", 30, lwd=1, col = rgb(0, 0, 0));\n";
				script += "segments(" + x1axis + ", 36 + " + addBuffer2 + ", " + x2axis + ", 30 + " + addBuffer2 + ", lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis_p1 + ", 36, " + x2axis + ", 32, lwd=1, col = rgb(0, 0, 0));\n";
				//script += "segments(" + x1axis_p2 + ", 36, " + x2axis + ", 34, lwd=1, col = rgb(0, 0, 0));\n";
			} else {
				if (pho_zscores[i] < -range) {
					pho_zscores[i] = -range;
				}
				if (pho_zscores[i] > range) {
					pho_zscores[i] = range;
				}
				//double val = 1 - Math.abs(pho_zscores[i]) / range;
				double val = 1 - Math.abs(pho_zscores[i] + range) / (range * 2);
				script += "rect(" + x1axis + ", 30 + " + addBuffer2 + ", " + x2axis + ", 36 + " + addBuffer2 + ", lwd=2, col = rgb(" + val + "," + val + ", 1))\n";
				/*if (pho_zscores[i] < 0) {
					script += "rect(" + x1axis + ", 30, " + x2axis + ", 36, lwd=2, col = rgb(" + val + "," + val + ", 1))\n";
				} else if (pho_zscores[i] > 0) {
					script += "rect(" + x1axis + ", 30, " + x2axis + ", 36, lwd=2, col = rgb(1, " + val + "," + val + "))\n";
				} else if (pho_zscores[i] == 0) {
					script += "rect(" + x1axis + ", 30, " + x2axis + ", 36, lwd=2, col = rgb(1, 1, 1))\n";
				}*/
			}
		}
		
		/*
		if (kinaseName.equals("TBK1")) {
			for (double z: kinase_zscores) {
				System.out.println("TBK1\t" + z);
			}
		}
		if (kinaseName.equals("AURKA")) {
			for (double z: kinase_zscores) {
				System.out.println("AURKA\t" + z);
			}
		}*/
		
		script += "dev.off()\n";
		return script;
	}
}
