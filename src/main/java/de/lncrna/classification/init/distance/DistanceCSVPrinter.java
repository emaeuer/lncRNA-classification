package de.lncrna.classification.init.distance;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DistanceCSVPrinter implements Closeable {
	private final CSVPrinter printer;

	public DistanceCSVPrinter(String fileName, String[] header) throws IOException {
		this.printer = new CSVPrinter(new FileWriter(fileName), CSVFormat.DEFAULT.withHeader(header));
	}

	public void addRecord(List<Object> recordEntries) throws IOException {
		this.printer.printRecord(recordEntries);
	}

	@Override
	public void close() throws IOException {
		this.printer.close();
	}
}
