package de.lncrna.classification;

import de.lncrna.classification.cli.StartCommand;
import picocli.CommandLine;

public class Launcher {
	public static void main(String[] args) {	
		new CommandLine(new StartCommand())
			.execute(args);
	}
}