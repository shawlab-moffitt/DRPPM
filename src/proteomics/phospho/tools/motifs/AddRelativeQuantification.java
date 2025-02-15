package proteomics.phospho.tools.motifs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import statistics.general.MathTools;

/**
 * Add relative quantification
 * @author tshaw
 *
 */
public class AddRelativeQuantification {

	public static void execute(String[] args) {
		try {
			String originalFile = args[0];
			String ascoreFile = args[1];
			String totalProteomeFile = args[2];
			String groupInfo = args[3];
			String outputFile = args[4];
			
			System.out.println("Running Grab Data From Ascore");
			HashMap ascore = grabDataFromAscore(ascoreFile, groupInfo);
			System.out.println("Load Ascore File");
			HashMap total = grabDataFromTotal(totalProteomeFile, groupInfo);
			System.out.println("Load Total Proteome File");

			FileWriter fwriter = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fwriter);
			
			HashMap uniq = new HashMap();
			int count = 0;
			FileInputStream fstream = new FileInputStream(originalFile);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			in.readLine(); // skip header
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String[] geneName_ids = split[6].split(",");
				String peptide = split[3];
				for (String geneName: geneName_ids) {
					geneName = geneName.toUpperCase();
					if (!geneName.equals("")) {
						String ascoreData = (String)ascore.get(peptide);
						if (total.containsKey(geneName) && !geneName.equals("NA")) {
							HashMap peptideHash = (HashMap)total.get(geneName);
							Iterator itr = peptideHash.keySet().iterator();
							while (itr.hasNext()) {
								String peptideStr = (String)itr.next();
								String totalData = (String)peptideHash.get(peptideStr);
								//String totalData = (String)total.get(geneName);			
								String[] ascoreDataSplit = ascoreData.split("\t");
								double[] ascoreDataNum = new double[ascoreDataSplit.length];
								for (int i = 0; i < ascoreDataSplit.length; i++) {
									ascoreDataNum[i] = new Double(ascoreDataSplit[i]);
								}
								String[] totalDataSplit = totalData.split("\t");
								double[] totalDataNum = new double[totalDataSplit.length];
								for (int i = 0; i < totalDataSplit.length; i++) {
									totalDataNum[i] = new Double(totalDataSplit[i]);
								}
								double pearson = MathTools.PearsonCorrel(ascoreDataNum,  totalDataNum);
								double spearman = MathTools.SpearmanRank(ascoreDataNum,  totalDataNum);
								String tag = peptide + geneName + "_" + peptideStr;
								if (!uniq.containsKey(tag)) {
									out.write(str + "\t" + pearson + "\t" + spearman + "\t" + peptide + "\t" + ascoreData + "\t" + geneName + "_" + peptideStr + "\t" + totalData + "\n");									
									uniq.put(tag, tag);
								}
							}
						}
					}
				}
				count++;
				//System.out.println(count);
			}
			in.close();
			out.close();
			System.out.println("Finish Appending");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap grabAScoreFromPeptide(String fileName) {
		HashMap map = new HashMap();		
		try {					
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String uniprot = split[0];
				String peptide = split[3];
				String loc1 = split[17];
				String score1 = split[18];
				String loc2 = split[19];
				String score2 = split[20];
				String loc3 = split[21];
				String score3 = split[22];
				
				LinkedList list = MotifTools.convert(MotifTools.replaceTag(peptide.trim()));
				
				Iterator itr = list.iterator();
				while (itr.hasNext()) {
					String peptides = (String)itr.next();					
					map.put(peptides, loc1 + "\t" + score1 + "\t" + loc2 + "\t" + score2 + "\t" + loc3 + "\t" + score3);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	public static HashMap grabUniprotNameFromPeptide(String fileName) {
		HashMap map = new HashMap();
		
		
		try {
			
			
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String uniprot = split[0];
				String peptide = split[3];
				LinkedList list = MotifTools.convert(MotifTools.replaceTag(peptide.trim()));
				
				Iterator itr = list.iterator();
				while (itr.hasNext()) {
					String peptides = (String)itr.next();					
					map.put(peptides, uniprot);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	public static HashMap grabDataFromAscore(String fileName, String groupInfo) {
		HashMap map = new HashMap();
		String[] groups = groupInfo.split(":");
		
		try {
			
			
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				String peptide = split[3];
				LinkedList list = MotifTools.convert(MotifTools.replaceTag(peptide.trim()));
				
				Iterator itr = list.iterator();
				while (itr.hasNext()) {
					String peptides = (String)itr.next();
					String data = ""; // the content of the data info
					//String data = split[7];
					
					//for (int i = 8; i <= 16; i++) {
						//data += "\t" + split[i];
					//}
					// buffer is for column where the index of the data starts
					int buffer = 7;
					LinkedList[] list_groups = new LinkedList[groups.length];
					for (int i = 0; i < groups.length; i++) {
						list_groups[i] = new LinkedList();
					}
					// convert the data into a linkedlist
					for (int i = 0 + buffer; i <= 9 + buffer; i++) {
						for (int j = 0; j < groups.length; j++) {
							String[] split_group = groups[j].split(",");
							for (int k = 0; k < split_group.length; k++) {
								int group_id = new Integer(split_group[k].trim());
								if (group_id == (i - buffer)) {
									list_groups[j].add(split[i]);
								}
							}
						}
					}
					
					// place the data into a linkedlist
					
					for (int j = 0; j < groups.length; j++) {
						double[] num = MathTools.convertListStr2Double(list_groups[j]);
						if (j == 0) {
							data += MathTools.mean(num) + "";
						} else {
							data += "\t" + MathTools.mean(num);
						}
					}
					//System.out.println(data);
					map.put(peptides, data);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static HashMap grabDataFromTotal(String fileName, String groupInfo) {
		HashMap map = new HashMap();
		String[] groups = groupInfo.split(":");
		
		try {
			
			
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream din = new DataInputStream(fstream);
			BufferedReader in = new BufferedReader(new InputStreamReader(din));
			in.readLine();
			in.readLine();
			while (in.ready()) {
				String str = in.readLine();
				String[] split = str.split("\t");
				if (split.length > 3) {
					//String acc_id = split[1].split("\\|")[1];
					String geneName = split[3].toUpperCase();
					String peptide = split[8];
					String uniprotName = split[1];
					
					
					if (checkGeneNameUniprotCombo(geneName, uniprotName)) {
						if (split.length <= 23) {
							if (geneName.equals("AKT1")) {
								System.out.println(str);
							}
							//System.out.println(str);
						} else {
							String data = "";
							//String data = split[22];
							//for (int i = 23; i <= 31; i++) {
								//data += "\t" + split[i];
							//}
							LinkedList[] list_groups = new LinkedList[groups.length];
							for (int i = 0; i < groups.length; i++) {
								list_groups[i] = new LinkedList();
							}
							int buffer = 22;
							
							// convert the data into a linkedlist
							for (int i = 0 + buffer; i <= 9 + buffer; i++) {
								for (int j = 0; j < groups.length; j++) {
									String[] split_group = groups[j].split(",");
									for (int k = 0; k < split_group.length; k++) {
										int group_id = new Integer(split_group[k].trim());
										if (group_id == (i - buffer)) {
											list_groups[j].add(split[i]);
										}
									}
								}
							}
							
							// place the data into a linkedlist
							
							for (int j = 0; j < groups.length; j++) {
								double[] num = MathTools.convertListStr2Double(list_groups[j]);
								if (j == 0) {
									data += MathTools.mean(num) + "";
								} else {
									data += "\t" + MathTools.mean(num);
								}
							}
							//System.out.println(geneName + "\t" + data);
							if (map.containsKey(geneName)) {
								HashMap peptideHash = (HashMap)map.get(geneName);
								peptideHash.put(uniprotName, data);
								map.put(geneName, peptideHash);
								if (geneName.equals("AKT1")) {
									System.out.println("Should add to map");
								}
							} else {
								HashMap peptideHash = new HashMap();
								peptideHash.put(uniprotName, data);
								map.put(geneName, peptideHash);
								if (geneName.equals("AKT1")) {
									System.out.println("Else Should add to map");
								}
							}
						}					
					}
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * Specify the specific genename to uniprot geneid combination
	 * @param geneName
	 * @param uniprot
	 * @return
	 */
	public static boolean checkGeneNameUniprotCombo(String geneName, String uniprot) {
	
		if (geneName.toUpperCase().trim().equals("AKT1")) {
			//System.out.println(uniprot);
			if (uniprot.contains("P31750")) {
				//System.out.println("Should find");
				return true;
			}
			if (uniprot.contains("P31749")) {
				//System.out.println("Should find");
				return true;
			}
			return false;
		}
		
		return true;
	}
}
