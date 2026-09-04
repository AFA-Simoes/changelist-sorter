package io.github.afa_simoes.changelistsorter.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerBundle;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerIcons;
import io.github.afa_simoes.changelistsorter.organize.ChangelistOrganizerService;

public class OrganizeChangesAction extends AnAction {
    public OrganizeChangesAction() {
        super(ChangelistOrganizerBundle.message("action.toolbar.text"), ChangelistOrganizerBundle.message("action.toolbar.description"), ChangelistOrganizerIcons.get("icon_16x16.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ChangelistOrganizerService.getInstance(anActionEvent.getRequiredData(CommonDataKeys.PROJECT)).organize();
    }
}
