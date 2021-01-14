/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.Activator;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * Service to manage traces.
 *
 * @author Loic Prieur-Drevon
 */
@Path("/traces")
public class TraceManagerService {

    /**
     * Getter method to access the list of traces
     *
     * @return a response containing the list of traces
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getTraces() {
        return Response.ok(Collections2.filter(TmfTraceManager.getInstance().getOpenedTraces(),
                Predicates.not(TmfExperiment.class::isInstance))).build();
    }

    /**
     * Method to open the trace, initialize it, index it and add it to the trace
     * manager.
     *
     * @param queryParameters
     *            Parameters to post a trace as described by
     *            {@link QueryParameters}
     * @return the new trace model object or the exception if it failed to load.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putTrace(QueryParameters queryParameters) {
        Map<String, Object> parameters = queryParameters.getParameters();
        String name = (String) parameters.get("name");
        String path = (String) parameters.get("uri");
        Object typeIDObject = parameters.get("typeID");
        String typeID = typeIDObject != null ? (String) typeIDObject : "";

        if (!Paths.get(path).toFile().exists()) {
            return Response.status(Status.NOT_FOUND).entity("No trace at " + path).build(); //$NON-NLS-1$
        }

        Optional<@NonNull ITmfTrace> optional = Iterables.tryFind(TmfTraceManager.getInstance().getOpenedTraces(), t -> t.getPath().equals(path));
        if (optional.isPresent()) {
            return Response.ok(optional.get()).build();
        }

        try {
            ITmfTrace trace = put(path, name, typeID);
            if (trace == null) {
                return Response.status(Status.NOT_IMPLEMENTED).entity("Trace type not supported").build(); //$NON-NLS-1$
            }
            return Response.ok(trace).build();
        } catch (TmfTraceException | TmfTraceImportException | InstantiationException
                | IllegalAccessException | CoreException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
        }
    }

    private ITmfTrace put(String path, String name, String typeID)
            throws TmfTraceException, TmfTraceImportException, InstantiationException,
            IllegalAccessException, CoreException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        List<TraceTypeHelper> traceTypes = TmfTraceType.selectTraceType(path, typeID);
        if (traceTypes.isEmpty()) {
            return null;
        }

        IResource resource = getResource(path);

//        TmfTraceTypeUIUtils.setTraceType(resource, traceTypeHelper);
        TraceTypeHelper helper = traceTypes.get(0);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, helper.getTraceTypeId());

        ITmfTrace trace = helper.getTraceClass().getDeclaredConstructor().newInstance();
        trace.initTrace(resource, path, ITmfEvent.class, name, typeID);
        trace.indexTrace(false);
        // read first event to make sure start time is initialized
        ITmfContext ctx = trace.seekEvent(0);
        trace.getNext(ctx);
        ctx.dispose();

        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null));
        return trace;
    }

    /**
     * Gets the Eclipse resource from the path and prepares the supplementary
     * directory for this trace.
     *
     * @param path
     *            the absolute path string to the trace
     * @return The Eclipse resources
     *
     * @throws CoreException
     *             if an error occurs
     */
    private static IResource getResource(String path) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
        IFolder tracesFolder = project.getFolder("Traces");
        IPath iPath = org.eclipse.core.runtime.Path.forPosix(path);

        IResource resource = null;
        boolean isSuccess = false;
        // create the resource hierarchy.
        if (new File(path).isFile()) {
            IFile file = tracesFolder.getFile(path);
            createFolder((IFolder) file.getParent(), null);
            isSuccess = ResourceUtil.createSymbolicLink(file, iPath, true, null);
            resource = file;
        } else {
            IFolder folder = tracesFolder.getFolder(path);
            createFolder((IFolder) folder.getParent(), null);
            isSuccess = ResourceUtil.createSymbolicLink(folder, iPath, true, null);
            resource = folder;
        }

        if (!isSuccess) {
            return null;
        }

        // create supplementary folder on file system:
        IFolder supplRootFolder = project.getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);
        IFolder supplFolder = supplRootFolder.getFolder(path);
        createFolder(supplFolder, null);
        resource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocation().toOSString());

        return resource;
    }

    /**
     * Getter method to get a trace object
     *
     * @param uuid
     *            Unique trace ID
     * @return a response containing the trace
     */
    @GET
    @Path("/{uuid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getTrace(@PathParam("uuid") @NotNull UUID uuid) {
        ITmfTrace trace = getTraceByUUID(uuid);
        if (trace == null || trace instanceof TmfExperiment) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(trace).build();
    }

    /**
     * Delete a trace from the manager and dispose of it
     *
     * @param uuid
     *            Unique trace ID
     * @return a not found response if there is no such trace or the entity.
     */
    @DELETE
    @Path("/{uuid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteTrace(@PathParam("uuid") @NotNull UUID uuid) {
        ITmfTrace trace = getTraceByUUID(uuid);
        if (trace == null || trace instanceof TmfExperiment) {
            return Response.status(Status.NOT_FOUND).build();
        }
        TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, trace));
        trace.dispose();
        TmfTraceManager.deleteSupplementaryFolder(trace);
        try {
            IResource resource = trace.getResource();
            if (resource != null) {
                resource.delete(IResource.FORCE, null);
            }
            ResourcesPlugin.getWorkspace().getRoot()
                .getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME)
                .refreshLocal(Integer.MAX_VALUE, null);
        } catch (CoreException e) {
            Activator.getInstance().logError("Failed to delete trace", e); //$NON-NLS-1$
        }
        return Response.ok(trace).build();
    }

    /**
     * Try and find a trace with the queried UUID in the {@link TmfTraceManager}.
     *
     * @param uuid
     *            queried {@link UUID}
     * @return the trace or null if none match.
     */
    public static @Nullable ITmfTrace getTraceByUUID(UUID uuid) {
        return Iterables.tryFind(TmfTraceManager.getInstance().getOpenedTraces(), t -> uuid.equals(t.getUUID())).orNull();
    }

    private static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        // Taken from: org.eclipse.tracecompass.tmf.ui.project.model.TraceUtil.java
        // TODO: have a tmf.core util for that.
        if (!folder.exists()) {
            if (folder.getParent() instanceof IFolder) {
                createFolder((IFolder) folder.getParent(), monitor);
            }
            folder.create(true, true, monitor);
        }
    }
}
