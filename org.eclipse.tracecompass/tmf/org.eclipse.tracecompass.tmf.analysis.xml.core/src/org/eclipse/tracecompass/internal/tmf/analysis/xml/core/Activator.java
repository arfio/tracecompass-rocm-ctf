/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Geneviève Bastien
 */
public class Activator extends Plugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.analysis.xml.core"; //$NON-NLS-1$

    // The shared instance
    private static Activator fPlugin;

    private IEclipsePreferences fCorePreferenceStore;

    /**
     * The constructor
     */
    public Activator() {
        setDefault(this);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
        XmlUtils.loadFilesStatus();
        XmlUtils.initOutputElements();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        XmlUtils.saveFilesStatus();
        setDefault(null);
        XmlDataProviderManager.dispose();
        XmlUtils.clearOutputElements();
        super.stop(context);
    }

    /**
     * Returns a preference store for org.eclipse.tracecompass.tmf.analysis.xml.core preferences
     * @return the preference store
     */
    public IEclipsePreferences getCorePreferenceStore() {
        if (fCorePreferenceStore == null) {
            fCorePreferenceStore = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
        }
        return fCorePreferenceStore;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    // Sets plug-in instance
    private static void setDefault(Activator plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Log an IStatus
    // ------------------------------------------------------------------------

    /**
     * Log an IStatus object directly
     *
     * @param status
     *            The status to log
     */
    public static void log(IStatus status) {
        fPlugin.getLog().log(status);
    }

    // ------------------------------------------------------------------------
    // Log INFO
    // ------------------------------------------------------------------------

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logInfo(String message) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logInfo(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log WARNING
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logWarning(String message) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logWarning(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log ERROR
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logError(String message) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logError(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }
}
