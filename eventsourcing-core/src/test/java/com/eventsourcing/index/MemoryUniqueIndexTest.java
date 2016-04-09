/**
 * Copyright 2016 Eventsourcing team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package com.eventsourcing.index;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.unique.UniqueIndex;

public class MemoryUniqueIndexTest extends UniqueIndexTest<UniqueIndex> {

    @Override
    public <A, O> UniqueIndex onAttribute(Attribute<O, A> attribute) {
        return UniqueIndex.onAttribute(attribute);
    }
}