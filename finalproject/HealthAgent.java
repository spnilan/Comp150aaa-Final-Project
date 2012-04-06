// TODO:
// give the agents energy
// remove energy somehow
//
// spread disease
//
// still have to figure out 2d movement : attaction / repulsion

import sim.util.*;
import sim.engine.*;
import java.awt.*;
import sim.portrayal.*;

public class HealthAgent extends Agent
{

    public boolean infected = false;
    public Disease disease = null; 

    public HealthAgent( String id, Double2D location ) 
    {
        super( id, location );
        try
            {
            intID = Integer.parseInt( id.substring(4) ); // "HealthAgent"
            }
        catch( IndexOutOfBoundsException e )
            {
            System.err.println( "Exception generated: " + e );
            System.exit(1);
            }
        catch( NumberFormatException e )
            {
            System.err.println( "Exception generated: " + e );
            System.exit(1);
            }
    }

    //Double2D desiredLocation = null;
    //Double2D suggestedLocation = null;

    public boolean isInfected() {
        return infected;
    }
    
    public void setInfected(boolean b) {
        infected = b;
    }

    public void step( final SimState state )
    {
        DiseaseSpread hb = (DiseaseSpread)state;

        Bag neighbors = hb.environment.getObjectsWithinDistance(agentLocation, 10.0 * DiseaseSpread.INFECTION_DISTANCE);
        for (int i=0; i < neighbors.numObjs; i++) {

            if (neighbors.objs[i] != null && neighbors.objs[i] != this) {
                Agent agent = (Agent)neighbors.objs[i];
                if (agent instanceof Food) {
                    Food food = (Food)agent;

                }
                else if (agent instanceof HealthAgent) {
                    HealthAgent opp = (HealthAgent)agent;
                    if (this.isInfected() && 
                           hb.withinInfectionDistance(this, agentLocation, opp, opp.agentLocation)) { 
                        opp.setInfected(true);
                    }
                }
            }
        }

        //if( ! hb.acceptablePosition( this, new Double2D(agentLocation.x + dx, agentLocation.y + dy) ) )
        //    {
        //    steps = 0;
        //    }
        double dx = 1, dy = 1;
        agentLocation = new Double2D(agentLocation.x + dx, agentLocation.y + dy);
        hb.environment.setObjectLocation(this,agentLocation);
    }

    protected Color sickColor  = new Color(255,0,0);
    protected Color healthyColor = new Color(0, 255, 0);
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        double diamx = info.draw.width*DiseaseSpread.DIAMETER;
        double diamy = info.draw.height*DiseaseSpread.DIAMETER;

        graphics.setColor( this.isInfected() ? sickColor : healthyColor);

        graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
    }

    public String getType() { return "HealthAgent"; }

}

