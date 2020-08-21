package de.lncrna.classification.cli;

import java.io.IOException;
import java.io.PrintStream;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarStyle;

public class ProgressBarHelper {

	// Adjusted implementation of ConsoleProgressBarConsumer (Inheritance is not possible)
	private class CustomProgressBarConsumer implements ProgressBarConsumer {

		private final PrintStream out;
		private Terminal terminal = null;
		
		private int maxProgressLength = 80;

		public CustomProgressBarConsumer(int maxProgressLength) {
			this.maxProgressLength = maxProgressLength;
			try {
				this.terminal = TerminalBuilder.builder().dumb(true).build();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out = System.out;
		}

		@Override
		public int getMaxProgressLength() {
			return this.maxProgressLength;
		}

		@Override
		public void accept(String str) {
			out.print('\r'); // before update
			out.print(str);
		}

		@Override
		public void close() {
			out.println();
			out.flush();
			try {
				terminal.close();
			} catch (IOException ignored) {
				/* noop */ }
		}

	}

	private ProgressBar bar = null;

	public void stop() {
		if (this.bar != null) {
			this.bar.close();
		}
	}

	public synchronized void next() {
		if (this.bar != null) {
			this.bar.step();
		}
	}

	public void nextBlock(long numberOfCalculations, String taskName) {
		if (this.bar != null) {
			stop();
		}

		this.bar = new ProgressBarBuilder()
				.setInitialMax(numberOfCalculations)
				.setTaskName(taskName)
				.setStyle(ProgressBarStyle.ASCII)
				.setUpdateIntervalMillis(2000)
				.showSpeed()
				.setConsumer(new CustomProgressBarConsumer(160))
				.build();
	}

}
