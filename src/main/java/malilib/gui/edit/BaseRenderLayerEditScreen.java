package malilib.gui.edit;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import malilib.config.value.LayerMode;
import malilib.gui.BaseScreen;
import malilib.gui.BaseTabbedScreen;
import malilib.gui.icon.DefaultIcons;
import malilib.gui.icon.Icon;
import malilib.gui.listener.IntegerTextFieldListener;
import malilib.gui.tab.ScreenTab;
import malilib.gui.widget.BaseTextFieldWidget;
import malilib.gui.widget.CheckBoxWidget;
import malilib.gui.widget.IntegerTextFieldWidget;
import malilib.gui.widget.LabelWidget;
import malilib.gui.widget.button.GenericButton;
import malilib.gui.widget.button.OnOffButton;
import malilib.util.ListUtils;
import malilib.util.StringUtils;
import malilib.util.data.Int2BooleanFunction;
import malilib.util.game.wrap.GameUtils;
import malilib.util.position.LayerRange;

public abstract class BaseRenderLayerEditScreen extends BaseTabbedScreen
{
    protected BaseTextFieldWidget textField1;
    protected BaseTextFieldWidget textField2;
    protected boolean addPlayerFollowingOptions;
    protected boolean addLayerRangeHotkeyCheckboxes;
    protected int controlsStartX;
    protected int controlsStartY;

    public BaseRenderLayerEditScreen(String screenId,
                                     List<? extends ScreenTab> screenTabs,
                                     @Nullable ScreenTab defaultTab)
    {
        super(screenId, screenTabs, defaultTab);
        this.addPostInitListener(this::createDefaultLayerEditControls);
    }

    protected abstract LayerRange getLayerRange();

    protected Icon getValueAdjustButtonIcon()
    {
        return DefaultIcons.BTN_PLUSMINUS_16;
    }

    protected void createDefaultLayerEditControls()
    {
        this.createLayerEditControls(this.x + this.controlsStartX, this.y + this.controlsStartY, this.getLayerRange());
    }

    protected void createLayerEditControls(int x, int y, LayerRange layerRange)
    {
        int origX = x;

        x += this.createLayerConfigButton(x, y, ButtonListenerLayerEdit.Type.MODE, layerRange);
        this.createLayerConfigButton(x, y, ButtonListenerLayerEdit.Type.AXIS, layerRange);
        y += 26;

        this.createTextFields(origX, y, 60, layerRange);
    }

    protected int createLayerConfigButton(int x, int y, ButtonListenerLayerEdit.Type type, LayerRange layerRange)
    {
        if (type == ButtonListenerLayerEdit.Type.MODE || layerRange.getLayerMode() != LayerMode.ALL)
        {
            GenericButton button = GenericButton.create(type.getDisplayName(layerRange));
            button.setActionListener(new ButtonListenerLayerEdit(type, layerRange, this));
            button.setPosition(x, y);
            this.addWidget(button);

            return button.getWidth() + 2;
        }

        return 0;
    }

    protected void createHotkeyCheckBoxes(int x, int y, LayerRange layerRange)
    {
        String label = "malilib.checkbox.render_layers.hotkey";
        String hover = "malilib.hover.checkbox.render_layers.hotkey";

        CheckBoxWidget cb = new CheckBoxWidget(label, hover, layerRange::getMoveLayerRangeMax, layerRange::setMoveLayerRangeMax);
        cb.setPosition(x, y + 4);
        this.addWidget(cb);

        y += 23;
        cb = new CheckBoxWidget(label, hover, layerRange::getMoveLayerRangeMin, layerRange::setMoveLayerRangeMin);
        cb.setSelected(layerRange.getMoveLayerRangeMin(), false);
        cb.setPosition(x, y + 4);
        this.addWidget(cb);
    }

