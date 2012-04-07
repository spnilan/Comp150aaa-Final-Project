
import com.grid.simulations.simworld.Agent;
import com.grid.simulations.simworld.SpecialRandom;
import com.grid.simulations.simworld.components.ReducedSimObject;
import com.grid.simulations.simworld.components.SimObject;
import com.grid.simulations.simworld.components.SimScheduler;
import com.grid.simulations.simworld.worlds.collector.Entity;
import com.grid.simulations.simworld.worlds.collector.EntityState;
import java.util.ArrayList;
import java.util.Hashtable;

public class BasicAgent extends Agent {

    //Define any variables you want below, if you want them to be recordable,
    //make sure that they are public.
    public double speed, heading;
    public double energy;
   
    // specific to our agent
    private Disease disease = null;
    private double observability;

    //All agents need to have a default constructor
    public BasicAgent() {
        super();
        agentState = new BasicAgentState("", 0, 0.0, 0.0, 0.0, 0.0);
        initialize(this, 1000, 1000);
    }

    // Note: this constructor won't work for now
    public BasicAgent(String agentIDName, int agentIDNumber, double X, double Y,
		      long seed, double maxMovePerCycleX, double maxMovePerCycleY,
            Hashtable<String, SimObject> SchdulerObjectManagementList) {
        super(new BasicAgentState(agentIDName, agentIDNumber, X, Y,
                maxMovePerCycleX, maxMovePerCycleY),
                SchdulerObjectManagementList);
        this.seed = seed;
        random = new SpecialRandom(seed); // seed the local random number generator
        
        //Generate a location for the agent based off of max height and width of 
        //The environment size of your choosing
        initialize(this, 1000, 1000);
    }

    // The constructor for your agent
    public BasicAgent(String agentIDName, int agentIDNumber, double X, double Y,
		      long seed, Hashtable<String, SimObject> SchdulerObjectManagementList) {
        super(new BasicAgentState(agentIDName, agentIDNumber, X, Y), SchdulerObjectManagementList);
        this.seed = seed;
        random = new SpecialRandom(seed); // seed the local random number generator
        
        //Generate a location for the agent based off of max height and width of 
        //The environment size of your choosing
        initialize(this, 1000, 1000);
    }

    //You may want to decide the placement of your agents, for now I have it 
    //generating random positions for them
    public void initialize(BasicAgent agent, double maxX, double maxY) {
        // if we don't have numbers yet, create a random position
        if (!((agent.getX() != -1) && (agent.getY() != -1))) {
            agent.setX(Math.random() * maxX);
            agent.setY(Math.random() * maxY);
        }

        //observability = Math.random();
        observability = 1.0;
        speed = 3.0;
        heading = 150;
        energy = 1000;
        System.out.println("initializing agent " + agentState.agentID); 
    }

    public String getID() {
        return this.agentState.agentID;
    }

    public boolean isSick() {
        double rand = Math.random();
        boolean sick = disease != null;
        
        if (rand < observability) {
        // tell the truth
            return sick;
        }
        else {
            return !sick;
        }
    }


    //This method is called alongside all other agent's sense functions before
    //any act functions are called. This gives your agent the oportunity to sense
    //before it or any agent acts.
    @Override
    public void sense(ArrayList<SimObject> localworld, SimScheduler scheduler) {
        //A simple check the make sure you agent doesn't sense if its not alive
        if (!isAlive()) {
            return;
        }
        //an array of localworld + proxy Agents if they exist
        ArrayList<SimObject> all = getAllPerceivableAgents(scheduler);

        double totalWeight = 0.0;
        double repulsionFactor = 20;
        double healthyFactor = 10; // grouping factor
        double foodFactor = 15;   
        double angleSum = 0;

        for (SimObject so : all) {
            if (so instanceof BasicAgent) {
                BasicAgent agent = (BasicAgent)so;
                double d = distanceBetween(this, agent);
                double angle = (angleBetween(this, agent) + 3600) % 360;
                double weight;

                if (agent.getID() != this.getID()) {
                    if (agent.isSick()) { 
                        // repulsion from sick agents
                        weight = d > 0 ? repulsionFactor / (d * d * d) : 0;
                        if (angle >= 180) {
                           angle -= 180;
                        }
                        else {
                           angle += 180;
                        } 
                    }
                    else {
                        weight = d > 0 ? healthyFactor / (d * d * d) : 0;
                    }
                    angleSum += (weight * angle);
                    totalWeight += weight;
                }
            }
            else if (so instanceof Food) {
                Food food = (Food)so;
                double d = distanceBetween(this, food);
                double angle = (angleBetween(this, food) + 3600) % 360;
                double weight = d > 0 ? foodFactor / (d * d * d) : 0;
                totalWeight += weight;
                angleSum += weight * angle;
            }
        }

        double targetHeading = 0;
        if (totalWeight > 0) {
            targetHeading = angleSum / totalWeight;
        }
       
        heading = targetHeading; 
        System.out.println("target angle: " + targetHeading);
    }


