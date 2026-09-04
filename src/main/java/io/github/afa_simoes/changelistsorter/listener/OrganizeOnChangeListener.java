package io.github.afa_simoes.changelistsorter.listener;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import io.github.afa_simoes.changelistsorter.organize.ChangelistOrganizerService;
import io.github.afa_simoes.changelistsorter.settings.ProjectSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class OrganizeOnChangeListener implements StartupActivity, DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        ChangeListManager.getInstance(project).addChangeListListener(new ChangeListListener() {
            @Override
            public void changesAdded(Collection<Change> changes, ChangeList toList) {
                if (ProjectSettings.storedSettings(project).isAutomaticallyOrganize()) {
                    ChangelistOrganizerService.organize(project);
                }
            }
        });
    }
}
