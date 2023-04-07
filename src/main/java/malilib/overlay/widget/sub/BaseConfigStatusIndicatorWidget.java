package malilib.overlay.widget.sub;

import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import malilib.MaLiLib;
import malilib.MaLiLibReference;
import malilib.config.option.ConfigInfo;
import malilib.gui.BaseScreen;
import malilib.gui.config.BaseConfigTab;
import malilib.gui.config.ConfigTab;
import malilib.gui.config.indicator.BaseConfigStatusIndicatorEditScreen;
import malilib.gui.config.indicator.ConfigStatusWidgetFactory;
import malilib.gui.util.ScreenContext;
import malilib.overlay.widget.BaseOverlayWidget;
import malilib.registry.Registry;
import malilib.render.text.StyledTextLine;
import malilib.util.data.ConfigOnTab;
import malilib.util.data.json.JsonUtils;

public abstract class BaseConfigStatusIndicatorWidget<C extends ConfigInfo> extends BaseOverlayWidget
{
    protected final C config;
    protected final ConfigOnTab configOnTab;
    protected final String widgetTypeId;
    protected String name = "?";
    protected StyledTextLine styledName;
    @Nullable protected StyledTextLine valueDisplayText;
    protected boolean nameShadow = true;
    protected boolean valueShadow = true;
    protected int nameColor = 0xFFFFFFFF;
    protected int valueColor = 0xFF00FFFF;
    protected int valueRenderWidth;

    public BaseConfigStatusIndicatorWidget(C config, ConfigOnTab configOnTab, String widgetTypeId)
    {
        super();

        this.config = config;
        this.configOnTab = configOnTab;
        this.widgetTypeId = widgetTypeId;

        this.setHeight(this.getLineHeight());
        this.setName(config.getDisplayName());
    }

    @Override
    public String getWidgetTypeId()
    {
        return this.widgetTypeId;
    }

    public ConfigOnTab getConfigOnTab()
    {
        return this.configOnTab;
    }

    public StyledTextLine getStyledName()
    {
        return this.styledName;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
        this.styledName = StyledTextLine.parseFirstLine(name);
        this.geometryResizeNotifier.notifyContainerOfChanges();
    }

    public int getNameColor()
    {
        return this.nameColor;
    }

    public void setNameColor(int nameColor)
    {
        this.nameColor = nameColor;
    }

    public int getValueColor()
    {
        return this.valueColor;
    }

    public void setValueColor(int valueColor)
    {
        this.valueColor = valueColor;
    }

    public boolean getUseNameShadow()
    {
        return this.nameShadow;
    }

    public void setUseNameShadow(boolean nameShadow)
    {
        this.nameShadow = nameShadow;
    }

    public boolean getUseValueShadow()
    {
        return this.valueShadow;
    }

    public void setUseValueShadow(boolean valueShadow)
    {
        this.valueShadow = valueShadow;
    }

    public int getLabelRenderWidth()
    {
        return this.styledName.renderWidth;
    }

    public int getValueRenderWidth()
    {
        return this.valueRenderWidth;
    }

    public void openEditScreen()
    {
        BaseScreen.openScreenWithParent(new BaseConfigStatusIndicatorEditScreen<>(this));
    }

    public abstract void updateState(boolean force);

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        int usableHeight = this.getHeight() - this.padding.getVerticalTotal();
        int ty = this.getTextPositionY(y, usableHeight, this.getLineHeight());

        this.renderNameText(x, ty, z, ctx);
        this.renderValueDisplayText(x, ty, z, ctx);
    }

    protected void renderNameText(int x, int textY, float z, ScreenContext ctx)
    {
        this.renderTextLine(x, textY, z, this.nameColor, this.nameShadow, this.styledName, ctx);
    }

    protected void renderValueDisplayText(int x, int textY, float z, ScreenContext ctx)
    {
        if (this.valueDisplayText != null)
        {
            this.renderTextLine(x + this.getWidth() - this.valueDisplayText.renderWidth, textY, z,
                                this.valueColor, this.valueShadow, this.valueDisplayText, ctx);
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = super.toJson();

        obj.addProperty("config_path", this.configOnTab.getConfigPath());
        obj.addProperty("name", this.name);
        obj.addProperty("name_color", this.nameColor);
        obj.addProperty("name_shadow", this.nameShadow);
        obj.addProperty("value_color", this.valueColor);
        obj.addProperty("value_shadow", this.valueShadow);

        return obj;
    }

    @Override
    public void fromJson(JsonObject obj)
    {
        super.fromJson(obj);

        if (JsonUtils.hasString(obj, "name"))
        {
            this.setName(obj.get("name").getAsString());
        }

        this.nameColor = JsonUtils.getIntegerOrDefault(obj, "name_color", 0xFFFFFFFF);
        this.valueColor = JsonUtils.getIntegerOrDefault(obj, "value_color", 0xFF00FFFF);
        this.nameShadow = JsonUtils.getBooleanOrDefault(obj, "name_shadow", true);
        this.valueShadow = JsonUtils.getBooleanOrDefault(obj, "value_shadow", true);
    }

    @Nullable
    public static <C extends  ConfigInfo> BaseConfigStatusIndicatorWidget<?> fromJson(JsonElement el, Map<String, ConfigOnTab> configMap)
    {
        if (el.isJsonObject() == false)
        {
            return null;
        }

        JsonObject obj = el.getAsJsonObject();

        if (JsonUtils.hasString(obj, "type") &&
            JsonUtils.hasString(obj, "config_path"))
        {
            String configPath = obj.get("config_path").getAsString();
            ConfigOnTab configOnTab = configMap.get(configPath);

            if (configOnTab != null)
            {
                String type = obj.get("type").getAsString();

                try
                {
                    @SuppressWarnings("unchecked")
                    ConfigStatusWidgetFactory<C> factory = (ConfigStatusWidgetFactory<C>) Registry.CONFIG_STATUS_WIDGET.getConfigStatusWidgetFactory(type);

                    if (factory != null)
                    {
                        @SuppressWarnings("unchecked")
                        BaseConfigStatusIndicatorWidget<?> widget = factory.create((C) configOnTab.getConfig(), configOnTab);
                        widget.fromJson(obj);
                        widget.updateState(true);
                        return widget;
                    }
                }
                catch (Exception e)
                {
                    MaLiLib.LOGGER.error("Failed to create a config status indicator widget of type '{}' for config '{}'",
                                         type, configPath, e);
                }
            }
            else
            {
                ConfigTab dummyTab = new BaseConfigTab(MaLiLibReference.MOD_INFO, "?", -1, ImmutableList.of(PlaceholderConfigStatusIndicatorWidget.DUMMY_CONFIG), () -> null);
                configOnTab = new ConfigOnTab(dummyTab, PlaceholderConfigStatusIndicatorWidget.DUMMY_CONFIG);
                BaseConfigStatusIndicatorWidget<?> widget = new PlaceholderConfigStatusIndicatorWidget(configOnTab.getConfig(), configOnTab);
                widget.fromJson(obj);
                return widget;
            }
        }

        return null;
    }
}
