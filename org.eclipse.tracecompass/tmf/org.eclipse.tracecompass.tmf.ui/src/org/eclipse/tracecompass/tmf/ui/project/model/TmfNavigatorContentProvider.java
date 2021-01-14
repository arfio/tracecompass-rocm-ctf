/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Implement getParent()
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfProjectModelHelper;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * The TMF project content provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorContentProvider implements IPipelinedTreeContentProvider {

    // ------------------------------------------------------------------------
    // ICommonContentProvider
    // ------------------------------------------------------------------------

    @Override
    public Object[] getElements(Object inputElement) {
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.getParent();
        }

        if (element instanceof TmfProjectElement) {
            IProject p = TmfProjectModelHelper.getProjectFromShadowProject(((TmfProjectElement) element).getResource());
            if ((p != null) && (p.isAccessible())) {
                return p;
            }
            return ((TmfProjectElement) element).getResource();
        }

        if (element instanceof TmfTracesFolder) {
            TmfTracesFolder folder = (TmfTracesFolder) element;

            // Return TmfProjectElement if shadow project case
            if (folder.getParent() instanceof TmfProjectElement) {
                IProject p = TmfProjectModelHelper.getProjectFromShadowProject(((TmfProjectElement) folder.getParent()).getResource());
                if ((p != null) && (p.isAccessible())) {
                    return folder.getParent();
                }
            }
            // Return the corresponding IProject as parent because from CNF point of view the IProject is the parent.
            // The IProject is needed e.g. for link with Editor to work correctly.
            return folder.getParent().getResource();
        }

        if (element instanceof TmfExperimentFolder) {
            TmfExperimentFolder folder = (TmfExperimentFolder) element;

            // Return TmfProjectElement if shadow project case
            if (folder.getParent() instanceof TmfProjectElement) {
                IProject p = TmfProjectModelHelper.getProjectFromShadowProject(((TmfProjectElement) folder.getParent()).getResource());
                if ((p != null) && (p.isAccessible())) {
                    return folder.getParent();
                }
            }
            // Return the corresponding IProject as parent because from CNF point of view the IProject is the parent.
            // The IProject is needed e.g. for link with Editor to work correctly.
            return folder.getParent().getResource();
        }

        if (element instanceof ITmfProjectModelElement) {
            ITmfProjectModelElement modelElement = (ITmfProjectModelElement) element;
            return modelElement.getParent();
        }

        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.isAccessible();
        }
        if (element instanceof ITmfProjectModelElement) {
            ITmfProjectModelElement modelElement = (ITmfProjectModelElement) element;
            return modelElement.hasChildren();
        }
        return false;
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

    @Override
    public void restoreState(IMemento aMemento) {
        // Do nothing
    }

    @Override
    public void saveState(IMemento aMemento) {
        // Do nothing
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // ICommonContentProvider - getChildren()
    // ------------------------------------------------------------------------

    @Override
    public synchronized Object[] getChildren(Object parentElement) {

        // Tracing project level
        if (parentElement instanceof IProject) {
            IProject parentProject = (IProject) parentElement;
            if (TmfProjectElement.showProjectRoot(parentProject)) {
                if (TmfProjectModelHelper.shadowProjectAccessible(parentProject)) {
                    TmfProjectElement[] elements = new TmfProjectElement[1];
                    elements[0] = TmfProjectRegistry.getProject(parentProject, true);
                    return elements;
                }
                return new Object[0];
            }
            TmfProjectElement element = TmfProjectRegistry.getProject(parentProject, true);
            return element.getChildren().toArray();
        }

        // Other project model elements
        if (parentElement instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) parentElement).getChildren().toArray();
        }

        return new Object[0];
    }

    // ------------------------------------------------------------------------
    // IPipelinedTreeContentProvider
    // ------------------------------------------------------------------------

    @Override
    public void getPipelinedChildren(Object parent, Set currentChildren) {
        customizeTmfElements(getChildren(parent), currentChildren);
    }

    @Override
    public void getPipelinedElements(Object input, Set currentElements) {
        customizeTmfElements(getElements(input), currentElements);
    }

    /**
     * Add/replace the ITmfProjectElement to the list of children
     *
     * @param elements
     *            the list returned by getChildren()
     * @param children
     *            the current children
     */
    private static void customizeTmfElements(Object[] elements,
            Set<Object> children) {
        if (elements != null && children != null) {
            for (Object element : elements) {
                if (element instanceof ITmfProjectModelElement) {
                    ITmfProjectModelElement tmfElement = (ITmfProjectModelElement) element;
                    IResource resource = tmfElement.getResource();
                    if (resource != null) {
                        children.remove(resource);
                    }
                    children.add(element);
                }
                else if (element != null) {
                    children.add(element);
                }
            }
        }
    }

    @Override
    public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
        return aSuggestedParent;
    }

    @Override
    public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
        return anAddModification;
    }

    @Override
    public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
        return null;
    }

    @Override
    public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
        return false;
    }

    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        return false;
    }
}
