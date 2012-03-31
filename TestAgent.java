 /** Template for foraging agent
 *
 * @author matthias scheutz
 *
 */

import com.grid.simulations.simworld.*;
import com.grid.simulations.simworld.components.*;
import com.grid.simulations.simworld.worlds.collector.*;
import java.util.*;

public class TestAgent extends Agent_F {
    // add your local variables here
    Item target = null, closestAgent = null;
    final int eatingSpeed = 4;
    final int maxSpeed = 7;
    double segmentLength = 1;
    int segmentPos = 0;
    final double segmentDelta = 0.2;
    int fightLength = 0;
    int fightThreshold = 700; //Only fight if more than 700 energy


    /*
       We use the aggression value for three purposes:
       1) High aggression value indicates to other agents our agent is
       dangerous.
       2) The first 7 digits ( (int)(aggression * 1e7) ) are a fingerprint
       indicating to our agent that it is seeing another agent of the same kind.
       3) The next 5 digits ( (int)(aggression * 1e12) % 100000 ) encode the
       energy of the agent.

       NOTE: 64-bit double has about 16 decimal digits we can use.
    */
    final int fingerprint = 9972124;

    public TestAgent() {
        super();
    }

    // Do not change this line
    public TestAgent(String thisIDName, int thisIDNumber, double iX, double iY, long seed, Hashtable SchdulerObjectManagementList) {
        super((thisIDName.isEmpty() ? "testagent" : thisIDName), thisIDNumber, iX, iY, seed, SchdulerObjectManagementList);
    }

    // this function will be called automatically by the environment when agents are too
    // close to each other; needs to return true if the agent is fighting vs false if not
    // add your decision-making for fighting here
    public boolean fight() {
        //avoid getting caught in a loop with same agents
        if (closestAgent != null && sameAgentType(closestAgent)) {
            // weakest agent wins when they are the same type
            System.out.println(agentState.getCycleNumber() +  "  " + agentID + " spotted same type agent"); 
            if (fightLength > 0) {
                return false;
            }

            if (decodeEnergy(closestAgent) > getEnergy()) {
                fightLength++;  
                return true;
            }
            else {
                fightLength = 0;
                return false;
            }
        }

        if (target != null && getEnergy() > fightThreshold) {
            fightLength++;
            return true;
        }
        else {
            fightLength = 0;
            return false;
        }
    }

    // this returns true depending on whether the agent wants to consume a food source
    // add your decision-making for eating here
    public boolean eat() {
        return true;
    }

    //law of cosines
    private double distanceBetween(Item a, Item b) {
        double da = a.getDistance();
        double db = b.getDistance();
        double angle = (a.getHeading() - b.getHeading()) * Math.PI / 180.0;
        return Math.sqrt(da*da + db*db -2*da*db*Math.cos(angle));
    }

    // returns 0 if not our agent type
    private boolean sameAgentType(Item agent) {
        double aggr = agent.getAggression();
        int fprint = (int)(aggr * 1e7);
        return (fprint == fingerprint);
    }

    private int decodeEnergy(Item agent) {
        double aggr = agent.getAggression();
        int fprint = (int)(aggr * 1e7);
        int energy = (int)((aggr - fprint * 1e-7) * 1e12);
        return energy;
    }

    // picking target food
    // add your code for sensing here
    public void sense(ArrayList<Item> agents,ArrayList<Item> food) {
        double maxDesire = Double.NEGATIVE_INFINITY;
        target = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for(Item item : food) {
            if (item.getEnergy() > 0) {
                double dist = item.getDistance();
                double attraction = 1.0 / (dist*dist*dist);
                double penalty = 0;

                for (Item agent : agents) {
                    double d = distanceBetween(agent, item);
                    if (sameAgentType(agent)) {
                        int energy = decodeEnergy(agent);
                        // altruistic behavior - add a penalty if other agent of
                        // same type with lower energy near the food
                        if (energy < getEnergy()) {
                            penalty += (getEnergy() / energy) / (d*d*d);
                        }
                    }
                    else {
                        // treat all the other agents the same
                        penalty += 1.0 / (d*d*d);
                    }
                }
                if (agents.size() > 0) {
                    penalty /= agents.size();
                }
            
                double desire = attraction - penalty;
                //System.out.println(
                //        " attraction: " + attraction + " penalty:" + penalty +
                //        " desire: " + desire);
                if (desire > maxDesire) {
                    maxDesire = desire;
                    target = item;
                }

            }
        }
        //System.out.println(agentID + " cycle=" + agentState.getCycleNumber() +
        //                   " seeing " + agents.size() + " other agents.");
        for(Item agent : agents) {
            if (agent.getDistance() < minDistance) {
                minDistance = agent.getDistance();
                closestAgent = agent;
            }
        }
    }

    // template act function as descibed in the assignment
    // add your code for acting here (note that you can only perform one turn and one speed action at the same time
    public void act() {
        setAggression(fingerprint * 1e-7 + getEnergy() * 1e-12);

        if(target == null) {
            // Spiral.
            if(segmentPos > segmentLength) {
                segmentLength+=segmentDelta;
                segmentPos = 0;
                turnright();
            }
            else {
                segmentPos++;
            }
            if (getSpeed() > 4) {
                slowdown();
            }
            else if (getSpeed() < 4) {
                speedup();
            }
        } else {
            segmentLength = 1;
            double heading = (target.getHeading()+3600) % 360;

            if(heading < 10 || heading > 350) {
                // go straight
            } else if(heading < 180) {
                // FUTURE: change speed?
                turnright();
            } else {
                turnleft();
            }
            double speed = getSpeed();
            double d = (speed+eatingSpeed)*(speed-eatingSpeed+1)/2.0;
            if(d+speed >= target.getDistance()){
                slowdown();
            } else { 
                speedup();
            }
        }
    }
}

