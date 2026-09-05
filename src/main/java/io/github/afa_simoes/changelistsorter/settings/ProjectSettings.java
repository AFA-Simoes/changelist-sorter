package io.github.afa_simoes.changelistsorter.settings;

import com.intellij.openapi.project.Project;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectSettings implements Serializable {
    private List<ChangelistOrganizerItem> changelistOrganizerItems = new ArrayList<>();
    private boolean onlyApplyItemsOnDefaultChangelist = true;
    private boolean stopApplyingItemsAfterFirstMatch = true;
    private boolean removeEmptyChangelists = false;
    private boolean automaticallyOrganize = false;

    public static ProjectSettings storedSettings(Project project) {
        ProjectSettingsService projectSettingsService = project.getService(ProjectSettingsService.class);
        ProjectSettings state = projectSettingsService != null ? projectSettingsService.getState() : null;

        return state != null ? state : new ProjectSettings();
    }
}
