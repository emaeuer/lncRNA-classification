package de.lncrna.classification.util.csv;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DistanceCSVPrinter implements Closeable {
	
	private final CSVPrinter printer;

	public DistanceCSVPrinter(String fileName, String[] header, boolean append) throws IOException {
		if (append) {
			this.printer = new CSVPrinter(new FileWriter(fileName, append), CSVFormat.EXCEL);
		} else {
			this.printer = new CSVPrinter(new FileWriter(fileName, append), CSVFormat.EXCEL.withHeader(header));
		}
	}

	public void addRecord(List<Object> recordEntries) throws IOException {
		this.printer.printRecord(recordEntries);
		this.printer.flush();
	}

	@Override
	public void close() throws IOException {
		this.printer.close();
	}
}
