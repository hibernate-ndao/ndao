package org.herbst.ndao.optimistic;

/**
 * Created by Kris on 11.07.2015.
 */
public abstract class SuccessCommitAction {

    /**
     * Реализация операции.
     *
     * @throws Exception Если во время выполнения операции возникла ошибка.
     */
    public abstract void action() throws Exception;
}
