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
    protected static final Color falsePositiveColor = new Color(0 , 0, 127);
    protected static final Color falseNegativeColor = new Color(255, 0, 100);
    protected static final Color sensoryRangeColor = new Color(124, 140, 130);
    protected static final Color eatingRangeColor = new Color(150, 255, 0);
    protected static final Color infectionRangeColor = new Color(255, 80, 0);

    // Useful for inspector
    public String getName(LocationWrapper wrapper) {
        Agent agent = (Agent)wrapper.getObject();
        return "Agent " + agent.id;
    }

    public String getStatus(LocationWrapper wrapper) {
        Agent agent = (Agent)wrapper.getObject();
        return "Agent " + agent.id + ": energy=" + agent.energy + ", infected=" + agent.infected;
    }

    public void drawCircle(final int x, final int y, final int radius,
                          Graphics2D graphics, Color color, boolean filled)
    {

        final int topX = x - radius;
        final int topY = y - radius;
        final int width = 2 * radius;
        final int height = 2 * radius;

        graphics.setPaint(color);
        if (filled) {
            graphics.fillOval(topX, topY, width, height);
        } else {
           graphics.drawOval(topX, topY, width, height);
        }
    }

    public void drawAsterisk(final int x, final int y, int radius, Graphics2D graphics, Color color) 
    {
	final int topX = x - (radius * 2);
	final int topY = y - (radius * 2);
	final int width = radius;
	final int height = radius;

	if (color == healthyColor) {graphics.setPaint(falseNegativeColor);}
	else if (color == sickColor) {graphics.setPaint(falsePositiveColor);}
	else {graphics.setPaint(new Color(0,0,0));}
	
	graphics.fillOval(topX, topY, width, height);
    }

    public void drawAgent(Agent agent, Graphics2D graphics, DrawInfo2D info)
    {
        final int radius = (int)(info.draw.width / 2.0);
        final int x = (int)info.draw.x;
        final int y = (int)info.draw.y;

        Color agentColor = healthyColor;
	boolean symptoms = agent.symptomVisibility > agent.symptomTolerance;

	
	if (agent.infected) {
	    agentColor = sickColor;
	} else {
	    agentColor = healthyColor;
	}
	
        drawCircle(x, y, radius, graphics, agentColor, agent.isSatiated());
	
	if (agent.infected && !symptoms) {
	    drawAsterisk(x, y, radius, graphics, agentColor);
	} else if (!agent.infected && symptoms) {
	    drawAsterisk(x, y, radius, graphics, agentColor);
	}
    }

    public void drawSensoryRange(Graphics2D graphics, DrawInfo2D info)
    {
        final int radius = (int)(info.draw.width * Agent.sensoryRange);
        final int x = (int)info.draw.x;
        final int y = (int)info.draw.y;

        drawCircle(x, y, radius, graphics, sensoryRangeColor, false);
    }

    public void drawEatingRange(Graphics2D graphics, DrawInfo2D info)
    {
        final int radius = (int)(info.draw.width * Agent.eatingRange);
        final int x = (int)info.draw.x;
        final int y = (int)info.draw.y;

        drawCircle(x, y, radius, graphics, eatingRangeColor, false);
    }

    public void drawInfectionRange(Graphics2D graphics, DrawInfo2D info)
    {
        final int radius = (int)(info.draw.width * Agent.infectionRange);
        final int x = (int)info.draw.x;
        final int y = (int)info.draw.y;

        drawCircle(x, y, radius, graphics, infectionRangeColor, false);
    }

    // default agentWidth = 10, agentHeight = 10
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        Agent agent = (Agent)object;

        drawAgent(agent, graphics, info);
        if (info.selected) {
            drawSensoryRange(graphics, info);
            drawEatingRange(graphics, info);
            drawInfectionRange(graphics, info);
        }
    }

}
