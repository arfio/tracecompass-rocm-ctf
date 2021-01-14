/*******************************************************************************
* Copyright (c) 2012, 2014 Ericsson
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
 *   Patrick Tasse - Add support for DROP_LINK and rename prompt on name clash
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Drop adapter assistant for project explorer
 */
public class DropAdapterAssistant extends CommonDropAdapterAssistant {

    /**
     * Default constructor
     */
    public DropAdapterAssistant() {
        // Do nothing
    }

    @Override
    public boolean isSupportedType(TransferData aTransferType) {
        return super.isSupportedType(aTransferType) || FileTransfer.getInstance().isSupportedType(aTransferType);
    }

    @Override
    public IStatus validateDrop(Object target, int operation, TransferData transferType) {
        if (target instanceof TmfTraceFolder) {
            return Status.OK_STATUS;
        }
        if (target instanceof TmfExperimentElement) {
            return Status.OK_STATUS;
        }
        if (target instanceof TmfTraceElement) {
            ITmfProjectModelElement parent = ((TmfTraceElement) target).getParent();
            if (parent instanceof TmfTraceFolder) {
                return Status.OK_STATUS;
            }
            if (parent instanceof TmfExperimentElement) {
                return Status.OK_STATUS;
            }
        }

        if (target instanceof TmfProjectElement) {
            return Status.OK_STATUS;
        }

        if (target instanceof IProject) {
            return Status.CANCEL_STATUS;
        }

        return Status.CANCEL_STATUS;
    }

    @Override
    public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
        boolean ok = false;

        // Use local variable to avoid parameter assignment
        @Nullable Object targetToUse = aTarget;

        int operation = aDropTargetEvent.detail;
        if (operation != DND.DROP_LINK) {
            operation = DND.DROP_COPY;
        }

        // If target is a trace, use its parent (either trace folder or experiment)
        if (targetToUse instanceof TmfTraceElement) {
            targetToUse = ((TmfTraceElement) targetToUse).getParent();
        }

        // if target is project element
        if (targetToUse instanceof TmfProjectElement) {
            targetToUse = ((TmfProjectElement) targetToUse).getTracesFolder();
        }

        // If target is a project, use its trace folder
        if (targetToUse instanceof IProject) {
            TmfProjectElement projectElement = TmfProjectRegistry.getProject((IProject) targetToUse, true);
            if (projectElement != null) {
                targetToUse = projectElement.getTracesFolder();
            }
        }

