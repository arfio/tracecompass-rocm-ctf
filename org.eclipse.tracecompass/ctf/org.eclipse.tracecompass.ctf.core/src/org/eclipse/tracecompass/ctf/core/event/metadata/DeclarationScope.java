/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial Design and Grammar
 * Contributors: Simon Marchi    - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * <b><u>DeclarationScope</u></b>
 * <p>
 * A DeclarationScope keeps track of the various CTF declarations for a given
 * scope.
 *
 * TODO: The notion of "symbols" and the notion of "scope" are misused in this
 * parser, which leads to inefficient tree management. It should be cleaned up.
 *
 * @author Matthew Khouzam
 * @author Simon Marchi
 * @since 1.1
 *
 */
public class DeclarationScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static final AtomicLong UNIQUE_ID_FACTORY = new AtomicLong(0);
    private final DeclarationScope fParentScope;
    private @NonNull Map<String, DeclarationScope> fChildren = new HashMap<>();

    private final Map<String, StructDeclaration> fStructs = new HashMap<>();
    private final Map<String, EnumDeclaration> fEnums = new HashMap<>();
    private final Map<String, VariantDeclaration> fVariants = new HashMap<>();
    private final Map<String, IDeclaration> fTypes = new HashMap<>();
    private final Map<String, IDeclaration> fIdentifiers = new HashMap<>();
    private String fName;
    private long fUniqueId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a declaration scope with no parent.
     */
    public DeclarationScope() {
        this(null, (String) null);
    }

    /**
     * Creates a declaration scope with the specified parent.
     *
     * @param parentScope
     *            The parent of the newly created scope.
     * @param name
     *            scope name
     */
    public DeclarationScope(DeclarationScope parentScope, String name) {
        fParentScope = parentScope;
        fName = name;
        fUniqueId = UNIQUE_ID_FACTORY.getAndIncrement();
        if (parentScope != null) {
            parentScope.registerChild(name, this);
        }
    }

    /**
     * Copy constructor with a specified parent and a scope to copy
     *
     * @param parentScope
     *            the parent
     * @param source
     *            the scope to copy
     */
    private DeclarationScope(DeclarationScope parentScope, DeclarationScope source) {
        this(parentScope, source.fName);
        fUniqueId = source.fUniqueId;
        fStructs.putAll(source.fStructs);
        fEnums.putAll(source.fEnums);
        fVariants.putAll(source.fVariants);
        fTypes.putAll(source.fTypes);
        fIdentifiers.putAll(source.fIdentifiers);
        /*
         * Copy all the children recursively
         */
        for (DeclarationScope child : source.fChildren.values()) {
            addChild(child);
        }
    }

    private void registerChild(String name, DeclarationScope declarationScope) {
        fChildren.put(name, declarationScope);
    }

    private boolean checkValid(DeclarationScope childSource) {
        long uniqueId = childSource.fUniqueId;
        DeclarationScope parent = this;
        while (parent != null) {
            if (uniqueId == parent.fUniqueId) {
                return false;
            }
            parent = parent.getParentScope();
        }
        return true;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Returns the parent of the current scope.
     *
     * @return The parent scope.
     */
    public DeclarationScope getParentScope() {
        return fParentScope;
    }

    /**
     * Sets the name of the scope
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        if (hasParent()) {
            fParentScope.fChildren.remove(fName);
            fParentScope.fChildren.put(name, this);
            fName = name;
        }
    }

    // ------------------------------------------------------------------------
    // Registration operations
    // ------------------------------------------------------------------------

    /**
     * Registers a type declaration.
     *
     * @param name
     *            The name of the type.
     * @param declaration
     *            The type declaration.
     * @throws ParseException
     *             if a type with the same name has already been defined.
     */
    public void registerType(String name, IDeclaration declaration)
            throws ParseException {
        /* Check if the type has been defined in the current scope */
        if (fTypes.containsKey(name)) {
            throw new ParseException("Type has already been defined:" + name); //$NON-NLS-1$
        }

        /* Add it to the register. */
        fTypes.put(name, declaration);
    }

    /**
     * Registers an identifier declaration.
     *
     * @param name
     *            name of the identifier
     * @param declaration
     *            the identfier's declaration
     * @throws ParseException
     *             if an identifier with the same name has already been defined.
     */
    public void registerIdentifier(String name, IDeclaration declaration) throws ParseException {
        /* Check if the type has been defined in the current scope */
        if (fIdentifiers.containsKey(name)) {
            throw new ParseException("Identifier has already been defined:" + name); //$NON-NLS-1$
        }

        /* Add it to the register. */
        fIdentifiers.put(name, declaration);
    }

    /**
     * Registers a struct declaration.
     *
     * @param name
     *            The name of the struct.
     * @param declaration
     *            The declaration of the struct.
     * @throws ParseException
     *             if a struct with the same name has already been registered.
     */
    public void registerStruct(String name, StructDeclaration declaration)
            throws ParseException {
        /* Check if the struct has been defined in the current scope. */
        if (fStructs.containsKey(name)) {
            throw new ParseException("Struct has already been defined:" + name); //$NON-NLS-1$
        }

        /* Add it to the register. */
        fStructs.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String structPrefix = "struct "; //$NON-NLS-1$
        registerType(structPrefix + name, declaration);
    }

    /**
     * Registers an enum declaration.
     *
     * @param name
     *            The name of the enum.
     * @param declaration
     *            The declaration of the enum.
     * @throws ParseException
     *             if an enum with the same name has already been registered.
     */
    public void registerEnum(String name, EnumDeclaration declaration)
            throws ParseException {
        /* Check if the enum has been defined in the current scope. */
        if (lookupEnum(name) != null) {
            throw new ParseException("Enum has already been defined:" + name); //$NON-NLS-1$
        }

        /* Add it to the register. */
        fEnums.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String enumPrefix = "enum "; //$NON-NLS-1$
        registerType(enumPrefix + name, declaration);
    }

    /**
     * Registers a variant declaration.
     *
     * @param name
     *            The name of the variant.
     * @param declaration
     *            The declaration of the variant.
     * @throws ParseException
     *             if a variant with the same name has already been registered.
     */
    public void registerVariant(String name, VariantDeclaration declaration)
            throws ParseException {
        /* Check if the variant has been defined in the current scope. */
        final VariantDeclaration lookupVariant = lookupVariant(name);
        if (declaration.equals(lookupVariant)) {
            return;
        }
        if (lookupVariant != null) {
            throw new ParseException("Variant has already been defined:" + name); //$NON-NLS-1$
        }

        /* Add it to the register. */
        fVariants.put(name, declaration);

        /* It also defined a new type, so add it to the type declarations. */
        String variantPrefix = "variant "; //$NON-NLS-1$
        registerType(variantPrefix + name, declaration);
    }

    // ------------------------------------------------------------------------
    // Lookup operations
    // ------------------------------------------------------------------------

    /**
     * Lookup the children scopes of this scope
     *
     * @param name
     *            the child to lookup
     * @return the scope or null
     */
    public @Nullable DeclarationScope lookupChild(String name) {
        return fChildren.get(name);
    }

    /**
     * Lookup the children scopes of this scope and this scope's parents
     *
     * @param name
     *            the child to lookup
     * @return the scope or null
     */
    public @Nullable DeclarationScope lookupChildRecursive(String name) {
        final DeclarationScope declarationScope = fChildren.get(name);
        if (declarationScope == null && fParentScope != null) {
            return fParentScope.lookupChildRecursive(name);
        }
        return declarationScope;
    }

    /**
     * Looks up a type declaration in the current scope.
     *
     * @param name
     *            The name of the type to search for.
     * @return The type declaration, or null if no type with that name has been
     *         defined.
     */
    public IDeclaration lookupType(String name) {
        return fTypes.get(name);
    }

    /**
     * Looks up a type declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the type to search for.
     * @return The type declaration, or null if no type with that name has been
     *         defined.
     */
    public IDeclaration lookupTypeRecursive(String name) {
        IDeclaration declaration = lookupType(name);
        if (declaration != null) {
            return declaration;
        } else if (hasParent()) {
            return fParentScope.lookupTypeRecursive(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up a struct declaration.
     *
     * @param name
     *            The name of the struct to search for.
     * @return The struct declaration, or null if no struct with that name has
     *         been defined.
     */
    public StructDeclaration lookupStruct(String name) {
        return fStructs.get(name);
    }

    /**
     * Looks up a struct declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the struct to search for.
     * @return The struct declaration, or null if no struct with that name has
     *         been defined.
     */
    public StructDeclaration lookupStructRecursive(String name) {
        StructDeclaration declaration = lookupStruct(name);
        if (declaration != null) {
            return declaration;
        } else if (hasParent()) {
            return fParentScope.lookupStructRecursive(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up an enum declaration.
     *
     * @param name
     *            The name of the enum to search for.
     * @return The enum declaration, or null if no enum with that name has been
     *         defined.
     */
    public EnumDeclaration lookupEnum(String name) {
        return fEnums.get(name);
    }

    /**
     * Looks up an enum declaration in the current scope and recursively in the
     * parent scopes.
     *
     * @param name
     *            The name of the enum to search for.
     * @return The enum declaration, or null if no enum with that name has been
     *         defined.
     */
    public EnumDeclaration lookupEnumRecursive(String name) {
        EnumDeclaration declaration = lookupEnum(name);
        if (declaration != null) {
            return declaration;
        } else if (hasParent()) {
            return fParentScope.lookupEnumRecursive(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up a variant declaration.
     *
     * @param name
     *            The name of the variant to search for.
     * @return The variant declaration, or null if no variant with that name has
     *         been defined.
     */
    public VariantDeclaration lookupVariant(String name) {
        return fVariants.get(name);
    }

    /**
     * Looks up a variant declaration in the current scope and recursively in
     * the parent scopes.
     *
     * @param name
     *            The name of the variant to search for.
     * @return The variant declaration, or null if no variant with that name has
     *         been defined.
     */
    public VariantDeclaration lookupVariantRecursive(String name) {
        VariantDeclaration declaration = lookupVariant(name);
        if (declaration != null) {
            return declaration;
        } else if (hasParent()) {
            return fParentScope.lookupVariantRecursive(name);
        } else {
            return null;
        }
    }

    private boolean hasParent() {
        return fParentScope != null;
    }

    /**
     * Lookup query for an identifier in this scope.
     *
     * @param identifier
     *            the name of the identifier to search for. In the case of int
     *            x; it would be "x"
     * @return the declaration of the type associated to that identifier
     */
    public IDeclaration lookupIdentifier(String identifier) {
        return fIdentifiers.get(identifier);
    }

    /**
     * Lookup query for an identifier through this scope and its ancestors. An
     * ancestor scope is a scope in which this scope is nested.
     *
     * @param identifier
     *            the name of the identifier to search for. In the case of int
     *            x; it would be "x"
     * @return the declaration of the type associated to that identifier
     */
    public IDeclaration lookupIdentifierRecursive(String identifier) {
        if (identifier.contains(".")) { //$NON-NLS-1$
            String[] scopes = identifier.split("\\."); //$NON-NLS-1$
            return lookupIdentifierRecursive(scopes);
        }
        IDeclaration declaration = lookupIdentifier(identifier);
        if (declaration != null) {
            return declaration;
        } else if (hasParent()) {
            return fParentScope.lookupIdentifierRecursive(identifier);
        }
        return null;
    }

    private IDeclaration lookupIdentifierRecursive(String[] scopes) {
        if (scopes.length < 1) {
            return null;
        }
        // find first element
        DeclarationScope scope = this;
        scope = lookupRoot(scopes, scope);
        if (scope == null) {
            return null;
        }
        for (int i = 1; i < scopes.length; i++) {
            final String scopeName = scopes[i];
            if (scope == null) {
                return null;
            }
            IDeclaration declaration = lookupIdentifierElement(scope, scopeName, Arrays.copyOfRange(scopes, i, scopes.length));
            if (declaration != null) {
                return declaration;
            }
            scope = scope.fChildren.get(scopeName);
        }
        return null;
    }

    private static DeclarationScope lookupRoot(String[] scopes, DeclarationScope originScope) {
        DeclarationScope scope = originScope;
        final String rootElement = scopes[0];
        while (!rootElement.equals(scope.fName)) {
            if (!scope.hasParent()) {
                return scope.fChildren.get(rootElement);
            }
            scope = scope.getParentScope();
        }
        return scope;
    }

    private IDeclaration lookupIdentifierElement(DeclarationScope scope, String scopeName, String[] scopes) {
        if (scope.fStructs.containsKey(scopeName)) {
            return lookupStructScope(scope, scopeName, scopes);
        } else if (scope.fTypes.containsKey(scopeName)) {
            return scope.fTypes.get(scopeName);
        } else if (scope.fEnums.containsKey(scopeName)) {
            return scope.fEnums.get(scopeName);
        } else if (scope.fIdentifiers.containsKey(scopeName)) {
            return scope.fIdentifiers.get(scopeName);
        }
        return null;
    }

    private IDeclaration lookupStructScope(DeclarationScope scope, String scopeName, String[] scopes) {
        final IDeclaration structDeclaration = scope.fStructs.get(scopeName);
        if (scopes.length <= 1) {
            return structDeclaration;
        }
        return lookupIdentifierStructElement((StructDeclaration) structDeclaration, scopes[1], Arrays.copyOfRange(scopes, 2, scopes.length));
    }

    private IDeclaration lookupIdentifierStructElement(StructDeclaration structDeclaration, String string, String[] children) {
        IDeclaration field = structDeclaration.getField(string);
        if (children == null || children.length <= 0) {
            return field;
        }
        if (field instanceof StructDeclaration) {
            StructDeclaration fieldStructDeclaration = (StructDeclaration) field;
            return lookupIdentifierStructElement(fieldStructDeclaration, children[0], Arrays.copyOfRange(children, 1, children.length));
        }
        return null;
    }

    /**
     * Get all the type names of this scope.
     *
     * @return The type names
     */
    public Set<String> getTypeNames() {
        return fTypes.keySet();
    }

    /**
     * Replace a type with a new one.
     *
     * @param name
     *            The name of the type
     * @param newType
     *            The type
     * @throws ParseException
     *             If the type does not exist.
     */
    public void replaceType(String name, IDeclaration newType) throws ParseException {
        if (fTypes.containsKey(name)) {
            fTypes.put(name, newType);
        } else {
            throw new ParseException("Trace does not contain type:" + name); //$NON-NLS-1$
        }
    }

    /**
     * Add a child scope
     *
     * @param scope
     *            the child
     */
    public void addChild(DeclarationScope scope) {
        if (checkValid(scope)) {
            DeclarationScope scopeCopy = new DeclarationScope(this, scope);
            registerChild(scopeCopy.fName, scopeCopy);
        }

    }

    @Override
    public String toString() {
        return "Scope : " + fName + " children: " + fChildren.size() + (fParentScope == null ? "" : (" parent " + fParentScope.fName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