    protected void createTextFields(int x, int y, int width, final LayerRange layerRange)
    {
        int origX = x;
        LayerMode layerMode = layerRange.getLayerMode();

        if (layerMode == LayerMode.ALL)
        {
            return;
        }

        if (layerMode == LayerMode.LAYER_RANGE)
        {
            LabelWidget label1 = new LabelWidget("malilib.label.render_layers_screen.layer_min");
            LabelWidget label2 = new LabelWidget("malilib.label.render_layers_screen.layer_max");
            label1.setPosition(x, y +  5);
            label2.setPosition(x, y + 28);
            this.addWidget(label1);
            this.addWidget(label2);
            int w1 = label1.getWidth();
            int w2 = label2.getWidth();

            x += Math.max(w1, w2) + 4;
        }
        else
        {
            LabelWidget label = new LabelWidget("malilib.label.render_layers_screen.layer");
            label.setPosition(x, y + 5);
            this.addWidget(label);
            x += label.getWidth() + 4;
        }

        Icon valueAdjustIcon = this.getValueAdjustButtonIcon();

        if (layerMode == LayerMode.LAYER_RANGE)
        {
            this.textField2 = new BaseTextFieldWidget(width, 20);
            this.textField2.setPosition(x, y);
            this.textField2.setTextValidator(BaseTextFieldWidget.VALIDATOR_INTEGER);
            this.textField2.setListener(new TextChangeListener(layerMode, layerRange, true));
            this.addWidget(this.textField2);

            if (this.addLayerRangeHotkeyCheckboxes)
            {
                this.createHotkeyCheckBoxes(x + width + 24, y, layerRange);
            }

            this.createValueAdjustButton(x + width + 3, y, true, layerRange, valueAdjustIcon);
            y += 23;
        }
        else
        {
            this.textField2 = null;
        }

        this.textField1 = new BaseTextFieldWidget(width, 20);
        this.textField1.setPosition(x, y);
        this.textField1.setTextValidator(BaseTextFieldWidget.VALIDATOR_INTEGER);
        this.textField1.setListener(new TextChangeListener(layerMode, layerRange, false));
        this.addWidget(this.textField1);
        this.createValueAdjustButton(x + width + 3, y, false, layerRange, valueAdjustIcon);
        y += 23;

        this.updateTextFieldValues(layerRange);

        this.createLayerConfigButton(x - 1, y, ButtonListenerLayerEdit.Type.SET_TO_PLAYER, layerRange);
        y += 22;

        if (this.addPlayerFollowingOptions)
        {
            String strLabel = "malilib.button.render_layers.follow_player";
            final OnOffButton button = new OnOffButton(-1, 20, OnOffButton.OnOffStyle.TEXT_ON_OFF, layerRange::shouldFollowPlayer, strLabel);
            button.translateAndAddHoverString("malilib.hover.button.render_layers.follow_player");
            button.setPosition(origX, y);
            button.setActionListener(layerRange::toggleShouldFollowPlayer);
            this.addWidget(button);
            y += 24;

            LabelWidget label = new LabelWidget("malilib.label.render_layers_screen.player_follow_offset");
            label.setPosition(origX, y + 5);
            this.addWidget(label);
            int w = label.getWidth();

            final IntegerTextFieldWidget textField = new IntegerTextFieldWidget(40, 18, layerRange.getPlayerFollowOffset());
            textField.setPosition(origX + w + 4, y);
            textField.setUpdateListenerAlways(true);
            textField.setListener(new IntegerTextFieldListener(layerRange::setPlayerFollowOffset));
            this.addWidget(textField);

            int bx = textField.getX() + textField.getWidth() + 3;
            GenericButton button2 = GenericButton.create(this.getValueAdjustButtonIcon());
            button2.setPosition(bx, y + 1);
            button2.setActionListener((btn) -> {
                int change = btn == 1 ? -1 : 1;
                if (BaseScreen.isShiftDown()) { change *= 2; }
                if (BaseScreen.isCtrlDown())  { change *= 4; }
                layerRange.setPlayerFollowOffset(layerRange.getPlayerFollowOffset() + change);
                this.initScreen();
                return true;
            });

            this.addWidget(button2);
        }
    }

    protected void updateTextFieldValues(LayerRange layerRange)
    {
        if (this.textField1 != null)
        {
            this.textField1.setText(String.valueOf(layerRange.getCurrentLayerValue(false)));
        }

        if (this.textField2 != null)
        {
            this.textField2.setText(String.valueOf(layerRange.getCurrentLayerValue(true)));
        }
    }

    protected void createValueAdjustButton(int x, int y, boolean isSecondValue, LayerRange layerRange, Icon icon)
    {
        LayerMode layerMode = layerRange.getLayerMode();
        ButtonListenerChangeValue listener = new ButtonListenerChangeValue(layerMode, layerRange, isSecondValue, this);
        GenericButton button = GenericButton.create(icon);
        button.setActionListener(listener);
        button.setCanScrollToClick(true);
        button.setPosition(x, y + 2);
        this.addWidget(button);
    }

    protected static class ButtonListenerLayerEdit implements Int2BooleanFunction
    {
        protected final BaseRenderLayerEditScreen parent;
        protected final LayerRange layerRange;
        protected final Type type;

