/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

/**
 * Command handler implementation to enable one or more loggers.
 *
 * @author Bruno Roy
 */
public class EnableLoggerHandler extends ChangeLoggerStateHandler {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    protected TraceEnablement getNewState() {
        return TraceEnablement.ENABLED;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void changeState(TraceDomainComponent domain, List<String> loggerNames, ITraceLogLevel logLevel, LogLevelType logLevelType, IProgressMonitor monitor) throws ExecutionException {
        // This conditional statement makes it possible to enable the same logger multiple
        // times with different log levels (with the right-click enable, directly on the logger)
        if (logLevelType.equals(LogLevelType.LOGLEVEL_ALL)) {
            domain.enableEvents(loggerNames, null, null, monitor);
        } else {
            domain.enableLogLevel(loggerNames, logLevelType, logLevel, null, domain.getDomain(), monitor);
        }
    }
}
