package mathtools.expressionanalysis.differentialexpression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.math3.stat.inference.TTest;

import statistics.general.MathTools;
import statistics.general.StatTests;

public class CalculateTTest {

	public static void main(String[] args) {
		try {
			int[] pdgfra = {1, 3, 4};
			int[] ntrk = {5, 6, 7};
			int[] cntrl = {8, 9, 10};
			double[] values1 = {1, 2, 3, 1, 2, 3};
			double[] values2 = {5, 6, 7, 5, 6, 7};
			TTest test = new TTest();
			System.out.println(test.t(values2,values1));
			//String inputFile = "C:\\Users\\tshaw\\Desktop\\PROTEOMICS\\SusanBaker_Mouse_Hong\\COMPLETE_FINAL_ANALYSIS\\HGG_EXAMPLE\\raw\\total\\Total_Proteome_GeneName_Collapse.txt";
			//String outputFile1 = "C:\\Users\\tshaw\\Desktop\\PROTEOMICS\\SusanBaker_Mouse_Hong\\COMPLETE_FINAL_ANALYSIS\\HGG_EXAMPLE\\raw\\total\\Total_Proteome_TTest_PvC.txt";
			//String outputFile2 = "C:\\Users\\tshaw\\Desktop\\PROTEOMICS\\SusanBaker_Mouse_Hong\\COMPLETE_FINAL_ANALYSIS\\HGG_EXAMPLE\\raw\\total\\Total_Proteome_TTest_NvC.txt";
			//String outputFile3 = "C:\\Users\\tshaw\\Desktop\\PROTEOMICS\\SusanBaker_Mouse_Hong\\COMPLETE_FINAL_ANALYSIS\\HGG_EXAMPLE\\raw\\total\\Total_Proteome_TTest_PvN.txt";
			//calculateTTest(inputFile, outputFile1, pdgfra, cntrl);
			//calculateTTest(inputFile, outputFile2, ntrk, cntrl);
			//calculateTTest(inputFile, outputFile3, pdgfra, ntrk);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	public static String type() {
		return "DIFFERENTIAL.";
	}
	public static String description() {
		return "T-TEst between two groups.";
	}
	public static String parameter_info() {
		return "[inputFile] [groupAFile] [groupBFile] [outputUpRegFile] [outputDnRegFile] [outputAllFile] [filterType] [takeLog]";
	}
	public static void execute(String[] args) {
		try {
			String inputFile = args[0];			
			String groupAFile = args[1];
			String groupBFile = args[2];
			String outputUpRegFile = args[3];
			String outputDnRegFile = args[4];
			String outputAllFile = args[5];
			String filterType = args[6];
			boolean takeLog = Boolean.valueOf(args[7]);
			
			//[OutputUpReg] [OutputDownReg] [OutputAll] [FilterType: ALL,PVALUE,FOLDCHANGE,BOTH] [TAKELOG]
			FileInputStream fstream = new FileInputStream(inputFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			String header = in.readLine();
			in.close();
			
			LinkedList listA = new LinkedList();
			fstream = new FileInputStream(groupAFile);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String sample = in.readLine();
				listA.add(sample);
			}			

			LinkedList listB = new LinkedList();
			fstream = new FileInputStream(groupBFile);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String sample = in.readLine();
				listB.add(sample);
			}
			
			String[] split = header.split("\t");
			int indexA = 0;
			int indexB = 0;
			int index = 0;
			int[] groupA = new int[listA.size()];
			int[] groupB = new int[listB.size()];
			for (String sampleName: split) {
				//System.out.println("sampleName: " + sampleName);
				if (listA.contains(sampleName)) {
					groupA[indexA] = index;
					//System.out.println(indexA + "\t" + index);
					indexA++;
				}
				if (listB.contains(sampleName)) {
					groupB[indexB] = index;
					indexB++;
				}
				index++;
			}
			double pvalCutoff = 0.05;
			double logFC = 1.0;
			if (filterType.toUpperCase().equals("ALL")) {
				pvalCutoff = 1.0;
				logFC = 0.0;
			} else if (filterType.toUpperCase().equals("PVALUE")){
				pvalCutoff = 0.05;
				logFC = 0.0;
			} else if (filterType.toUpperCase().equals("FOLDCHANGE")) {
				pvalCutoff = 1.0;
				logFC = 1.0;
			} else {
				pvalCutoff = 0.05;
				logFC = 1.0;
			}
			generateTTestResult(inputFile, outputAllFile, outputUpRegFile, outputDnRegFile, groupA, groupB, pvalCutoff, logFC, takeLog);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void generateTTestResult(String inputFile, String outputAllFile, String outputUpRegFile, String outputDnRegFile, int[] index1, int[] index2, double pval, double foldChange, boolean takeLog) {
		TTest test = new TTest();
		try {
			
			FileWriter fwriter = new FileWriter(outputAllFile);
			BufferedWriter out = new BufferedWriter(fwriter);
			
			
			FileWriter fwriter_up = new FileWriter(outputUpRegFile);
			BufferedWriter out_up = new BufferedWriter(fwriter_up);
			
			FileWriter fwriter_dn = new FileWriter(outputDnRegFile);
			BufferedWriter out_dn = new BufferedWriter(fwriter_dn);
			
			out.write("logFC\tAveExpr\ttstat\tP.Value\tadj.P.Value\tB\n");
			out_up.write("logFC\tAveExpr\ttstat\tP.Value\tadj.P.Value\tB\n");
			out_dn.write("logFC\tAveExpr\ttstat\tP.Value\tadj.P.Value\tB\n");
			
			
			LinkedList list = new LinkedList();
			FileInputStream fstream = new FileInputStream(inputFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				double[] d1 = new double[index1.length];
				double[] d2 = new double[index2.length];
				double[] all = new double[index1.length + index2.length];
				int count = 0;
				int all_index = 0;
				for (int i: index1) {
					//System.out.println(i + "\t" + split[i]);
					double val = new Double(split[i]);
					if (takeLog) {
						val = MathTools.log2(val + 0.00001);
					}
					d1[count] = val;
					count++;
					
					all[all_index] = val;
					all_index++;
				}
				count = 0;
				for (int i: index2) {
					double val = new Double(split[i]);
					if (takeLog) {
						val = MathTools.log2(val + 0.00001);
					}
					d2[count] = val;
					count++;
					
					all[all_index] = val;
					all_index++;					
				}
				double group1 = MathTools.mean(d1);
				double group2 = MathTools.mean(d2);
				
				double all_mean = MathTools.mean(all);
				//out.write(split[0] + "\t" + group1 + "\t" + group2 + "\t" + (group1 - group2) + "\t" + test.tTest(d1, d2) + "\n");
				
				//System.out.println(split[0] + "\t" + (group1 - group2) + "\t" + all_mean + "\t" + test.tTest(d1, d2));
				if (group1 == group2) {
					list.add(split[0] + "\t" + (group1 - group2) + "\t" + all_mean + "\t" + 1.0);
				} else {
					list.add(split[0] + "\t" + (group1 - group2) + "\t" + all_mean + "\t" + test.tTest(d1, d2));
				}
				
			}
			
			in.close();
			
			double[] pvals = new double[list.size()];
			int count = 0;
			Iterator itr = list.iterator();
			while (itr.hasNext()) {
				String str = (String)itr.next();
				
				String[] split = str.split("\t");				
				pvals[count] = new Double(split[3]);
				System.out.println(str + "\t" + pvals[count]);
				count++;
			}
			count = 0;
			double[] fdr = StatTests.BenjaminiHochberg(pvals);
			
			itr = list.iterator();
			while (itr.hasNext()) {
				String str = (String)itr.next();
				String[] split = str.split("\t");
				
				double pval_ttest = new Double(split[3]);
				double logFC = new Double(split[1]); 
				if (logFC >= 0 && Math.abs(logFC) > Math.abs(foldChange) && pval_ttest < pval) {
					out_up.write(split[0] + "\t" + logFC + "\t" + split[2] + "\t" + pval_ttest + "\t" + pvals[count] + "\t" + fdr[count] + "\t" + fdr[count] + "\n");
				} else if (logFC < 0 && Math.abs(logFC) > Math.abs(foldChange) && pval_ttest < pval){
					out_dn.write(split[0] + "\t" + logFC + "\t" + split[2] + "\t" + pval_ttest + "\t" + pvals[count] + "\t" + fdr[count] + "\t" + fdr[count] + "\n");
				}
				out.write(split[0] + "\t" + logFC + "\t" + split[2] + "\t" + pval_ttest + "\t" + pvals[count] + "\t" + fdr[count] + "\t" + fdr[count] + "\n");
				
				count++;
			}
			
			out_up.close();
			out_dn.close();
			out.close();
			
			//System.out.println(test.tTest(d1, d2));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void calculateTTest(String inputFile, String outputFile, int[] index1, int[] index2) {
		TTest test = new TTest();
		try {
			
			FileWriter fwriter = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fwriter);
			
			FileInputStream fstream = new FileInputStream(inputFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				double[] d1 = new double[index1.length];
				double[] d2 = new double[index2.length];
				int count = 0;
				
				for (int i: index1) {					
					d1[count] = new Double(split[i]);
					count++;
				}
				count = 0;
				for (int i: index2) {
					d2[count] = new Double(split[i]);
					count++;
				}
				double group1 = MathTools.mean(d1);
				double group2 = MathTools.mean(d2);
				out.write(split[0] + "\t" + group1 + "\t" + group2 + "\t" + (group1 - group2) + "\t" + test.tTest(d1, d2) + "\n");
				
			}
			
			in.close();
			out.close();
			//System.out.println(test.tTest(d1, d2));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
