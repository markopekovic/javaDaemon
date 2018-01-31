package com.daemonize.daemonengine.quests;

import android.util.Log;

import com.daemonize.daemonengine.closure.Closure;
import com.daemonize.daemonengine.DaemonState;
import com.daemonize.daemonengine.utils.DaemonUtils;

public abstract class MainQuest<T> extends Quest<T> {

  public MainQuest() {
    this.state = DaemonState.MAIN_QUEST;
  }

  public MainQuest(Closure<T> closure){
    this();
    this.closure = closure;
  }

  @Override
  public final void run() {
    try {
      T result = pursue();
      if (!Thread.currentThread().isInterrupted()) {
        if (result != null) {
          setResultAndUpdate(result);
        }/* else if (!getIsVoid()) {//TODO debug only
          Log.d(Thread.currentThread().getName(), description + " returned null.");
        }*/
      }
    } catch (Exception ex) {
      if (ex instanceof InterruptedException) {
        Log.w(DaemonUtils.tag(),description + " interrupted.");
      }
      if (!getIsVoid()) {
        setErrorAndUpdate(ex);
      } else {
        Log.e(DaemonUtils.tag(), "Error in void returning method: " + description + ":");
        Log.e(DaemonUtils.tag(), Log.getStackTraceString(ex));
      }
    }
  }
}
