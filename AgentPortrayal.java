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
        filled = true;
        paint = healthyColor;
        if(agent.infected) {
            paint = sickColor;
        }
        super.draw(object, graphics, info);
        
        // draw an elipse around agent showing sensory range
        /*
        filled = false;
        double range = agent.sensoryRange;
        info.draw.width = 100;
        info.draw.height = 100;
        super.draw(object, graphics, info);
        */
    }

}
