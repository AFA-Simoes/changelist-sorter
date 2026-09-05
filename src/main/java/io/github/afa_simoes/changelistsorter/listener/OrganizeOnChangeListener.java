package io.github.afa_simoes.changelistsorter.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import io.github.afa_simoes.changelistsorter.organize.ChangelistOrganizerService;
import io.github.afa_simoes.changelistsorter.settings.ProjectSettings;

import java.util.Collection;

/**
 * Registered declaratively (see plugin.xml's {@code projectListeners}) rather than through an
 * eager {@code postStartupActivity}, so it is instantiated lazily on first use.
 *
 * <p>Dispatches to the organizer via {@code invokeLater} so that a confirmation dialog never
 * pops up modally from this VCS callback thread.
 */
public class OrganizeOnChangeListener implements ChangeListListener {
    private final Project project;

    public OrganizeOnChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void changesAdded(Collection<? extends Change> changes, ChangeList toList) {
        if (!ProjectSettings.storedSettings(project).isAutomaticallyOrganize()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(
                () -> ChangelistOrganizerService.getInstance(project).organize(),
                project.getDisposed()
        );
    }
}
