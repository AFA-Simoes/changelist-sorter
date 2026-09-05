package io.github.afa_simoes.changelistsorter.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerIcons;
import io.github.afa_simoes.changelistsorter.organize.ChangelistOrganizerService;
import org.jetbrains.annotations.NotNull;

/**
 * Registered twice in plugin.xml, under the two original action IDs (toolbar button and
 * changes-view context menu), so existing keymap customisations survive. Text/description come
 * from the plugin's resource bundle via the per-ID "action.&lt;id&gt;.text" / ".description"
 * key convention, rather than being hardcoded in the constructor.
 */
public class OrganizeChangesAction extends AnAction {
    public OrganizeChangesAction() {
        super(ChangelistOrganizerIcons.get("icon_16x16.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        if (project != null) {
            ChangelistOrganizerService.getInstance(project).organize();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(event.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