    /*
     * In the act function, your agent shouldn't be scanning through the agents
     * although you are allowed to if you really want. Traditionally this
     * function is used to call action functions based off of variables stored
     * during the sensing stage, such as moving towards an acquired goal item
     * that was noticed while sensing
     */
    @Override
    public void act(ArrayList<SimObject> localworld, SimScheduler scheduler) {
        //A simple check the make sure you agent doesn't act if its not alive
        if (!isAlive()) {
            return;
        }


       BasicAgentState ps = (BasicAgentState)agentState;
       ps.setX(ps.getX() + speed * Math.cos(Math.toRadians(heading)));
       ps.setY(ps.getY() + speed * Math.sin(Math.toRadians(heading)));
       System.out.println("agentID: " + getID() + " heading: " + heading + " x: " + ps.getX() + " y: " + ps.getY()); 
    
    }

    //Move the agent towards the x,y coordinate, adjusting its heading angle 
    //accordingly
    //The agent will move based off of its current speed
    public void move(double newx, double newy) {
        double new_heading;

        //If the the new coordinates are empty, don't change heading
        if (newx == 0 && newy == 0) {
            new_heading = heading;
        } else {
            new_heading = Math.toDegrees(Math.atan2(newy - getY(), newx - getX()));
        }

        //Update the agent states coordinates accordingly
        BasicAgentState ps = (BasicAgentState)agentState;
        ps.setX(ps.getX() + speed * Math.cos(Math.toRadians(new_heading)));
        ps.setY(ps.getY() + speed * Math.sin(Math.toRadians(new_heading)));

        heading = new_heading;
    }

    //Returns the distrance between two agents
    static public double distanceBetween(Agent obj1, Agent obj2) {
        double agentsX = obj1.getX() - obj2.getX();
        double agentsY = obj1.getY() - obj2.getY();
        return Math.sqrt(agentsX * agentsX + agentsY * agentsY);
    }

    //Returns the angle (in radians) between two agents, 
    // with the angle origin at the first agent pointing towards the second agent
    static public double angleBetween(Agent obj1, Agent obj2) {
        return Math.toDegrees(Math.atan2(obj2.getY() - obj1.getY(),
                obj2.getX() - obj1.getX()));
    }

    /*
     * Your agent's may or may not die, but this is a fairly stanard method for
     * deeming an agent dead and reporting it to the terminal The string given
     * can be used to print a more specific message about the cause of death
     */
    public final void die(String dC) {
        System.err.println("Your agent died!");
    }

    //Returns all local and proxy agents that this agent has access to
    private ArrayList<SimObject> getAllPerceivableAgents(SimScheduler scheduler) {
        ArrayList<SimObject> all = new ArrayList<SimObject>();
        for (SimObject so : scheduler.localAgents.values()) {
            all.add(so);
        }
        if (scheduler.proxyAgents != null) {
            if (scheduler.proxyAgents.size() > 0) {
                for (ReducedSimObject so : scheduler.proxyAgents.values()) {
                    if (!all.contains(so)) {
                        all.add(so);
                    }
                }
            }
        }
        return all;
    }

    //Returns whether or not the agent is alive
    public boolean isAlive() {
	//        return getBasicAgentState().isAlive();
        return ((BasicAgentState) agentState).isAlive();
    }
}

