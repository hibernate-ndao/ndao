package org.herbst.ndao.optimistic;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import javax.persistence.PersistenceException;
import java.util.List;

/**
 * Корневой класс доступа к данным, хранящимся в базе данных через ORM Hibernate.
 *
 * @author jatvarthur
 */
public class Database {

	/**
	 * Фабрика сессий Hibernate.
	 */
	protected final SessionFactory sessionFactory;

	/**
	 * Монадические контексты потоков.
	 */
	protected final ThreadLocal<MonadicContext> contexts = new ThreadLocal<MonadicContext>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MonadicContext initialValue() {
			return new MonadicContext();
		}

	};

	/**
	 * Количество попыток выполнения операции, по умолчанию 5 раза.
	 */
	protected final static int DEFAULT_RETRIES = 5;

	/**
	 * Время ожидания между попытками выполнить операцию в миллисекундах, по умолчанию 100 мс.
	 */
	protected int retryTimeout = 100;

	/**
	 * Конструктор.
	 */
	public Database(Configuration configuration) {
		sessionFactory = buildSessionFactory(configuration);
	}

	/**
	 * Создает и инициализирует фабрику сессий.
	 *
	 * @return Построенную фабрику сессий.
	 */
	private SessionFactory buildSessionFactory(Configuration configuration) {
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		return configuration.buildSessionFactory(serviceRegistry);
	}

	/**
	 * Начинает выполнение операции с использованием указанной сессии.
	 *
	 * @param transactional Должна ли операция использовать транзакцию.
	 * @param cx            Контекст операции.
	 * @return Открытую сессию.
	 */
	protected Session begin(boolean transactional, final MonadicContext cx) {
		final Session sx = sessionFactory.openSession();
		if (transactional)
			sx.beginTransaction();
		cx.setSession(sx);
		cx.setTransactional(transactional);
        cx.setLastError(null);
		return sx;
	}

	/**
	 * Завершает выполнение операции, закрывая переданную сессию.
	 *
	 * @param cx Контекст.
	 */
	protected void end(final MonadicContext cx) {
		final Session sx = cx.getSession();
		boolean transactional = cx.isTransactional();
		cx.deactivate();
		if (!sx.isOpen()) return;
		if (transactional) {
			final Transaction tx = sx.getTransaction();
			if (tx != null) tx.commit();
		}
		sx.close();
	}

	/**
	 * Отменяет выполнение операции, закрывая переданную сессию.
	 *
	 * @param cx Контекст.
	 */
	protected void cancel(final MonadicContext cx) {
		//ToDO избавится от этого hot fix
		final Session sx = cx.getSessionWithIgnoreNull();
		boolean transactional = cx.isTransactional();
		cx.deactivate();
		if (sx==null || !sx.isOpen()) return;
		try {
			if (transactional) {
				final Transaction tx = sx.getTransaction();
				if (tx != null) tx.rollback();
			}
			sx.close();
		} catch (Exception ignored) {}

//		TODO старая версия кода
//		final Session sx = cx.getSession();
//		boolean transactional = cx.isTransactional();
//		cx.deactivate();
//		if (!sx.isOpen()) return;
//		try {
//			if (transactional) {
//				final Transaction tx = sx.getTransaction();
//				if (tx != null) tx.rollback();
//			}
//			sx.close();
//		} catch (Exception ignored) {}
	}

	/**
	 * Возвращает монадический контекст для текущего потока. Если метод вызывается
	 * вне операции, то контекст может быть не активен.
	 *
	 * @return Контекст операции.
	 */
	public MonadicContext getContext() {
		return contexts.get();
	}

	/**
	 * Внутренняя реализация операции с данными.
	 *
	 * @param transactional Должна ли операция использовать транзакцию.
	 * @param operation     Выполняемая операция.
	 * @throws HerbstException
	 *          Если при выполнении операции возникли ошибки.
	 */
	public void execute(boolean transactional, final Monad operation, int retries) throws HerbstException {
		final MonadicContext context = getContext();
		boolean root = !context.isActive();
		int attempt = 0;
		boolean restart = false;
		do {
			if (root) begin(transactional, context);
			// пытаемся выполнить операцию некоторое количество раз
			try {
				operation.action(context);
				// если все закончилось удачно, пытаемся закрыть сессию, подтверждая транзакцию
				if (root) end(context);
				operation.compleated(context);
				return;
			} catch (MonadicOperationException ex) {
				restart = ex.canRestart();
				context.setLastError((Exception) ex.getCause());
				operation.fail(context);
				if (!root) throw ex;
			} catch (StaleObjectStateException sosex) {
				//Обязательно надо почистить кеш
				Cache cache = sessionFactory.getCache();
				if (cache!=null) {
					cache.evictEntity(sosex.getEntityName(), sosex.getIdentifier());
				}
				restart = true;
				context.setLastError(sosex);
				operation.fail(context);
				if (!root) throw new MonadicOperationException(sosex, restart);
			} catch (HibernateException ex) {
				restart = true;
				context.setLastError(ex);
				operation.fail(context);
				if (!root) throw new MonadicOperationException(ex, restart);
			} catch (PersistenceException ex) {
				restart = true;
				context.setLastError(ex);
				operation.fail(context);
				if (!root) throw new MonadicOperationException(ex, restart);
			} catch (Exception ex) {
				restart = false;
				context.setLastError(ex);
				operation.fail(context);
				if (!root) throw new MonadicOperationException(ex, restart);
			}
			// тут мы оказываемся только если мы корневой контекст и возникла ошибка операции
			cancel(context);
			attempt += 1;
			restart = restart && attempt < retries;
			if (!restart) {
				final Exception ex = context.getLastError();
				if (ex instanceof HerbstException)
					throw (HerbstException) ex;
				else
					throw new HerbstException(ex, "#persistence-error", "reason", ex != null ? ex.getMessage() : "Unknown error");
			}
			// подождем и перезапустим
			context.restarted();
			try {
				Thread.sleep(retryTimeout*attempt);
			} catch (InterruptedException ignored) {}
		} while (restart);
	}

	/**
	 * Операция с данными.
	 *
	 * @param operation Выполняемая операция.
	 * @throws HerbstException
	 *          Если при выполнении операции возникли ошибки.
	 */
	public void execute(final Monad operation) throws HerbstException {
		execute(true, operation, DEFAULT_RETRIES);
	}

	/**
	 * Операция с данными без создания транзакции.
	 *
	 * @param operation Выполняемая операция.
	 * @throws HerbstException
	 *          Если при выполнении операции возникли ошибки.
	 */
	public void executeNonTx(final Monad operation) throws HerbstException {
		execute(false, operation, DEFAULT_RETRIES);
	}

	/**
	 * Выполняет произвольный запрос обновления HQL DML.
	 * <p/>
	 * NB. не забывать для объектов с версией выполнять update versioned
	 * http://docs.jboss.org/hibernate/orm/3.3/reference/en/html/batch.html#batch-update
	 * <pre>
	 *     db.update("update versioned Player p set p.mana = p.mana + 1");
	 * </pre>
	 *
	 * @param cx    Контекст операции.
	 * @param query Запрос.
	 * @return Количество сущностей, измененных или удаленных в результате выполнения запроса.
	 * @throws PersistenceException  Если возникла ошибка.
	 * @throws IllegalStateException Если метод вызывается вне монадического контекста.
	 */
	@SuppressWarnings("unchecked")
	public int update(final MonadicContext cx, final String query) throws PersistenceException {
		return cx.getSession().createQuery(query).executeUpdate();
	}

	/**
	 * Выполняет произвольный запрос получения данных HQL DML.
	 *
	 * @param cx    Контекст операции.
	 * @param query Запрос.
	 * @return Количество сущностей, измененных или удаленных в результате выполнения запроса.
	 * @throws PersistenceException  Если возникла ошибка.
	 * @throws IllegalStateException Если метод вызывается вне монадического контекста.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> query(final MonadicContext cx, final String query) throws PersistenceException {
		return cx.getSession().createQuery(query).list();
	}

	/**
	 * Выполняет произвольный запрос получения скаляра HQL DML.
	 *
	 * @param cx    Контекст операции.
	 * @param query Запрос.
	 * @return Количество сущностей, измененных или удаленных в результате выполнения запроса.
	 * @throws PersistenceException  Если возникла ошибка.
	 * @throws IllegalStateException Если метод вызывается вне монадического контекста.
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryScalar(final MonadicContext cx, final String query) throws PersistenceException {
		final Object[] tuple = (Object[]) cx.getSession().createQuery(query).uniqueResult();
		return (T) tuple[0];
	}

	public Cache getCache() {
		return sessionFactory.getCache();
	}

}
