package org.herbst.ndao.optimistic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс исключения, генерируемый Herbst.
 *
 * @author jatvarthur
 * @author kiRach
 */
public class HerbstException extends Exception {

	/**
	 * Кастомный сериализатор для класса.
	 */
	/*public static class Serializer extends JsonSerializer<HerbstException> {

		@Override
		public void serialize(HerbstException value, JsonGenerator g, SerializerProvider provider) throws IOException {

			final Throwable cause = value.getCause();
			g.writeStartObject();
			g.writeStringField("code", value.errorCode);
			if (cause != null) {
				g.writeStringField("exception", cause.getClass().getName());
				g.writeStringField("message", cause.getMessage());
			}
			g.writeObjectField("params", value.params);
			g.writeEndObject();

		}

	}*/

	/**
	 * Общий сбой, причина неизвестна.
	 */
	public final static String GENERAL_FAILURE = "#general-failure";

	/**
	 * Код ошибки. Код ошибки рекомендуется задавать как строковый идентификатор, состоящий
	 * из маленьких латинских букв и знаков дефиса, начинающихся с символа хеша, например,
	 * <code>#general-failure</code>. Каждый код ошибки подразумевает наличие некоторых параметров.
	 * Этот же код ошибки используется для поиска локализованной строки и подстановки параметров.
	 */
	protected String errorCode;

	/**
	 * Дополнительные параметры ошибки.
	 */
	protected Map<String, Object> params = null;

	/**
	 * Конструктор по умолчанию - создает ошибку GENERAL_FAILURE со
	 * служебным сообщением.
	 *
	 * @param cause     Базовое исключение - причина ошибки.
	 * @param errorCode Код ошибки.
	 */
	public HerbstException(final Throwable cause, final String errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * Конструктор, который создает новый объект ошибки по описанным параметрам.
	 *
	 * @param cause     Базовое исключение - причина ошибки.
	 * @param errorCode Код ошибки.
	 * @param arg0name  Имя первого аргумента.
	 * @param arg0      Значение первого аргумента.
	 */
	public HerbstException(final Throwable cause, final String errorCode, final String arg0name, final Object arg0) {
		this(cause, errorCode);
		this.params = new HashMap<String, Object>();
		this.params.put(arg0name, arg0);
	}

	/**
	 * Возвращает код ошибки.
	 *
	 * @return Код ошибки.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Выполняет проверку, что код ошибки в объекте совпадает с указанным.
	 *
	 * @param errorCode Код ошибки для проверки.
	 * @return <code>true</code>, если код ошибки совпадает с указанным.
	 */
	public boolean errorCodeIs(final String errorCode) {
		return this.errorCode != null && this.errorCode.equals(errorCode);
	}

	/**
	 * Возвращает параметры ошибки.
	 *
	 * @return Параметры ошибки.
	 */
	public Map<String, Object> getParameters() {
		return params != null ? Collections.unmodifiableMap(params) : Collections.<String, Object>emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> params = getParameters();
		sb.append('[').append(errorCode).append("] ").append(" (").append(params.size()).append(" parameters)");
		if (params.size() > 0) {
			sb.append("{\n");
			for (Map.Entry<String, Object> param : params.entrySet()) {
				sb.append("\t\"").append(param.getKey()).append("\": \"").append(param.getValue()).append("\"\n");
			}
			sb.append("}");
		}
		return sb.toString();
		/*
		try {
			//todo:может быть улучшить это дело
			//ServerErrorManager serverErrorManager = (ServerErrorManager) ContainerProvider.getContainer().getBean("serverErrorManager");
			//return serverErrorManager.formatServiceMessage(this.messageid, this.params);
			throw new IllegalArgumentException("Error description is missing, check error messages file.");
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder();
			sb.append(ex.getMessage()).append("\nException information: [").append(errorCode).append("] ");
			sb.append(messageid).append(" (").append(params.size()).append(" parameters) {\n");
			for (Map.Entry<String, Object> param : params.entrySet()) {
				sb.append("\t\"").append(param.getKey()).append("\": \"").append(param.getValue()).append("\"\n");
			}
			sb.append("}");
			return sb.toString();
		} */
	}

}
