package malilib.gui.widget.list.entry;

import malilib.gui.BaseScreen;
import malilib.gui.edit.CustomIconEditScreen;
import malilib.gui.edit.CustomIconListScreen;
import malilib.gui.icon.Icon;
import malilib.gui.util.GuiUtils;
import malilib.gui.util.ScreenContext;
import malilib.gui.widget.button.GenericButton;
import malilib.registry.Registry;
import malilib.render.text.StyledTextLine;

public class IconEntryWidget extends BaseDataListEntryWidget<Icon>
{
    protected final GenericButton editButton;
    protected final GenericButton removeButton;

    public IconEntryWidget(Icon data, DataListEntryWidgetData constructData)
    {
        super(data, constructData);

        this.editButton = GenericButton.create(20, "malilib.button.misc.edit", this::openEditScreen);
        this.removeButton = GenericButton.create(20, "malilib.button.misc.remove", this::removeIcon);

        int w = data.getWidth();
        int h = data.getHeight();
        int u = data.getU();
        int v = data.getV();
        String texture = data.getTexture().toString();

        this.iconOffset.setXOffset(4);
        this.textOffset.setXOffset(28);
        this.setIcon(data);
        this.setText(StyledTextLine.parseFirstLine(String.format("%d x %d @ [ %d, %d ] @ %s", w, h, u, v, texture)));
        this.getBackgroundRenderer().getNormalSettings().setEnabled(true);
        this.getBackgroundRenderer().getNormalSettings().setColor(this.isOdd ? 0x30707070 : 0x50707070);
        this.getBackgroundRenderer().getHoverSettings().setColor(0x50909090);
    }

    @Override
    public void reAddSubWidgets()
    {
        super.reAddSubWidgets();

        this.addWidget(this.editButton);
        this.addWidget(this.removeButton);
    }

    @Override
    public void updateSubWidgetPositions()
    {
        super.updateSubWidgetPositions();

        int y = this.getY() + 1;

        this.removeButton.setRight(this.getRight() - 2);
        this.removeButton.setY(y);

        this.editButton.setRight(this.removeButton.getX() - 2);
        this.editButton.setY(y);
    }

    @Override
    protected void renderIcon(int x, int y, float z, boolean enabled, boolean hovered, ScreenContext ctx)
    {
        Icon icon = this.getIcon();

        if (icon != null)
        {
            int width = icon.getWidth();
            int height = icon.getHeight();
            int maxSize = this.getHeight() - 2;

            if (width > maxSize || height > maxSize)
            {
                double scale = (double) maxSize / (double) Math.max(width, height);
                width = (int) Math.floor(scale * width);
                height = (int) Math.floor(scale * height);
            }

            int usableWidth = this.getWidth() - this.padding.getHorizontalTotal();
            int usableHeight = this.getHeight() - this.padding.getVerticalTotal();
            x = this.getIconPositionX(x, usableWidth, width);
            y = this.getIconPositionY(y, usableHeight, height);

            icon.renderScaledAt(x, y, z + 0.025f, width, height);
        }
    }

    protected void openEditScreen()
    {
        CustomIconListScreen screen = GuiUtils.getCurrentScreenIfMatches(CustomIconListScreen.class);

        if (screen != null)
        {
            CustomIconEditScreen editScreen = new CustomIconEditScreen(this.data, this::replaceIcon);
            editScreen.setParent(screen);
            BaseScreen.openPopupScreen(editScreen);
        }
    }

    protected void replaceIcon(Icon icon)
    {
        this.scheduleTask(() -> {
            Registry.ICON.unregisterUserIcon(this.data);
            Registry.ICON.registerUserIcon(icon);
            this.listWidget.refreshEntries();
        });
    }

    protected void removeIcon()
    {
        this.scheduleTask(() -> {
            Registry.ICON.unregisterUserIcon(this.data);
            this.listWidget.refreshEntries();
        });
    }
}
