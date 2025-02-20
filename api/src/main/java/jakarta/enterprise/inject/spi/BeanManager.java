/*
 * Copyright 2010, 2013 Red Hat, Inc., and individual contributors
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

package jakarta.enterprise.inject.spi;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.InjectionException;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.Stereotype;

/**
 * <p>
 * Allows a portable extension to interact directly with the container. Provides operations for obtaining contextual references
 * for beans, along with many other operations of use to portable extensions.
 * </p>
 *
 * <p>
 * In CDI Lite environment, applications may obtain a {@code BeanManager}, but invoking methods that are not inherited
 * from {@link BeanContainer} results in non-portable behavior.
 * </p>
 * 
 * <p>
 * Any bean may obtain an instance of <code>BeanManager</code> by injecting it:
 * </p>
 * 
 * <pre>
 * &#064;Inject
 * BeanManager manager;
 * </pre>
 * 
 * <p>
 * Java EE components may obtain an instance of <code>BeanManager</code> from {@linkplain javax.naming JNDI} by looking up the name
 * {@code java:comp/BeanManager}.
 * </p>
 * 
 * <p>
 * Most operations of BeanManager may be called at any time during the execution of the application.
 * </p>
 * <p>    
 * However, the following  operations must not be called before the {@link AfterBeanDiscovery} event is fired:
 * </p>
 * <ul>
 *     <li>{@link #getBeans(String)},</li>
 *     <li>{@link #getBeans(java.lang.reflect.Type, java.lang.annotation.Annotation...)},</li>
 *     <li>{@link #getPassivationCapableBean(String)},</li>
 *     <li>{@link #resolve(java.util.Set)},</li>
 *     <li>{@link #resolveDecorators(java.util.Set, java.lang.annotation.Annotation...)},</li>
 *     <li>{@link #resolveInterceptors(InterceptionType, java.lang.annotation.Annotation...)},</li>
 *     <li>{@link #resolveObserverMethods(Object, java.lang.annotation.Annotation...)},</li>
 *     <li>{@link #validate(InjectionPoint)},</li>
 * </ul>
 * <p>
 * and the following operations must not be called before the {@link AfterDeploymentValidation} event is fired:
 * </p>
 * <ul>
 *     <li>{@link #createInstance()}</li>
 *     <li>{@link #getReference(Bean, java.lang.reflect.Type, CreationalContext)},</li>
 *     <li>{@link #getInjectableReference(InjectionPoint, CreationalContext)},</li>
 * </ul>
 * <p>
 * or the container will throw an Exception.
 * </p>
 * 
 * @author Gavin King
 * @author Pete Muir
 * @author Clint Popetz
 * @author David Allen
 * @author Antoine Sabot-Durand
 */
public interface BeanManager extends BeanContainer {

