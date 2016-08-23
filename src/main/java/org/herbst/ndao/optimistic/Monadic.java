package org.herbst.ndao.optimistic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация показывает, что заданный метод должен запускаться в контексте монадической операции.
 * Диспетчер, вызывающий методы, помеченные этой аннотацией должен открывать контекст до начала вызова метода,
 * и закрывать его после окончания.
 *
 * @author jatvarthur
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monadic {

	/**
	 * Признак, показывающий, что монадическая операция должна выполняться в контексте
	 * изоляции транзакции.
	 *
	 * @return <code>true</code>, если операция требует транзакцию для выполнения.
	 */
	public boolean transactional() default true;

}
