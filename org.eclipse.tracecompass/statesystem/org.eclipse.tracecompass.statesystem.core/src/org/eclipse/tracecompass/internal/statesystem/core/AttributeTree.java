/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.statesystem.core.ITmfStateSystem.INVALID_ATTRIBUTE;
import static org.eclipse.tracecompass.statesystem.core.ITmfStateSystem.ROOT_ATTRIBUTE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;

/**
 * The Attribute Tree is the /proc-like filesystem used to organize attributes.
 * Each node of this tree is both like a file and a directory in the
 * "file system".
 *
 * @author alexmont
 *
 */
public final class AttributeTree {

    /* "Magic number" for attribute tree files or file sections */
    private static final int ATTRIB_TREE_MAGIC_NUMBER = 0x06EC3671;

    /**
     * Character used to indicate an attribute path element is the same as the
     * previous attribute. Used for serialization.
     */
    private static final String SERIALIZATION_WILDCARD = "*"; //$NON-NLS-1$

    private final StateSystem fSs;
    private final List<Attribute> fAttributeList;
    private final Attribute fAttributeTreeRoot;
    private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

    /**
     * Standard constructor, create a new empty Attribute Tree
     *
     * @param ss
     *            The StateSystem to which this AT is attached
     */
    public AttributeTree(StateSystem ss) {
        fSs = ss;
        fAttributeList = new ArrayList<>();
        fAttributeTreeRoot = new Attribute(null, "root", ROOT_ATTRIBUTE); //$NON-NLS-1$
    }

