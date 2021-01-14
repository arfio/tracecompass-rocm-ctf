/*******************************************************************************
 * Copyright (c) 2010, 2019 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Experiment instantiated with experiment type
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.editors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.internal.tmf.ui.editors.TmfTableColumnUtils;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.Messages;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.google.common.collect.Iterables;

/**
 * Editor for TMF events
 *
 * @author Patrick Tasse
 */
public class TmfEventsEditor extends TmfEditor implements ITmfTraceEditor, IReusableEditor, IPropertyListener, IResourceChangeListener, ISelectionProvider, ISelectionChangedListener, IPartListener, IGotoMarker {

    /** ID for this class */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.editors.events"; //$NON-NLS-1$

    private TmfEventsTable fEventsTable;
    private IFile fFile;
    private ITmfTrace fTrace;
    private Composite fParent;
    private ListenerList<ISelectionChangedListener> fSelectionChangedListeners = new ListenerList<>();
    private boolean fTraceSelected;
    private IMarker fPendingGotoMarker;

    @Override
    public void doSave(final IProgressMonitor monitor) {
        // Do nothing
    }

    @Override
    public void doSaveAs() {
        // Do nothing
    }

    @Override
    public void init(final IEditorSite site, IEditorInput input) throws PartInitException {
        IFileEditorInput fileEditorInput;
        if (input instanceof TmfEditorInput) {
            fFile = ((TmfEditorInput) input).getFile();
            fTrace = ((TmfEditorInput) input).getTrace();
            /* change the input to a FileEditorInput to allow open handlers to find this editor */
            fileEditorInput = new FileEditorInput(fFile);
        } else if (input instanceof IFileEditorInput) {
            fileEditorInput = (IFileEditorInput) input;
            fFile = fileEditorInput.getFile();
            if (fFile == null) {
                throw new PartInitException("Invalid IFileEditorInput: " + fileEditorInput); //$NON-NLS-1$
            }
            try {
                final String traceTypeId = fFile.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (traceTypeId == null) {
                    throw new PartInitException(Messages.TmfOpenTraceHelper_NoTraceType);
                }

                if (ITmfEventsEditorConstants.EXPERIMENT_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                    // Special case: experiment bookmark resource
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    if (project == null) {
                        throw new PartInitException(Messages.TmfOpenTraceHelper_NoTraceType);
                    }
                    TmfExperimentFolder experimentFolder = project.getExperimentsFolder();
                    if (experimentFolder != null) {
                        for (final TmfExperimentElement experimentElement : experimentFolder.getExperiments()) {
                            if (experimentElement.getResource().equals(fFile.getParent())) {
                                setPartName(experimentElement.getName());
                                super.setSite(site);
                                super.setInput(fileEditorInput);
                                TmfOpenTraceHelper.reopenTraceFromElement(experimentElement, this);
                                return;
                            }
                        }
                    }
                } else if (ITmfEventsEditorConstants.TRACE_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                    // Special case: trace bookmark resource
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    if (project == null) {
                        throw new PartInitException(Messages.TmfOpenTraceHelper_NoTraceType);
                    }
                    final TmfTraceFolder tracesFolder = project.getTracesFolder();
                    if (tracesFolder != null) {
                        for (final TmfTraceElement traceElement : tracesFolder.getTraces()) {
                            if (traceElement.getResource().equals(fFile.getParent())) {
                                setPartName(traceElement.getElementPath());
                                super.setSite(site);
                                super.setInput(fileEditorInput);
                                TmfOpenTraceHelper.reopenTraceFromElement(traceElement, this);
                                return;
                            }
                        }
                    }
                } else {
                    final TmfProjectElement project = TmfProjectRegistry.getProject(fFile.getProject(), true);
                    final TmfTraceFolder tracesFolder = project.getTracesFolder();
                    if (tracesFolder != null) {
                        for (final TmfTraceElement traceElement : tracesFolder.getTraces()) {
                            if (traceElement.getResource().equals(fFile)) {
                                setPartName(traceElement.getElementPath());
                                super.setSite(site);
                                super.setInput(fileEditorInput);
                                TmfOpenTraceHelper.reopenTraceFromElement(traceElement, this);
                                return;
                            }
                        }
                    }
                }
            } catch (final PartInitException e) {
                throw e;
            } catch (final InvalidRegistryObjectException e) {
                Activator.getDefault().logError("Error initializing TmfEventsEditor", e); //$NON-NLS-1$
            } catch (final CoreException e) {
                Activator.getDefault().logError("Error initializing TmfEventsEditor", e); //$NON-NLS-1$
            }
        } else {
            throw new PartInitException("Invalid IEditorInput: " + input.getClass()); //$NON-NLS-1$
        }
        if (fTrace == null) {
            throw new PartInitException("Invalid IEditorInput: " + fFile.getName()); //$NON-NLS-1$
        }
        super.setSite(site);
        super.setInput(fileEditorInput);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void setInput(final IEditorInput input) {
        super.setInput(input);
        firePropertyChange(IEditorPart.PROP_INPUT);
    }

    @Override
    public void propertyChanged(final Object source, final int propId) {
        if (propId == IEditorPart.PROP_INPUT && getEditorInput() instanceof TmfEditorInput) {
            if (fTrace != null) {
                broadcast(new TmfTraceClosedSignal(this, fTrace));
                saveState();
            }
            fEventsTable.dispose();
            fFile = ((TmfEditorInput) getEditorInput()).getFile();
            fTrace = ((TmfEditorInput) getEditorInput()).getTrace();
            /* change the input to a FileEditorInput to allow open handlers to find this editor */
            super.setInput(new FileEditorInput(fFile));
            createAndInitializeTable();
            // The table was swapped for a new one, make sure it gets focus if
            // the editor is active. Otherwise, the new table will not get focus
            // because the editor already had focus.
            if (!PlatformUI.getWorkbench().isClosing() && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart() == getSite().getPart()) {
                fEventsTable.setFocus();
            }
            fParent.layout();
        }
    }

    private void loadState() {
        final @Nullable String traceTypeId = fTrace.getTraceTypeId();
        fEventsTable.setColumnOrder(TmfTableColumnUtils.loadColumnOrder(traceTypeId));
        fEventsTable.setColumnWidth(TmfTableColumnUtils.loadColumnWidth(traceTypeId), TmfTableColumnUtils.loadColumnResizable(traceTypeId));
    }

    private void saveState() {
        final @Nullable String traceTypeId = fTrace.getTraceTypeId();
        TmfTableColumnUtils.saveColumnOrder(traceTypeId, fEventsTable.getColumnOrder());
        TmfTableColumnUtils.saveColumnWidth(traceTypeId, fEventsTable.getColumnWidth());
        TmfTableColumnUtils.saveColumnResizability(traceTypeId, fEventsTable.getColumnResizable());
    }

    @Override
    public void createPartControl(final Composite parent) {
        fParent = parent;
        createAndInitializeTable();
        addPropertyListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        // we need to wrap the ISelectionProvider interface in the editor because
        // the events table can be replaced later while the selection changed listener
        // is only added once by the platform to the selection provider set here
        getSite().setSelectionProvider(this);
        getSite().getPage().addPartListener(this);
    }

    private void createAndInitializeTable() {
        if (fTrace != null) {
            fEventsTable = createEventsTable(fParent, fTrace.getCacheSize());
            fEventsTable.registerContextMenus(getSite());
            fEventsTable.addSelectionChangedListener(this);
            fEventsTable.setTrace(fTrace, true);
            fEventsTable.refreshBookmarks(fFile);
            loadState();

            /* ensure start time is set */
            final ITmfContext context = fTrace.seekEvent(0);
            fTrace.getNext(context);
            context.dispose();

            broadcast(new TmfTraceOpenedSignal(this, fTrace, fFile));
            if (fTraceSelected) {
                broadcast(new TmfTraceSelectedSignal(this, fTrace));
            }

            /* update part name only after trace manager notified */
            setPartName(TmfTraceManager.getInstance().getTraceUniqueName(fTrace));

            /* go to marker after trace opened */
            if (fPendingGotoMarker != null) {
                fEventsTable.gotoMarker(fPendingGotoMarker);
                fPendingGotoMarker = null;
            }
        } else {
            fEventsTable = new TmfEventsTable(fParent, 0);
            fEventsTable.addSelectionChangedListener(this);
        }
        IStatusLineManager statusLineManager = getEditorSite().getActionBars().getStatusLineManager();
        fEventsTable.setStatusLineManager(statusLineManager);
    }

    @Override
    public void dispose() {
        if (getSite() != null) {
            getSite().getPage().removePartListener(this);
        }
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        removePropertyListener(this);
        if (fTrace != null) {
            broadcast(new TmfTraceClosedSignal(this, fTrace));
            if (fEventsTable != null) {
                saveState();
            }
        }
        super.dispose();
    }

    /**
     * Create the event table
     *
     * @param parent
     *            The parent composite
     * @param cacheSize
     *            The cache size
     * @return The event table instance
     */
    protected @NonNull TmfEventsTable createEventsTable(final Composite parent, final int cacheSize) {
        ITmfTrace trace = fTrace;

        /*
         * Check if the trace (or experiment type) defines a specific event
         * table in its extension point.
         */
        TmfEventsTable table = TmfTraceTypeUIUtils.getEventTable(trace, parent, cacheSize);
        if (table != null) {
            return table;
        }

        /*
         * Use the aspects defined by the trace type (or each trace type in an
         * experiment) to build a table consisting of these.
         */
        Iterable<ITmfEventAspect<?>> aspects = getTraceAspects(trace);
        if (Iterables.isEmpty(aspects)) {
            /* Couldn't find any event aspects, use a default table */
            return new TmfEventsTable(parent, cacheSize);
        }
        return new TmfEventsTable(parent, cacheSize, aspects);
    }

    /**
     * Get the event table for the given trace. It will be of the type defined
     * by the extension point if applicable, else it will be a default table
     * with the extension-point-defined columns (if any).
     *
     * @param trace
     *            The event table is for this trace
     * @param parent
     *            The parent composite of the table
     * @param cacheSize
     *            The cache size to use
     * @return The event table for the trace
     */
    private static @NonNull Iterable<ITmfEventAspect<?>> getTraceAspects(ITmfTrace trace) {
        return trace.getEventAspects();
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void setFocus() {
        fEventsTable.setFocus();
    }

    @Override
    public <T> T getAdapter(final Class<T> adapter) {
        if (IGotoMarker.class.equals(adapter)) {
            if (fTrace == null || fEventsTable == null) {
                return adapter.cast(this);
            }
            return adapter.cast(fEventsTable);
        } else if (IPropertySheetPage.class.equals(adapter)) {
            return adapter.cast(new UnsortedPropertySheetPage());
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void gotoMarker(IMarker marker) {
        if (fTrace == null || fEventsTable == null) {
            fPendingGotoMarker = marker;
        } else {
            fEventsTable.gotoMarker(marker);
        }
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        final Set<@NonNull IMarker> added = new HashSet<>();
        final Set<@NonNull IMarker> removed = new HashSet<>();
        boolean deltaFound = false;
        for (final IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false)) {
            if (delta.getResource().equals(fFile)) {
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    removed.add(delta.getMarker());
                } else if (delta.getKind() == IResourceDelta.ADDED) {
                    added.add(delta.getMarker());
                }
                /* this also covers IResourceDelta.CHANGED */
                deltaFound = true;
            }
        }
        if (!deltaFound) {
            return;
        }
        Display.getDefault().asyncExec(() -> {
            if (removed.isEmpty() && added.isEmpty()) {
                fEventsTable.getTable().refresh();
            } else {
                if (!removed.isEmpty()) {
                    fEventsTable.removeBookmark(Iterables.toArray(removed, IMarker.class));
                }
                if (!added.isEmpty()) {
                    fEventsTable.addBookmark(Iterables.toArray(added, IMarker.class));
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // ISelectionProvider
    // ------------------------------------------------------------------------

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionChangedListeners.add(listener);
    }

    @Override
    public ISelection getSelection() {
        if (fEventsTable == null) {
            return StructuredSelection.EMPTY;
        }
        return fEventsTable.getSelection();
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionChangedListeners.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        // not implemented
    }

    /**
     * Notifies any selection changed listeners that the viewer's selection has changed.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a selection changed event
     *
     * @see ISelectionChangedListener#selectionChanged
     */
    protected void fireSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = fSelectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // ISelectionChangedListener
    // ------------------------------------------------------------------------

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        fireSelectionChanged(event);
    }

    // ------------------------------------------------------------------------
    // IPartListener
    // ------------------------------------------------------------------------

    @Override
    public void partActivated(IWorkbenchPart part) {
        if (part == this && !fTraceSelected) {
            fTraceSelected = true;
            if (fTrace == null) {
                return;
            }
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        if (part == this && !fTraceSelected) {
            fTraceSelected = true;
            if (fTrace == null) {
                return;
            }
            broadcast(new TmfTraceSelectedSignal(this, fTrace));
        }
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
        // Do nothing
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
        // Do nothing
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Global commands
    // ------------------------------------------------------------------------

    /**
     * Add a bookmark
     */
    public void addBookmark() {
        fEventsTable.addBookmark(fFile);
    }


    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the Trace Selected signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if ((signal.getSource() != this)) {
            if (signal.getTrace().equals(fTrace)) {
                getSite().getPage().bringToTop(this);
            } else {
                fTraceSelected = false;
            }
        }
    }

    /**
     * Update the display to use the updated timestamp format
     *
     * @param signal the incoming signal
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        if (fEventsTable != null) {
            fEventsTable.refresh();
        }
    }

}
