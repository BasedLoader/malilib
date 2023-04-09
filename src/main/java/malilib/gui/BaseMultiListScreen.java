package malilib.gui;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import malilib.gui.tab.ScreenTab;
import malilib.gui.util.ScreenContext;
import malilib.gui.widget.BaseTextFieldWidget;
import malilib.gui.widget.InteractableWidget;
import malilib.gui.widget.InteractableWidget.MousePredicate;
import malilib.gui.widget.list.BaseListWidget;
import malilib.input.Keys;

public class BaseMultiListScreen extends BaseTabbedScreen
{
    protected final ArrayList<BaseListWidget> listWidgets = new ArrayList<>();

    public BaseMultiListScreen(String screenId, List<? extends ScreenTab> screenTabs, @Nullable ScreenTab defaultTab)
    {
        super(screenId, screenTabs, defaultTab);

        this.shouldCreateTabButtons = screenTabs.isEmpty() == false;
        this.addPostInitListener(this::refreshListWidgets);
        this.addPreScreenCloseListener(this::closeListWidgets);
    }

    @Override
    protected void clearElements()
    {
        super.clearElements();
        this.listWidgets.clear();
    }

    protected void addListWidget(BaseListWidget widget)
    {
        widget.setTaskQueue(this::addTask);
        widget.setZ(this.z + 10);
        widget.initListWidget();

        this.listWidgets.add(widget);
    }

    protected void refreshListWidgets()
    {
        for (BaseListWidget listWidget : this.listWidgets)
        {
            listWidget.refreshEntries();
        }
    }

    protected void closeListWidgets()
    {
        for (BaseListWidget listWidget : this.listWidgets)
        {
            listWidget.onScreenClosed();
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.onMouseClicked(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            if (listWidget.tryMouseClick(mouseX, mouseY, mouseButton))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        if (super.onMouseReleased(mouseX, mouseY, mouseButton))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            listWidget.onMouseReleased(mouseX, mouseY, mouseButton);
        }

        return false;
    }

    @Override
    public boolean onMouseScrolled(int mouseX, int mouseY, double mouseWheelDelta)
    {
        if (super.onMouseScrolled(mouseX, mouseY, mouseWheelDelta))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            if (listWidget.tryMouseScroll(mouseX, mouseY, mouseWheelDelta))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onMouseMoved(int mouseX, int mouseY)
    {
        if (super.onMouseMoved(mouseX, mouseY))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            if (listWidget.onMouseMoved(mouseX, mouseY))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers)
    {
        // Try to handle everything except ESC in the parent first
        if (keyCode != Keys.KEY_ESCAPE && super.onKeyTyped(keyCode, scanCode, modifiers))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            if (listWidget.onKeyTyped(keyCode, scanCode, modifiers))
            {
                return true;
            }
        }

        // If the list widget or its sub widgets didn't consume the ESC, then send that to the parent (to close the GUI)
        return keyCode == Keys.KEY_ESCAPE && super.onKeyTyped(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char charIn, int modifiers)
    {
        if (super.onCharTyped(charIn, modifiers))
        {
            return true;
        }

        for (BaseListWidget listWidget : this.listWidgets)
        {
            if (listWidget.onCharTyped(charIn, modifiers))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected InteractableWidget getHighestMatchingWidget(int mouseX, int mouseY,
                                                          MousePredicate predicate,
                                                          @Nullable InteractableWidget highestFoundWidget)
    {
        highestFoundWidget = super.getHighestMatchingWidget(mouseX, mouseY, predicate, highestFoundWidget);
        return InteractableWidget.getHighestMatchingWidgetFromList(mouseX, mouseY, predicate, highestFoundWidget, this.listWidgets);
    }

    @Override
    protected List<BaseTextFieldWidget> getAllTextFields()
    {
        List<BaseTextFieldWidget> textFields = new ArrayList<>(super.getAllTextFields());

        for (BaseListWidget listWidget : this.listWidgets)
        {
            textFields.addAll(listWidget.getAllTextFields());
        }

        return textFields;
    }

    @Override
    protected int getCurrentScrollbarPosition()
    {
        return 0;
    }

    @Override
    protected void setCurrentScrollbarPosition(int position)
    {
    }

    @Override
    protected void renderCustomContents(ScreenContext ctx)
    {
        for (BaseListWidget listWidget : this.listWidgets)
        {
            listWidget.render(ctx);
        }
    }

    @Override
    public void renderDebug(ScreenContext ctx)
    {
        super.renderDebug(ctx);

        if (ctx.isActiveScreen)
        {
            for (BaseListWidget listWidget : this.listWidgets)
            {
                listWidget.renderDebug(listWidget.isMouseOver(ctx.mouseX, ctx.mouseY), ctx);
            }
        }
    }
}
