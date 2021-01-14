/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Handler for task migration events. Normally moves a (non-running) process
 * from one run queue to another.
 *
 * @author Alexandre Montplaisir
 */
public class SchedMigrateTaskHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            The event layout to use
     */
    public SchedMigrateTaskHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Long tid = event.getContent().getFieldValue(Long.class, getLayout().fieldTid());
        Long destCpu = event.getContent().getFieldValue(Long.class, getLayout().fieldDestCpu());

        if (tid == null || destCpu == null) {
            return;
        }

        long t = event.getTimestamp().toNanos();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), null);
        if (threadAttributeName == null) {
            /* Swapper threads do not get migrated */
            return;
        }
        int threadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /*
         * Put the thread in the "wait for cpu" state. Some older versions of
         * the kernel/tracers may not have the corresponding sched_waking events
         * that also does so, so we can set it at the migrate, if applicable.
         */
        ss.modifyAttribute(t, ProcessStatus.WAIT_CPU.getStateValue().unboxValue(), threadNode);

        /* Update the thread's running queue to the new one indicated by the event */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        ss.modifyAttribute(t, destCpu.intValue(), quark);
    }

}
