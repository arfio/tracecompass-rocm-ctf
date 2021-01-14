/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.traceevent.ui;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.traceevent.core.trace.TraceEventTrace;
import org.eclipse.tracecompass.incubator.internal.traceevent.ui.markers.ContextMarkerFactory;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceAdapterManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /** The plugin ID */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.incubator.traceevent.ui"; //$NON-NLS-1$

    // The shared instance
    private static @Nullable Activator plugin;

    private @Nullable ContextMarkerFactory fContextMarkerFactory;

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        fContextMarkerFactory = new ContextMarkerFactory();
        TmfTraceAdapterManager.registerFactory(fContextMarkerFactory, TraceEventTrace.class);
        plugin = this;
    }

    @Override
    public void stop(@Nullable BundleContext context) throws Exception {
        plugin = null;
        TmfTraceAdapterManager.unregisterFactory(fContextMarkerFactory);
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static @Nullable Activator getDefault() {
        return plugin;
    }

}
