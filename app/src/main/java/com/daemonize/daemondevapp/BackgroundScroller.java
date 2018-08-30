package com.daemonize.daemondevapp;


import com.daemonize.daemondevapp.imagemovers.ImageMoverDaemon;
import com.daemonize.daemonprocessor.annotations.Daemonize;
import com.daemonize.daemonprocessor.annotations.SideQuest;

@Daemonize
public class BackgroundScroller {

    private ImageMoverDaemon target;

    private Pair<Integer, Integer> lastTargetCoord = Pair.create(0, 0);

    public BackgroundScroller(ImageMoverDaemon target) {
        this.target = target;
    }

    @SideQuest(SLEEP = 25)
    public Pair<Integer, Integer> scroll(){
        Pair<Integer, Integer> currentTargetCoord = Pair.create(
                Math.round(target.getLastCoordinates().getFirst() + 100),
                Math.round(target.getLastCoordinates().getSecond() + 100)
        );

        if (currentTargetCoord.equals(lastTargetCoord))
            return null;

        return currentTargetCoord;

    }
}
