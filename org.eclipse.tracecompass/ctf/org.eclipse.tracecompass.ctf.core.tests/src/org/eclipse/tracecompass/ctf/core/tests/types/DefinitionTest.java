/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.junit.Test;

/**
 * The class <code>DefinitionTest</code> contains tests for the class
 * <code>{@link Definition}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
public class DefinitionTest {

    /**
     * Since Definition is abstract, we'll minimally extend it here to
     * instantiate it.
     */
    static class DefTest extends Definition {

        @NonNull
        private static final StringDeclaration STRINGDEC = StringDeclaration.getStringDeclaration(Encoding.UTF8);

        public DefTest(IDefinitionScope definitionScope, @NonNull String fieldName) {
            super(DefTest.STRINGDEC, definitionScope, fieldName);
        }

        @Override
        @NonNull
        public IDeclaration getDeclaration() {
            return DefTest.STRINGDEC;
        }

    }

    /**
     * Test a definition
     */
    @Test
    public void testToString() {
        IDefinition fixture = new DefTest(null, "Hello");
        String result = fixture.toString();

        assertNotNull(result);
    }
}