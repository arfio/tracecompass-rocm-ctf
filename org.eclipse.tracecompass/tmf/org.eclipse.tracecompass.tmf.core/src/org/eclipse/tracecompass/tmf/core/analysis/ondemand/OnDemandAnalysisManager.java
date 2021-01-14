/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.ondemand;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

/**
 * Manager for {@link IOnDemandAnalysis}.
 *
 * Takes care of reading the extension point information, and providing analyses
 * for traces via the {@link #getOndemandAnalyses(ITmfTrace)} method.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class OnDemandAnalysisManager {

    /** The singleton instance */
    private static @Nullable OnDemandAnalysisManager INSTANCE;

    private static final String EXTENSION_POINT_ID = "org.eclipse.tracecompass.tmf.core.analysis.ondemand"; //$NON-NLS-1$
    private static final String ELEM_NAME_ANALYSIS = "analysis"; //$NON-NLS-1$

    private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    private final LoadingCache<ITmfTrace, Set<IOnDemandAnalysis>> analysisCache = checkNotNull(CacheBuilder.newBuilder()
            .weakKeys()
            .softValues()
            .build(new CacheLoader<ITmfTrace, Set<IOnDemandAnalysis>>() {
                    @Override
                    public Set<IOnDemandAnalysis> load(ITmfTrace trace) {
                        return fAnalysisWrappers.stream()
                                .map(wrapper -> wrapper.analysis)
                                .filter(analysis -> analysis.appliesTo(trace))
                                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
                    }
            }));

    private final Set<OndemandAnalysisWrapper> fAnalysisWrappers;

    /**
     * Internal class used to store extension point information
     */
    private static class OndemandAnalysisWrapper {

        public final IOnDemandAnalysis analysis;

        public OndemandAnalysisWrapper(IOnDemandAnalysis analysis) {
            this.analysis = analysis;
        }

        @Override
        public int hashCode() {
            return this.analysis.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (!(o instanceof OndemandAnalysisWrapper)) {
                return false;
            }
            OndemandAnalysisWrapper other = (OndemandAnalysisWrapper) o;

            return this.analysis.getName().equals(other.analysis.getName());
        }
    }

    /**
     * Get the instance of this manager.
     *
     * @return The singleton instance of this class
     */
    public static synchronized OnDemandAnalysisManager getInstance() {
        OnDemandAnalysisManager inst = INSTANCE;
        if (inst == null) {
            inst = new OnDemandAnalysisManager();
            INSTANCE = inst;
        }
        return inst;
    }

    /**
     * Private constructor, should only be called via {@link #getInstance()}.
     */
    private OnDemandAnalysisManager() {
        fAnalysisWrappers = new HashSet<>();
        IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);

        for (IConfigurationElement element : configElements) {
            if (ELEM_NAME_ANALYSIS.equals(element.getName())) {
                try {
                    Object extension = element.createExecutableExtension(ATTR_CLASS);
                    if (extension != null) {
                        fAnalysisWrappers.add(new OndemandAnalysisWrapper((IOnDemandAnalysis) extension));
                    }
                } catch (CoreException | ClassCastException e) {
                    Activator.logError("Exception while loading extension point", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Get all the registered on-demand analyses that apply to the given trace.
     *
     * @param trace
     *            The trace to get the analyses for
     * @return The set of on-demand analyses that apply to this trace. It can be
     *         empty, but not null
     */
    public Set<IOnDemandAnalysis> getOndemandAnalyses(ITmfTrace trace) {
        return checkNotNull(analysisCache.getUnchecked(trace));
    }

    /**
     * Registers an on-demand analysis to this manager.
     *
     * @param analysis On-demand analysis to register
     */
    public void registerAnalysis(IOnDemandAnalysis analysis) {
        if (fAnalysisWrappers.stream().anyMatch(wrapper -> wrapper.analysis.getName().equals(analysis.getName()))) {
            Activator.logWarning(String.format("Ignoring external analysis with existing name \"%s\"", analysis.getName())); //$NON-NLS-1$
            return;
        }

        fAnalysisWrappers.add(new OndemandAnalysisWrapper(analysis));
        analysisCache.invalidateAll();
    }

    /**
     * Unregisters an on-demand analysis from this manager.
     *
     * @param analysis On-demand analysis to unregister
     */
    public void unregisterAnalysis(IOnDemandAnalysis analysis) {
        if (!analysis.isUserDefined()) {
            Activator.logWarning(String.format("Not unregistering built-in on-demand analysis \"%s\"", analysis.getName())); //$NON-NLS-1$
            return;
        }

        fAnalysisWrappers.remove(new OndemandAnalysisWrapper(analysis));
        analysisCache.invalidateAll();
    }

}
