/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vincent Perot - Initial API and implementation
 *     Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.shared;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapHelper;

/**
 * Here is the list of the available test traces for the Pcap parser.
 *
 * @author Vincent Perot
 */

public enum PcapTestTrace {

    /** A bad pcap file. */
    BAD_PCAPFILE("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "BadPcapFile.pcap"),

    /** A Valid Pcap that is empty. */
    EMPTY_PCAP("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "EmptyPcap.pcap"),

    /** A Pcap that mostly contains TCP packets. */
    MOSTLY_TCP("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "mostlyTCP.pcap"),

    /** A Pcap that mostly contains UDP packets. */
    MOSTLY_UDP("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "mostlyUDP.pcap"),

    /** A big-endian trace that contains two packets. */
    SHORT_BIG_ENDIAN("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "Short_BigEndian.pcap"),

    /** A little-endian trace that contains two packets. */
    SHORT_LITTLE_ENDIAN("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "Short_LittleEndian.pcap"),

    /** A large trace for benchmarking. */
    BENCHMARK_TRACE("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "benchmarkTrace.pcap"),

    /** A Kernel trace directory. */
    KERNEL_DIRECTORY("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "kernel"),

    /** A Kernel trace file. */
    KERNEL_TRACE("..", "..", "pcap", "org.eclipse.tracecompass.pcap.core.tests", "rsc", "kernel", "channel0_0");

    private final @NonNull Path fPath;

    private PcapTestTrace(@NonNull String first, String... more) {
        @SuppressWarnings("null")
        @NonNull Path path = FileSystems.getDefault().getPath(first, more);
        fPath = path;
    }

    /** @return The path to the test trace */
    public @NonNull Path getPath() {
        return fPath;
    }

    /**
     * Get a Pcap or PcapNg Trace instance of a test trace. Make sure to call
     * {@link #exists()} before calling this!
     *
     * @return The PcapOldFile or PcapNgFile object
     * @throws IOException
     *             Thrown when some IO error occurs.
     * @throws BadPcapFileException
     *             Thrown when the file is not a valid Pcap File.
     */
    public PcapFile getTrace() throws BadPcapFileException, IOException {
        return PcapHelper.getPcapFile(fPath);
    }

    /**
     * Check if this test trace actually exists on disk.
     *
     * @return If the trace exists
     */
    public boolean exists() {
        if (Files.notExists(fPath)) {
            return false;
        }
        return true;
    }

}
