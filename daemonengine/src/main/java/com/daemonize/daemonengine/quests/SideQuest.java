package com.daemonize.daemonengine.quests;

import com.daemonize.daemonengine.closure.Closure;
import com.daemonize.daemonengine.closure.Return;
import com.daemonize.daemonengine.closure.ReturnRunnable;
import com.daemonize.daemonengine.DaemonState;
import com.daemonize.daemonengine.utils.DaemonUtils;


public abstract class SideQuest<T, M> extends Quest<T> {

  private int sleepInterval;
  protected M prototype;

  public void setPrototype(M prototype) {
    this.prototype = prototype;
  }

  @SuppressWarnings("unchecked")
  public <K extends SideQuest> K setSleepInterval(int milliseconds) {
    this.sleepInterval = milliseconds;
    return (K) this;
  }

  public SideQuest() {
    this.state = DaemonState.SIDE_QUEST;
  }

  @SuppressWarnings("unchecked")
  public SideQuest<T, M> setClosure(Closure<T> closure) {
    this.returnRunnable = new ReturnRunnable<>(closure);
    return this;
  }

  @Override
  public final void run(){
    try {

      T result = pursue();
      if (!Thread.currentThread().isInterrupted() && result != null) {
        if (!setResultAndUpdate(result)) {
          System.err.println(DaemonUtils.tag() + description + ": Could not enqueue result to consumer's event queue.");
        }
      }

      if (sleepInterval > 0) {
        Thread.sleep(sleepInterval);
      }

    } catch (InterruptedException ex) {
      //System.out.println(DaemonUtils.tag() + description + " interrupted.");
    } catch (Exception ex) {
      if (getIsVoid())
        returnRunnable = new ReturnRunnable<>(new Closure<T>() {
          @Override
          public void onReturn(Return<T> ret) {
            ret.get();
          }
        });
      if (!setErrorAndUpdate(ex))
        System.err.println(DaemonUtils.tag() + description + ": Could not enqueue error to consumer's event queue.");
    }
  }
}
