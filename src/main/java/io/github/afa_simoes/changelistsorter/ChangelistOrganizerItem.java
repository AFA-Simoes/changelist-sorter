package io.github.afa_simoes.changelistsorter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelistOrganizerItem implements Serializable {
    private boolean enabled;
    private String changeListName;
    private String filePattern;
    private boolean checkFullPath;
    private boolean confirmationDialog;

    /**
     * Deep-copies another item. Used to hand the settings UI (and persistence roundtrips) its
     * own objects, rather than references into whatever list the caller holds - so editing a
     * table cell can never reach back and mutate state it shouldn't.
     */
    public ChangelistOrganizerItem(ChangelistOrganizerItem other) {
        this(other.enabled, other.changeListName, other.filePattern, other.checkFullPath, other.confirmationDialog);
    }
}
