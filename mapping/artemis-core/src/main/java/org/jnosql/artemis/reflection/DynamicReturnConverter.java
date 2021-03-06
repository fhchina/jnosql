/*
 *  Copyright (c) 2019 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.reflection;

import org.jnosql.artemis.PreparedStatement;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The converter within the return method at Repository class.
 */
enum DynamicReturnConverter {

    INSTANCE;


    private final DynamicExecutorQueryConverter defaultConverter = new DefaultDynamicExecutorQueryConverter();

    private final DynamicExecutorQueryConverter paginationConverter = new PaginationDynamicExecutorQueryConverter();

    /**
     * Converts the entity from the Method return type.
     *
     * @param dynamic the information about the method and return source
     * @return the conversion result
     * @throws NullPointerException when the dynamic is null
     */
    public Object convert(DynamicReturn<?> dynamic) {

        Method method = dynamic.getMethod();
        Class<?> typeClass = dynamic.typeClass();
        Class<?> returnType = method.getReturnType();

        DynamicReturnType type = DynamicReturnType.of(typeClass, returnType);
        DynamicExecutorQueryConverter converter = getConverter(dynamic);

        switch (type) {
            case INSTANCE:
                return converter.toInstance(dynamic);
            case OPTIONAL:
                return converter.toOptional(dynamic);
            case LIST:
            case ITERABLE:
            case COLLECTION:
                return converter.toList(dynamic);
            case SET:
                return converter.toSet(dynamic);
            case QUEUE:
            case DEQUE:
                return converter.toLinkedList(dynamic);
            case NAVIGABLE_SET:
            case SORTED_SET:
                return converter.toTreeSet(dynamic);
            case STREAM:
                return converter.toStream(dynamic);
            case PAGE:
                return converter.toPage(dynamic);
            default:
                return converter.toDefault(dynamic);

        }
    }

    /**
     * Reads and execute JNoSQL query from the Method that has the {@link org.jnosql.artemis.Query} annotation
     *
     * @return the result from the query annotation
     */
    public Object convert(DynamicQueryMethodReturn dynamicQueryMethod) {
        Method method = dynamicQueryMethod.getMethod();
        Object[] args = dynamicQueryMethod.getArgs();
        Function<String, List<?>> queryConverter = dynamicQueryMethod.getQueryConverter();
        Function<String, PreparedStatement> prepareConverter = dynamicQueryMethod.getPrepareConverter();
        Class<?> typeClass = dynamicQueryMethod.getTypeClass();

        String value = RepositoryReflectionUtils.INSTANCE.getQuery(method);


        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, args);
        List<?> entities;
        if (params.isEmpty()) {
            entities = queryConverter.apply(value);
        } else {
            PreparedStatement prepare = prepareConverter.apply(value);
            params.forEach(prepare::bind);
            entities = prepare.getResultList();
        }

        Supplier<List<?>> listSupplier = () -> entities;

        Supplier<Optional<?>> singleSupplier = DynamicReturn.toSingleResult(method).apply(listSupplier);

        DynamicReturn dynamicReturn = DynamicReturn.builder()
                .withClassSource(typeClass)
                .withMethodSource(method)
                .withList(listSupplier)
                .withSingleResult(singleSupplier)
                .build();

        return convert(dynamicReturn);
    }


    private DynamicExecutorQueryConverter getConverter(DynamicReturn<?> dynamic) {
        if (dynamic.hasPagination()) {
            return paginationConverter;
        } else {
            return defaultConverter;
        }
    }
}
