package org.herbst.ndao.optimistic;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Класс, предоставляющий базовые операции для восстановления/сохранения устойчивого
 * класса <code>T</code> с идентификатором <code>ID</code>.
 *
 * @author jatvarthur
 */
public abstract class Persister<T, ID extends Serializable> {

	/**
	 * Управляемый тип.
	 */
	protected Class<?> type;

	/**
	 * Строка запроса для получения всех элементов указанного типа.
	 */
	protected String findAllQuery;

	/**
	 * Строка запроса для получения количества всех элементов указанного типа.
	 */
	protected String countAllQuery;

	/**
	 * Конструктор.
	 */
	public Persister() {
		this.type = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.findAllQuery = "from " + type.getSimpleName();
		this.countAllQuery = "select count (*) from " + type.getSimpleName();
	}

	/**
	 * Загружает объект из БД по указанному идентификатору.
	 *
	 * @param cx Контекст операции.
	 * @param id Идентификатор объекта.
	 * @return Загруженный объект.
	 * @throws javax.persistence.PersistenceException
	 *          Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public T get(final MonadicContext cx, final ID id) throws PersistenceException {
		return (T) cx.getSession().get(type, id);
	}

	/**
	 * Выполняет поиск объекта в БД по указанному идентификатору.
	 *
	 * @param cx Контекст операции.
	 * @param id Идентификатор объекта.
	 * @return Найденный объект.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	public T findByID(final MonadicContext cx, final ID id) throws PersistenceException {
		return get(cx, id);
	}

	/**
	 * Выполняет поиск и загрузку всех объектов управляемого типа.
	 *
	 * @param cx Контекст операции.
	 * @return Список объектов.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll(final MonadicContext cx) throws PersistenceException {
		return (List<T>) cx.getSession().createQuery(findAllQuery).list();
	}

	/**
	 * Выполняет подсчет всех объектов управляемого типа.
	 *
	 * @param cx Контекст операции.
	 * @return Количество объектов.
	 * @throws PersistenceException Если возникла ошибка.
	 */
	@SuppressWarnings("unchecked")
	public int countAll(final MonadicContext cx) throws PersistenceException {
		return ((Number) cx.getSession().createQuery(countAllQuery).uniqueResult()).intValue();
	}

}
