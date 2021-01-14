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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the XML analysis module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.core.messages"; //$NON-NLS-1$

    /**
     * Default title of an XML XY chart
     */
    public static String XmlDataProvider_DefaultXYTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
