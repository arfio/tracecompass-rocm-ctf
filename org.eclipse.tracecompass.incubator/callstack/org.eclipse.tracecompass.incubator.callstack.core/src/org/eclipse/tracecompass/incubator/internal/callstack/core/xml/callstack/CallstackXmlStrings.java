/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.callstack.core.xml.callstack;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Geneviève Bastien
 * @noimplement This interface only contains static defines
 */
@SuppressWarnings({ "javadoc"})
public interface CallstackXmlStrings {

    /** The callstack base element string */
    String CALLSTACK = "callstack"; //$NON-NLS-1$
    String CALLSTACK_LEVEL = "level"; //$NON-NLS-1$
    String CALLSTACK_PATH = "path"; //$NON-NLS-1$
    String CALLSTACK_HOST = "host"; //$NON-NLS-1$
    String CALLSTACK_THREAD = "thread"; //$NON-NLS-1$
    String CALLSTACK_THREADCPU = "cpu"; //$NON-NLS-1$
    String CALLSTACK_THREADLEVEL = "level"; //$NON-NLS-1$
    String CALLSTACK_THREADLEVEL_NAME = "name"; //$NON-NLS-1$
    String CALLSTACK_THREADLEVEL_TYPE = "type"; //$NON-NLS-1$
    String CALLSTACK_THREADLEVEL_VALUE = "value"; //$NON-NLS-1$
    String CALLSTACK_GROUP = "callstackGroup"; //$NON-NLS-1$

}
