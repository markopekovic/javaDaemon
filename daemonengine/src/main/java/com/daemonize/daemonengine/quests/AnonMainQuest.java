package com.daemonize.daemonengine.quests;

import com.daemonize.daemonengine.closure.Closure;
import com.daemonize.daemonengine.closure.ReturnRunnable;

public class AnonMainQuest<T> extends MainQuest<T> {

    private Quest<T> userQuest;
    private ReturnRunnable<T> returnRunnable = new ReturnRunnable<>();

    public AnonMainQuest(Quest<T> userQuest, Closure<T> closure) {
        super(closure);
        this.userQuest = userQuest;
    }

    @Override
    public T pursue() throws Exception {
        return userQuest.pursue();
    }
}
