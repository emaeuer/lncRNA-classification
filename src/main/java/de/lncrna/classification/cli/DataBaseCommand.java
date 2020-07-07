package de.lncrna.classification.cli;

import java.util.Scanner;

import de.lncrna.classification.db.Neo4JHelperServer;
import picocli.CommandLine.Command;

@Command(name = "database",
	headerHeading = "@|bold,underline Usage|@:%n%n",
	synopsisHeading = "%n",
	descriptionHeading = "%n@|bold,underline Description|@:%n%n",
	optionListHeading = "%n@|bold,underline Options|@:%n",
	header = "Start NEO4J database",
	description = "Just start the NEO4J database to enable connections from the NEO4J client",
	separator = " ")
public class DataBaseCommand implements Runnable {
	
	@Override
	public void run() {
		Neo4JHelperServer helper = Neo4JHelperServer.CONNECTION;
		System.out.println("Enter 'stop' to terminate the database");
		Scanner scan = new Scanner(System.in);
		while (!"stop".equalsIgnoreCase(scan.next())) {
			// wait for the user to stop the database
		}
		scan.close();
	}

}
