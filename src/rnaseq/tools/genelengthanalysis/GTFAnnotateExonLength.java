package rnaseq.tools.genelengthanalysis;


import idconversion.tools.GTFFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Summarize GeneLength
 * Calculate the gene length based on ensembl gtf file
 * @author tshaw
 *
 */
public class GTFAnnotateExonLength {

	public static String parameter_info() {
		return "[Ensembl gtfFile] [transcript Output] [geneID Output] [geneName Output]";
	}
	public static void execute(String[] args) {
		
		
		try {
			HashMap transcript_length = new HashMap();
			HashMap transcript2GeneName = new HashMap();
			HashMap transcript2GeneID = new HashMap();
			HashMap geneName2transcript = new HashMap();
			HashMap geneID2transcript = new HashMap();
			
			String inputFile = args[0];
			String transcript_output = args[1];
			String gene_id_output = args[2];
			String gene_name_output = args[3];
			
			FileWriter fwriter_transcript = new FileWriter(transcript_output);
			BufferedWriter out_transcript = new BufferedWriter(fwriter_transcript);
			
			FileWriter fwriter_geneID = new FileWriter(gene_id_output);
			BufferedWriter out_geneID = new BufferedWriter(fwriter_geneID);
			
			FileWriter fwriter_geneName = new FileWriter(gene_name_output);
			BufferedWriter out_geneName = new BufferedWriter(fwriter_geneName);
			
			FileInputStream fstream = new FileInputStream(inputFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));					
			while (in.ready()) {
				String str = in.readLine().trim();
				if (!str.substring(0, 1).equals("#")) {
					String[] split = str.split("\t");
					
					String geneType = split[2];
					int length = new Integer(split[4]) - new Integer(split[3]) + 1;
					String meta = split[8];
					String gene_id = GTFFile.grabMeta(meta, "gene_id");
					String gene_name = GTFFile.grabMeta(meta, "gene_name");
					String transcript_id = GTFFile.grabMeta(meta, "transcript_id");
					if (!gene_id.trim().equals("") && !transcript_id.trim().equals("")) {
						transcript2GeneName.put(transcript_id, gene_name);
						transcript2GeneID.put(transcript_id, gene_id);
						if (geneName2transcript.containsKey(gene_name)) {
							LinkedList list = (LinkedList)geneName2transcript.get(gene_name);
							if (!list.contains(transcript_id)) {
								list.add(transcript_id);
							}
							geneName2transcript.put(gene_name, list);
						} else {
							LinkedList list = new LinkedList();
							if (!list.contains(transcript_id)) {
								list.add(transcript_id);
							}
							geneName2transcript.put(gene_name, list);
						}
						
						if (geneID2transcript.containsKey(gene_id)) {
							LinkedList list = (LinkedList)geneID2transcript.get(gene_id);
							if (!list.contains(transcript_id)) {
								list.add(transcript_id);
							}
							geneID2transcript.put(gene_id, list);
						} else {
							LinkedList list = new LinkedList();
							if (!list.contains(transcript_id)) {
								list.add(transcript_id);
							}
							geneID2transcript.put(gene_id, list);
						}
						if (geneType.equals("exon")) {
							if (transcript_length.containsKey(transcript_id)) {
								int len = (Integer)transcript_length.get(transcript_id);
								len = len + length;
								transcript_length.put(transcript_id, len);
							} else {
								transcript_length.put(transcript_id, length);
							}
						}				
					}
				}
				
			}
			in.close();
			
			out_transcript.write("TranscriptID\tLength\n");
			Iterator itr = transcript_length.keySet().iterator();
			while (itr.hasNext()) {
				String name = (String)itr.next();
				int len = (Integer)transcript_length.get(name);
				out_transcript.write(name + "\t" + len + "\n");
			}
			out_transcript.close();
			
			out_geneID.write("GeneID\tAvgLength\n");
			itr = geneID2transcript.keySet().iterator();
			while (itr.hasNext()) {
				String geneID = (String)itr.next();
				String transcripts = "";
				LinkedList list = (LinkedList)geneID2transcript.get(geneID);
				int total = 0;
				int count_good_list = 0;
				Iterator itr2 = list.iterator();
				while (itr2.hasNext()) {
					String transcript_id = (String)itr2.next();
					if (transcript_length.containsKey(transcript_id)) {
						int transcript_len = (Integer)transcript_length.get(transcript_id); 
						transcripts += transcript_id + "(" + transcript_len + "),";
						total += transcript_len;
						count_good_list++;
					}
				}
				//out_geneID.write(geneID + "\t" + (total / list.size()) + "\t" + total + "\t" + list.size() + "\t" + transcripts + "\n");
				out_geneID.write(geneID + "\t" + (total / count_good_list) + "\n");
			}
			out_geneID.close();
			
			out_geneName.write("GeneID\tAvgLength\n");
			itr = geneName2transcript.keySet().iterator();
			while (itr.hasNext()) {
				String geneName = (String)itr.next();
				LinkedList list = (LinkedList)geneName2transcript.get(geneName);
				int total = 0;
				int count_good_list = 0;
				Iterator itr2 = list.iterator();
				while (itr2.hasNext()) {
					String transcript_id = (String)itr2.next();
					if (transcript_length.containsKey(transcript_id)) {
						total += (Integer)transcript_length.get(transcript_id);
						count_good_list++;
					}
				}
				out_geneName.write(geneName + "\t" + (total / count_good_list) + "\n");
			}
			out_geneName.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
