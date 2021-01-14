package org.eclipse.tracecompass.incubator.rocm.ctf.core.trace;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;

/**
 * @author Arnaud
 *
 */
public class RocmTraceContext extends TmfTraceContext {

    /**
     * @param selection
     *            The selected time range
     * @param windowRange
     *            The visible window's time range
     * @param editorFile
     *            The file representing the selected editor
     * @param filter
     *            The currently applied filter. 'null' for none.
     * @param trace
     *            The trace
     */
    public RocmTraceContext(TmfTimeRange selection, TmfTimeRange windowRange, @Nullable IFile editorFile, @Nullable ITmfFilter filter, ITmfTrace trace) {
        super(selection, windowRange, editorFile, filter);
    }

    /**
     * @param builder the builder
     */
    public RocmTraceContext(Builder builder) {
        super(builder);
    }

}
