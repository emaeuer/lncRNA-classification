package de.lncrna.classification.cli;

import picocli.CommandLine.Command;

@Command(subcommands = {ClusterCommand.class, InitCommand.class, FilterCommand.class, DataBaseCommand.class, ScoreCommand.class})
public class StartCommand implements Runnable {

	@Override
	public void run() {
		// do nothing
	}

}
