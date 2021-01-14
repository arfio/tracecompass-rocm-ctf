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

package org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.tests.fused;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.fused.FusedAttributes;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources.StateValues;
import org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.tests.shared.vm.VmTestCase;
import org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.tests.shared.vm.VmTestExperiment;
import org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.tests.shared.vm.VmTraces;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.IntervalInfo;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateIntervalStub;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateSystemTestUtils;

import com.google.common.collect.ImmutableList;

/**
 * Test case for an experiment with only one trace containing one container with
 * one nested container
 *
 * @author Geneviève Bastien
 */
public class SimpleContainersTestCase extends VmTestCase {

    private static final ITmfStateValue CONTAINER_SV_STRING = TmfStateValue.newValueString(VmTraces.ONE_CONTAINER.getHostId());
    private static final long NS_LV0 = 4026531836L;
    private static final long NS_LV1 = 654321;
    private static final long NS_LV2 = 987654;

    /**
     * Constructor
     */
    public SimpleContainersTestCase() {
        super(VmTestExperiment.ONE_CONTAINER);
    }

    @Override
    public Set<IntervalInfo> getTestIntervals() {
        Set<IntervalInfo> info = new HashSet<>();

        /* Verify the 'CPUs/*' attributes: first the machine names */
        ImmutableList<ITmfStateInterval> intervals = ImmutableList.of(new StateIntervalStub(1, 35, CONTAINER_SV_STRING));
        info.add(new IntervalInfo(intervals, FusedAttributes.CPUS, "0", FusedAttributes.MACHINE_NAME));

        intervals = ImmutableList.of(new StateIntervalStub(1, 3, TmfStateValue.nullValue()),
                new StateIntervalStub(4, 35, CONTAINER_SV_STRING));
        info.add(new IntervalInfo(intervals, FusedAttributes.CPUS, "1", FusedAttributes.MACHINE_NAME));

        /* Verify the current thread */
        intervals = ImmutableList.of(new StateIntervalStub(1, 29, TmfStateValue.newValueInt(31)),
                new StateIntervalStub(30, 34, TmfStateValue.newValueInt(43)),
                new StateIntervalStub(35, 35, TmfStateValue.newValueInt(44)));
        info.add(new IntervalInfo(intervals, FusedAttributes.CPUS, "0", FusedAttributes.CURRENT_THREAD));

        intervals = ImmutableList.of(new StateIntervalStub(1, 3, TmfStateValue.nullValue()),
                new StateIntervalStub(4, 16, TmfStateValue.newValueInt(42)),
                new StateIntervalStub(17, 23, TmfStateValue.newValueInt(41)),
                new StateIntervalStub(24, 35, TmfStateValue.newValueInt(42)));
        info.add(new IntervalInfo(intervals, FusedAttributes.CPUS, "1", FusedAttributes.CURRENT_THREAD));

        return info;
    }

    @Override
    public Set<PunctualInfo> getPunctualTestData() {
        Set<PunctualInfo> info = new HashSet<>();

        // Check the 'Machines' sub-tree towards the end of the trace
        PunctualInfo oneInfo = new PunctualInfo(34L);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId()), StateValues.MACHINE_HOST);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CPUS, "0"), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CPUS, "1"), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.PARENT), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1)), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0)), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2)), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.PARENT), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.PARENT), -1L);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.PARENT), NS_LV1);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.THREADS, "40"), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.THREADS, "41"), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.THREADS, "42"), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.THREADS, "43"), 4);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.THREADS, "44"), 5);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.PCPUS, "0"), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV1), FusedAttributes.PCPUS, "1"), null);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.THREADS, "40"), 40);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.THREADS, "41"), 41);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.THREADS, "42"), 42);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.THREADS, "43"), 43);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV0), FusedAttributes.THREADS, "44"), 44);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.THREADS, "42"), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.THREADS, "43"), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.THREADS, "44"), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.PCPUS, "0"), null);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.HOSTS, VmTraces.ONE_CONTAINER.getHostId(), FusedAttributes.CONTAINERS, String.valueOf(NS_LV2), FusedAttributes.PCPUS, "1"), null);

        // Check the 'Threads' sub-tree and VTID informations
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.NS_MAX_LEVEL), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.NS_INUM), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.NS_LEVEL), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.VTID), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.VTID, FusedAttributes.VPPID), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "40", FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 1);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.NS_MAX_LEVEL), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.NS_INUM), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.NS_LEVEL), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.VTID), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.VTID, FusedAttributes.VPPID), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "41", FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 1);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.NS_MAX_LEVEL), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.NS_INUM), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.NS_LEVEL), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.VPPID), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.VTID), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.VPPID), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "42", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 2);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.NS_MAX_LEVEL), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.NS_INUM), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.NS_LEVEL), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID), 4);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.VPPID), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.VTID), 2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.VPPID), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "43", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 2);

        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.NS_MAX_LEVEL), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.NS_INUM), NS_LV0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.NS_LEVEL), 0);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID), 5);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.VPPID), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.VTID), 3);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.VPPID), 1);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_INUM), NS_LV2);
        oneInfo.addValue(StateSystemTestUtils.makeAttribute(FusedAttributes.THREADS, VmTraces.ONE_CONTAINER.getHostId(), "44", FusedAttributes.VTID, FusedAttributes.VTID, FusedAttributes.NS_LEVEL), 2);

        info.add(oneInfo);

        return info;
    }
}
