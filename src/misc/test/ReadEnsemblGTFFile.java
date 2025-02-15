package misc.test;

import idconversion.tools.GTFFile;

import java.util.HashMap;

public class ReadEnsemblGTFFile {

	public static String parameter_info() {
		return "[inputEnsemblFile]";
	}
	public static void execute(String[] args) {
		
		try {
			String inputFile = args[0];
			GTFFile gtf = new GTFFile();
			gtf.initialize(inputFile);
			HashMap map = gtf.transcript2gene;
			System.out.println(map.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
