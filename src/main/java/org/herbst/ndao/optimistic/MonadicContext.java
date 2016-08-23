package org.herbst.ndao.optimistic;

import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Контекст монадической операции.
 *
 * @author jatvarthur
 */
public class MonadicContext {

	/**
	 * Текущая сессия Hibernate, связанная с контекстом.
	 */
	private Session session;

	/**
	 * Признак, использует ли операция транзакцию.
	 */
	private boolean transactional;

	/**
	 * Признак того, что операция была перезапущена.
	 */
	private boolean restarted;

	/**
	 * Последняя возникшая ошибка.
	 */
	private Exception lastError;

	private final List<SuccessCommitAction> successCommitActions;

	/**
	 * Конструктор.
	 */
	public MonadicContext() {
		successCommitActions=new ArrayList<SuccessCommitAction>();
	}

	public MonadicContext(Session session) {
		this();
		this.session=session;
	}

	/**
	 * Проверка, что этот контекст активен, то есть в текущем потоке выполняется монадическая
	 * операция и имеется связанная с контекстом сессия. Если контекст не активен, то значения
	 * остальных свойств контекста не определено.
	 *
	 * @return <code>true</code>, если контекст активен.
	 */
	public boolean isActive() {
		return session != null;
	}

	/**
	 * Деактивирует контекст, очищая его.
	 */
	void deactivate() {
		session = null;
		restarted = false;
	}

	/**
	 * Возвращает текущую сессию, связанную с контекстом. Если с контекстом не связана
	 * сессия, то выбрасывается исключение IllegalStateException.
	 *
	 * @return Объект сессии.
	 * @throws IllegalStateException Если сессии нет.
	 */
	public Session getSession() throws IllegalStateException {
		if (session == null){
			if (getLastError()!=null){
				throw new IllegalStateException("Context is not active. Reason: " + getLastError().getMessage(), getLastError().getCause());
			} else {
				throw new IllegalStateException("Context is not active.");
			}
		}
		return session;
	}

	//TODO Избавится от этого костыля!!!!
	public Session getSessionWithIgnoreNull() {
		return session;
	}

	/**
	 * Устанавливает объект сессии для контекста.
	 *
	 * @param value Сессия.
	 */
	void setSession(final Session value) {
		this.session = value;
	}

	/**
	 * Возвращает признак того, что операция использует транзакцию.
	 *
	 * @return <code>true</code>, если операция использует транзакцию.
	 */
	public boolean isTransactional() {
		return transactional;
	}

	/**
	 * Устанавливает признак, что операция использует транзакцию.
	 *
	 * @param value Если <code>true</code>, то операция использует транзакцию.
	 */
	void setTransactional(boolean value) {
		this.transactional = value;
	}

	/**
	 * Возвращает признак того, что операция была перезапущена.
	 *
	 * @return <code>true</code>, если операция была перезапущена.
	 */
	public boolean isRestarted() {
		return restarted;
	}

	/**
	 * Устанавливает признак, что операция была перезапущена.
	 */
	protected void restarted() {
		this.restarted = true;
		successCommitActions.clear();
	}

	/**
	 * Возвращает последнюю возникшую ошибку.
	 *
	 * @return Возвращает последнюю возникшую ошибку.
	 */
	public Exception getLastError() {
		return lastError;
	}

	/**
	 * Устанавливает последнюю возникшую ошибку.
	 *
	 * @param value Последняя возникшая ошибка.
	 */
	void setLastError(final Exception value) {
		this.lastError = value;
	}

	public List<SuccessCommitAction> getSuccessCommitActions() {return successCommitActions;}
	public void addSuccessCommitAction(SuccessCommitAction action){
		successCommitActions.add(action);
	}
}
