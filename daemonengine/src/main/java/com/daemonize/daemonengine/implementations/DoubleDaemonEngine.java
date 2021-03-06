package com.daemonize.daemonengine.implementations;

import com.daemonize.daemonengine.DaemonEngine;
import com.daemonize.daemonengine.DaemonState;
import com.daemonize.daemonengine.EagerDaemon;
import com.daemonize.daemonengine.SideQuestDaemon;
import com.daemonize.daemonengine.closure.Closure;
import com.daemonize.daemonengine.consumer.Consumer;
import com.daemonize.daemonengine.quests.AnonMainQuest;
import com.daemonize.daemonengine.quests.Quest;
import com.daemonize.daemonengine.quests.SideQuest;
import com.daemonize.daemonengine.quests.VoidMainQuest;
import com.daemonize.daemonengine.quests.VoidQuest;

import java.util.ArrayList;
import java.util.List;

public class DoubleDaemonEngine implements EagerDaemon<DoubleDaemonEngine>, DaemonEngine<DoubleDaemonEngine> {

    private EagerMainQuestDaemonEngine mainQuestDaemonEngine;
    private SideQuestDaemonEngine sideQuestDaemonEngine;

    public DoubleDaemonEngine(Consumer consumer) {
        this.mainQuestDaemonEngine = new EagerMainQuestDaemonEngine(consumer).setName(this.getClass().getSimpleName());
        this.sideQuestDaemonEngine = new SideQuestDaemonEngine().setName(this.getClass().getSimpleName() + " - SIDE");
    }

    @Override
    public <T> DoubleDaemonEngine daemonize(Quest<T> quest, Closure<T> closure) {
        return daemonize(mainQuestDaemonEngine.getConsumer(), quest, closure);
    }

    @Override
    public DoubleDaemonEngine daemonize(final VoidQuest quest, Runnable closure) {
        return daemonize(mainQuestDaemonEngine.getConsumer(), quest, closure);
    }

    @Override
    public DoubleDaemonEngine daemonize(final VoidQuest quest) {
        return daemonize(quest, null);
    }

    @Override
    public <T> DoubleDaemonEngine daemonize(Consumer consumer, Quest<T> quest, Closure<T> closure) {
        mainQuestDaemonEngine.addMainQuest((AnonMainQuest<T>) new AnonMainQuest(quest, closure).setConsumer(consumer)); //TODO check ret
        return this;
    }

    @Override
    public DoubleDaemonEngine daemonize(Consumer consumer, final VoidQuest quest, Runnable closure) {
        mainQuestDaemonEngine.addMainQuest(new VoidMainQuest(closure) {
            @Override
            public Void pursue() throws Exception {
                quest.pursue();
                return null;
            }
        }.setConsumer(consumer));
        return this;
    }

    public <T> SideQuest<T> setSideQuest(Consumer consumer, final Quest<T> sideQuest) {
        return sideQuestDaemonEngine.setSideQuest(consumer, sideQuest);
    }

    @Override
    public DoubleDaemonEngine interrupt() {
        mainQuestDaemonEngine.interrupt();
        return this;
    }

    @Override
    public DoubleDaemonEngine clearAndInterrupt() {
        mainQuestDaemonEngine.clearAndInterrupt();
        return this;
    }

    @Override
    public DoubleDaemonEngine start() {
        mainQuestDaemonEngine.start();
        sideQuestDaemonEngine.start();
        return this;
    }

    @Override
    public void stop() {
        mainQuestDaemonEngine.stop();
        sideQuestDaemonEngine.stop();
    }

    @Override
    public DoubleDaemonEngine queueStop() {
        mainQuestDaemonEngine.queueStop(this);
        return this;
    }

    @Override
    public List<DaemonState> getEnginesState() {
        List<DaemonState> ret = new ArrayList<>(2);
        ret.add(mainQuestDaemonEngine.getState());
        ret.add(sideQuestDaemonEngine.getState());
        return ret;
    }

    @Override
    public DoubleDaemonEngine setName(String name) {
        mainQuestDaemonEngine.setName(name);
        sideQuestDaemonEngine.setName(name  + " - SIDE");
        return this;
    }

    @Override
    public String getName() {
        return mainQuestDaemonEngine.getName();
    }

    @Override
    public DoubleDaemonEngine setConsumer(Consumer consumer) {
        mainQuestDaemonEngine.setConsumer(consumer);
        return this;
    }

    @Override
    public Consumer getConsumer() {
        return mainQuestDaemonEngine.getConsumer();
    }

    @Override
    public DoubleDaemonEngine clear() {
        mainQuestDaemonEngine.clear();
        return this;
    }
}
