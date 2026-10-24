package io.github.afa_simoes.changelistsorter.settings;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerBundle;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings UI for the project's organizer rules: a reorderable table of
 * {@link ChangelistOrganizerItem} rows plus a handful of behaviour checkboxes.
 *
 * <p>Replaces the old GUI-Designer-bound pane (deleted {@code ProjectSettingsPane.form}) with
 * platform components ({@link ToolbarDecorator} / {@link TableView}), which also gets rid of a
 * {@code getColumnClass} that threw on an empty table.
 */
public class ProjectSettingsPanel implements Disposable {
    private static final ColumnInfo<ChangelistOrganizerItem, Boolean> ENABLED_COLUMN =
            new ColumnInfo<>(ChangelistOrganizerBundle.message("settings.table.column.enabled")) {
                @Override
                public Boolean valueOf(ChangelistOrganizerItem item) {
                    return item.isEnabled();
                }

                @Override
                public boolean isCellEditable(ChangelistOrganizerItem item) {
                    return true;
                }

                @Override
                public void setValue(ChangelistOrganizerItem item, Boolean value) {
                    item.setEnabled(value);
                }

                @Override
                public Class<?> getColumnClass() {
                    return Boolean.class;
                }
            };

    private static final ColumnInfo<ChangelistOrganizerItem, String> CHANGE_LIST_NAME_COLUMN =
            new ColumnInfo<>(ChangelistOrganizerBundle.message("settings.table.column.changelist.name")) {
                @Override
                public String valueOf(ChangelistOrganizerItem item) {
                    return item.getChangeListName() != null ? item.getChangeListName() : "";
                }

                @Override
                public boolean isCellEditable(ChangelistOrganizerItem item) {
                    return true;
                }

                @Override
                public void setValue(ChangelistOrganizerItem item, String value) {
                    item.setChangeListName(value);
                }
            };

