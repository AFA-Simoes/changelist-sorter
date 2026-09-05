package io.github.afa_simoes.changelistsorter.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class ProjectSettingsConfigurable implements Configurable {
    private ProjectSettingsPanel projectSettingsPanel;
    private final Project project;

    public ProjectSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return ChangelistOrganizerBundle.message("settings.display.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (projectSettingsPanel == null) {
            projectSettingsPanel = new ProjectSettingsPanel();
        }

        return projectSettingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        if (project == null || projectSettingsPanel == null) {
            return false;
        }

        return !projectSettingsPanel.getState().equals(ProjectSettings.storedSettings(project));
    }

    @Override
    public void apply() throws ConfigurationException {
        ProjectSettingsService projectSettingsService = project.getService(ProjectSettingsService.class);

        if (projectSettingsService != null) {
            projectSettingsService.loadState(projectSettingsPanel.getState());
        }
    }

    @Override
    public void reset() {
        projectSettingsPanel.setState(ProjectSettings.storedSettings(project));
    }

    @Override
    public void disposeUIResources() {
        if (projectSettingsPanel != null) {
            Disposer.dispose(projectSettingsPanel);
            this.projectSettingsPanel = null;
        }
    }
}
