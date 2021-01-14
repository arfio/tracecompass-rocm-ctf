/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.core.session;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized message strings from the lttng2.core.control.session
 *
 * @author Guilliano Molaire
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.lttng2.control.core.session.messages"; //$NON-NLS-1$

    public static String SessionConfigXML_BadRequirementType;
    public static String SessionConfigXML_DomainTypeMissing;
    public static String SessionConfigXML_EventTypeMissing;
    public static String SessionConfigXML_InvalidSessionInfoList;
    public static String SessionConfigXML_InvalidTraceSessionPath;
    public static String SessionConfigXML_UnknownEventType;
    public static String SessionConfigXML_UnknownDomainBufferType;
    public static String SessionConfigXML_SessionConfigGenerationError;
    public static String SessionConfigXML_XmlParseError;
    public static String SessionConfigXML_XmlValidateError;
    public static String SessionConfigXML_XmlValidationError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
