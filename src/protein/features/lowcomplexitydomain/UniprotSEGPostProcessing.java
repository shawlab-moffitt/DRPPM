package protein.features.lowcomplexitydomain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class UniprotSEGPostProcessing {
	public static String type() {
		return "PROTEOMICS";
	}
	public static String description() {
		return "Generate ";
	}
	public static String parameter_info() {
		return "[inputFile] [fastaFile] [sp_only_flag true/false] [outputFile]";
	}
	public static void execute(String[] args) {
		
		try {
			
			String inputFile = args[0];
			String fastaFile = args[1];
			boolean sp_only_flag = new Boolean(args[2]);
			String outputFile = args[3];
			HashMap accession2geneSymbol = new HashMap();
			HashMap map = new HashMap();
			
			HashMap map_uniprot_geneName = new HashMap();
			FileInputStream fstream = new FileInputStream(fastaFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String str = in.readLine();
				if (str.contains(">")) {
					String accession = str.split("\\|")[1];
					String uniprot_geneName = str.split("\\|")[2].split(" ")[0];
					for (String stuff: str.split(" ")) {
						if (stuff.contains("GN=")) {
							accession2geneSymbol.put(accession, stuff.replaceAll("GN=", ""));
						}
					}
					if (sp_only_flag) {
						if (str.contains(">sp")) {
							map_uniprot_geneName.put(accession,  uniprot_geneName);
						}
					} else {
						map_uniprot_geneName.put(accession,  uniprot_geneName);
					}
				}
			}
			in.close();
			
			FileWriter fwriter_len = new FileWriter(outputFile);
            BufferedWriter out_len = new BufferedWriter(fwriter_len);
            out_len.write("Accession\tAlias\tUniprotGeneName\tLCDLength\tDataType\n");
			fstream = new FileInputStream(inputFile);
			din = new DataInputStream(fstream);
			in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String str = in.readLine();
				if (str.contains(">")) {
					String accession = str.split("\\|")[1];
					String uniprot_geneName = str.split("\\|")[2].split("\\(")[0];
					System.out.println(accession + "\t" + uniprot_geneName);
					
					
					String seq = "";
					while (true) {
						String line = in.readLine().trim();
						if (line.length() == 0) {
							break;
						}
						seq += line;
					}
					if (map.containsKey(accession)) {
						String line = (String)map.get(accession);
						String[] split_line = line.split("\t");
						if (seq.length() > new Integer(split_line[3])) {
							map.put(accession, accession + "\t" + accession2geneSymbol.get(accession) + "\t" + map_uniprot_geneName.get(accession) + "\t" + seq.length() + "\t" + "HumanProteome");
						}
					} else {
						map.put(accession, accession + "\t" + accession2geneSymbol.get(accession) + "\t" + map_uniprot_geneName.get(accession) + "\t" + seq.length() + "\t" + "HumanProteome");
					}
					//out_len.write(accession + "\t" + uniprot_geneName.split("_")[0] + "\t" + uniprot_geneName + "\t" + seq.length() + "\t" + "HumanProteome\n");
				}
			}
			in.close();
			Iterator itr = map_uniprot_geneName.keySet().iterator();
			while (itr.hasNext()) {
				String accession = (String)itr.next();
				if (map.containsKey(accession)) {
					String line = (String)map.get(accession);
					out_len.write(line + "\n");
				} else {
					//String accession = (String)map_uniprot_geneName.get(uniprot_geneName);
					out_len.write(accession + "\t" + accession2geneSymbol.get(accession) + "\t" + map_uniprot_geneName.get(accession) + "\t" + 0.0 + "\t" + "HumanProteome\n");
				}
			}
			out_len.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
