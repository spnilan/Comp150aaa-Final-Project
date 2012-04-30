import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.*;
import java.awt.*;

/**
 * A portrayal (visualization) for an Agent.
 */
class AgentPortrayal extends OvalPortrayal2D
{
    // Portrayal parameters:
    protected static final Color healthyColor = new Color(0, 0, 127);
    protected static final Color sickColor  = new Color(255, 0, 0);
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

    // Signifies if an agent is perceived as infected
    public void drawAsterisk(final int x, final int y, int radius,
                             Graphics2D graphics, Color color) 
    {
        final int topX = x - (radius * 2);
        final int topY = y - (radius * 2);
        final int width = radius;
        final int height = radius;

        graphics.setPaint(color);
        graphics.fillOval(topX, topY, width, height);
    }

    public void drawEnergyBar(int x, int y, int width, double fullness,
                              Graphics2D graphics, Color color)
    {
        final int topX = x - width / 2;
        final int topY = y - width;
        final int height = width / 4;
        if(fullness > 1) {
            fullness = 1;
        }
        graphics.setPaint(color);
        graphics.drawRect(topX, topY, width, height);
        graphics.fillRect(topX, topY, (int)(fullness * width), height);
    }

    public void drawAgent(Agent agent, Graphics2D graphics, DrawInfo2D info)
    {
        final int radius = (int)(info.draw.width / 2.0);
        final int x = (int)info.draw.x;
        final int y = (int)info.draw.y;

        Color agentColor = healthyColor;
        if(agent.infected) {
            agentColor = sickColor;
        }
        drawCircle(x, y, radius, graphics, agentColor, true);
        boolean symptoms = agent.symptomVisibility > agent.symptomTolerance;
        if (symptoms) {
            drawAsterisk(x, y, radius, graphics, agentColor);
        }
        drawEnergyBar(x, y, (int)(info.draw.width * 2),
                      agent.getEnergy() / agent.initialEnergy,
                      graphics, agentColor);
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

    public void drawForces(final Agent agent, Graphics2D graphics, DrawInfo2D info)
    {
        final double radius = 100;
        Double2D sum = new Double2D(0, 0);
        double maxLength = 1.0;
        for(final Agent.Force f : agent.lastForces) {
            Double2D actualForce = f.force.multiply(f.multiplier);
            sum = sum.add(actualForce);
            maxLength = Math.max(maxLength, actualForce.length());
        }
        maxLength = Math.max(maxLength, sum.length());
        double resizeFactor = radius / maxLength;
        drawOneForce("sum", sum.multiply(resizeFactor), graphics, info);
        for(final Agent.Force f : agent.lastForces) {
            drawOneForce(f.name, f.force.multiply(f.multiplier * resizeFactor), graphics, info);
        }
    }

    public void drawOneForce(String name, Double2D force, Graphics2D graphics, DrawInfo2D info)
    {
        final double minLength = 40;

        graphics.drawLine((int)(info.draw.x),
                          (int)(info.draw.y),
                          (int)(info.draw.x + force.x),
                          (int)(info.draw.y + force.y));
        if(force.length() >= minLength) {
            graphics.drawString(name,
                    (int)(info.draw.x + force.x),
                    (int)(info.draw.y + force.y));
        }
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
            drawForces(agent, graphics, info);
        }
    }
}
