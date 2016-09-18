/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.index;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityHandle;
import com.eventsourcing.Journal;
import com.eventsourcing.Repository;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Service;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.Index;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.function.Function;

public interface IndexEngine extends Service {

    String getType();

    /**
     * Sets journal to be used in this repository
     * <p>
     * Should be done before invoking {@link #startAsync()}
     *
     * @param journal
     * @throws IllegalStateException if called after the service is started
     */
    void setJournal(Journal journal) throws IllegalStateException;

    /**
     * Set repository. Should be done before invoking {@link #startAsync()}
     *
     * @param repository
     * @throws IllegalStateException if called after the service is started
     */
    void setRepository(Repository repository) throws IllegalStateException;

    @SuppressWarnings("unchecked") <O extends Entity, A> Index<O> getIndexOnAttributes(Attribute<O, A>[] attributes,
                                                                                               IndexFeature... features)
            throws IndexNotSupported;

    enum IndexFeature {
        UNIQUE, COMPOUND,

        EQ,
        IN,
        LT,
        GT,
        BT,
        SW,
        EW,
        SC,
        CI,
        RX,
        HS,
        AQ,
        QZ
    }

    @Value class IndexCapabilities<T> {
        private String name;
        private IndexFeature[] features;
        private Function<T, Index> index;
    }

    <O extends Entity, A> Index<O> getIndexOnAttribute(Attribute<O, A> attribute, IndexFeature... features) throws
                                                                                                IndexNotSupported;

    @AllArgsConstructor class IndexNotSupported extends Exception {
        @Getter
        private Attribute[] attribute;
        @Getter
        private IndexFeature[] features;
        @Getter
        private IndexEngine indexEngine;

        @Override
        public String getMessage() {
            return "Index " + Joiner.on(", ").join(features) + " on " + Joiner.on(", ")
                                                                              .join(attribute) + " is not supported by " + indexEngine;
        }
    }

    <T extends Entity> IndexedCollection<EntityHandle<T>> getIndexedCollection(Class<T> klass);
}
