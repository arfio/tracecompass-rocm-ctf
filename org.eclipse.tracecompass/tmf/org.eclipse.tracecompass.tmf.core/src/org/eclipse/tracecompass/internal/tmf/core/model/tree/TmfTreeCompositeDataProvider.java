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

package org.eclipse.tracecompass.internal.tmf.core.model.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Represents a base implementation of {@link ITmfTreeDataProvider} that
 * supports experiments. Clients of this data provider must provide a list of
 * {@link ITmfTreeDataProvider} for each trace in the experiment which supports
 * the provider. From the list of sub data provider, this data provider will
 * merge all responses into one.
 *
 * @param <M>
 *            The type of {@link ITmfTreeDataModel} that this composite's tree
 *            provider must return.
 * @param <P>
 *            The type of {@link ITmfTreeDataProvider} that this composite must
 *            encapsulate
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class TmfTreeCompositeDataProvider<M extends ITmfTreeDataModel, P extends ITmfTreeDataProvider<M>> implements ITmfTreeDataProvider<M>, IOutputAnnotationProvider {

    private final CopyOnWriteArrayList<P> fProviders = new CopyOnWriteArrayList<>();
    private final String fId;

    /**
     * Return a composite {@link ITmfTreeDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param id
     *            the provider's ID
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTreeCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> create(Collection<ITmfTrace> traces, String id) {
        List<@NonNull ITmfTreeDataProvider<ITmfTreeDataModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITmfTreeDataProvider<ITmfTreeDataModel> provider = DataProviderManager.getInstance().getDataProvider(child, id, ITmfTreeDataProvider.class);
            if (provider != null) {
                providers.add(provider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTreeCompositeDataProvider<>(providers, id);
    }

    /**
     * Constructor
     *
     * @param providers
     *            A list of data providers. Each data provider should be associated
     *            to a different trace.
     * @param id
     *            the provider's ID
     */
    public TmfTreeCompositeDataProvider(List<P> providers, String id) {
        fProviders.addAll(providers);
        fId = id;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<M>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<M> series = ImmutableList.builder();
        List<ITableColumnDescriptor> columnDescriptor = null;

        for (P dataProvider : fProviders) {
            TmfModelResponse<TmfTreeModel<M>> response = dataProvider.fetchTree(fetchParameters, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            TmfTreeModel<M> model = response.getModel();
            if (model != null) {
                series.addAll(model.getEntries());
                // Use the column descriptor of the first model. All descriptors are supposed to be the same
                if (columnDescriptor == null) {
                    columnDescriptor = model.getColumnDescriptors();
                }
            }
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }

        TmfTreeModel.Builder<M> treeModelBuilder = new TmfTreeModel.Builder<>();
        if (columnDescriptor == null) {
            columnDescriptor = Collections.emptyList();
        }
        treeModelBuilder.setColumnDescriptors(columnDescriptor)
                        .setEntries(series.build());

        if (isComplete) {
            return new TmfModelResponse<>(treeModelBuilder.build(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(treeModelBuilder.build(), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    @Override
    public String getId() {
        return fId;
    }

    /**
     * Get the list of encapsulated providers
     *
     * @return the list of encapsulated providers
     */
    protected List<P> getProviders() {
        return fProviders;
    }

    /**
     * Adds a new data provider to the list of providers
     *
     * @param dataProvider
     *              a data provider to add
     */
    protected void addProvider(P dataProvider) {
        fProviders.add(dataProvider);
    }

    /**
     * Removes a give data provider from the list of providers.
     * It will dispose the data provider.
     *
     * @param dataProvider
     *            the data provider to remove
     */
    protected void removeProvider(P dataProvider) {
        fProviders.remove(dataProvider);
        dataProvider.dispose();
    }

    @Override
    public void dispose() {
        fProviders.forEach(ITmfTreeDataProvider::dispose);
        fProviders.clear();
    }

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Set<String> categories = new HashSet<>();
        for (P dataProvider : getProviders()) {
            if (dataProvider instanceof IOutputAnnotationProvider) {
                TmfModelResponse<AnnotationCategoriesModel> response = ((IOutputAnnotationProvider) dataProvider).fetchAnnotationCategories(fetchParameters, monitor);
                AnnotationCategoriesModel model = response.getModel();
                if (model != null) {
                    categories.addAll(model.getAnnotationCategories());
                }
            }
        }
        if (categories.isEmpty()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(new AnnotationCategoriesModel(ImmutableList.copyOf(categories)), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableMap.Builder<String, Collection<Annotation>> annotationsBuilder = ImmutableMap.builder();

        for (P dataProvider : getProviders()) {
            if (dataProvider instanceof IOutputAnnotationProvider) {
                TmfModelResponse<AnnotationModel> response = ((IOutputAnnotationProvider) dataProvider).fetchAnnotations(fetchParameters, monitor);
                isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
                AnnotationModel model = response.getModel();
                if (model != null) {
                    annotationsBuilder.putAll(model.getAnnotations());
                }

                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
            }
        }
        if (isComplete) {
            return new TmfModelResponse<>(new AnnotationModel(annotationsBuilder.build()), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(new AnnotationModel(annotationsBuilder.build()), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }
}

