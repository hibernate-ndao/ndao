package org.herbst.ndao.optimistic;

/**
 * Ошибка монадической операции.
 *
 * @author jatvarthur
 */
public class MonadicOperationException
		extends RuntimeException {

	/**
	 * Выполнять ли рестарт операции.
	 */
	private boolean restart;

	/**
	 * Конструктор.
	 *
	 * @param cause   Причина ошибки.
	 * @param restart Надо ли выполнять рестарт.
	 */
	public MonadicOperationException(final Exception cause, boolean restart) {
		super(cause);
		this.restart = restart;
	}

	/**
	 * Возвращает признак, надо ли выполнять ли рестарт операции.
	 *
	 * @return <code>true</code>, если надо выполнять рестарт.
	 */
	public boolean canRestart() {
		return restart;
	}

}
