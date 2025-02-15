package rnaseq.splicing.alternativejuncpipeline.intronretention;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

import misc.FileTools;

public class CountNumberOfUniqReadsScript {
	public static void execute(String[] args) {
		
		String inputFile = args[0];
		System.out.println(generateScript(inputFile));
		
	}
	
	public static String generateScript(String inputFile1) {
		 
		LinkedList listFile = FileTools.readFileList(inputFile1);
		String script = "";
		Iterator itr = listFile.iterator();		
		
		while (itr.hasNext()) {
			String file = (String)itr.next();
			String bedFile = file.split("/")[file.split("/").length - 1] + ".bed";
			script += "drppm -CountNumberOfUniqReads " + bedFile + " true 101 2 > " + bedFile + ".count" + "\n";			
			script += "drppm -CountNumberOfUniqReads " + bedFile + "_exon.bed true 101 2 > " + bedFile + "_exon.bed.count" + "\n";
			script += "drppm -CountNumberOfUniqReads " + bedFile + "_gene.bed true 101 2 > " + bedFile + "_gene.bed.count" + "\n";
			script += "drppm -CountNumberOfUniqReads " + bedFile + "_intron.bed false 101 2 > " + bedFile + "_intron.bed.count" + "\n";
		}
		return script;
	}
	
}
