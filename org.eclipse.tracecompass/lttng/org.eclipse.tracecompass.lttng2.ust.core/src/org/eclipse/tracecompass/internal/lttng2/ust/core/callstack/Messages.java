/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.core.callstack;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the call stack analysis module
 *
 * @author Sonia Farrah
 */

public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /** Information regarding events loading prior to the analysis execution */
    public static String LttnUstCallStackAnalysisModule_EventsLoadingInformation;

    private Messages() {
    }
}
