/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Singleton that keeps track of the event providers.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProviderManager {

    // ------------------------------------------------------------------------
    // No constructor
    // ------------------------------------------------------------------------

    private TmfProviderManager() {
    }

    // ------------------------------------------------------------------------
    // Keeps track of the providers for each event type
    // ------------------------------------------------------------------------

    private static Map<Class<? extends ITmfEvent>, List<TmfEventProvider>> fProviders =
            new HashMap<>();

    /**
     * Registers [provider] as a provider of [eventType]
     *
     * @param eventType
     *            The event type
     * @param provider
     *            The data provider
     */
    public static synchronized <T extends ITmfEvent> void register(Class<T> eventType, TmfEventProvider provider) {
        List<TmfEventProvider> typeProviders = fProviders.computeIfAbsent(eventType, type -> new ArrayList<>());
        typeProviders.add(provider);
    }

    /**
     * Re-registers [provider] as a provider of [eventType]
     *
     * @param eventType
     *            The event type
     * @param provider
     *            The data provider
     */
    public static synchronized <T extends ITmfEvent> void deregister(Class<T> eventType, TmfEventProvider provider) {
        List<TmfEventProvider> list = fProviders.get(eventType);
        if (list != null) {
            list.remove(provider);
            if (list.isEmpty()) {
                fProviders.remove(eventType);
            }
        }
    }

    /**
     * Returns the list of components that provide [eventType]
     *
     * @param eventType
     *            The event type
     * @return the list of components that provide [eventType]
     */
    public static TmfEventProvider[] getProviders(Class<? extends ITmfEvent> eventType) {
        List<TmfEventProvider> list = fProviders.get(eventType);
        if (list == null) {
            list = new ArrayList<>();
        }
        TmfEventProvider[] result = new TmfEventProvider[list.size()];
        return list.toArray(result);
    }

    /**
     * Returns the list of components of type [providerType] that provide
     * [eventType]
     *
     * @param eventType
     *            The event type
     * @param providerType
     *            The data provider
     * @return the list of components of type [providerType] that provide
     *         [eventType]
     */
    public static TmfEventProvider[] getProviders(Class<? extends ITmfEvent> eventType, Class<? extends TmfEventProvider> providerType) {
        if (providerType == null) {
            return getProviders(eventType);
        }
        TmfEventProvider[] list = getProviders(eventType);
        List<TmfEventProvider> result = new ArrayList<>();
        if (list != null) {
            for (TmfEventProvider provider : list) {
                if (provider.getClass() == providerType) {
                    result.add(provider);
                }
            }
        }
        TmfEventProvider[] array = new TmfEventProvider[result.size()];
        return result.toArray(array);
    }

}
