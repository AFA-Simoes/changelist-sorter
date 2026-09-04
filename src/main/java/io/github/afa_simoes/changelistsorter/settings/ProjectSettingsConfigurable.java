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
    private ProjectSettingsPanel projectSettingsPane;
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
        if (projectSettingsPane == null) {
            projectSettingsPane = new ProjectSettingsPanel();
        }

        return projectSettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        if (project == null) {
            return false;
        }

        return projectSettingsPane != null && projectSettingsPane.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        projectSettingsPane.storeSettings(ProjectSettings.storedSettings(project));
    }

    @Override
    public void reset() {
        projectSettingsPane.setData(ProjectSettings.storedSettings(project));
    }

    @Override
    public void disposeUIResources() {
        if (projectSettingsPane != null) {
            Disposer.dispose(projectSettingsPane);
            this.projectSettingsPane = null;
        }
    }
}
