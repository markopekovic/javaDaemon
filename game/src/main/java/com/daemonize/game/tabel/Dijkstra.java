package com.daemonize.game.tabel;

//import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Dijkstra extends PathFinding {

    @Override
    public boolean recalculate(Grid grid) {
        Field startNode =  grid.getGrid()[grid.endPoint.getFirst()][grid.endPoint.getSecond()];
        Field endNode = grid.getGrid()[grid.startPoint.getFirst()][grid.startPoint.getSecond()];

        Field [] heapArray = new Field[ grid.getGrid().length * grid.getGrid()[0].length];
        Heap<Field> openSet = new Heap<Field>(heapArray) ;
        HashSet<Field> closedSet = new HashSet<Field>();

        openSet.add(startNode);

        while (openSet.size() > 0) {
            Field currentNode = openSet.removeFirst();

            closedSet.add(currentNode);
            if ( currentNode.equals(endNode)){
                startNode.gCost = 0;
                System.out.println("MATRICA \n" +  grid.gridToString().toString());
                return true;
            }
            for (Field neighbour : grid.getNeighbors(currentNode.getRow(),currentNode.getColumn())) {
                if (!neighbour.isWalkable() || closedSet.contains(neighbour)) {
                    continue;
                }
                int newMovementCostToNeighbour = getDistance(currentNode,neighbour) + (currentNode.gCost == Integer.MAX_VALUE ? 0 : currentNode.gCost);//+ neighbour.getWeight();
                if ( newMovementCostToNeighbour < neighbour.gCost ) {
                    neighbour.gCost = newMovementCostToNeighbour;
                    if ( !openSet.contains(neighbour)) {
                        openSet.add(neighbour);
                    } else {
                        openSet.updateItem(neighbour);
                    }
                }
            }
        }

        grid.getGrid()[grid.endPoint.getFirst()][grid.endPoint.getSecond()].gCost = 0;

        return false;
    }


}
