package io.github.afa_simoes.changelistsorter.organize;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerBundle;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerIcons;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import io.github.afa_simoes.changelistsorter.settings.ProjectSettings;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project-level service that runs the "organize" workflow: plan every move first (pure, no
 * platform mutation), confirm once if any planned move asks for it, then apply the whole batch.
 *
 * <p>Registered as a {@code projectService}, so actions and the change-list listener both reach
 * it via {@link Project#getService(Class)} / {@link #getInstance(Project)} rather than through a
 * static entry point.
 */
public final class ChangelistOrganizerService {
    private final Project project;

    /**
     * Guards against reentrancy: applying a move fires {@code changesAdded} on the change-list
     * listener, which - when auto-organize is on - re-enters {@link #organize()} through the very
     * run that triggered it. A plain {@code synchronized} would not help here, since the listener
     * calls back in on the same thread and {@code synchronized} is reentrant.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ChangelistOrganizerService(Project project) {
        this.project = project;
    }

    public static ChangelistOrganizerService getInstance(Project project) {
        return project.getService(ChangelistOrganizerService.class);
    }

    public void organize() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            ProjectSettings settings = ProjectSettings.storedSettings(project);
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);

            OrganizePlan plan = plan(settings, changeListManager);

            if (plan.isEmpty()) {
                return;
            }

            if (plan.anyRequiresConfirmation() && !confirm(plan)) {
                return;
            }

            apply(plan, changeListManager, settings.isRemoveEmptyChangelists());
        } finally {
            running.set(false);
        }
    }

    // Package-private rather than private so ChangelistOrganizerServiceTest can build a plan
    // directly from injected settings, without needing to mock the static
    // ProjectSettings.storedSettings(Project) lookup that organize() otherwise goes through.
    OrganizePlan plan(ProjectSettings settings, ChangeListManager changeListManager) {
        OrganizePlan plan = new OrganizePlan();
        RuleMatcher ruleMatcher = new RuleMatcher(settings.getChangelistOrganizerItems());

        for (VirtualFile file : changeListManager.getAffectedFiles()) {
            LocalChangeList currentChangeList = changeListManager.getChangeList(file);

            // ChangeListManager.getChangeList(VirtualFile) is @Nullable: a file can be affected
            // without (yet) belonging to a known change list.
            if (currentChangeList == null) {
                continue;
            }

            if (settings.isOnlyApplyItemsOnDefaultChangelist() && !currentChangeList.isDefault()) {
                continue;
            }

            List<ChangelistOrganizerItem> matches = ruleMatcher.match(
                    file.getName(),
                    relativePath(file),
                    settings.isStopApplyingItemsAfterFirstMatch()
            );

            for (ChangelistOrganizerItem item : matches) {
                if (currentChangeList.getName().equals(item.getChangeListName())) {
                    continue;
                }

                plan.addMove(file, item.getChangeListName(), item.isConfirmationDialog());
            }
        }

        return plan;
    }

    private String relativePath(VirtualFile file) {
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        String relativePath = projectDir != null ? VfsUtilCore.getRelativePath(file, projectDir, '/') : null;

        return relativePath != null ? relativePath : file.getPath();
    }

    private boolean confirm(OrganizePlan plan) {
        StringBuilder message = new StringBuilder();

        for (OrganizePlan.Move move : plan.getMoves()) {
            if (move.requiresConfirmation()) {
                message
                        .append(ChangelistOrganizerBundle.message(
                                "organize.confirmation.dialog.message",
                                move.file().getName(),
                                move.targetChangeListName()
                        ))
                        .append('\n');
            }
        }

        boolean[] confirmed = new boolean[1];

        ApplicationManager.getApplication().invokeAndWait(() -> confirmed[0] = Messages.showOkCancelDialog(
                project,
                message.toString().trim(),
                ChangelistOrganizerBundle.message("organize.confirmation.dialog.title"),
                Messages.getOkButton(),
                Messages.getCancelButton(),
                ChangelistOrganizerIcons.ORGANIZE_LARGE
        ) == Messages.OK);

        return confirmed[0];
    }

    private void apply(OrganizePlan plan, ChangeListManager changeListManager, boolean removeEmptyChangelists) {
        for (OrganizePlan.Move move : plan.getMoves()) {
            LocalChangeList targetChangeList = changeListManager.addChangeList(
                    move.targetChangeListName(),
                    ChangelistOrganizerBundle.message("organize.changelist.comment")
            );

            changeListManager.moveChangesTo(
                    targetChangeList,
                    ArrayUtil.toObjectArray(changeListManager.getChangesIn(move.file()), Change.class)
            );
        }

        if (removeEmptyChangelists) {
            for (LocalChangeList changeList : changeListManager.getChangeLists()) {
                if (!changeList.isDefault() && changeList.getChanges().isEmpty()) {
                    changeListManager.removeChangeList(changeList);
                }
            }
        }
    }
}