    // A pattern containing '/' can never match when checkFullPath is off: a bare file name never
    // contains a path separator, so the rule would silently never fire. Flag it in the table
    // instead of leaving people to discover it by confusion.
    private static final TableCellRenderer PATTERN_NEEDS_FULL_PATH_WARNING_RENDERER = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(AllIcons.General.Warning);
            label.setToolTipText(ChangelistOrganizerBundle.message("settings.table.warning.pattern.needs.full.path"));
            return label;
        }
    };

    private static final ColumnInfo<ChangelistOrganizerItem, String> FILE_PATTERN_COLUMN =
            new ColumnInfo<>(ChangelistOrganizerBundle.message("settings.table.column.file.pattern")) {
                @Override
                public String valueOf(ChangelistOrganizerItem item) {
                    return item.getFilePattern() != null ? item.getFilePattern() : "";
                }

                @Override
                public boolean isCellEditable(ChangelistOrganizerItem item) {
                    return true;
                }

                @Override
                public void setValue(ChangelistOrganizerItem item, String value) {
                    item.setFilePattern(value);
                }

                @Override
                public TableCellRenderer getRenderer(ChangelistOrganizerItem item) {
                    return item != null && patternCanNeverMatch(item) ? PATTERN_NEEDS_FULL_PATH_WARNING_RENDERER : null;
                }
            };

    private static final ColumnInfo<ChangelistOrganizerItem, Boolean> CHECK_FULL_PATH_COLUMN =
            new ColumnInfo<>(ChangelistOrganizerBundle.message("settings.table.column.check.full.path")) {
                @Override
                public Boolean valueOf(ChangelistOrganizerItem item) {
                    return item.isCheckFullPath();
                }

                @Override
                public boolean isCellEditable(ChangelistOrganizerItem item) {
                    return true;
                }

                @Override
                public void setValue(ChangelistOrganizerItem item, Boolean value) {
                    item.setCheckFullPath(value);
                }

                @Override
                public Class<?> getColumnClass() {
                    return Boolean.class;
                }
            };

    private static final ColumnInfo<ChangelistOrganizerItem, Boolean> CONFIRMATION_DIALOG_COLUMN =
            new ColumnInfo<>(ChangelistOrganizerBundle.message("settings.table.column.confirmation.dialog")) {
                @Override
                public Boolean valueOf(ChangelistOrganizerItem item) {
                    return item.isConfirmationDialog();
                }

                @Override
                public boolean isCellEditable(ChangelistOrganizerItem item) {
                    return true;
                }

                @Override
                public void setValue(ChangelistOrganizerItem item, Boolean value) {
                    item.setConfirmationDialog(value);
                }

                @Override
                public Class<?> getColumnClass() {
                    return Boolean.class;
                }
            };

    @SuppressWarnings("unchecked")
    private static final ColumnInfo<ChangelistOrganizerItem, ?>[] COLUMNS = new ColumnInfo[] {
            ENABLED_COLUMN, CHANGE_LIST_NAME_COLUMN, FILE_PATTERN_COLUMN, CHECK_FULL_PATH_COLUMN, CONFIRMATION_DIALOG_COLUMN
    };

    private final ListTableModel<ChangelistOrganizerItem> tableModel = new ListTableModel<>(COLUMNS);
    private final TableView<ChangelistOrganizerItem> table = new TableView<>(tableModel);

    private final JBCheckBox onlyApplyItemsOnDefaultChangelistCheckBox =
            new JBCheckBox(ChangelistOrganizerBundle.message("settings.only.apply.on.default.changelist"));
    private final JBCheckBox stopApplyingItemsAfterFirstMatchCheckBox =
            new JBCheckBox(ChangelistOrganizerBundle.message("settings.stop.after.first.match"));
    private final JBCheckBox removeEmptyChangelistsCheckBox =
            new JBCheckBox(ChangelistOrganizerBundle.message("settings.remove.empty.changelists"));
    private final JBCheckBox automaticallyOrganizeCheckBox =
            new JBCheckBox(ChangelistOrganizerBundle.message("settings.automatically.organize"));

    private final JPanel panel;

    public ProjectSettingsPanel() {
        JPanel tablePanel = ToolbarDecorator.createDecorator(table)
                .setAddAction(button -> {
                    if (!tableAlreadyContainsEmptyItem()) {
                        tableModel.addRow(new ChangelistOrganizerItem());
                    }
                })
                .setRemoveAction(button -> {
                    int selectedRow = table.getSelectedRow();

                    if (selectedRow != -1) {
                        tableModel.removeRow(selectedRow);
                    }
                })
                .setMoveUpAction(button -> {
                    int selectedRow = table.getSelectedRow();

                    if (selectedRow > 0) {
                        tableModel.exchangeRows(selectedRow, selectedRow - 1);
                        table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
                    }
                })
                .setMoveDownAction(button -> {
                    int selectedRow = table.getSelectedRow();

                    if (selectedRow != -1 && selectedRow < tableModel.getRowCount() - 1) {
                        tableModel.exchangeRows(selectedRow, selectedRow + 1);
                        table.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
                    }
                })
                .addExtraAction(new AnAction(
                        ChangelistOrganizerBundle.message("settings.table.button.duplicate.text"),
                        ChangelistOrganizerBundle.message("settings.table.button.duplicate.description"),
                        AllIcons.Actions.Copy
                ) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent event) {
                        int selectedRow = table.getSelectedRow();

                        if (selectedRow == -1) {
                            return;
                        }

                        List<ChangelistOrganizerItem> items = new ArrayList<>(tableModel.getItems());
                        items.add(selectedRow + 1, new ChangelistOrganizerItem(items.get(selectedRow)));
                        tableModel.setItems(items);
                        table.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
                    }

                    @Override
                    public void update(@NotNull AnActionEvent event) {
                        event.getPresentation().setEnabled(table.getSelectedRow() != -1);
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                })
                .createPanel();

        panel = FormBuilder.createFormBuilder()
                .addComponentFillVertically(tablePanel, 0)
                .addComponent(onlyApplyItemsOnDefaultChangelistCheckBox)
                .addComponent(stopApplyingItemsAfterFirstMatchCheckBox)
                .addComponent(removeEmptyChangelistsCheckBox)
                .addComponent(automaticallyOrganizeCheckBox)
                .getPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    /**
     * The settings currently shown in the UI, as a fresh, independent snapshot - never the same
     * {@link ChangelistOrganizerItem} instances backing the table, so callers can't reach back in
     * and mutate what the user is still editing.
     */
    public ProjectSettings getState() {
        ProjectSettings settings = new ProjectSettings();

        List<ChangelistOrganizerItem> items = new ArrayList<>();

        for (ChangelistOrganizerItem item : tableModel.getItems()) {
            if (item.getChangeListName() != null && !item.getChangeListName().trim().isEmpty()) {
                items.add(new ChangelistOrganizerItem(item));
            }
        }

        settings.setChangelistOrganizerItems(items);
        settings.setOnlyApplyItemsOnDefaultChangelist(onlyApplyItemsOnDefaultChangelistCheckBox.isSelected());
        settings.setStopApplyingItemsAfterFirstMatch(stopApplyingItemsAfterFirstMatchCheckBox.isSelected());
        settings.setRemoveEmptyChangelists(removeEmptyChangelistsCheckBox.isSelected());
        settings.setAutomaticallyOrganize(automaticallyOrganizeCheckBox.isSelected());

        return settings;
    }

    /**
     * Loads a deep copy of the given settings into the UI, so further edits in the table can
     * never write through to the caller's (typically persisted) objects.
     */
    public void setState(ProjectSettings settings) {
        List<ChangelistOrganizerItem> items = new ArrayList<>();

        for (ChangelistOrganizerItem item : settings.getChangelistOrganizerItems()) {
            items.add(new ChangelistOrganizerItem(item));
        }

        tableModel.setItems(items);

        onlyApplyItemsOnDefaultChangelistCheckBox.setSelected(settings.isOnlyApplyItemsOnDefaultChangelist());
        stopApplyingItemsAfterFirstMatchCheckBox.setSelected(settings.isStopApplyingItemsAfterFirstMatch());
        removeEmptyChangelistsCheckBox.setSelected(settings.isRemoveEmptyChangelists());
        automaticallyOrganizeCheckBox.setSelected(settings.isAutomaticallyOrganize());
    }

    @Override
    public void dispose() {
    }

    private static boolean patternCanNeverMatch(ChangelistOrganizerItem item) {
        String pattern = item.getFilePattern();
        return pattern != null && pattern.contains("/") && !item.isCheckFullPath();
    }

    private boolean tableAlreadyContainsEmptyItem() {
        for (ChangelistOrganizerItem item : tableModel.getItems()) {
            if (!item.isEnabled()
                    && (item.getChangeListName() == null || item.getChangeListName().trim().isEmpty())
                    && (item.getFilePattern() == null || item.getFilePattern().trim().isEmpty())
                    && !item.isCheckFullPath()
                    && !item.isConfirmationDialog()) {
                return true;
            }
        }

        return false;
    }
}
