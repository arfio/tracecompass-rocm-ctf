/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.serialization;

/**
 * Interface for a safe ByteBuffer for reading purposes. This interface allows
 * only to read data from a buffer, no other operation is allowed on it. The
 * implementations must make sure that only the allowed data can be read.
 *
 * @author Geneviève Bastien
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.1
 */
public interface ISafeByteBufferReader {

    /**
     * Reads a byte at the buffer's current position
     *
     * @return The byte read
     */
    byte get();

    /**
     * Transfers bytes from this buffer's current position into the destination
     * array
     *
     * @param dst
     *            The destination array
     */
    void get(byte[] dst);

    /**
     * Reads the char at the buffer's current position
     *
     * @return The char read
     */
    char getChar();

    /**
     * Reads the double at the buffer's current position
     *
     * @return The double read
     */
    double getDouble();

    /**
     * Reads the float at the buffer's current position
     *
     * @return The float read
     */
    float getFloat();

    /**
     * Reads the int at the buffer's current position
     *
     * @return The int read
     */
    int getInt();

    /**
     * Reads the long at the buffer's current position
     *
     * @return The long read
     */
    long getLong();

    /**
     * Reads the short at the buffer's current position
     *
     * @return The short read
     */
    short getShort();

    /**
     * Gets a string from the byte buffer.
     *
     * @return The string value read
     */
    String getString();

}
