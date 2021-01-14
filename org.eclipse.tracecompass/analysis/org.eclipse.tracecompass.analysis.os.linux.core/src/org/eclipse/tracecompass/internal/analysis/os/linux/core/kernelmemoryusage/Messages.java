/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelmemoryusage;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the kernel memory usage messages
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelmemoryusage.messages"; //$NON-NLS-1$

    /** Data Provider help text */
    public static String KernelMemoryDataProviderFactory_descriptionText;

    /**
     * Kernel Memory Usage's title view
     */
    public static String KernelMemoryUsageDataProvider_title;

    /**
     * Kernel Memory Usage's total series name
     */
    public static String KernelMemoryUsageDataProvider_Total;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
