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
    protected static Color healthySatiatedColor = new Color(7, 202, 250);
    protected static Color sickSatiatedColor = new Color(0, 85, 255);

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        Agent agent = (Agent)object;
        paint = healthyColor;
        if(agent.energy == 1000) {
          paint = healthySatiatedColor;
        }
        if(agent.infected) {
          paint = sickColor;
          if(agent.energy ==  1000) {
            paint = sickSatiatedColor;
          }
        }

        super.draw(object, graphics, info);
    }
}
