package io.github.afa_simoes.changelistsorter.organize;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import io.github.afa_simoes.changelistsorter.settings.ProjectSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangelistOrganizerServiceTest {
    @Mock
    private Project project;

    @Mock
    private ChangeListManager changeListManager;

    @Test
    void matchingFileIsPlannedToMoveToItsTargetChangelist() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");
        LocalChangeList currentList = changeList("default", true);

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(currentList);

        OrganizePlan plan = plan(settings(item("images", "*.jpg")));

        assertEquals(1, plan.getMoves().size());
        assertEquals("images", plan.getMoves().get(0).targetChangeListName());
        assertEquals(file, plan.getMoves().get(0).file());
    }

    @Test
    void fileAlreadyInTheTargetChangelistIsNotPlanned() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");
        LocalChangeList currentList = changeList("images", true);

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(currentList);

        OrganizePlan plan = plan(settings(item("images", "*.jpg")));

        assertTrue(plan.isEmpty());
    }

    @Test
    void aFileWithNoKnownChangeListIsSkippedRatherThanDereferencedUnconditionally() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(null);

        OrganizePlan plan = plan(settings(item("images", "*.jpg")));

        assertTrue(plan.isEmpty());
    }

    @Test
    void onlyApplyOnDefaultChangelistFiltersOutNonDefaultFiles() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");
        LocalChangeList nonDefaultList = changeList("other", false);

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(nonDefaultList);

        ProjectSettings settings = settings(item("images", "*.jpg"));
        settings.setOnlyApplyItemsOnDefaultChangelist(true);

        assertTrue(plan(settings).isEmpty());
    }

    @Test
    void confirmationDialogFlagCarriesIntoThePlannedMove() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");
        LocalChangeList currentList = changeList("default", true);

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(currentList);

        ChangelistOrganizerItem item = item("images", "*.jpg");
        item.setConfirmationDialog(true);

        OrganizePlan plan = plan(settings(item));

        assertTrue(plan.getMoves().get(0).requiresConfirmation());
    }

    @Test
    void reentrantOrganizeCallDuringApplyIsIgnored() {
        VirtualFile file = virtualFile("test.jpg", "/test.jpg");
        LocalChangeList currentList = changeList("default", true);
        LocalChangeList targetList = changeList("images", false);

        when(changeListManager.getAffectedFiles()).thenReturn(List.of(file));
        when(changeListManager.getChangeList(file)).thenReturn(currentList);
        when(changeListManager.addChangeList(eq("images"), anyString())).thenReturn(targetList);
        when(changeListManager.getChangesIn(file)).thenReturn(List.of(mock(Change.class)));

        ChangelistOrganizerService service = new ChangelistOrganizerService(project);

        try (
                MockedStatic<ProjectUtil> projectUtil = mockStatic(ProjectUtil.class);
                MockedStatic<ChangeListManager> changeListManagerStatic = mockStatic(ChangeListManager.class);
                MockedStatic<ProjectSettings> projectSettingsStatic = mockStatic(ProjectSettings.class)
        ) {
            projectUtil.when(() -> ProjectUtil.guessProjectDir(any())).thenReturn(null);
            changeListManagerStatic.when(() -> ChangeListManager.getInstance(project)).thenReturn(changeListManager);
            projectSettingsStatic.when(() -> ProjectSettings.storedSettings(project)).thenReturn(settings(item("images", "*.jpg")));

            // moving a file fires changesAdded on the real listener, which - with auto-organize on
            // - re-enters organize() through the very run that triggered it. Simulate that here.
            doAnswer(invocation -> {
                service.organize();
                return null;
            }).when(changeListManager).moveChangesTo(eq(targetList), any(Change[].class));

            service.organize();

            // exactly one move: the reentrant call must have been ignored by the running guard,
            // not produced (and applied) a second plan.
            verify(changeListManager, times(1)).moveChangesTo(eq(targetList), any(Change[].class));
        }
    }

    private OrganizePlan plan(ProjectSettings settings) {
        ChangelistOrganizerService service = new ChangelistOrganizerService(project);

        try (MockedStatic<ProjectUtil> projectUtil = mockStatic(ProjectUtil.class)) {
            projectUtil.when(() -> ProjectUtil.guessProjectDir(any())).thenReturn(null);

            return service.plan(settings, changeListManager);
        }
    }

    private static ProjectSettings settings(ChangelistOrganizerItem... items) {
        ProjectSettings settings = new ProjectSettings();
        settings.setChangelistOrganizerItems(List.of(items));
        settings.setOnlyApplyItemsOnDefaultChangelist(false);
        settings.setStopApplyingItemsAfterFirstMatch(true);
        return settings;
    }

    private static ChangelistOrganizerItem item(String changeListName, String filePattern) {
        return new ChangelistOrganizerItem(true, changeListName, filePattern, false, false);
    }

    // lenient(): not every test exercises every stub here - e.g. a file filtered out by
    // onlyApplyItemsOnDefaultChangelist never reaches the code that reads its name/path.
    private static VirtualFile virtualFile(String name, String path) {
        VirtualFile file = mock(VirtualFile.class);
        lenient().when(file.getName()).thenReturn(name);
        lenient().when(file.getPath()).thenReturn(path);
        return file;
    }

    private static LocalChangeList changeList(String name, boolean isDefault) {
        LocalChangeList changeList = mock(LocalChangeList.class);
        lenient().when(changeList.getName()).thenReturn(name);
        lenient().when(changeList.isDefault()).thenReturn(isDefault);
        return changeList;
    }
}
