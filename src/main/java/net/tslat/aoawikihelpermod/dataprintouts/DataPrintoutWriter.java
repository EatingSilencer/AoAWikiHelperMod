package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.ICommandSender;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class DataPrintoutWriter {
	public static File configDir = null;
	private static PrintWriter writer = null;

	public static void writeData(String name, List<String> data, ICommandSender sender, boolean copyToClipboard) {
		String fileName = name + " Printout " + AdventOfAscension.version + ".txt";

		enableWriter(fileName);

		data.forEach(DataPrintoutWriter::write);

		disableWriter();
		sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Generated data file: ", new File(configDir, fileName), name, copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
	}

	private static void enableWriter(final String fileName) {
		configDir = AoAWikiHelperMod.prepConfigDir("Data Printouts");

		File streamFile = new File(configDir, fileName);

		try {
			if (streamFile.exists())
				streamFile.delete();

			streamFile.createNewFile();

			writer = new PrintWriter(streamFile);
		}
		catch (Exception e) {}
	}

	private static void disableWriter() {
		if (writer != null)
			IOUtils.closeQuietly(writer);

		writer = null;
	}

	private static void write(String line) {
		if (writer != null)
			writer.println(line);
	}
}
