import sim.portrayal.*;
import sim.portrayal.simple.*;
import java.awt.*;

/**
 * A portrayal (visualization) for a Food item.
 */
class FoodPortrayal extends OvalPortrayal2D
{
    // Portrayal parameters:
    protected static Color color = new Color(0, 255, 0);

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
        paint = color;
        super.draw(object, graphics, info);
    }
}
