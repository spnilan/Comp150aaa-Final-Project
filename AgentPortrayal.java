import sim.portrayal.*;
import sim.portrayal.simple.*;
import java.awt.*;

/**
 * A portrayal (visualization) for an Agent.
 */
class AgentPortrayal extends OvalPortrayal2D
{
    // Portrayal parameters:
    protected static Color healthyColor = new Color(0, 0, 127);
    protected static Color sickColor  = new Color(255, 0, 0);

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        Agent agent = (Agent)object;
        paint = healthyColor;
        if(agent.infected) {
            paint = sickColor;
        }
        super.draw(object, graphics, info);
    }
}
