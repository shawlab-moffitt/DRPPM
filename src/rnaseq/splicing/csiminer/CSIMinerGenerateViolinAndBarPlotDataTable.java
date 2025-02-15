package rnaseq.splicing.csiminer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class CSIMinerGenerateViolinAndBarPlotDataTable {

	public static String description() {
		return "Generate the file for the violin and barplot. Skipping header for inputExonList and inputDisease file.";
	}
	public static String type() {
		return "CSI-Miner";
	}
	public static String parameter_info() {
		return "[inputExonList] [inputDiseaseFiles] [sampleBlackList] [outputBarPlotFile] [outputBoxplotData]";
	}
	public static void execute(String[] args) {
		
		try {
			
			String inputExonList = args[0];
			String inputDiseaseFiles = args[1]; // tab separated file sampleName\trank_quartile\trank_percentage.
			String inputSampleBlackList = args[2];
			String outputBarPlotFile = args[3];
			String outputBoxPlotFile = args[4];
			
			FileWriter fwriter_barplot = new FileWriter(outputBarPlotFile);
			BufferedWriter out_barplot = new BufferedWriter(fwriter_barplot);
			
			FileWriter fwriter_boxplot = new FileWriter(outputBoxPlotFile);
			BufferedWriter out_boxplot = new BufferedWriter(fwriter_boxplot);
			out_boxplot.write("exon_name\texpr\tdisease_type\n");
			HashMap exonlist = new HashMap();
			FileInputStream fstream = new FileInputStream(inputExonList);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			String header = in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				exonlist.put(split[0], split[0]);			
			}
			in.close();
			
			HashMap blackList = new HashMap();
			fstream = new FileInputStream(inputSampleBlackList);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			header = in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				blackList.put(split[0], split[0]);			
			}
			in.close();
			
			out_barplot.write("ExonName");
			fstream = new FileInputStream(inputDiseaseFiles);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			header = in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String sampleName = split[0];
				String rankQuartileFile = split[1];
				String rankFPKMFile = split[2];
				
			}
			in.close();
			
			
			HashMap quartile_result = new HashMap();
			fstream = new FileInputStream(inputDiseaseFiles);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			header = in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String sampleName = split[0];
				String rankQuartileFile = split[1];
				String rankFPKMFile = split[2];
				
				File f = new File(rankQuartileFile);
				if (f.exists()) {
					out_barplot.write("\t" + sampleName + "_0" + "\t" + sampleName + "_1" + "\t" + sampleName + "_2" + "\t" + sampleName + "_3");
				} else {
					System.out.println(sampleName + ": " + f.getName() + " doesn't exist... Exiting");
					System.exit(0);
				}
				// read the rankMedianFile			
				
				
				
				FileInputStream fstream2 = new FileInputStream(rankQuartileFile);
				DataInputStream din2 = new DataInputStream(fstream2);
				BufferedReader in2 = new BufferedReader(new InputStreamReader(din2));
				String header2 = in2.readLine();
				String[] split_header2 = header2.split("\t");
				while (in2.ready()) {
					String str2 = in2.readLine();
					String[] split2 = str2.split("\t");
					if (exonlist.containsKey(split2[0])) {
						int first_quartile = 0;
						int second_quartile = 0;
						int third_quartile = 0;
						int fourth_quartile = 0;
						for (int i = 1; i < split2.length; i++) {
							if (!blackList.containsKey(split_header2[i])) {
								if (new Integer(split2[i]) == 0) {
									first_quartile++;
								} else if (new Integer(split2[i]) == 1) {
									second_quartile++;
								} else if (new Integer(split2[i]) == 2) {
									third_quartile++;
								} else if (new Integer(split2[i]) == 3) {
									fourth_quartile++;
								}
							}
						}
						/*if (quartile_result.containsKey(sampleName)) {
							HashMap exon_quartile = (HashMap)quartile_result.get(sampleName);
							exon_quartile.put(split2[0], first_quartile + "\t" + second_quartile + "\t" + third_quartile + "\t" + fourth_quartile);
							quartile_result.put(sampleName, exon_quartile);
						} else {
							HashMap exon_quartile = new HashMap();
							exon_quartile.put(split2[0], first_quartile + "\t" + second_quartile + "\t" + third_quartile + "\t" + fourth_quartile);
							quartile_result.put(sampleName, exon_quartile);
						}*/
						quartile_result.put(sampleName + "\t" + split2[0], first_quartile + "\t" + second_quartile + "\t" + third_quartile + "\t" + fourth_quartile);
					}
				}
				in2.close();
				
				// read the rankExpressionFile				
				FileInputStream fstream3 = new FileInputStream(rankFPKMFile);
				DataInputStream din3 = new DataInputStream(fstream3);
				BufferedReader in3 = new BufferedReader(new InputStreamReader(din3));
				String header3 = in3.readLine();
				String[] split_header3 = header3.split("\t");
				while (in3.ready()) {
					String str3 = in3.readLine();
					String[] split3 = str3.split("\t");
					if (exonlist.containsKey(split3[0])) {
						for (int i = 1; i < split3.length; i++) {
							if (!blackList.containsKey(split_header3[i])) {
								out_boxplot.write(split3[0] + "\t" + split3[i] + "\t" + sampleName + "\n");
							}
						}
					}
				}
				in3.close();
			}
			in.close();
			out_barplot.write("\n");
			
			Iterator itr = exonlist.keySet().iterator();
			while (itr.hasNext()) {
				String exon_name = (String)itr.next();
				out_barplot.write(exon_name);
				fstream = new FileInputStream(inputDiseaseFiles);
				din = new DataInputStream(fstream);
				in = new BufferedReader(new InputStreamReader(din));
				header = in.readLine();
				String[] split_header = header.split("\t");
				while (in.ready()) {
					String str = in.readLine();
					String[] split = str.split("\t");
					String sampleName = split[0];			
					if (quartile_result.containsKey(sampleName + "\t" + exon_name)) {
						String line = (String)quartile_result.get(sampleName + "\t" + exon_name);
						out_barplot.write("\t" + line);
					} else {
						out_barplot.write("\t" + 1 + "\t" + 0 + "\t" + 0 + "\t" + 0);
					}
					
				}
				in.close();
				out_barplot.write("\n");
			}
			out_boxplot.close();
			out_barplot.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
