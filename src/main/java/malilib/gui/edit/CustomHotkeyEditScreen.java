package malilib.gui.edit;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;

import malilib.action.NamedAction;
import malilib.action.ParameterizableNamedAction;
import malilib.gui.BaseScreen;
import malilib.gui.SettingsExportImportScreen;
import malilib.gui.action.BaseActionListScreen;
import malilib.gui.widget.LabelWidget;
import malilib.gui.widget.button.GenericButton;
import malilib.gui.widget.list.DataListWidget;
import malilib.gui.widget.list.entry.DataListEntryWidgetData;
import malilib.gui.widget.list.entry.action.ActionListBaseActionEntryWidget;
import malilib.gui.widget.list.entry.action.ParameterizableActionEntryWidget;
import malilib.input.CustomHotkeyDefinition;
import malilib.input.CustomHotkeyManager;
import malilib.overlay.message.MessageDispatcher;
import malilib.util.data.json.JsonUtils;

public class CustomHotkeyEditScreen extends BaseActionListScreen
{
    protected final ImmutableList<NamedAction> originalActionsList;
    protected final List<NamedAction> currentActionsList;
    protected final CustomHotkeyDefinition hotkey;
    protected final GenericButton addActionsButton;
    protected final GenericButton exportImportButton;
    protected final LabelWidget actionsLabelWidget;

    public CustomHotkeyEditScreen(CustomHotkeyDefinition hotkey)
    {
        super("", Collections.emptyList(), null);

        this.hotkey = hotkey;
        this.originalActionsList = hotkey.getActionList();
        this.setTitle("malilib.title.screen.custom_hotkey_edit");

        this.actionsLabelWidget = new LabelWidget(0xFFF0F0F0, "malilib.label.custom_hotkeys.edit.bound_actions");

        this.addActionsButton = GenericButton.create(15, "malilib.button.custom_hotkey_edit_screen.add_actions", this::addSelectedActions);
        this.addActionsButton.translateAndAddHoverString("malilib.hover.custom_hotkey_edit_screen.add_actions");
        this.addActionsButton.setEnabledStatusSupplier(this::canAddActions);

        this.exportImportButton = GenericButton.create(15, "malilib.button.export_slash_import", this::openExportImportScreen);

        this.leftSideListWidget.setDataListEntryWidgetFactory(this::createLeftSideActionEntryWidget);
        this.rightSideListWidget = this.createRightSideActionListWidget();

        // fetch the backing list reference from the list widget
        this.currentActionsList = this.rightSideListWidget.getNonFilteredDataList();
        this.currentActionsList.addAll(this.originalActionsList);
    }

    @Override
    protected void addActionListScreenWidgets()
    {
        super.addActionListScreenWidgets();

        this.addWidget(this.actionsLabelWidget);
        this.addWidget(this.addActionsButton);
        this.addWidget(this.exportImportButton);
        this.addListWidget(this.rightSideListWidget);
    }

    @Override
    protected void updateActionListScreenWidgetPositions(int x, int y, int w)
    {
        super.updateActionListScreenWidgetPositions(x, y, w);

        this.addActionsButton.setY(y);
        this.addActionsButton.setRight(this.leftSideListWidget.getRight());

        x = this.leftSideListWidget.getRight() + this.centerGap;
        y = this.leftSideListWidget.getY();
        int h = this.screenHeight - y - 6;

        this.rightSideListWidget.setPositionAndSize(x, y, w, h);
        this.actionsLabelWidget.setPosition(x + 2, y - 10);
        this.exportImportButton.setY(y - 16);
        this.exportImportButton.setRight(this.rightSideListWidget.getRight());
    }

    @Override
    protected void saveChangesOnScreenClose()
    {
        if (this.originalActionsList.equals(this.currentActionsList) == false)
        {
            this.hotkey.setActionList(ImmutableList.copyOf(this.currentActionsList));
            CustomHotkeyManager.INSTANCE.saveToFile();
        }
    }

    protected boolean canAddActions()
    {
        return this.leftSideListWidget.getEntrySelectionHandler().getSelectedEntryCount() > 0;
    }

