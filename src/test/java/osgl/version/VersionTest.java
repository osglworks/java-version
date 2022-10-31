package osgl.version;

/*-
 * #%L
 * OSGL Version
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import net.evil.pkg.Kit;
import net.tab.NetTab;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mrcool.swissknife.SwissKnife;
import org.mrcool.swissknife.db.DbUtil;
import org.mrcool.swissknife.internal.StringUtil;
import org.mrsuck.MyTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class VersionTest extends Assert {

    protected Logger logger;

    @BeforeAll
    public void prepareLogFactory() {
        logger = Mockito.mock(Logger.class);
        try (MockedStatic mocked = mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger(anyString())).thenReturn(logger);
        }
    }

    @BeforeEach
    public void prepare() throws Exception {
        Version.clearCache();
    }

    @BeforeEach
    public void clearVariableFoundWarnSuppressSetting() {
        System.clearProperty(Version.PROP_SUPPRESS_VAR_FOUND_WARNING);
    }


    @Test
    public void itShallLoadVersionInfoFromResourceIfCacheNotHit() {
        // Arrange/Act
        Version version = Version.of(SwissKnife.class);

        // Assert
        assertEquals("swissknife", version.getArtifactId());
        assertEquals("1.0", version.getProjectVersion());
        assertEquals("3a77", version.getBuildNumber());
    }

    @Test
    public void itShallNotLoadVersionInfoFromResourceIfCacheHits() {
        // Arrange/Act
        Version version1 = Version.of(SwissKnife.class);
        Version version2 = Version.of(StringUtil.class);

        // Assert
        assertSame(version1, version2);
    }

    @Test
    public void itShallLoadVersionInfoFromSubPackageIfDefined() {
        // Arrange/Act

        // load parent package version
        Version.of(SwissKnife.class);

        // load subpackage version
        Version version = Version.of(DbUtil.class);

        // Assert
        assertEquals("swissknife-db", version.getArtifactId());
        assertEquals("0.8-SNAPSHOT", version.getProjectVersion());
        assertEquals("", version.getBuildNumber());
    }

    @Test
    public void itShallReturnUnknownIfVersionInfoNotProvided() {
        // Arrange/Act/Assert
        assertSame(Version.UNKNOWN, Version.of(MyTool.class.getPackage()));
    }

    @Test
    public void itShallNotRejectLoadingVersionInfoFromFirstLevelPackage() {
        // Arrange/Act/Assert
        assertNotEquals(Version.UNKNOWN, Version.of(Kit.class));
    }

    @Test
    public void testIllegalPackageNameCaseOne() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, ()-> {
            Version.ofPackage("org.mrsuck..proj");
        });
    }

    @Test
    public void testIllegalPackageNameCaseTwo() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, ()-> {
            Version.ofPackage("org.#abc.xyz");
        });
    }

    @Test
    public void testDecoratedSnapshotProjectVersion() {
        // Arrange
        String projectVersion = "1.0.0-SNAPSHOT";

        // Act/Assert
        assertEquals("v" + projectVersion, Version.decoratedProjectVersion(projectVersion));
    }

    @Test
    public void testDecoratedProjectVersion() {
        // Arrange
        String projectVersion = "1.0.0";

        // Act/Assert
        assertEquals("r" + projectVersion, Version.decoratedProjectVersion(projectVersion));
    }

    @Test
    public void versionTagShallBeCombinationOfDecoratedProjectVersionAndDecoratedBuildNumberIfBuildNumberDefined() {
        // Arrange/Act
        Version version = Version.of(SwissKnife.class);

        // Assert
        String versionTag = version.getVersion();
        String expected = Version.decoratedProjectVersion(version.getProjectVersion())
                + "-" + version.getBuildNumber();
        assertEquals(expected, versionTag);
    }

    @Test
    public void versionTagShallBeDecoratedProjectVersionWhenBuildNumberIsNotDefined() {
        // Arrange/Act
        Version version = Version.of(DbUtil.class);

        // Assert
        assertEquals(Version.decoratedProjectVersion(version.getProjectVersion()), version.getVersion());
    }

    @Test
    public void itShallPrintArtifactsAndVersionTagInToString() {
        // Arrange/Act
        Version versionWithoutBuildNumber = Version.of(DbUtil.class);

        // Assert
        String expected = versionWithoutBuildNumber.getArtifactId() + "-" +versionWithoutBuildNumber.getVersion();
        assertEquals(expected, versionWithoutBuildNumber.toString());

        Version versionWithBuildNumber = Version.of(SwissKnife.class);
        expected = versionWithBuildNumber.getArtifactId() + "-" +versionWithBuildNumber.getVersion();
        assertEquals(expected, versionWithBuildNumber.toString());
    }

    @Test
    public void itShallUsePackageNameAsArtifiactIdIfNotDefinedAndLogWarnMessage() {
        // Arrange
        String pkg = "org.demo.badversion.noart";
        String subPkg = pkg + ".sub";

        // Act
        Version v = Version.ofPackage(subPkg);

        // Assert
        assertEquals(pkg, v.getArtifactId());
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageArgCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(logger, Mockito.times(1)).warn(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("artifact not defined in .version file: org.demo.badversion.noart"));
    }

    @Test
    public void itShallReturnUnknownAndLogErrorMessageIfNoVersionDefinedInVersionFile() {
        // Arrange/Act/Assert
        assertSame(Version.UNKNOWN, Version.ofPackage("org.demo.badversion.noversion"));
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageArgCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(logger, Mockito.times(1)).error(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("version not defined in .version file: org.demo.badversion.noversion"));
    }

    @Test
    public void unknownVersionShallBeUnknownOtherVersionShallNot() {
        // Arrange/Act/Assert
        assertTrue(Version.UNKNOWN.isUnknown());
        assertFalse(Version.of(DbUtil.class).isUnknown());
        assertFalse(Version.of(SwissKnife.class).isUnknown());
    }

    @Test
    public void unknownVersionTagShouldBeUnknown() {
        // Arrange/Act/Assert
        assertEquals(Version.UNKNOWN_STR, Version.UNKNOWN.getVersion());
    }

    @Test
    public void itShallLogWarnMessageIfThereAreEnvironmentVariableInVersionFile() {
        // Arrange/Act
        Version.of(NetTab.class);

        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageArgCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(logger, Mockito.times(3)).warn(messageCaptor.capture(), messageArgCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("variable found in .version file for {}"));
        assertEquals("net.tab", messageArgCaptor.getValue().toString());
    }

    @Test
    public void itShallReturnJavaPackageVersionInfoIfPresented() {
        // Arrange
        Package pkg = String.class.getPackage();
        String pkgImplVersion = pkg.getImplementationVersion();

        // Act
        Version version = Version.of(String.class);

        // Assert
        assertTrue(pkgImplVersion.contains(version.getProjectVersion()));
        assertTrue(pkgImplVersion.contains(version.getBuildNumber()));
    }

    @Test
    public void itShallReturnVersionOfCallerClassOnGetMethodCall() {
        // Act/Assert
        assertSame(Version.of(VersionTest.class), Version.get());
    }

    @Test
    public void equalToSelfShouldBeTrue() {
        // Arrange/Act
        Version version = new Version("com.bar", "foo", "1.0", "a12f");

        // Assert
        assertEquals(version, version);
    }

    @Test
    public void equalToNullOrNonVersionObjectShouldBeFalse() {
        // Arrange/Act
        Version version = new Version("com.bar","foo", "1.0", "a12f");

        // Assert
        assertNotEquals(null, version);
        assertNotEquals(version, new Object());
    }

    @Test
    public void equalToVersionWithDifferentPartShouldBeFalse() {
        // Arrange
        Version version = new Version("com.bar","foo", "1.0", "a12f");

        // Act/Assert
        assertNotEquals(new Version("com.bar","foo", "1.0.1", "a12f"), version);
        assertNotEquals(new Version("com.bar","bar", "1.0", "a12f"), version);
        assertNotEquals(new Version("com.bar","foo", "1.0", "a12e"), version);
        assertNotEquals(new Version("net.bar","foo", "1.0", "a12f"), version);
    }

    @Test
    public void equalToVersionWithSamePartsShouldBeTrue() {
        // Arrange
        Version version = new Version("com.bar","foo", "1.0", "a12f");

        // Act/Assert
        assertEquals(new Version("com.bar","foo", "1.0", "a12f"), version);
    }

    @Test
    public void hashCodeShouldBeSameWithVersionWithSameParts() {
        // Arrange
        Version v1 = new Version("com.bar","foo", "1.0", "a12f");
        Version v2 = new Version("com.bar","foo", "1.0", "a12f");

        // Act/Assert
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void hashCodeShouldBeDifferentWithVersionWithSameParts() {
        Version v1 = new Version("com.bar","foo", "1.0", "a12f");

        Version v2 = new Version("net.bar","foo", "1.0", "a12f");
        assertNotEquals(v1.hashCode(), v2.hashCode());

        v2 = new Version("com.bar","Foo", "1.0", "a12f");
        assertNotEquals(v1.hashCode(), v2.hashCode());

        v2 = new Version("com.bar","foo", "1.1", "a12f");
        assertNotEquals(v1.hashCode(), v2.hashCode());

        v2 = new Version("com.bar","foo", "1.0", "a12x");
        assertNotEquals(v1.hashCode(), v2.hashCode());

    }

    @Test
    public void testSerialization() throws Exception {
        // Arrange
        Version v1 = new Version("com.bar","foo", "1.0", "a12f");

        // Act
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(v1);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Version v2 = (Version) ois.readObject();

        // Assert
        assertEquals(v1, v2);
    }

    @Test
    public void itShallNotWarnIfNoVariablesFoundInVersion() {
        // Arrange
        // nothing to do here, we will use default property setting

        // Act/Assert
        assertFalse(Version.shouldWarnIfVariableFoundIn("foo-1.0.0"));
    }

    @Test
    public void itShallWarnIfVariablesFoundInVersionSuppressedIsNotSet() {
        // Arrange
        // nothing to do here, we will use default property setting

        // Act/Assert
        assertTrue(Version.shouldWarnIfVariableFoundIn("foo-${project.version}"));
    }

    @Test
    public void itShallNotWarnIfVariablesFoundInVersionSuppressedIsSet() {
        // Arrange
        System.setProperty(Version.PROP_SUPPRESS_VAR_FOUND_WARNING, "true");

        // Act/Assert
        assertFalse(Version.shouldWarnIfVariableFoundIn("foo-${project.version}"));
    }

    @Test
    public void itShallNotWarnIfVariablesFoundInVersionSuppressedIsSetAsYes() {
        // Arrange
        System.setProperty(Version.PROP_SUPPRESS_VAR_FOUND_WARNING, "yes");

        // Act/Assert
        assertFalse(Version.shouldWarnIfVariableFoundIn("foo-${project.version}"));
    }
}