    /**
     * "Existing file" constructor. Builds an attribute tree from a
     * "mapping file" or mapping section previously saved somewhere.
     *
     * @param ss
     *            StateSystem to which this AT is attached
     * @param fis
     *            File stream where to read the AT information. Make sure it's
     *            sought at the right place!
     * @throws IOException
     *             If there is a problem reading from the file stream
     */
    public AttributeTree(StateSystem ss, FileInputStream fis) throws IOException {
        this(ss);
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));

        /* Read the header of the Attribute Tree file (or file section) */
        int res = ois.readInt(); /* Magic number */
        if (res != ATTRIB_TREE_MAGIC_NUMBER) {
            throw new IOException("The attribute tree file section is either invalid or corrupted."); //$NON-NLS-1$
        }


        ArrayList<@NonNull String @NonNull []> attribList;
        try {
            @SuppressWarnings("unchecked")
            ArrayList<@NonNull String @NonNull []> list = (ArrayList<@NonNull String @NonNull []>) ois.readObject();
            attribList = list;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unrecognizable attribute list"); //$NON-NLS-1$
        }

        /*
         * Now we have 'list', the ArrayList of String arrays representing all
         * the attributes. Simply create attributes the normal way from them.
         */
        String[] prevFullAttribute = null;
        for (String[] attrib : attribList) {
            String[] curFullAttribute = decodeFullAttribute(prevFullAttribute, attrib);
            getQuarkAndAdd(ROOT_ATTRIBUTE, curFullAttribute);
            prevFullAttribute = curFullAttribute;
        }
    }

    /**
     * Tell the Attribute Tree to write itself somewhere in a file.
     *
     * @param file
     *            The file to write to
     * @param pos
     *            The position (in bytes) in the file where to write
     */
    public void writeSelf(File file, long pos) {
        fLock.readLock().lock();
        try (FileOutputStream fos = new FileOutputStream(file, true);
                FileChannel fc = fos.getChannel();) {
            fc.position(pos);
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                /* Write the almost-magic number */
                oos.writeInt(ATTRIB_TREE_MAGIC_NUMBER);

                /* Compute the serialized list of attributes and write it */
                List<String[]> list = new ArrayList<>(fAttributeList.size());
                String[] prevFullAttribute = null;
                for (Attribute entry : fAttributeList) {
                    String[] curFullAttribute = entry.getFullAttribute();
                    String[] curEncodedAttribute = encodeFullAttribute(prevFullAttribute, entry.getFullAttribute());
                    list.add(curEncodedAttribute);
                    prevFullAttribute = curFullAttribute;
                }
                oos.writeObject(list);
            }
        } catch (IOException e) {
            Activator.getDefault().logError("Error writing the file " + file, e); //$NON-NLS-1$
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Avoid repeating path elements that are the same from one attribute to the
     * next, and replace identical path elements with "*".
     *
     * @param prevPath
     *            The previous attribute's full attribute path
     * @param curPath
     *            The current attribute's full attribute path
     * @return An encoded version of the current entry's full attribute path
     *         where subpaths[i] equal to that of prevEntry's subpaths[i] is
     *         replaced by "*" or curPath if prevPath is null.
     */
    private static String[] encodeFullAttribute(String[] prevPath, String[] curPath) {
        if (prevPath == null) {
            return curPath;
        }
        String[] diff = new String[curPath.length];
        for (int i = 0; i < curPath.length; i++) {
            if (i < prevPath.length && prevPath[i].equals(curPath[i])) {
                diff[i] = SERIALIZATION_WILDCARD;
            } else {
                diff[i] = curPath[i];
            }
        }
        return diff;
    }

    /**
     * Decode a full attribute path that was encoded by
     * {@link #encodeFullAttribute}.
     *
     * @param prevPath
     *            The previous attribute's decoded full attribute path
     * @param curPath
     *            The current attribute's encoded full attribute path
     * @return A decoded version of the current entry's full attribute path
     *         where subpaths[i] equal to "*" are replaced by prevEntry's
     *         subpaths[i] or curPath if prevPath is null.
     */
    private static String[] decodeFullAttribute(String[] prevPath, String[] curPath) {
        if(prevPath == null){
            return curPath;
        }
        String[] diff = new String[curPath.length];
        for (int i = 0; i < curPath.length; i++) {
            if (i < prevPath.length && curPath[i].equals(SERIALIZATION_WILDCARD)) {
                diff[i] = prevPath[i];
            } else {
                diff[i] = curPath[i];
            }
        }
        return diff;
    }

    /**
     * Return the number of attributes this system as seen so far. Note that
     * this also equals the integer value (quark) the next added attribute will
     * have.
     *
     * @return The current number of attributes in the tree
     */
    public int getNbAttributes() {
        fLock.readLock().lock();
        try {
            return fAttributeList.size();
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Get the quark for a given attribute path. No new attribute will be
     * created : if the specified path does not exist, return
     * {@link ITmfStateSystem#INVALID_ATTRIBUTE}.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which relative queries will
     *            start. Use {@link ITmfStateSystem#ROOT_ATTRIBUTE} to start at
     *            the root node.
     * @param subPath
     *            The path to the attribute, relative to the starting node.
     * @return The quark of the specified attribute, or
     *         {@link ITmfStateSystem#INVALID_ATTRIBUTE} if that attribute does
     *         not exist.
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     */
    public int getQuarkDontAdd(int startingNodeQuark, String... subPath) {
        /* If subPath is empty, simply return the starting quark */
        if (subPath == null || subPath.length == 0) {
            return startingNodeQuark;
        }

        fLock.readLock().lock();
        try {

            /* Get the "starting node" */
            Attribute startingNode = getAttribute(startingNodeQuark);
            return startingNode.getSubAttributeQuark(subPath);
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Get the quark of a given attribute path. If that specified path does not
     * exist, it will be created (and the quark that was just created will be
     * returned).
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which relative queries will
     *            start. Use {@link ITmfStateSystem#ROOT_ATTRIBUTE} to start at
     *            the root node.
     * @param subPath
     *            The path to the attribute, relative to the starting node.
     * @return The quark of the attribute represented by the path
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     */
    public int getQuarkAndAdd(int startingNodeQuark, String... subPath) {
        fLock.writeLock().lock();
        try {
            /* Get the "starting node" */
            Attribute prevNode = getAttribute(startingNodeQuark);

            int knownQuark = prevNode.getSubAttributeQuark(subPath);
            if (knownQuark == INVALID_ATTRIBUTE) {
                /*
                 * The attribute was not in the table previously, and we want to add it
                 */
                for (String curDirectory : subPath) {
                    Attribute nextNode = prevNode.getSubAttributeNode(curDirectory);
                    if (nextNode == null) {
                        /* This is where we need to start adding */
                        nextNode = new Attribute(prevNode, checkNotNull(curDirectory), fAttributeList.size());
                        prevNode.addSubAttribute(nextNode);
                        fAttributeList.add(nextNode);
                        fSs.addEmptyAttribute();
                    }
                    prevNode = nextNode;
                }
                return fAttributeList.size() - 1;
            }
            /*
             * The attribute already existed, return the quark of that attribute
             */
            return knownQuark;
        } finally {
            fLock.writeLock().unlock();
        }
    }

    /**
     * Returns the sub-attributes of the quark passed in parameter
     *
     * @param attributeQuark
     *            The quark of the attribute to print the sub-attributes of.
     * @param recursive
     *            Should the query be recursive or not? If false, only children
     *            one level deep will be returned. If true, all descendants will
     *            be returned (depth-first search)
     * @return The list of quarks representing the children attributes
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    public @NonNull List<@NonNull Integer> getSubAttributes(int attributeQuark, boolean recursive) {
        fLock.readLock().lock();
        try {
            List<@NonNull Integer> listOfChildren = new ArrayList<>();
            /* Set up the node from which we'll start the search */
            Attribute startingAttribute = getAttribute(attributeQuark);

            /* Iterate through the sub-attributes and add them to the list */
            addSubAttributes(listOfChildren, startingAttribute, recursive);

            return listOfChildren;
        } finally {
            fLock.readLock().unlock();
        }
    }

    private Attribute getAttribute(int startingNodeQuark) {
        if (startingNodeQuark == ROOT_ATTRIBUTE) {
            return fAttributeTreeRoot;
        }
        return fAttributeList.get(startingNodeQuark);
    }

    /**
     * Returns the parent quark of the attribute. The root attribute has no
     * parent and will return {@link ITmfStateSystem#ROOT_ATTRIBUTE}.
     *
     * @param quark
     *            The quark of the attribute
     * @return Quark of the parent attribute or
     *         {@link ITmfStateSystem#ROOT_ATTRIBUTE} for the root attribute
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public int getParentAttributeQuark(int quark) {
        if (quark == ROOT_ATTRIBUTE) {
            return quark;
        }
        fLock.readLock().lock();
        try {
            return fAttributeList.get(quark).getParentAttributeQuark();
        } finally {
            fLock.readLock().unlock();
        }
    }

    private void addSubAttributes(List<Integer> list, Attribute curAttribute,
            boolean recursive) {
        for (Attribute childNode : curAttribute.getSubAttributes()) {
            list.add(childNode.getQuark());
            if (recursive) {
                addSubAttributes(list, childNode, true);
            }
        }
    }

    /**
     * Get then base name of an attribute specified by a quark.
     *
     * @param quark
     *            The quark of the attribute
     * @return The (base) name of the attribute
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public @NonNull String getAttributeName(int quark) {
        fLock.readLock().lock();
        try {
            return fAttributeList.get(quark).getName();
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Get the full path name of an attribute specified by a quark.
     *
     * @param quark
     *            The quark of the attribute
     * @return The full path name of the attribute
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public @NonNull String getFullAttributeName(int quark) {
        fLock.readLock().lock();
        try {
            return fAttributeList.get(quark).getFullAttributeName();
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Get the full path name (as an array of path elements) of an attribute
     * specified by a quark.
     *
     * @param quark
     *            The quark of the attribute
     * @return The path elements of the full path
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public String @NonNull [] getFullAttributePathArray(int quark) {
        fLock.readLock().lock();
        try {
            return fAttributeList.get(quark).getFullAttribute();
        } finally {
            fLock.readLock().unlock();
        }
    }

    /**
     * Debug-print all the attributes in the tree.
     *
     * @param writer
     *            The writer where to print the output
     */
    public void debugPrint(PrintWriter writer) {
        fLock.readLock().lock();
        try {
            fAttributeTreeRoot.debugPrint(writer);
        } finally {
            fLock.readLock().unlock();
        }
    }

}
