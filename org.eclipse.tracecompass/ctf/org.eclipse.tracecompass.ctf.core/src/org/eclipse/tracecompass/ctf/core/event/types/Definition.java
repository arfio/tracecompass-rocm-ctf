/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;

/**
 * A CTF definition
 *
 * A definition is like an object of a declaration class. It fills the
 * declaration with values. <br>
 * An example: <br>
 * int i = 0; <br>
 * <b>int</b> is the declaration.<br>
 * <b>i</b> is the definition.<br>
 * <b>0</b> is the value assigned to the definition, not the declaration.<br>
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public abstract class Definition implements IDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fFieldName;

    /** The complete path of this field */
    private final @NonNull ILexicalScope fPath;

    private final IDefinitionScope fDefinitionScope;

    @NonNull
    private final IDeclaration fDeclaration;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the event declaration
     * @param definitionScope
     *            the definition is in a scope, (normally a struct) what is it?
     * @param fieldName
     *            the name of the definition. (it is a field in the parent
     *            scope)
     */
    public Definition(@NonNull IDeclaration declaration, IDefinitionScope definitionScope, @NonNull String fieldName) {
        this(declaration, definitionScope, fieldName, declaration.getPath(definitionScope, fieldName));
    }

    /**
     * Constructor This one takes the scope and thus speeds up definition
     * creation
     *
     *
     * @param declaration
     *            the event declaration
     *
     * @param definitionScope
     *            the definition is in a scope, (normally a struct) what is it?
     *
     * @param fieldName
     *            the name of the defintions. it is a field in the parent scope.
     *
     * @param scope
     *            the scope
     * @since 1.0
     */
    public Definition(@NonNull IDeclaration declaration, IDefinitionScope definitionScope, @NonNull String fieldName, @NonNull ILexicalScope scope) {
        fDeclaration = declaration;
        fDefinitionScope = definitionScope;
        fFieldName = fieldName;
        fPath = scope;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get the field name in its container.
     *
     * @return The field name
     */
    protected String getFieldName() {
        return fFieldName;
    }

    /**
     * @since 1.0
     */
    @Override
    public ILexicalScope getScopePath() {
        return fPath;
    }

    /**
     * Get the definition scope in which this definition is found.
     *
     * The complete path of a definition is thus the path of the definition
     * scope DOT the name of the definition (name of the field in its container)
     *
     * @return The definition scope
     */
    protected IDefinitionScope getDefinitionScope() {
        return fDefinitionScope;
    }

    @Override
    public long size() {
        return getDeclaration().getMaximumSize();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IDeclaration getDeclaration() {
        return fDeclaration;
    }

    @Override
    public String toString() {
        return fPath.getPath() + '[' + Integer.toHexString(hashCode()) + ']';
    }
}
