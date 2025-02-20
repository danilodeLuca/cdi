/*
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jakarta.enterprise.inject.spi.configurator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

/**
 *
 * This interface is part of the {@link AnnotatedTypeConfigurator} SPI and helps defining an {@link AnnotatedConstructor}
 *
 * <p>CDI Lite implementations are not required to provide support for Portable Extensions.</p>
 *
 * @author Martin Kouba
 * @author Antoine Sabot-Durand
 * @since 2.0
 * @param <T> the class declaring the constructor
 */
public interface AnnotatedConstructorConfigurator<T> {

    /**
     * 
     * @return the original {@link AnnotatedConstructor}
     */
    AnnotatedConstructor<T> getAnnotated();

    /**
     * Add an annotation to the constructor.
     * 
     * @param annotation the annotation to add
     * @return self
     */
    AnnotatedConstructorConfigurator<T> add(Annotation annotation);

    /**
     * Remove annotations that match the specified predicate.
     *
     * <p>
     * Example predicates:
     * </p>
     * 
     * <pre>
     *  {@code
     * // To remove all the annotations:
     * (a) -> true
     * 
     * // To remove annotations with a concrete annotation type:
     * (a) -> a.annotationType().equals(Foo.class)
     * 
     * // To remove annotation equal to a specified object:
     * (a) -> a.equals(fooAnnotation)
     * 
     * // To remove annotations that are considered equivalent for the purposes of typesafe resolution:
     * (a) -> beanManager.areQualifiersEquivalent(a, fooQualifier)
     * (a) -> beanManager.areInterceptorBindingsEquivalent(a, fooInterceptorBinding)
     * }
     * </pre>
     * 
     * @param predicate {@link Predicate} used to filter annotations to remove
     * @return self
     */
    AnnotatedConstructorConfigurator<T> remove(Predicate<Annotation> predicate);
    
    /**
     * Remove all the annotations.
     * 
     * @return self
     */
    default AnnotatedConstructorConfigurator<T> removeAll() {
        return remove((a) -> true);
    }

    /**
     * 
     * @return an immutable list of {@link AnnotatedParameterConfigurator}s reflecting the
     *         {@link AnnotatedConstructor#getParameters()}
     */
    List<AnnotatedParameterConfigurator<T>> params();

    /**
     * 
     * @param predicate Testing the original {@link AnnotatedParameter}
     * @return a sequence of {@link AnnotatedParameterConfigurator}s matching the given predicate
     * @see AnnotatedParameterConfigurator#getAnnotated()
     */
    default Stream<AnnotatedParameterConfigurator<T>> filterParams(Predicate<AnnotatedParameter<T>> predicate) {
        return params().stream().filter(p -> predicate.test(p.getAnnotated()));
    }

}
