/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnosql.diana.api.column;

/**
 * The diana configuration to create a {@link ColumnFamilyManagerFactory}
 *
 * @param <SYNC> the ColumnFamilyManagerFactory type
 */
public interface ColumnConfiguration<SYNC extends ColumnFamilyManagerFactory, ASYNC extends ColumnFamilyManagerAsyncFactory> {

    /**
     * Reads configuration either from default configuration or a file defined by NoSQL
     * provider and then creates a {@link ColumnFamilyManagerFactory} instance.
     *
     * @return a {@link ColumnFamilyManagerFactory}
     */
    SYNC get();

    /**
     * Reads configuration either from default configuration or a file defined by NoSQL
     * provider and then creates a {@link ColumnFamilyManagerAsyncFactory} instance.
     *
     * @return a {@link ColumnFamilyManagerAsyncFactory}
     */
    ASYNC getAsync();
}
