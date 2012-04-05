/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.virus;

import sim.util.*;
import sim.engine.*;
import java.awt.*;
import sim.portrayal.*;

public /*strictfp*/ class Food extends Agent
{

    public Food( String id, Double2D location ) 
    {
        super( id, location );
        try
            {
            intID = Integer.parseInt( id.substring(5) ); // "Human"
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


    public void step( final SimState state )
    {
        // food doesn't move
    }

    protected Color foodColor = new Color(128,255,128);
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        double diamx = info.draw.width*VirusInfectionDemo.DIAMETER;
        double diamy = info.draw.height*VirusInfectionDemo.DIAMETER;
    
        graphics.setColor ( foodColor ); 
        graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
    }


    public String getType()
    {
        return "Food";
    }
}
