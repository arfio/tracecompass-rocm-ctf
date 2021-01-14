/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.junit.Assert;

/**
 *  Singleton class to facilitate the test cases. Creates UML2SD view and loader objects as well as provides
 *  utility methods for interacting with the loader/view.
 */
@SuppressWarnings("javadoc")
public class TraceControlTestFacility {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    public final static int WAIT_FOR_JOBS_DELAY = 50;
    public final static int GUI_REFESH_DELAY = 500;

    public final static String DIRECTORY = "testfiles";
    public final static String COMMAND_CATEGORY_PREFIX = "org.eclipse.linuxtools.internal.lttng2.ui.commands.control.";
    public final static String SCEN_INIT_TEST = "Initialize";
    public final static String SCEN_SCENARIO_SESSION_HANDLING = "SessionHandling";
    public final static String SCEN_SCENARIO_SESSION_HANDLING_WITH_PATH = "SessionHandlingWithPath";

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static TraceControlTestFacility fInstance = null;
    private ControlView fControlView = null;
    private volatile boolean fIsInitialized = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    private TraceControlTestFacility() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public static TraceControlTestFacility getInstance() {
        if (fInstance == null) {
            fInstance = new TraceControlTestFacility();
        }
        return fInstance;
    }

    /**
     * Initial the test facility.
     */
    public void init() {

        if (!fIsInitialized) {
            IViewPart view;
            try {
                hideView("org.eclipse.ui.internal.introview");
                view = showView(ControlView.ID);
            } catch (PartInitException e) {
                throw new RuntimeException(e);
            }
            fControlView = (ControlView) view;

            /*
             * It is possible that the connections are saved due to the
             * auto-save feature of the workbench which calls
             * ControlView.saveState(IMemento). This can happen at any
             * time (e.g. when calling delay()).
             *
             * When showing the view above ControlView.init(IMemento) is
             * called which restores saved connections.
             *
             * The tests require that the ControlView is empty. So
             * we remove all the connection nodes from the root.
             */
            fControlView.getTraceControlRoot().removeAllChildren();

            fIsInitialized = true;
        }
    }

    /**
     * Disposes the facility (and GUI)
     */
    public void dispose() {
        if (fIsInitialized) {
            waitForJobs();
            hideView(ControlView.ID);
            delay(200);
            fIsInitialized = false;
        }
    }

    /**
     * Creates a delay for given time.
     * @param waitTimeMillis - time in milli seconds
     */
    public void delay(long waitTimeMillis) {
        Display display = Display.getCurrent();
        if (display != null) {
            long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while(System.currentTimeMillis() < endTimeMillis) {
                if (!display.readAndDispatch()) {
                    // We do not use Display.sleep because it might never wake up
                    // if there is no user interaction
                    try {
                        Thread.sleep(Math.min(waitTimeMillis, 10));
                    } catch (final InterruptedException e) {
                        // Ignored
                    }
                }
                display.update();
            }
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (InterruptedException e) {
                // Ignored
            }
        }
    }

    /**
     * Waits for a connection to be connected
     */
    public void waitForConnect(TargetNodeComponent node) {
        for (int i = 1; i < 5000 && node.getTargetNodeState() == TargetNodeState.CONNECTING; i *= 2) {
            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {
                Assert.fail();
            }
        }
    }

    /**
     * Waits for a view to be closed
     */
    public void waitForViewClosed(String viewId) {
        for (int i = 1; i < 5000 && (getViewPart(viewId) != null); i *= 2) {
            delay(i);
        }
    }

    /**
     * Waits for a view to be closed
     */
    public void waitForViewOpend(String viewId) {
        for (int i = 1; i < 5000 && (getViewPart(viewId) == null); i *= 2) {
            delay(i);
        }
    }

    /**
     * Waits for all Eclipse jobs to finish
     */
    public void waitForJobs() {
        WaitUtils.waitForJobs();
    }

    private IViewPart showView(String viewId) throws PartInitException {
        IViewPart view = getViewPart(viewId);

        if (view == null) {
            view = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage().showView(viewId);

            waitForViewOpend(viewId);
        }
        assertNotNull(view);
        return view;
    }

    private void hideView(String viewId) {
        IViewPart view = getViewPart(viewId);
        if (view != null) {
            PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage().hideView(view);
        }
        waitForViewClosed(viewId);
    }

    private static IViewPart getViewPart(String viewId) {
        return PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow()
        .getActivePage()
        .findView(viewId);
    }

    /**
     * @return current control view
     */
    public ControlView getControlView() {
        return fControlView;
    }

    /**
     * Executes an Eclipse command with command ID after selecting passed component
     * @param component - component to select in the tree
     * @param commandId - command ID
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(ITraceControlComponent component, String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        setSelection(component);
        executeCommand(commandId);
    }

    /**
     * Executes an Eclipse command with command ID after selecting passed components
     * @param components - array of components to select in the tree
     * @param commandId - command ID
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(ITraceControlComponent[] components, String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        setSelection(components);
        executeCommand(commandId);
    }

    /**
     * Executes an Eclipse command with command ID
     * @param commandId
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        Object handlerServiceObject = fControlView.getSite().getService(IHandlerService.class);
        IHandlerService handlerService = (IHandlerService) handlerServiceObject;
        handlerService.executeCommand(COMMAND_CATEGORY_PREFIX + commandId, null);
        waitForJobs();
    }

    /**
     * Selects passed component
     * @param component - component to select in the tree
     * @param commandId - command ID
     */
    public void setSelection(ITraceControlComponent component) {
        fControlView.setSelection(component);
        // Selection is done in own job
        waitForJobs();
    }


    /**
     * Selects passed components
     * @param components - array of component to select in the tree
     * @param commandId - command ID
     */
    public void setSelection(ITraceControlComponent[] components) {
        fControlView.setSelection(components);

        // Selection is done in own job
        waitForJobs();
    }

    /**
     * Creates session on passed session group.
     * @param group - session group
     * @return - trace session group if it's successful else null
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public TraceSessionComponent createSession(ITraceControlComponent group) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(group, "createSession");

        ITraceControlComponent[] sessions = group.getChildren();
        if ((sessions == null) || (sessions.length == 0)) {
            return null;
        }
        return (TraceSessionComponent)sessions[0];
    }

    /**
     * Destroys a given session.
     * @param session - session to destroy
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void destroySession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "destroySession");
    }

    /**
     * Starts a given session
     * @param session - session to start
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void startSession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "start");
    }

    /**
     * Stops a given session
     * @param session - session to stop
     * @throws ExecutionException
     * @throws NotDefinedException
     * @throws NotEnabledException
     * @throws NotHandledException
     */
    public void stopSession(TraceSessionComponent session) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        executeCommand(session, "stop");
    }
}
