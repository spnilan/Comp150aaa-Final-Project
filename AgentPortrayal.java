import sim.portrayal.*;
import sim.portrayal.simple.*;
import java.awt.*;

/**
 * A portrayal (visualization) for an Agent.
 */
class AgentPortrayal extends OvalPortrayal2D
{
    // Portrayal parameters:
    protected static final Color healthyColor = new Color(0, 0, 127);
    protected static final Color sickColor  = new Color(255, 0, 0);

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        Agent agent = (Agent)object;
        
        // draw the agent object
        filled = false;
        paint = healthyColor;
        if(agent.infected) {
            paint = sickColor;
        }
        if(agent.isSatiated()) {
            filled = true;
        }
        super.draw(object, graphics, info);
    }

}
