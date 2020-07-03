package de.lncrna.classification;

import de.lncrna.classification.cli.StartCommand;
import picocli.CommandLine;

public class Launcher {
	public static void main(String[] args) {	
		new CommandLine(new StartCommand()).execute(args);
		
//		System.out.println(Neo4JCypherQueries.getAllSequenceNames());
		
//		try {
//			Thread.sleep(1000000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}