    /**
     * <p>
     * Obtains an injectable reference for a certain {@linkplain InjectionPoint injection point}.
     * </p>
     *
     * @param ij  the target injection point
     * @param ctx a {@link CreationalContext} that may be used to destroy any object with scope
     *            {@link Dependent} that is created
     * @return the injectable reference
     * @throws UnsatisfiedResolutionException if typesafe resolution results in an unsatisfied dependency
     * @throws AmbiguousResolutionException   typesafe resolution results in an unresolvable ambiguous dependency
     * @throws IllegalStateException          if called during application initialization, before the {@link AfterDeploymentValidation}
     *                                        event is fired.
     */
    Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx);

    /**
     * Returns the {@link PassivationCapable} bean with the given identifier.
     *
     * Note that when called during invocation of an {@link AfterBeanDiscovery} event observer, 
     * this method will only return beans discovered by the container before the {@link AfterBeanDiscovery} event is fired.
     * 
     * @param id the identifier
     * @return a {@link Bean} that implements {@link PassivationCapable} and has the given
     *         identifier, or a null value if there is no such bean
     * @throws IllegalStateException if called during application initialization, before the {@link AfterBeanDiscovery}
     *         event is fired.
     */
    public Bean<?> getPassivationCapableBean(String id);

    /**
     * Validate a certain {@linkplain InjectionPoint injection point}.
     *
     * Note that when called during invocation of an {@link AfterBeanDiscovery} event observer,
     * this method will only validate injection points discovered by the container before the {@link AfterBeanDiscovery} 
     * event is fired.
     * 
     * @param injectionPoint the {@linkplain InjectionPoint injection point} to validate
     * @throws InjectionException if there is a deployment problem (for example, an unsatisfied or unresolvable ambiguous
     *         dependency) associated with the injection point
     * @throws IllegalStateException if called during application initialization, before the {@link AfterBeanDiscovery}
     *         event is fired.
     */
    public void validate(InjectionPoint injectionPoint);

    /**
     * Return an ordered list of {@linkplain Decorator decorators} for a set of bean types and a set of qualifiers and which are
     * enabled in the module or library containing the class into which the <code>BeanManager</code> was injected or the Java EE
     * component from whose JNDI environment namespace the <code>BeanManager</code> was obtained.
     *
     * Note that when called during invocation of an {@link AfterBeanDiscovery} event observer,
     * this method will only return decorators discovered by the container before the {@link AfterBeanDiscovery} event is fired.
     * 
     * @param types the set of bean types of the decorated bean
     * @param qualifiers the qualifiers declared by the decorated bean
     * @return the resulting set of {@linkplain Decorator decorators}
     * @throws IllegalArgumentException if the set of bean types is empty
     * @throws IllegalArgumentException if an annotation which is not a binding type is passed
     * @throws IllegalArgumentException if two instances of the same binding type are passed
     * @throws IllegalStateException if called during application initialization, before the {@link AfterBeanDiscovery}
     *         event is fired.
     */
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers);

    /**
     * Test the given annotation type to determine if it is a passivating {@linkplain jakarta.enterprise.context scope type}.
     * 
     * @param annotationType the annotation type
     * @return <code>true</code> if the annotation type is a passivating scope type
     */
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType);

    /**
     * Obtains the set of meta-annotations for a certain {@linkplain jakarta.interceptor.InterceptorBinding interceptor binding
     * type} .
     * 
     * @param bindingType the {@linkplain jakarta.interceptor.InterceptorBinding interceptor binding type}
     * @return the set of meta-annotations
     */
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType);

    /**
     * Obtains meta-annotations for a certain {@linkplain Stereotype stereotype}.
     * 
     * @param stereotype the {@linkplain Stereotype stereotype}
     * @return the set of meta-annotations
     */
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype);

    /**
     * Determine if two qualifiers are considered equivalent for the purposes of typesafe resolution, taking into account any
     * members annotated with {@link Nonbinding}.
     * 
     * @param qualifier1 a qualifier to check
     * @param qualifier2 a qualifier to check
     * @return true if the two qualifiers are equivalent, otherwise false
     * @since 1.1
     */
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2);

    /**
     * Determine if two interceptor bindings are considered equivalent for the purposes of typesafe resolution, taking into
     * account any members annotated with {@link Nonbinding}.
     * 
     * @param interceptorBinding1 an interceptor binding to check
     * @param interceptorBinding2 an interceptor binding to check
     * @return true if the two interceptor bindings are equivalent, otherwise false
     * @since 1.1
     */
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2);

    /**
     * Determine the hash code of a qualifier, using the JDK algorithm for determining an annotation hash code, ignoring any
     * members annotated with {@link Nonbinding}.
     * 
     * @param qualifier the qualifier to consider
     * @return the hashCode for the qualifier
     * @since 1.1
     */
    public int getQualifierHashCode(Annotation qualifier);

    /**
     * Determine the hash code of an interceptor binding, using the JDK algorithm for determining an annotation hash code,
     * ignoring any members annotated with {@link Nonbinding}.
     * 
     * @param interceptorBinding the interceptor binding to consider
     * @return the hashCode for the interceptor binding
     * @since 1.1
     */
    public int getInterceptorBindingHashCode(Annotation interceptorBinding);

    /**
     * Returns a {@link jakarta.el.ELResolver} that resolves beans by EL name.
     *
     * @deprecated use {@code ELAwareBeanManager}, this method will be removed in CDI 5.0
     * @return the {@link jakarta.el.ELResolver}
     */
    @Deprecated(since = "4.1", forRemoval = true)
    public ELResolver getELResolver();

    /**
     * Returns a wrapper {@link jakarta.el.ExpressionFactory} that delegates {@link jakarta.el.MethodExpression} and
     * {@link jakarta.el.ValueExpression} creation to the given {@link jakarta.el.ExpressionFactory}. When a Unified EL expression
     * is evaluated using a {@link jakarta.el.MethodExpression} or {@link jakarta.el.ValueExpression} returned by the wrapper
     * {@link jakarta.el.ExpressionFactory}, the container handles destruction of objects with scope
     * {@link Dependent}.
     * 
     * @deprecated use {@code ELAwareBeanManager}, this method will be removed in CDI 5.0
     * @param expressionFactory the {@link jakarta.el.ExpressionFactory} to wrap
     * @return the wrapped {@link jakarta.el.ExpressionFactory}
     */
    @Deprecated(since = "4.1", forRemoval = true)
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory);

    /**
     * Obtain an {@link AnnotatedType} that may be used to read the annotations of the given class or interface.
     * 
     * @param <T> the class or interface
     * @param type the {@link java.lang.Class} object
     * @return the {@link AnnotatedType}
     */
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type);

    /**
     * <p>
     * An implementation of {@link InjectionTargetFactory} that provides container created {@link InjectionTarget} instances.
     * </p>
     * 
     * <p>
     * This factory can be wrapped to add behavior to container created injection targets.
     * </p>
     *
     * @param <T> the type
     * @param annotatedType the annotated type to create the injection target factory for
     * @return an {@link InjectionTargetFactory}
     * @since 1.1
     */
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType);

    /**
     * <p>
     * An implementation of {@link ProducerFactory} that provides container created {@link Producer} instances for the given
     * field.
     * </p>
     * 
     * <p>
     * This factory can be wrapped to add behavior to container created producers.
     * </p>
     *
     * @param <X> the declaring type
     * @param field the field to create the producer factory for
     * @param declaringBean the bean declaring the producer. May be null if the producer is static or the declaring object is
     *        non-contextual
     * @return the producer factory for the field
     * @since 1.1
     */
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean);

    /**
     * <p>
     * An implementation of {@link ProducerFactory} that provides container created {@link Producer} instances for the given
     * method.
     * </p>
     * 
     * <p>
     * This factory can be wrapped to add behavior to container created producers.
     * </p>
     *
     * @param <X> bean type
     * @param method the method to create the producer factory for
     * @param declaringBean the bean declaring the producer. May be null if the producer is static or the declaring object is
     *        non-contextual
     * @return the producer factory for the method
     * @since 1.1
     */
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean);

    /**
     * Obtains a {@link BeanAttributes} for the given {@link AnnotatedType}. The container ignores the annotations and types
     * declared by the elements of the actual Java class and uses the metadata provided via the {@link Annotated} interface
     * instead.
     * 
     * @param <T> the type
     * @param type the {@link AnnotatedType}
     * @return a container provided implementation of {@link InjectionTarget}
     * @since 1.1
     */
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type);

    /**
     * Obtains a {@link BeanAttributes} for the given {@link AnnotatedType}. The container ignores the annotations and types
     * declared by the elements of the actual Java class and uses the metadata provided via the {@link Annotated} interface
     * instead.
     * 
     * @param type the {@link AnnotatedType}
     * @return a container provided implementation of {@link InjectionTarget}
     * @since 1.1
     */
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type);

    /**
     * <p>
     * Obtains a {@link Bean} for the given {@link BeanAttributes}, bean class and {@link InjectionTarget}.
     * </p>
     * 
     * <p>
     * The {@link InjectionTarget} creates and destroys instances of the bean, performs dependency injection and lifecycle
     * callbacks, and determines the return value of {@link Bean#getInjectionPoints()}. The {@link InjectionTarget} is obtained
     * from the {@link InjectionTargetFactory}. {@link #getInjectionTargetFactory(AnnotatedType)} allows use of a container created
     * {@link InjectionTarget}.
     * </p>
     * 
     * @param <T> the type
     * @param attributes a {@link BeanAttributes} which determines the bean types, qualifiers, scope, name and stereotypes of
     *        the returned {@link Bean}, and the return value of {@link Bean#isAlternative()}
     * @param beanClass a class, which determines the return value of {@link Bean#getBeanClass()}
     * @param injectionTargetFactory an {@link InjectionTargetFactory}, used to obtain an {@link InjectionTarget}
     * @return a container provided implementation of {@link Bean}
     * @since 1.1
     */
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> injectionTargetFactory);

    /**
     * <p>
     * Obtains a {@link Bean} for the given {@link BeanAttributes}, bean class and {@link Producer}.
     * </p>
     * 
     * <p>
     * The {@link Producer} creates and destroys instances of the decorator, and determines the return value of
     * {@link Bean#getInjectionPoints()}. The {@link Producer} is obtained from the {@link ProducerFactory}.
     * {@link #getProducerFactory(AnnotatedMethod, Bean)} or {@link #getProducerFactory(AnnotatedField, Bean)} allows use of a 
     * container created {@link Producer}.
     * </p>
     * 
     * @param <T> the type
     * @param <X> the type of the declaring bean
     * @param attributes a {@link BeanAttributes} which determines the bean types, qualifiers, scope, name and stereotypes of
     *        the returned {@link Bean}, and the return value of {@link Bean#isAlternative()}
     * @param beanClass a class, which determines the return value of <code>Bean.getClass()</code>
     * @param producerFactory a {@link ProducerFactory}, used to obtain a {@link Producer}
     * @return a container provided implementation of {@link Bean}
     * @since 1.1
     */
    public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> producerFactory);

    /**
     * Obtains a container provided implementation of {@link InjectionPoint} for the given {@link AnnotatedField}.
     * 
     * @param field the {@link AnnotatedField} defining the injection point
     * @return the container provided {@link InjectionPoint}
     * @throws IllegalArgumentException if there is a definition error associated with the injection point
     * @since 1.1
     */
    public InjectionPoint createInjectionPoint(AnnotatedField<?> field);

    /**
     * Obtains a container provided implementation of {@link InjectionPoint} for the given {@link AnnotatedParameter}.
     * 
     * @param parameter the {@link AnnotatedParameter} defining the injection point
     * @return the container provided {@link InjectionPoint}
     * @throws IllegalArgumentException if there is a definition error associated with the injection point
     * @since 1.1
     */
    public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter);

    /**
     * Obtains the container's instance of an Extension class declared in <code>META-INF/services</code>.
     * 
     * @param <T> the type of the extension
     * @param extensionClass the type of the extension class
     * @return the extension instance
     * @throws IllegalArgumentException if the container has no instance of the given class
     * @since 1.1
     */
    public <T extends Extension> T getExtension(Class<T> extensionClass);

    /**
     *
     * Create an {@link InterceptionFactory} for the given {@link CreationalContext} and type.
     *
     * @param ctx {@link CreationalContext} for the {@link InterceptionFactory} to create
     * @param clazz class of the instance this factory will work on
     * @param <T> type of the instance this factory will work on
     * @return a new {@link InterceptionFactory} to add services on on instances of T
     * @since 2.0
     */
    <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz);

}
