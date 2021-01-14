/*******************************************************************************.
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Matthew Khouzam - Initial Design and implementation + overhaul
 *  Francis Giraldeau - Initial API and implementation
 *  Philippe Proulx - Some refinement and optimization
 *  Etienne Bergeron <Etienne.Bergeron@gmail.com> - fix zero size read + cleanup
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.io;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * <b><u>BitBuffer</u></b>
 * <p>
 * A bitwise buffer capable of accessing fields with bit offsets.
 */
public final class BitBuffer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /* default bit width */
    private static final int BIT_CHAR = Byte.SIZE; // yum
    private static final int BYTE_MASK = (1 << BIT_CHAR) - 1;
    private static final int BIT_SHORT = Short.SIZE;
    private static final int SHORT_MASK = (1 << BIT_SHORT) - 1;
    private static final int BIT_INT = Integer.SIZE;
    private static final long INT_MASK = (1L << BIT_INT) - 1;
    private static final int BIT_LONG = Long.SIZE;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final @NonNull ByteBuffer fBuffer;
    private final long fBitCapacity;

    /**
     * Bit-buffer's position, maximum value = Integer.MAX_VALUE * 8
     */
    private long fPosition;
    private ByteOrder fByteOrder;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor, makes a big-endian buffer
     */
    public BitBuffer() {
        this(ByteBuffer.allocateDirect(0), ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor, makes a big-endian buffer
     *
     * @param buf
     *            the bytebuffer to read
     */
    public BitBuffer(@NonNull ByteBuffer buf) {
        this(buf, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Constructor that is fully parameterizable
     *
     * @param buf
     *            the buffer to read
     * @param order
     *            the byte order (big-endian, little-endian, network?)
     */
    public BitBuffer(@NonNull ByteBuffer buf, ByteOrder order) {
        fBuffer = buf;
        setByteOrder(order);
        resetPosition();
        fBitCapacity = (long) fBuffer.capacity() * BIT_CHAR;
    }

    private void resetPosition() {
        fPosition = 0;
    }

    // ------------------------------------------------------------------------
    // 'Get' operations on buffer
    // ------------------------------------------------------------------------

    /**
     * Relative <i>get</i> method for reading 32-bit integer.
     *
     * Reads next four bytes from the current bit position according to current
     * byte order.
     *
     * @return The int value (signed) read from the buffer
     * @throws CTFException
     *             An error occurred reading the long. This exception can be
     *             raised if the buffer tries to read out of bounds
     */
    public int getInt() throws CTFException {
        return getInt(BIT_INT, true);
    }

    /**
     * Relative <i>get</i> method for reading 64-bit integer.
     *
     * Reads next eight bytes from the current bit position according to current
     * byte order.
     *
     * @return The long value (signed) read from the buffer
     * @throws CTFException
     *             An error occurred reading the long. This exception can be
     *             raised if the buffer tries to read out of bounds
     */
    public long getLong() throws CTFException {
        return get(BIT_LONG, true);
    }

    /**
     * Relative <i>get</i> method for reading long of <i>length</i> bits.
     *
     * Reads <i>length</i> bits starting at the current position. The result is
     * signed extended if <i>signed</i> is true. The current position is
     * increased of <i>length</i> bits.
     *
     * @param length
     *            The length in bits of this integer
     * @param signed
     *            The sign extended flag
     * @return The long value read from the buffer
     * @throws CTFException
     *             An error occurred reading the data. If more than 64 bits at a
     *             time are read, or the buffer is read beyond its end, this
     *             exception will be raised.
     */
    public long get(int length, boolean signed) throws CTFException {
        if (length > BIT_LONG) {
            throw new CTFException("Cannot read a long longer than 64 bits. Rquested: " + length); //$NON-NLS-1$
        }
        if (length > BIT_INT) {
            final int highShift = length - BIT_INT;
            long a = getInt();
            long b = getInt(highShift, false);
            long retVal;
            /* Cast the signed-extended int into a unsigned int. */
            a &= INT_MASK;
            b &= (1L << highShift) - 1L;

            retVal = (fByteOrder == ByteOrder.BIG_ENDIAN) ? ((a << highShift) | b) : ((b << BIT_INT) | a);
            /* sign extend */
            if (signed) {
                int signExtendBits = BIT_LONG - length;
                retVal = (retVal << signExtendBits) >> signExtendBits;
            }
            return retVal;
        }
        long retVal = getInt(length, signed);
        return (signed ? retVal : (retVal & INT_MASK));
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p>
     * This method transfers <strong>bytes</strong> from this buffer into the
     * given destination array. This method currently only supports reads
     * aligned to 8 bytes. It is up to the developer to shift the bits in
     * post-processing to do unaligned reads.
     *
     * @param dst
     *            the bytes to write to
     * @throws BufferUnderflowException
     *             - If there are fewer than length bytes remaining in this
     *             buffer
     */
    public void get(byte @NonNull [] dst) {
        fBuffer.position((int) (fPosition / BIT_CHAR));
        fBuffer.get(dst);
        fPosition += dst.length * BIT_CHAR;
    }

    /**
     * Relative <i>get</i> method for reading integer of <i>length</i> bits.
     *
     * Reads <i>length</i> bits starting at the current position. The result is
     * signed extended if <i>signed</i> is true. The current position is
     * increased of <i>length</i> bits.
     *
     * @param length
     *            The length in bits of this integer
     * @param signed
     *            The sign extended flag
     * @return The int value read from the buffer
     * @throws CTFException
     *             An error occurred reading the data. When the buffer is read
     *             beyond its end, this exception will be raised.
     */
    private int getInt(int length, boolean signed) throws CTFException {

        /* Nothing to read. */
        if (length == 0) {
            return 0;
        }

        /* Validate that the buffer has enough bits. */
        if (!canRead(length)) {
            throw new CTFException("Cannot read the integer, " + //$NON-NLS-1$
                    "the buffer does not have enough remaining space. " + //$NON-NLS-1$
                    "Requested:" + length + " Available:" + (fBitCapacity - fPosition)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Get the value from the byte buffer. */
        int val = 0;
        boolean gotIt = false;

        /*
         * Try a fast read when the position is byte-aligned by using
         * java.nio.ByteBuffer's native methods
         */
        /*
         * A faster alignment detection as the compiler cannot guaranty that pos
         * is always positive.
         */
        if ((fPosition & (BitBuffer.BIT_CHAR - 1)) == 0) {
            switch (length) {
            case BitBuffer.BIT_CHAR:
                // Byte
                val = fBuffer.get((int) (fPosition / BIT_CHAR));
                if (!signed) {
                    val = val & BYTE_MASK;
                }
                gotIt = true;
                break;

            case BitBuffer.BIT_SHORT:
                // Word
                val = fBuffer.getShort((int) (fPosition / BIT_CHAR));
                if (!signed) {
                    val = val & SHORT_MASK;
                }
                gotIt = true;
                break;

            case BitBuffer.BIT_INT:
                // Double word
                val = fBuffer.getInt((int) (fPosition / BIT_CHAR));
                gotIt = true;
                break;

            default:
                break;
            }
        }

        /* When not byte-aligned, fall-back to a general decoder. */
        if (!gotIt) {
            // Nothing read yet: use longer methods
            if (fByteOrder == ByteOrder.LITTLE_ENDIAN) {
                val = getIntLE(fPosition, length, signed);
            } else {
                val = getIntBE(fPosition, length, signed);
            }
        }
        fPosition += length;

        return val;
    }

    private int getIntBE(long index, int length, boolean signed) {
        if ((length <= 0) || (length > BIT_INT)) {
            throw new IllegalArgumentException("Length must be between 1-32 bits"); //$NON-NLS-1$
        }
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask, cache;
        int value = 0;

        currByte = startByte;
        cache = fBuffer.get(currByte) & BYTE_MASK;
        boolean isNeg = (cache & (1 << (BIT_CHAR - (index % BIT_CHAR) - 1))) != 0;
        if (signed && isNeg) {
            value = ~0;
        }
        if (startByte == (endByte - 1)) {
            cmask = cache >>> ((BIT_CHAR - (end % BIT_CHAR)) % BIT_CHAR);
            if (((length) % BIT_CHAR) > 0) {
                mask = ~((~0) << length);
                cmask &= mask;
            }
            value <<= length;
            value |= cmask;
            return value;
        }
        cshift = (int) (index % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << (BIT_CHAR - cshift));
            cmask = cache & mask;
            lshift = BIT_CHAR - cshift;
            value <<= lshift;
            value |= cmask;
            currByte++;
        }
        for (; currByte < (endByte - 1); currByte++) {
            value <<= BIT_CHAR;
            value |= fBuffer.get(currByte) & BYTE_MASK;
        }
        lshift = (int) (end % BIT_CHAR);
        if (lshift > 0) {
            mask = ~((~0) << lshift);
            cmask = fBuffer.get(currByte) & BYTE_MASK;
            cmask >>>= BIT_CHAR - lshift;
            cmask &= mask;
            value <<= lshift;
            value |= cmask;
        } else {
            value <<= BIT_CHAR;
            value |= fBuffer.get(currByte) & BYTE_MASK;
        }
        return value;
    }

    private int getIntLE(long index, int length, boolean signed) {
        if ((length <= 0) || (length > BIT_INT)) {
            throw new IllegalArgumentException("Length must be between 1-32 bits"); //$NON-NLS-1$
        }
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask, cache, mod;
        int value = 0;

        currByte = endByte - 1;
        cache = fBuffer.get(currByte) & BYTE_MASK;
        mod = (int) (end % BIT_CHAR);
        lshift = (mod > 0) ? mod : BIT_CHAR;
        boolean isNeg = (cache & (1 << (lshift - 1))) != 0;
        if (signed && isNeg) {
            value = ~0;
        }
        if (startByte == (endByte - 1)) {
            cmask = cache >>> (index % BIT_CHAR);
            if (((length) % BIT_CHAR) > 0) {
                mask = ~((~0) << length);
                cmask &= mask;
            }
            value <<= length;
            value |= cmask;
            return value;
        }
        cshift = (int) (end % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = cache & mask;
            value <<= cshift;
            value |= cmask;
            currByte--;
        }
        for (; currByte >= (startByte + 1); currByte--) {
            value <<= BIT_CHAR;
            value |= fBuffer.get(currByte) & BYTE_MASK;
        }
        lshift = (int) (index % BIT_CHAR);
        if (lshift > 0) {
            mask = ~((~0) << (BIT_CHAR - lshift));
            cmask = fBuffer.get(currByte) & BYTE_MASK;
            cmask >>>= lshift;
            cmask &= mask;
            value <<= (BIT_CHAR - lshift);
            value |= cmask;
        } else {
            value <<= BIT_CHAR;
            value |= fBuffer.get(currByte) & BYTE_MASK;
        }
        return value;
    }

    // ------------------------------------------------------------------------
    // 'Put' operations on buffer
    // ------------------------------------------------------------------------

    /**
     * Relative <i>put</i> method to write signed 32-bit integer.
     *
     * Write four bytes starting from current bit position in the buffer
     * according to the current byte order. The current position is increased of
     * <i>length</i> bits.
     *
     * @param value
     *            The int value to write
     * @throws CTFException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     */
    public void putInt(int value) throws CTFException {
        putInt(BIT_INT, value);
    }

    /**
     * Relative <i>put</i> method to write <i>length</i> bits long.
     *
     * Writes <i>length</i> lower-order bits from the provided <i>value</i>,
     * starting from current bit position in the buffer. Sequential bytes are
     * written according to the current byte order. The sign bit is carried to the
     * MSB if signed is true. The sign bit is included in <i>length</i>. The current
     * position is increased of <i>length</i>.
     *
     * @param length
     *            The number of bits to write
     * @param value
     *            The value to write
     * @throws CTFException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     * @since 3.0
     */
    public void putLong(int length, long value) throws CTFException {
        // No overflow check since a long can only be 64 bits.
        if (!canRead(length)) {
            throw new CTFException("Cannot write to bitbuffer, " //$NON-NLS-1$
                    + "insufficient space. Requested: " + length); //$NON-NLS-1$
        }
        if (length == 0) {
            return;
        }
        if (length <= BIT_INT) {
            putInt(length, (int) value);
            return;
        }
        if (getByteOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            putInt(BIT_INT, (int) (value & 0xffffffffL));
            putInt(length - BIT_INT, (int) (value >> Integer.SIZE));
            return;
        }
        putInt(length - BIT_INT, (int) (value >> Integer.SIZE));
        putInt(BIT_INT, (int) (value & 0xffffffffL));
    }

    /**
     * Relative <i>put</i> method to write a byte array.
     *
     * Writes a byte array to the bit buffer.
     *
     * @param value
     *            The value to write
     * @throws CTFException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     * @since 3.0
     */
    public void put(byte[] value) throws CTFException {
        if (value == null || value.length == 0) {
            return;
        }
        if (!canRead(value.length)) {
            throw new CTFException("Cannot write to bitbuffer, " //$NON-NLS-1$
                    + "insufficient space. Requested: " + value.length); //$NON-NLS-1$
        }
        fBuffer.put(value);
    }

    /**
     * Relative <i>put</i> method to write a byte.
     *
     * Writes a byte to the bit buffer.
     *
     * @param value
     *            The value to write
     * @throws CTFException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     * @since 3.0
     */
    public void put(byte value) throws CTFException {
        if (!canRead(Byte.SIZE)) {
            throw new CTFException("Cannot write to bitbuffer, " //$NON-NLS-1$
                    + "insufficient space. Requested: " + Byte.SIZE); //$NON-NLS-1$
        }
        fBuffer.put(value);
    }

    /**
     * Relative <i>put</i> method to write <i>length</i> bits integer.
     *
     * Writes <i>length</i> lower-order bits from the provided <i>value</i>,
     * starting from current bit position in the buffer. Sequential bytes are
     * written according to the current byte order. The sign bit is carried to
     * the MSB if signed is true. The sign bit is included in <i>length</i>. The
     * current position is increased of <i>length</i>.
     *
     * @param length
     *            The number of bits to write
     * @param value
     *            The value to write
     * @throws CTFException
     *             An error occurred writing the data. If the buffer is written
     *             beyond its end, this exception will be raised.
     */
    public void putInt(int length, int value) throws CTFException {
        final long curPos = fPosition;

        if (!canRead(length)) {
            throw new CTFException("Cannot write to bitbuffer, " //$NON-NLS-1$
                    + "insufficient space. Requested: " + length); //$NON-NLS-1$
        }
        if (length == 0) {
            return;
        }
        if (fByteOrder == ByteOrder.LITTLE_ENDIAN) {
            putIntLE(curPos, length, value);
        } else {
            putIntBE(curPos, length, value);
        }
        fPosition += length;
    }

    private void putIntBE(long index, int length, int value) {
        if ((length <= 0) || (length > BIT_INT)) {
            throw new IllegalArgumentException("Length must be between 1-32 bits"); //$NON-NLS-1$
        }
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask;
        int correctedValue = value;

        /*
         * mask v high bits. Works for unsigned and two complement signed
         * numbers which value do not overflow on length bits.
         */

        if (length < BIT_INT) {
            correctedValue &= ~(~0 << length);
        }

        /* sub byte */
        if (startByte == (endByte - 1)) {
            lshift = (int) ((BIT_CHAR - (end % BIT_CHAR)) % BIT_CHAR);
            mask = ~((~0) << lshift);
            if ((index % BIT_CHAR) > 0) {
                mask |= (~(0)) << (BIT_CHAR - (index % BIT_CHAR));
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of left-shift and high bits are
             * already cleared
             */
            cmask &= ~mask;
            int b = fBuffer.get(startByte) & BYTE_MASK;
            fBuffer.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte contains MSB */
        currByte = endByte - 1;
        cshift = (int) (end % BIT_CHAR);
        if (cshift > 0) {
            lshift = BIT_CHAR - cshift;
            mask = ~((~0) << lshift);
            cmask = correctedValue << lshift;
            cmask &= ~mask;
            int b = fBuffer.get(currByte) & BYTE_MASK;
            fBuffer.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= cshift;
            currByte--;
        }

        /* middle byte(s) */
        for (; currByte >= (startByte + 1); currByte--) {
            fBuffer.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte contains LSB */
        if ((index % BIT_CHAR) > 0) {
            mask = (~0) << (BIT_CHAR - (index % BIT_CHAR));
            cmask = correctedValue & ~mask;
            int b = fBuffer.get(currByte) & BYTE_MASK;
            fBuffer.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            fBuffer.put(currByte, (byte) correctedValue);
        }
    }

    private void putIntLE(long index, int length, int value) {
        if ((length <= 0) || (length > BIT_INT)) {
            throw new IllegalArgumentException("Length must be between 1-32 bits"); //$NON-NLS-1$
        }
        long end = index + length;
        int startByte = (int) (index / BIT_CHAR);
        int endByte = (int) ((end + (BIT_CHAR - 1)) / BIT_CHAR);
        int currByte, lshift, cshift, mask, cmask;
        int correctedValue = value;

        /*
         * mask v high bits. Works for unsigned and two complement signed
         * numbers which value do not overflow on length bits.
         */

        if (length < BIT_INT) {
            correctedValue &= ~(~0 << length);
        }

        /* sub byte */
        if (startByte == (endByte - 1)) {
            lshift = (int) (index % BIT_CHAR);
            mask = ~((~0) << lshift);
            if ((end % BIT_CHAR) > 0) {
                mask |= (~(0)) << (end % BIT_CHAR);
            }
            cmask = correctedValue << lshift;
            /*
             * low bits are cleared because of left-shift and high bits are
             * already cleared
             */
            cmask &= ~mask;
            int b = fBuffer.get(startByte) & BYTE_MASK;
            fBuffer.put(startByte, (byte) ((b & mask) | cmask));
            return;
        }

        /* head byte */
        currByte = startByte;
        cshift = (int) (index % BIT_CHAR);
        if (cshift > 0) {
            mask = ~((~0) << cshift);
            cmask = correctedValue << cshift;
            cmask &= ~mask;
            int b = fBuffer.get(currByte) & BYTE_MASK;
            fBuffer.put(currByte, (byte) ((b & mask) | cmask));
            correctedValue >>>= BIT_CHAR - cshift;
            currByte++;
        }

        /* middle byte(s) */
        for (; currByte < (endByte - 1); currByte++) {
            fBuffer.put(currByte, (byte) correctedValue);
            correctedValue >>>= BIT_CHAR;
        }
        /* end byte */
        if ((end % BIT_CHAR) > 0) {
            mask = (~0) << (end % BIT_CHAR);
            cmask = correctedValue & ~mask;
            int b = fBuffer.get(currByte) & BYTE_MASK;
            fBuffer.put(currByte, (byte) ((b & mask) | cmask));
        } else {
            fBuffer.put(currByte, (byte) correctedValue);
        }
    }

    // ------------------------------------------------------------------------
    // Buffer attributes handling
    // ------------------------------------------------------------------------

    /**
     * Can this buffer be read for thus amount of bits?
     *
     * @param length
     *            the length in bits to read
     * @return does the buffer have enough room to read the next "length"
     */
    public boolean canRead(int length) {
        return ((fPosition + length) <= fBitCapacity);
    }

    /**
     * Sets the order of the buffer.
     *
     * @param order
     *            The order of the buffer.
     */
    public void setByteOrder(ByteOrder order) {
        if (!order.equals(fByteOrder)) {
            fByteOrder = order;
            fBuffer.order(order);
        }
    }

    /**
     * Sets the order of the buffer.
     *
     * @return The order of the buffer.
     */
    public ByteOrder getByteOrder() {
        return fByteOrder;
    }

    /**
     * Sets the position in the buffer.
     *
     * @param newPosition
     *            The new position of the buffer.
     * @throws CTFException
     *             Thrown on out of bounds exceptions
     */
    public void position(long newPosition) throws CTFException {

        if (newPosition > fBitCapacity) {
            throw new CTFException("Out of bounds exception on a position move, attempting to access position: " + newPosition + " / " + fBitCapacity); //$NON-NLS-1$ //$NON-NLS-2$
        }
        fPosition = newPosition;
    }

    /**
     * Gets the position in the buffer, in bits.
     *
     * @return order The position of the buffer.
     */
    public long position() {
        return fPosition;
    }

    /**
     * Gets the byte buffer
     *
     * @return The byte buffer
     */
    public ByteBuffer getByteBuffer() {
        return fBuffer;
    }

    /**
     * Resets the bitbuffer.
     */
    public void clear() {
        resetPosition();
        fBuffer.clear();
    }

}
