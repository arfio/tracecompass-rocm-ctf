/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.ui.views.vcpuview;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$"

    public static @Nullable String VcpuStyles_idle = null;
    public static @Nullable String VcpuStyles_vcpuUsermode = null;
    public static @Nullable String VcpuStyles_waitVmm = null;
    public static @Nullable String VcpuStyles_vcpuPreempted = null;
    public static @Nullable String VcpuStyles_wait = null;
    public static @Nullable String VcpuStyles_waitBlocked = null;
    public static @Nullable String VcpuStyles_waitForCPU = null;
    public static @Nullable String VcpuStyles_usermode = null;
    public static @Nullable String VcpuStyles_systemCall = null;
    public static @Nullable String VcpuStyles_Interrupt = null;
    public static @Nullable String VcpuStyles_unknow;

    public static @Nullable String VmView_threads;
    public static @Nullable String VmView_stateTypeName;
    public static @Nullable String VmView_multipleStates;
    public static @Nullable String VmView_nextResourceActionNameText;
    public static @Nullable String VmView_nextResourceActionToolTipText;
    public static @Nullable String VmView_previousResourceActionNameText;
    public static @Nullable String VmView_previousResourceActionToolTipText;
    public static @Nullable String VmView_VCpu;
    public static @Nullable String VmView_virtualMachine;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
