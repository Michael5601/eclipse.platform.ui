package org.eclipse.ui.internal.ide.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class EclipseStartUpTest {

	static long endTime = 0;

	@Test
	public void testStandardUIStartupTime() {
		int iterations = 1;
		String logFile = "D:\\dev\\oomph\\Bachelor\\startup_times_PNG_125.txt";

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
			for (int i = 1; i <= iterations; i++) {
				long elapsedTime = measureStandardUIStartupTime();
				writer.write(elapsedTime + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private long measureStandardUIStartupTime() {
		endTime = 0;
		Display display = PlatformUI.createDisplay();
		long startTime = System.currentTimeMillis();
		PlatformUI.createAndRunWorkbench(display, new IDEWorkbenchAdvisor(100, null) {

			@Override
			public void postStartup() {
				super.postStartup();

			}

			@Override
			public void eventLoopIdle(Display display) {
				super.eventLoopIdle(display);
				if (endTime == 0) {
					endTime = System.currentTimeMillis();
				}
				display.execute(() -> {
					PlatformUI.getWorkbench().close();
				});
			}

			@Override
			public String getInitialWindowPerspectiveId() {
				return "org.eclipse.debug.ui.DebugPerspective";
			}
		});
		return endTime - startTime;
	}
}