        if (aDropTargetEvent.data instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) aDropTargetEvent.data;
            for (Object source : selection.toArray()) {
                if (source instanceof IResource) {
                    // If source resource is a trace, use the trace element
                    IResource sourceResource = (IResource) source;
                    TmfProjectElement projectElement = TmfProjectRegistry.getProject(sourceResource.getProject());
                    if (projectElement != null) {
                        TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
                        if (tracesFolder != null) {
                            for (TmfTraceElement trace : tracesFolder.getTraces()) {
                                if (trace.getResource().equals(sourceResource)) {
                                    source = trace;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (source instanceof TmfTraceElement) {
                    TmfTraceElement sourceTrace = (TmfTraceElement) source;
                    // If source trace is under an experiment, use the original trace from the traces folder
                    sourceTrace = sourceTrace.getElementUnderTraceFolder();
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= drop(sourceTrace, targetExperiment, operation);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= drop(sourceTrace, traceFolder, operation);
                    }
                } else if (source instanceof IResource) {
                    IResource sourceResource = (IResource) source;
                    if (sourceResource.getType() != IResource.FILE && sourceResource.getType() != IResource.FOLDER) {
                        continue;
                    }
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= (drop(sourceResource, targetExperiment, operation) != null);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= (drop(sourceResource, traceFolder, operation) != null);
                    }
                }
            }
        } else if (aDropTargetEvent.data instanceof String[]) {
            String[] sources = (String[]) aDropTargetEvent.data;
            for (String source : sources) {
                Path path = new Path(source);
                if (targetToUse instanceof TmfExperimentElement) {
                    TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                    ok |= drop(path, targetExperiment, operation);
                } else if (targetToUse instanceof TmfTraceFolder) {
                    TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                    ok |= drop(path, traceFolder, operation);
                }
            }
        }
        return (ok ? Status.OK_STATUS : Status.CANCEL_STATUS);
    }


    /**
     * Drop a trace by copying/linking a trace element in a target experiment
     *
     * @param sourceTrace the source trace element to copy
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(TmfTraceElement sourceTrace,
            TmfExperimentElement targetExperiment,
            int operation) {

        IResource sourceResource = sourceTrace.getResource();
        IResource targetResource = drop(sourceResource, targetExperiment, operation);

        if (targetResource != null) {
            if (! sourceTrace.getProject().equals(targetExperiment.getProject())) {
                IFolder destinationSupplementaryFolder = targetExperiment.getTraceSupplementaryFolder(targetResource.getName());
                sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            }
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by copying/linking a resource in a target experiment
     *
     * @param sourceResource the source resource
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return the target resource or null if unsuccessful
     */
    private static IResource drop(IResource sourceResource,
            TmfExperimentElement targetExperiment,
            int operation) {

        IResource traceResource = sourceResource;

        TmfTraceFolder tracesFolder = targetExperiment.getProject().getTracesFolder();
        if (tracesFolder == null) {
            return null;
        }

        IPath tracesFolderPath = tracesFolder.getPath();
        if (tracesFolderPath.isPrefixOf(sourceResource.getFullPath())) {
            String elementPath = sourceResource.getFullPath().makeRelativeTo(tracesFolderPath).toString();
            for (TmfTraceElement trace : targetExperiment.getTraces()) {
                if (trace.getElementPath().equals(elementPath)) {
                    return null;
                }
            }
        } else {
            String targetName = sourceResource.getName();
            for (ITmfProjectModelElement element : tracesFolder.getChildren()) {
                if (element.getName().equals(targetName)) {
                    targetName = promptRename(element);
                    if (targetName == null) {
                        return null;
                    }
                    break;
                }
            }
            try {
                if (operation == DND.DROP_COPY && !ResourceUtil.isSymbolicLink(sourceResource)) {
                    IPath destination = tracesFolder.getResource().getFullPath().addTrailingSeparator().append(targetName);
                    sourceResource.copy(destination, false, null);
                    cleanupBookmarks(destination);
                } else {
                    createLink(tracesFolder.getResource(), sourceResource, targetName);
                }
                // use the copied resource for the experiment
                if (sourceResource.getType() == IResource.FILE) {
                    traceResource = tracesFolder.getResource().getFile(targetName);
                } else if (sourceResource.getType() == IResource.FOLDER) {
                    traceResource = tracesFolder.getResource().getFolder(targetName);
                }
                String sourceLocation = sourceResource.getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
                if (sourceLocation == null) {
                     sourceLocation = URIUtil.toUnencodedString(new File(ResourceUtil.getLocationURI(sourceResource)).toURI());
                }
                traceResource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
            } catch (CoreException e) {
                TraceUtils.displayErrorMsg(e);
                return null;
            }
        }
        if (traceResource != null && traceResource.exists()) {
            setTraceType(traceResource);
            for (TmfTraceElement trace : tracesFolder.getTraces()) {
                if (trace.getResource().equals(traceResource)) {
                    targetExperiment.addTrace(trace);
                    targetExperiment.closeEditors();
                    targetExperiment.deleteSupplementaryResources();
                    break;
                }
            }
            return traceResource;
        }
        return null;
    }

    /**
     * Drop a trace by copying/linking a trace element in a trace folder
     *
     * @param sourceTrace the source trace
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(TmfTraceElement sourceTrace,
            TmfTraceFolder traceFolder,
            int operation) {

        IResource sourceResource = sourceTrace.getResource();
        IResource targetResource = drop(sourceResource, traceFolder, operation);
        TmfTraceFolder tracesFolder = traceFolder.getProject().getTracesFolder();

        if ((targetResource != null) && (tracesFolder != null)) {
            String elementPath = targetResource.getFullPath().makeRelativeTo(tracesFolder.getPath()).toString();
            IFolder destinationSupplementaryFolder = traceFolder.getTraceSupplementaryFolder(elementPath);
            sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by copying/linking a resource in a trace folder
     *
     * @param sourceResource the source resource
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return the target resource or null if unsuccessful
     */
    private static IResource drop(IResource sourceResource,
            TmfTraceFolder traceFolder,
            int operation) {

        if (sourceResource.getParent().equals(traceFolder.getResource())) {
            return null;
        }
        String targetName = sourceResource.getName();
        for (ITmfProjectModelElement element : traceFolder.getChildren()) {
            if (element.getName().equals(targetName)) {
                targetName = promptRename(element);
                if (targetName == null) {
                    return null;
                }
                break;
            }
        }
        try {
            if (operation == DND.DROP_COPY && !ResourceUtil.isSymbolicLink(sourceResource)) {
                IPath destination = traceFolder.getResource().getFullPath().addTrailingSeparator().append(targetName);
                sourceResource.copy(destination, false, null);
                cleanupBookmarks(destination);
            } else {
                createLink(traceFolder.getResource(), sourceResource, targetName);
            }
            IResource traceResource = traceFolder.getResource().findMember(targetName);
            if (traceResource != null && traceResource.exists()) {
                String sourceLocation = sourceResource.getPersistentProperty(TmfCommonConstants.SOURCE_LOCATION);
                if (sourceLocation == null) {
                    sourceLocation = URIUtil.toUnencodedString(new File(ResourceUtil.getLocationURI(sourceResource)).toURI());
                }
                traceResource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
                setTraceType(traceResource);
            }
            return traceResource;
        } catch (CoreException e) {
            TraceUtils.displayErrorMsg(e);
        }
        return null;
    }

    /**
     * Drop a trace by importing/linking a path in a target experiment
     *
     * @param path the source path
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(Path path,
            TmfExperimentElement targetExperiment,
            int operation) {

        TmfTraceFolder tracesFolder = targetExperiment.getProject().getTracesFolder();
        if (tracesFolder == null) {
            return false;
        }

        IPath tracesFolderPath = tracesFolder.getResource().getLocation();
        IResource traceResource = null;
        if (tracesFolderPath.isPrefixOf(path)) {
            String elementPath = path.makeRelativeTo(tracesFolderPath).toString();
            for (TmfTraceElement trace : targetExperiment.getTraces()) {
                if (trace.getElementPath().equals(elementPath)) {
                    return false;
                }
            }
            traceResource = tracesFolder.getResource().findMember(elementPath);
        } else {
            String targetName = path.lastSegment();
            for (ITmfProjectModelElement element : tracesFolder.getChildren()) {
                if (element.getName().equals(targetName)) {
                    targetName = promptRename(element);
                    if (targetName == null) {
                        return false;
                    }
                    break;
                }
            }
            if (operation == DND.DROP_COPY) {
                importTrace(tracesFolder.getResource(), path, targetName);
            } else {
                createLink(tracesFolder.getResource(), path, targetName);
            }
            // use the copied resource for the experiment
            File file = new File(path.toString());
            if (file.exists() && file.isFile()) {
                traceResource = tracesFolder.getResource().getFile(targetName);
            } else if (file.exists() && file.isDirectory()) {
                traceResource = tracesFolder.getResource().getFolder(targetName);
            }
        }
        if (traceResource != null && traceResource.exists()) {
            try {
                String sourceLocation = URIUtil.toUnencodedString(path.toFile().toURI());
                traceResource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
            } catch (CoreException e) {
                TraceUtils.displayErrorMsg(e);
            }
            setTraceType(traceResource);
            for (TmfTraceElement trace : tracesFolder.getTraces()) {
                if (trace.getResource().equals(traceResource)) {
                    targetExperiment.addTrace(trace);
                    targetExperiment.closeEditors();
                    targetExperiment.deleteSupplementaryResources();
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by importing/linking a path in a trace folder
     *
     * @param path the source path
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(Path path,
            TmfTraceFolder traceFolder,
            int operation) {

        String targetName = path.lastSegment();
        for (ITmfProjectModelElement element : traceFolder.getChildren()) {
            if (element.getName().equals(targetName)) {
                targetName = promptRename(element);
                if (targetName == null) {
                    return false;
                }
                break;
            }
        }
        if (operation == DND.DROP_COPY) {
            importTrace(traceFolder.getResource(), path, targetName);
        } else {
            createLink(traceFolder.getResource(), path, targetName);
        }
        IResource traceResource = traceFolder.getResource().findMember(targetName);
        if (traceResource != null && traceResource.exists()) {
            try {
                String sourceLocation = URIUtil.toUnencodedString(path.toFile().toURI());
                traceResource.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);
            } catch (CoreException e) {
                TraceUtils.displayErrorMsg(e);
            }
            setTraceType(traceResource);
        }
        return true;
    }

    /**
     * Import a trace to the trace folder
     *
     * @param folder the trace folder resource
     * @param path the path to the trace to import
     * @param targetName the target name
     */
    private static void importTrace(final IFolder folder, final Path path, final String targetName) {
        final File source = new File(path.toString());
        if (source.isDirectory()) {
            IPath containerPath = folder.getFullPath().addTrailingSeparator().append(targetName);
            IOverwriteQuery overwriteImplementor = pathString -> IOverwriteQuery.NO_ALL;
            List<File> filesToImport = Arrays.asList(source.listFiles());
            ImportOperation operation = new ImportOperation(
                    containerPath,
                    source,
                    FileSystemStructureProvider.INSTANCE,
                    overwriteImplementor,
                    filesToImport);
            operation.setCreateContainerStructure(false);
            try {
                operation.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
                TraceUtils.displayErrorMsg(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            IRunnableWithProgress runnable = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try (InputStream inputStream = new FileInputStream(source);) {
                        IFile targetFile = folder.getFile(targetName);
                        targetFile.create(inputStream, IResource.NONE, monitor);
                    } catch (CoreException | IOException e) {
                        TraceUtils.displayErrorMsg(e);
                    }
                }
            };
            WorkspaceModifyDelegatingOperation operation = new WorkspaceModifyDelegatingOperation(runnable);
            try {
                operation.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
                TraceUtils.displayErrorMsg(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create a link to the actual trace and set the trace type
     *
     * @param parentFolder the parent folder
     * @param resource the resource
     * @param targetName the target name
     */
    private static void createLink(IFolder parentFolder, IResource resource, String targetName) {
        IPath location = ResourceUtil.getLocation(resource);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            String traceType = TmfTraceType.getTraceTypeId(resource);
            TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(traceType);

            if (resource instanceof IFolder) {
                IFolder folder = parentFolder.getFolder(targetName);
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    folder.createLink(location, IResource.REPLACE, null);
                    if (traceTypeHelper != null) {
                        TmfTraceTypeUIUtils.setTraceType(folder, traceTypeHelper);
                    }
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            } else {
                IFile file = parentFolder.getFile(targetName);
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    file.createLink(location, IResource.REPLACE, null);
                    if (traceTypeHelper != null) {
                        TmfTraceTypeUIUtils.setTraceType(file, traceTypeHelper);
                    }
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            TraceUtils.displayErrorMsg(e);
        }
    }

    /**
     * Create a link to a file or folder
     *
     * @param parentFolder the parent folder
     * @param source the file or folder
     * @param targetName the target name
     */
    private static void createLink(IFolder parentFolder, IPath location, String targetName) {
        File source = new File(location.toString());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {

            if (source.isDirectory()) {
                IFolder folder = parentFolder.getFolder(targetName);
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    folder.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            } else {
                IFile file = parentFolder.getFile(targetName);
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK() || result.matches(IStatus.INFO | IStatus.WARNING)) {
                    file.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            TraceUtils.displayErrorMsg(e);
        }
    }

    /**
     * Prompts the user to rename a trace
     *
     * @param element the conflicting element
     * @return the new name to use or null if rename is canceled
     */
    private static String promptRename(ITmfProjectModelElement element) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        mb.setText(Messages.DropAdapterAssistant_RenameTraceTitle);
        mb.setMessage(NLS.bind(Messages.DropAdapterAssistant_RenameTraceMessage, element.getName()));
        if (mb.open() != SWT.OK) {
            return null;
        }
        IContainer folder = element.getResource().getParent();
        int i = 2;
        while (true) {
            String name = element.getName() + '(' + Integer.toString(i++) + ')';
            IResource resource = folder.findMember(name);
            if (resource == null) {
                return name;
            }
        }
    }

    /**
     * Cleanup bookmarks file in copied trace
     */
    private static void cleanupBookmarks(IPath path) {
        IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        if (folder.exists()) {
            try {
                for (IResource member : folder.members()) {
                    if (ITmfEventsEditorConstants.TRACE_INPUT_TYPE_CONSTANTS.contains(TmfTraceType.getTraceTypeId(member))) {
                        member.delete(true, null);
                    }
                }
            } catch (CoreException e) {
                TraceUtils.displayErrorMsg(e);
            }
        }
    }

    private static void setTraceType(IResource traceResource) {
        try {
            String traceType = TmfTraceType.getTraceTypeId(traceResource);
            TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(traceType);
            if (traceTypeHelper == null) {
                traceTypeHelper = TmfTraceTypeUIUtils.selectTraceType(traceResource.getLocation().toOSString(), null, null);
            }
            if (traceTypeHelper != null) {
                TmfTraceTypeUIUtils.setTraceType(traceResource, traceTypeHelper);
            }
        } catch (TmfTraceImportException e) {
            // Ignored, no trace type selected
        } catch (CoreException e) {
            TraceUtils.displayErrorMsg(e);
        }
    }
}
