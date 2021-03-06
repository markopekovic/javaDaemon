package com.daemonize.game.repo;

import java.util.Stack;

public abstract class StackedEntityRepo<T> implements EntityRepo<Stack<T>, T> {

    private Stack<T> stack;

    public StackedEntityRepo() {
        this.stack = new Stack<>();
    }

    public StackedEntityRepo(Stack<T> stack) {
        this.stack = stack;
    }

    @Override
    public void setStructure(Stack<T> structure) {
        this.stack = structure;
    }

    @Override
    public Stack<T> getStructure() {
        return stack;
    }

    @Override
    public boolean add(T entity) {
        this.onAdd(entity);
        stack.push(entity);
        return true;
    }

    @Override
    public T configureAndGet(EntityConfigurator<T> configurator) {
        T ret  = stack.pop();
        if (ret != null) {
            if (configurator != null)
                configurator.configure(ret);
            this.onGet(ret);
        }
        return ret;
    }

    @Override
    public T getAndConfigure(EntityConfigurator<T> configurator) {
        T ret = stack.pop();
        if(ret != null) {
            this.onGet(ret);
            if (configurator != null)
                configurator.configure(ret);
        }
        return ret;
    }

    @Override
    public T get() {
        return configureAndGet(null);
    }

    @Override
    public abstract void onAdd(T entity);

    @Override
    public abstract void onGet(T entity);

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public void forEach(EntityConfigurator<T> configurator) {
        for(T entity : getStructure())
            configurator.configure(entity);
    }
}
