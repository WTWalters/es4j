/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.layout.types;

import com.eventsourcing.layout.TypeHandler;
import com.fasterxml.classmate.TypeResolver;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EnumTypeHandlerTest {

    enum A {A, B, C}

    enum A1 {A, B, C}

    enum A2 {C, A, B}

    @Test @SneakyThrows
    public void fingerprintShape() {
        TypeHandler typeHandlerA = TypeHandler.lookup(new TypeResolver().resolve(A.class));
        TypeHandler typeHandlerA1 = TypeHandler.lookup(new TypeResolver().resolve(A1.class));
        TypeHandler typeHandlerA2 = TypeHandler.lookup(new TypeResolver().resolve(A2.class));
        assertTrue(Arrays.equals(typeHandlerA.getFingerprint(), typeHandlerA1.getFingerprint()));
        assertFalse(Arrays.equals(typeHandlerA.getFingerprint(), typeHandlerA2.getFingerprint()));
    }

}