package de.lncrna.classification.cli;

import java.util.stream.IntStream;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class ProgressBarHelper {
		
		private final ProgressBar bar;
		
		/**
		 * Progress bar for distance calculation
		 * --> total number of calculations equals (listSize * (listSize + 1)) /2
		 * 
		 * @param listSize
		 * @param startIndex
		 * @param taskName
		 */
		public ProgressBarHelper(int listSize, int startIndex, String taskName) {
			long totalNumberOfCalculations = (Long.valueOf(listSize) * (listSize + 1)) / 2;
			this.bar = new ProgressBarBuilder()
					.setInitialMax(totalNumberOfCalculations)
//					.setTaskName(taskName)
					.setStyle(ProgressBarStyle.ASCII)
					.setUpdateIntervalMillis(2000)
					.showSpeed()
					.build();
			this.bar.stepTo(IntStream.range(0, startIndex).sum());
		}
		
		/**
		 * Progress bar for filtering 
		 * --> total number of calculations equals the size of the list
		 * 
		 * @param listSize
		 * @param taskName
		 */
		public ProgressBarHelper(int listSize, String taskName) {
			this.bar = new ProgressBarBuilder()
					.setInitialMax(listSize)
//					.setTaskName(taskName)
					.setStyle(ProgressBarStyle.ASCII)
					.setUpdateIntervalMillis(2000)
					.showSpeed()
					.build();
		}
		
		public void stop() {
			this.bar.close();
		}

		public synchronized void next() {
			this.bar.step();
		}
		
}
