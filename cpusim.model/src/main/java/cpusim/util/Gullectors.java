/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cpusim.util;

// Taken from: https://raw.githubusercontent.com/maciejmiklas/cyclop/b90df02ab952e1aebccbb4be2595dc49e1464a99/cyclop-webapp/src/main/java/org/cyclop/common/Gullectors.java

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public abstract class Gullectors {

    private Gullectors() {
        // no instantiation possible
        throw new UnsupportedOperationException("No instantiation.");
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        Supplier<ImmutableList.Builder<T>> supplier = ImmutableList.Builder::new;
        BiConsumer<ImmutableList.Builder<T>, T> accumulator = (b, v) -> b.add(v);
        BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet.Builder::new;
        BiConsumer<ImmutableSet.Builder<T>, T> accumulator = (b, v) -> b.add(v);
        BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T extends Comparable<?>> Collector<T, ?, ImmutableSortedSet<T>> toNaturalImmutableSortedSet() {
        Supplier<ImmutableSortedSet.Builder<T>> supplier = ImmutableSortedSet::naturalOrder;
        BiConsumer<ImmutableSortedSet.Builder<T>, T> accumulator = (b, v) -> b.add(v);
        BinaryOperator<ImmutableSortedSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> finisher = ImmutableSortedSet.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {

        Supplier<ImmutableMap.Builder<K, V>> supplier = ImmutableMap.Builder::new;
        BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (b, t) -> b.put(keyMapper.apply(t),
                valueMapper.apply(t));
        BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
        Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

}