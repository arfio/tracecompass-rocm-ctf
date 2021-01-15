/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.handlers.provider;

/**
 * Interface for providing an extended find provider.
 *
 * An advanced paging provider is able to compute number of pages, and to display the number of items it treats on each
 * page and for total counts.<br>
 * An item can be a message, a node or anything meaningful from loader standpoint.<br>
 * Items are only here for information to the user.
 *
 * @version 1.0
 * @author sveyrier
 */
public interface ISDAdvancedPagingProvider extends ISDPagingProvider {

    /**
     * Returns the current page.
     *
     * @return the current page the loader is dealing with. <b>Note</b> that first page has the 0 index (indexes are from
     *         0 to pagesCount()-1).
     */
    int currentPage();

    /**
     * Returns the number of pages.
     *
     * @return number of pages the loader is dealing with
     */
    int pagesCount();

    /**
     * Instructs a load of the &lt;pageNumber_&gt;<i>th</i> page.<br>
     * <b>Note</b> that first page has the index 0 (indexes are from 0 to pagesCount()-1).
     *
     * @param pageNumber index of the page to load
     */
    void pageNumberChanged(int pageNumber);

}
