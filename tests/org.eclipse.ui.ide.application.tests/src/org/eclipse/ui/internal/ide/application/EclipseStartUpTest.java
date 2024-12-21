package org.eclipse.ui.internal.ide.application;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class EclipseStartUpTest {

	static long endTime;

	@Test
	public void testStandardUIStartupTime() throws Exception {
		endTime = 0;
		long startTime = System.currentTimeMillis();
		PlatformUI.createAndRunWorkbench(PlatformUI.createDisplay(), new WorkbenchAdvisor() {

			@Override
			public void eventLoopIdle(Display display) {
				if (endTime == 0) {
					endTime = System.currentTimeMillis();
				}
			}

			@Override
			public String getInitialWindowPerspectiveId() {
				return "org.eclipse.ui.resourcePerspective";
			}
		});
		long elapsedTime = endTime - startTime;
		System.out.println("Eclipse UI startup time: " + elapsedTime + " ms");
	}
}
