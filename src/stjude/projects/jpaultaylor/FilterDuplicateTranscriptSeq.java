package stjude.projects.jpaultaylor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FilterDuplicateTranscriptSeq {

	public static String type() {
		return "MISC";
	}
	public static String description() {
		return "Filter duplicate lines";
	}
	public static String parameter_info() {
		return "[inputFile] [outputFile]";
	}
	public static void execute(String[] args) {
		
		try {
			
			String inputFile = args[0];
			String outputFile = args[1];
			
        	FileWriter fwriter = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(fwriter);
			HashMap map = new HashMap();
            FileInputStream fstream = new FileInputStream(inputFile);
            DataInputStream din = new DataInputStream(fstream);
            BufferedReader in = new BufferedReader(new InputStreamReader(din));
            while (in.ready()) {
            	String str = in.readLine();
            	String[] split = str.split("\t");
            	if (!map.containsKey(split[2] + "\t" + split[3])) {
            		out.write(str + "\n");
            		map.put(split[2] + "\t" + split[3], "");
            	} 
            }
            in.close();            			
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
