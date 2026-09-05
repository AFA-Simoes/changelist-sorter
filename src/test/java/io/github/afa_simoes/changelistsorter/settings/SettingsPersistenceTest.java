package io.github.afa_simoes.changelistsorter.settings;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks the on-disk shape of {@code changelistorganizer_project.xml}. The {@code @State} name,
 * the storage file name, and the {@code ProjectSettings}/{@code ChangelistOrganizerItem} simple
 * class names are the persistence-compatibility contract with both the original "Changelist
 * Organizer" plugin and this plugin's own prior releases - renaming any of them silently
 * discards every configured rule on upgrade.
 *
 * <p>This locks the {@link XmlSerializer} shape that {@code @State}/{@code @Storage} delegate
 * to, rather than exercising {@link ProjectSettingsService} directly, since that needs a live
 * {@code PersistentStateComponent} container.
 */
class SettingsPersistenceTest {
    private static final String LEGACY_XML = """
            <ProjectSettings>
              <option name="automaticallyOrganize" value="false" />
              <option name="changelistOrganizerItems">
                <list>
                  <ChangelistOrganizerItem>
                    <option name="changeListName" value="images" />
                    <option name="checkFullPath" value="false" />
                    <option name="confirmationDialog" value="false" />
                    <option name="enabled" value="true" />
                    <option name="filePattern" value="*.jpg" />
                  </ChangelistOrganizerItem>
                </list>
              </option>
              <option name="onlyApplyItemsOnDefaultChangelist" value="true" />
              <option name="removeEmptyChangelists" value="false" />
              <option name="stopApplyingItemsAfterFirstMatch" value="true" />
            </ProjectSettings>""";

    @Test
    void serializesToTheLegacyXmlShape() {
        Element serialized = XmlSerializer.serialize(populatedSettings());

        assertEquals(LEGACY_XML, JDOMUtil.writeElement(serialized));
    }

    @Test
    void legacyXmlRoundTripsIntoTheEquivalentSettings() throws Exception {
        Element element = JDOMUtil.load(LEGACY_XML);

        ProjectSettings deserialized = XmlSerializer.deserialize(element, ProjectSettings.class);

        assertEquals(populatedSettings(), deserialized);
    }

    private static ProjectSettings populatedSettings() {
        ProjectSettings settings = new ProjectSettings();

        settings.setChangelistOrganizerItems(List.of(
                new ChangelistOrganizerItem(true, "images", "*.jpg", false, false)
        ));
        settings.setOnlyApplyItemsOnDefaultChangelist(true);
        settings.setStopApplyingItemsAfterFirstMatch(true);
        settings.setRemoveEmptyChangelists(false);
        settings.setAutomaticallyOrganize(false);

        return settings;
    }
}