        public ButtonListenerLayerEdit(Type type, LayerRange layerRange, BaseRenderLayerEditScreen parent)
        {
            this.type = type;
            this.layerRange = layerRange;
            this.parent = parent;
        }

        @Override
        public boolean apply(int mouseButton)
        {
            if (this.type == Type.MODE)
            {
                this.layerRange.setLayerMode(ListUtils.getNextEntry(LayerMode.VALUES, this.layerRange.getLayerMode(), mouseButton != 0));
            }
            else if (this.type == Type.AXIS)
            {
                EnumFacing.Axis axis = this.layerRange.getAxis();
                int next = mouseButton == 0 ? ((axis.ordinal() + 1) % 3) : (axis.ordinal() == 0 ? 2 : axis.ordinal() - 1);
                axis = EnumFacing.Axis.values()[next % 3];
                this.layerRange.setAxis(axis);
            }
            else if (this.type == Type.SET_TO_PLAYER && GameUtils.getClientPlayer() != null)
            {
                this.layerRange.setSingleBoundaryToPosition(GameUtils.getCameraEntity());
            }

            this.parent.initScreen();

            return true;
        }

        public enum Type
        {
            MODE            ("malilib.button.render_layers.layers"),
            AXIS            ("malilib.button.render_layers.axis"),
            SET_TO_PLAYER   ("malilib.button.render_layers.set_to_player");

            private final String translationKey;

            Type(String translationKey)
            {
                this.translationKey = translationKey;
            }

            public String getDisplayName(LayerRange layerRange)
            {
                if (this == SET_TO_PLAYER)
                {
                    return StringUtils.translate(this.translationKey);
                }
                else
                {
                    String valueStr = this == MODE ? layerRange.getLayerMode().getDisplayName() : layerRange.getAxis().name();
                    return StringUtils.translate(this.translationKey, valueStr);
                }
            }
        }
    }

    protected static class ButtonListenerChangeValue implements Int2BooleanFunction
    {
        protected final BaseRenderLayerEditScreen parent;
        protected final LayerRange layerRange;
        protected final LayerMode mode;
        protected final boolean isSecondLimit;

        protected ButtonListenerChangeValue(LayerMode mode, LayerRange layerRange, boolean isSecondLimit, BaseRenderLayerEditScreen parent)
        {
            this.mode = mode;
            this.layerRange = layerRange;
            this.isSecondLimit = isSecondLimit;
            this.parent = parent;
        }

        @Override
        public boolean apply(int mouseButton)
        {
            int change = mouseButton == 1 ? -1 : 1;

            if (BaseScreen.isShiftDown())
            {
                change *= 16;
            }

            if (BaseScreen.isCtrlDown())
            {
                change *= 64;
            }

            if (this.mode == LayerMode.LAYER_RANGE)
            {
                if (this.isSecondLimit)
                {
                    this.layerRange.setLayerRangeMax(this.layerRange.getLayerRangeMax() + change);
                }
                else
                {
                    this.layerRange.setLayerRangeMin(this.layerRange.getLayerRangeMin() + change);
                }
            }
            else
            {
                this.layerRange.moveLayer(change);
            }

            this.parent.updateTextFieldValues(this.layerRange);

            return true;
        }
    }

    protected static class TextChangeListener implements Consumer<String >
    {
        protected final LayerRange layerRange;
        protected final LayerMode mode;
        protected final boolean isSecondLimit;

        protected TextChangeListener(LayerMode mode, LayerRange layerRange, boolean isSecondLimit)
        {
            this.mode = mode;
            this.layerRange = layerRange;
            this.isSecondLimit = isSecondLimit;
        }

        @Override
        public void accept(String newText)
        {
            int value;

            try
            {
                value = Integer.parseInt(newText);
            }
            catch (NumberFormatException e)
            {
                return;
            }

            switch (this.mode)
            {
                case ALL_ABOVE:
                    this.layerRange.setLayerAbove(value);
                    break;

                case ALL_BELOW:
                    this.layerRange.setLayerBelow(value);
                    break;

                case SINGLE_LAYER:
                    this.layerRange.setLayerSingle(value);
                    break;

                case LAYER_RANGE:
                    if (this.isSecondLimit)
                    {
                        this.layerRange.setLayerRangeMax(value);
                    }
                    else
                    {
                        this.layerRange.setLayerRangeMin(value);
                    }
                    break;

                default:
            }
        }
    }
}
