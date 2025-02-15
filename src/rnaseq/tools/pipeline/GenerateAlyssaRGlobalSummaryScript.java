package rnaseq.tools.pipeline;


import java.io.BufferedWriter;
import java.io.FileWriter;

public class GenerateAlyssaRGlobalSummaryScript {

	public static String type() {
		return "RNASEQ";
	}
	public static String description() {
		return "Generate python script to summarize the RseQC";
	}
	public static String parameter_info() {
		return "[inputFolder] [outputFilePrefix]";
	}
	public static void execute(String[] args) {
		
		try {
			String inputFolder = args[0];
			String outputFilePrefix = args[1];

			FileWriter fwriter = new FileWriter("globalsumm_v2.r");
			BufferedWriter out = new BufferedWriter(fwriter);						

			StringBuffer string_buffer = new StringBuffer();			
			string_buffer.append("summ_dir <- '" + inputFolder + "/'\n");
			string_buffer.append("outfile <- '" + outputFilePrefix + "'\n");
			string_buffer.append("\n");
			string_buffer.append("#load in files to read\n");
			string_buffer.append("summ_files <- list.files(summ_dir, full.names = T)\n");
			string_buffer.append("\n");
			string_buffer.append("#subset only summary_row files\n");
			string_buffer.append("summ_files <- subset(summ_files,grepl('summary_row', summ_files))\n");
			string_buffer.append("\n");
			string_buffer.append("#make data tables from files\n");
			string_buffer.append("tablelist <- lapply(summ_files, FUN = function(x){\n");
			string_buffer.append("  read.table(x, header = T, sep = '\t', check.names = F)})\n");
			string_buffer.append("\n");
			string_buffer.append("#rename according to sample name\n");
			string_buffer.append("summ_dir2 <- list.files(summ_dir, full.names = F)\n");
			string_buffer.append("summ_dir3 <- subset(summ_dir2,grepl('summary_row', summ_dir2))\n");
			string_buffer.append("summ_dir4 <- gsub('_summary_row.tsv$','',summ_dir3)\n");
			string_buffer.append("names(tablelist) <- summ_dir4\n");
			string_buffer.append("\n");
			string_buffer.append("#combine separate samples to global summary table\n");
			string_buffer.append("gsumm <- Reduce(function(x,y) merge(x,y,all = T), tablelist, accumulate=F)\n");
			string_buffer.append("\n");
			string_buffer.append("#write output table\n");
			string_buffer.append("write.table(gsumm, file = paste(outfile,'.tsv', sep = ''), row.names = F, sep = '\t')\n");
			
			/* previous code
			 * string_buffer.append("\n");
			string_buffer.append("#load in files to read\n");
			string_buffer.append("summ_files <- list.files(summ_dir, full.names = T)\n");
			string_buffer.append("\n");
			string_buffer.append("#make data tables from files\n");
			string_buffer.append("tablelist <- lapply(summ_files, FUN = function(x){\n");
			string_buffer.append("  read.table(x, header = T, sep = '\t')})\n");
			string_buffer.append("\n");
			string_buffer.append("#rename according to sample name\n");
			string_buffer.append("names(tablelist) <- list.files(summ_dir, full.names = F)\n");
			string_buffer.append("names(tablelist) <- gsub('_summary_row.tsv$','',names(tablelist))\n");
			string_buffer.append("\n");
			string_buffer.append("#combine separate samples to global summary table\n");
			string_buffer.append("gsumm <- Reduce(function(x,y) merge(x,y,all = T), tablelist, accumulate=F)\n");
			string_buffer.append("\n");
			string_buffer.append("#write output table\n");
			string_buffer.append("write.table(gsumm, file = paste(outfile,'.tsv', sep = ''), row.names = F, sep = '\t')\n");
			*/
			
			
			out.write(string_buffer.toString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
