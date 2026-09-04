package io.github.afa_simoes.changelistsorter.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizer;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerIcons;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerStrings;

public class ChangesViewToolbarAction extends AnAction {
    public ChangesViewToolbarAction() {
        super(ChangelistOrganizerStrings.message("action.toolbar.text"), ChangelistOrganizerStrings.message("action.toolbar.description"), ChangelistOrganizerIcons.get("icon_16x16.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ChangelistOrganizer.organize(anActionEvent.getRequiredData(CommonDataKeys.PROJECT));
    }
}
