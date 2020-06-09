package de.lncrna.classification.cli;

import picocli.CommandLine.Command;

@Command(subcommands = {ClusterCommand.class, InitCommand.class})
public class StartCommand implements Runnable {

	
	@Override
	public void run() {
		System.out.println("start");
		
	}

}
