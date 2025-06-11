package eu.maveniverse.domtrip.snippets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Base class for all snippet tests that provides common setup and utilities.
 */
public abstract class BaseSnippetTest {

    protected String currentTestName;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.currentTestName = testInfo.getDisplayName();
    }

    /**
     * Helper method to create test XML content for snippets.
     */
    protected String createTestXml(String rootElement) {
        return String.format(
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <%s>
            </%s>
            """,
                rootElement, rootElement);
    }

    /**
     * Helper method to create a more complex test XML structure.
     */
    protected String createConfigXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <config>
                <database>
                    <host>localhost</host>
                    <port>5432</port>
                </database>
                <application>
                    <name>MyApp</name>
                    <version>1.0.0</version>
                </application>
            </config>
            """;
    }

    /**
     * Helper method to create a Maven POM XML for testing.
     */
    protected String createMavenPomXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>

                <groupId>com.example</groupId>
                <artifactId>my-app</artifactId>
                <version>1.0.0</version>
                <packaging>jar</packaging>

                <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                </properties>
            </project>
            """;
    }
}
