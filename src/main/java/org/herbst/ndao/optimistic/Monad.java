package org.herbst.ndao.optimistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Единица работы с данными - атомарная операция, выполняющаяся в контексте изоляции транзакции,
 * обычно с оптимистичной блокировкой.
 *
 * @author jatvarthur
 */
public abstract class Monad {

	private final static Logger log = LoggerFactory.getLogger(Monad.class);

	/**
	 * Реализация операции.
	 *
	 * @param cx Контекст, в котором выполняется операция.
	 * @throws Exception Если во время выполнения операции возникла ошибка.
	 */
	public abstract void action(final MonadicContext cx) throws Exception;

	/**
	 * Обработчик успешного выполнения операции.
	 */
	protected void compleated(final MonadicContext cx) {
		Iterator<SuccessCommitAction> iteratorSuccessActions = cx.getSuccessCommitActions().iterator();
		while(iteratorSuccessActions.hasNext()){
			SuccessCommitAction successCommitAction = iteratorSuccessActions.next();
			try {
				successCommitAction.action();
			} catch (Exception e) {
				log.error("Exception run successCommitAction", e);
			}
			iteratorSuccessActions.remove();
		}
		success(cx);
	}

	/**
	 * Обработчик успешного выполнения операции.
	 *
	 * @param cx Контекст, в котором выполняется операция.
	 */
	public void success(final MonadicContext cx) {
	}

	/**
	 * Обработчик ошибки операции.
	 *
	 * @param cx Контекст, в котором выполняется операция.
	 */
	public void fail(final MonadicContext cx) {
	}

}