    protected void addSelectedActions()
    {
        Collection<NamedAction> selectedActions = this.leftSideListWidget.getSelectedEntries();

        if (selectedActions.isEmpty() == false)
        {
            this.currentActionsList.addAll(selectedActions);
            this.rightSideListWidget.refreshEntries();
        }
    }

    protected boolean addAction(NamedAction action)
    {
        this.currentActionsList.add(action);
        this.rightSideListWidget.refreshEntries();
        return true;
    }

    protected void openExportImportScreen()
    {
        String title = "malilib.title.screen.custom_hotkey_edit.export_import";
        this.hotkey.setActionList(ImmutableList.copyOf(this.currentActionsList));
        String settingsStr = JsonUtils.jsonToString(this.hotkey.toJson(), true);
        SettingsExportImportScreen screen = new SettingsExportImportScreen(title, settingsStr, this::importOverwrite);
        screen.setAppendStringConsumer(this::importAppend);
        screen.setRadioWidgetHoverText("malilib.hover.custom_hotkey_export_import_screen.append_overwrite");
        screen.setParent(this);
        BaseScreen.openPopupScreen(screen);
    }

    protected boolean importOverwrite(String settingsStr)
    {
        return this.importHotkeySettings(settingsStr, true);
    }

    protected boolean importAppend(String settingsStr)
    {
        return this.importHotkeySettings(settingsStr, false);
    }

    protected boolean importHotkeySettings(String settingsStr, boolean overwrite)
    {
        CustomHotkeyDefinition hotkey = this.readHotkeyFromSettings(settingsStr);

        if (hotkey == null)
        {
            MessageDispatcher.error("malilib.message.error.custom_hotkey_import_from_string_failed");
            return false;
        }

        if (overwrite)
        {
            this.currentActionsList.clear();
        }

        this.currentActionsList.addAll(hotkey.getActionList());
        this.rightSideListWidget.refreshEntries();

        return true;
    }

    @Nullable
    protected CustomHotkeyDefinition readHotkeyFromSettings(String settingsStr)
    {
        JsonElement el = JsonUtils.parseJsonFromString(settingsStr);

        if (el != null && el.isJsonObject())
        {
            return CustomHotkeyDefinition.fromJson(el.getAsJsonObject());
        }

        return null;
    }

    protected ActionListBaseActionEntryWidget
    createLeftSideActionEntryWidget(NamedAction data, DataListEntryWidgetData constructData)
    {
        ActionListBaseActionEntryWidget widget;

        if (data instanceof ParameterizableNamedAction)
        {
            ParameterizableActionEntryWidget parWidget = new ParameterizableActionEntryWidget(data, constructData);
            parWidget.setParameterizedActionConsumer(this::addAction);
            parWidget.setParameterizationButtonHoverText("malilib.hover.button.parameterize_action_for_hotkey");
            parWidget.setNoRemoveButtons();
            widget = parWidget;
        }
        else
        {
            widget = new ActionListBaseActionEntryWidget(data, constructData);
        }

        return widget;
    }

    @Override
    protected DataListWidget<NamedAction> createRightSideActionListWidget()
    {
        DataListWidget<NamedAction> listWidget = this.createBaseActionListWidget(Collections::emptyList, false);

        listWidget.setDataListEntryWidgetFactory(this::createRightSideEntryWidget);

        return listWidget;
    }

    protected ActionListBaseActionEntryWidget createRightSideEntryWidget(NamedAction data,
                                                                         DataListEntryWidgetData constructData)
    {
        ActionListBaseActionEntryWidget widget = new ActionListBaseActionEntryWidget(data, constructData);

        widget.setCanReOrder(true);
        widget.setActionRemoveFunction(this::removeAction);

        return widget;
    }

    protected void removeAction(int originalListIndex, NamedAction action)
    {
        if (originalListIndex >= 0 && originalListIndex < this.currentActionsList.size())
        {
            this.currentActionsList.remove(originalListIndex);
            this.rightSideListWidget.refreshEntries();
        }
    }
}
