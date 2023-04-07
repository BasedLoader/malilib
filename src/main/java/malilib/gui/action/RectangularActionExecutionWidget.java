package malilib.gui.action;

import com.google.gson.JsonObject;

import malilib.gui.util.DraggedCorner;
import malilib.gui.util.ScreenContext;
import malilib.render.text.StyledTextLine;
import malilib.util.MathUtils;
import malilib.util.data.json.JsonUtils;
import malilib.util.position.Vec2i;

public class RectangularActionExecutionWidget extends BaseActionExecutionWidget
{
    protected DraggedCorner draggedCorner = DraggedCorner.BOTTOM_RIGHT;
    protected Vec2i dragStartOffset = Vec2i.ZERO;

    public RectangularActionExecutionWidget()
    {
        super();

        this.getTextOffset().setCenterHorizontally(true);
        this.getTextOffset().setXOffset(0);
    }

    @Override
    protected Type getType()
    {
        return Type.RECTANGULAR;
    }

    @Override
    public void startDragging(int mouseX, int mouseY)
    {
        this.dragStartOffset = new Vec2i(mouseX - this.getX(),  mouseY - this.getY());
        super.startDragging(mouseX, mouseY);
    }

    @Override
    protected void startResize(int mouseX, int mouseY)
    {
        this.draggedCorner = DraggedCorner.getFor(mouseX, mouseY, this);
        this.resizing = true;
        this.notifyChange();
    }

    @Override
    public void moveWidget(int mouseX, int mouseY)
    {
        int x = mouseX - this.dragStartOffset.x;
        int y = mouseY - this.dragStartOffset.y;
        int gridSize = this.getGridSize();

        if (gridSize > 1)
        {
            x = MathUtils.roundDown(x, gridSize);
            y = MathUtils.roundDown(y, gridSize);
        }

        this.setPosition(x, y);
    }

    @Override
    protected void resizeWidget(int mouseX, int mouseY)
    {
        this.draggedCorner.updateWidgetSize(mouseX, mouseY, this.getGridSize(), this);
    }

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        if (this.resizing || this.dragging || this.selected)
        {
            StyledTextLine line = StyledTextLine.unParsed(String.format("%d x %d", this.getWidth(), this.getHeight()));
            this.renderTextLine(x, y - 10, z, 0xFFFFFFFF, true, line, ctx);
        }

        super.renderAt(x, y, z, ctx);
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = super.toJson();

        obj.addProperty("x", this.getX());
        obj.addProperty("y", this.getY());
        obj.addProperty("width", this.getWidth());
        obj.addProperty("height", this.getHeight());

        return obj;
    }

    @Override
    public void fromJson(JsonObject obj)
    {
        super.fromJson(obj);

        int x = JsonUtils.getIntegerOrDefault(obj, "x", 0);
        int y = JsonUtils.getIntegerOrDefault(obj, "y", 0);
        this.setPosition(x, y);
        this.setWidth(JsonUtils.getIntegerOrDefault(obj, "width", this.getWidth()));
        this.setHeight(JsonUtils.getIntegerOrDefault(obj, "height", this.getHeight()));
    }
}
