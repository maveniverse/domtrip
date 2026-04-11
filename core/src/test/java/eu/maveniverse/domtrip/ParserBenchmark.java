/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmarks for DomTrip parser performance.
 *
 * <p>Run via main method or: {@code java -cp ... org.openjdk.jmh.Main ParserBenchmark}</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ParserBenchmark {

    private Parser parser;
    private String tinyXml;
    private String smallXml;
    private String mediumXml;
    private String largeXml;

    // Pre-parsed documents for serialization benchmarks
    private Document smallDoc;
    private Document mediumDoc;
    private Document largeDoc;

    @Setup
    public void setup() {
        parser = new Parser();
        tinyXml = buildTinyXml();
        smallXml = buildSmallXml();
        mediumXml = buildMediumXml();
        largeXml = buildLargeXml();

        smallDoc = parser.parse(smallXml);
        mediumDoc = parser.parse(mediumXml);
        largeDoc = parser.parse(largeXml);
    }

    // --- Parsing benchmarks ---

    @Benchmark
    public Document parseTiny() {
        return parser.parse(tinyXml);
    }

    @Benchmark
    public Document parseSmall() {
        return parser.parse(smallXml);
    }

    @Benchmark
    public Document parseMedium() {
        return parser.parse(mediumXml);
    }

    @Benchmark
    public Document parseLarge() {
        return parser.parse(largeXml);
    }

    // --- Round-trip benchmarks (parse + serialize) ---

    @Benchmark
    public String roundTripSmall() {
        return parser.parse(smallXml).toXml();
    }

    @Benchmark
    public String roundTripMedium() {
        return parser.parse(mediumXml).toXml();
    }

    @Benchmark
    public String roundTripLarge() {
        return parser.parse(largeXml).toXml();
    }

    // --- Serialization-only benchmarks ---

    @Benchmark
    public String serializeSmall() {
        return smallDoc.toXml();
    }

    @Benchmark
    public String serializeMedium() {
        return mediumDoc.toXml();
    }

    @Benchmark
    public String serializeLarge() {
        return largeDoc.toXml();
    }

    // --- Test data builders ---

    private static String buildTinyXml() {
        return "<root><child>text</child></root>";
    }

    private static String buildSmallXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "\n"
                + "  <groupId>com.example</groupId>\n"
                + "  <artifactId>my-app</artifactId>\n"
                + "  <version>1.0.0-SNAPSHOT</version>\n"
                + "  <packaging>jar</packaging>\n"
                + "\n"
                + "  <properties>\n"
                + "    <maven.compiler.release>17</maven.compiler.release>\n"
                + "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                + "  </properties>\n"
                + "\n"
                + "  <dependencies>\n"
                + "    <dependency>\n"
                + "      <groupId>org.junit.jupiter</groupId>\n"
                + "      <artifactId>junit-jupiter</artifactId>\n"
                + "      <version>5.10.0</version>\n"
                + "      <scope>test</scope>\n"
                + "    </dependency>\n"
                + "  </dependencies>\n"
                + "</project>\n";
    }

    private static String buildMediumXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!-- This is a medium-sized POM file for benchmarking -->\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        sb.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append(
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        sb.append("  <modelVersion>4.0.0</modelVersion>\n\n");
        sb.append("  <parent>\n");
        sb.append("    <groupId>com.example</groupId>\n");
        sb.append("    <artifactId>parent</artifactId>\n");
        sb.append("    <version>1.0.0</version>\n");
        sb.append("  </parent>\n\n");
        sb.append("  <groupId>com.example</groupId>\n");
        sb.append("  <artifactId>medium-app</artifactId>\n");
        sb.append("  <version>2.0.0-SNAPSHOT</version>\n");
        sb.append("  <packaging>jar</packaging>\n");
        sb.append("  <name>Medium Application</name>\n");
        sb.append(
                "  <description>A medium-sized application for &amp; benchmarking &lt;purposes&gt;</description>\n\n");

        sb.append("  <properties>\n");
        for (int i = 0; i < 20; i++) {
            sb.append("    <property.name")
                    .append(i)
                    .append(">value")
                    .append(i)
                    .append("</property.name")
                    .append(i)
                    .append(">\n");
        }
        sb.append("  </properties>\n\n");

        sb.append("  <dependencies>\n");
        for (int i = 0; i < 20; i++) {
            sb.append("    <dependency>\n");
            sb.append("      <groupId>com.example.group").append(i).append("</groupId>\n");
            sb.append("      <artifactId>artifact-").append(i).append("</artifactId>\n");
            sb.append("      <version>").append(i).append(".0.0</version>\n");
            if (i % 3 == 0) {
                sb.append("      <scope>test</scope>\n");
            }
            if (i % 5 == 0) {
                sb.append("      <optional>true</optional>\n");
            }
            sb.append("    </dependency>\n");
        }
        sb.append("  </dependencies>\n\n");

        sb.append("  <build>\n");
        sb.append("    <plugins>\n");
        for (int i = 0; i < 5; i++) {
            sb.append("      <plugin>\n");
            sb.append("        <groupId>org.apache.maven.plugins</groupId>\n");
            sb.append("        <artifactId>maven-plugin-").append(i).append("</artifactId>\n");
            sb.append("        <version>3.").append(i).append(".0</version>\n");
            sb.append("        <configuration>\n");
            sb.append("          <param1>value1</param1>\n");
            sb.append("          <param2>value2</param2>\n");
            sb.append("        </configuration>\n");
            sb.append("      </plugin>\n");
        }
        sb.append("    </plugins>\n");
        sb.append("  </build>\n");
        sb.append("</project>\n");
        return sb.toString();
    }

    private static String buildLargeXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!--\n  Large POM file for benchmarking.\n  Contains many dependencies and plugins.\n-->\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        sb.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append(
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        sb.append("  <modelVersion>4.0.0</modelVersion>\n\n");
        sb.append("  <groupId>com.example</groupId>\n");
        sb.append("  <artifactId>large-app</artifactId>\n");
        sb.append("  <version>3.0.0-SNAPSHOT</version>\n\n");

        sb.append("  <properties>\n");
        for (int i = 0; i < 50; i++) {
            sb.append("    <version.lib")
                    .append(i)
                    .append(">")
                    .append(i)
                    .append(".")
                    .append(i % 10)
                    .append(".0</version.lib")
                    .append(i)
                    .append(">\n");
        }
        sb.append("  </properties>\n\n");

        sb.append("  <dependencyManagement>\n");
        sb.append("    <dependencies>\n");
        for (int i = 0; i < 50; i++) {
            sb.append("      <dependency>\n");
            sb.append("        <groupId>com.example.managed").append(i).append("</groupId>\n");
            sb.append("        <artifactId>managed-artifact-").append(i).append("</artifactId>\n");
            sb.append("        <version>${version.lib").append(i).append("}</version>\n");
            if (i % 4 == 0) {
                sb.append("        <type>pom</type>\n");
                sb.append("        <scope>import</scope>\n");
            }
            sb.append("      </dependency>\n");
        }
        sb.append("    </dependencies>\n");
        sb.append("  </dependencyManagement>\n\n");

        sb.append("  <dependencies>\n");
        for (int i = 0; i < 100; i++) {
            sb.append("    <dependency>\n");
            sb.append("      <groupId>com.example.dep").append(i).append("</groupId>\n");
            sb.append("      <artifactId>dep-artifact-").append(i).append("</artifactId>\n");
            sb.append("      <version>")
                    .append(i % 10)
                    .append(".")
                    .append(i % 5)
                    .append(".")
                    .append(i % 3)
                    .append("</version>\n");
            if (i % 3 == 0) {
                sb.append("      <scope>test</scope>\n");
            } else if (i % 7 == 0) {
                sb.append("      <scope>provided</scope>\n");
            }
            if (i % 10 == 0) {
                sb.append("      <exclusions>\n");
                sb.append("        <exclusion>\n");
                sb.append("          <groupId>com.excluded</groupId>\n");
                sb.append("          <artifactId>excluded-").append(i).append("</artifactId>\n");
                sb.append("        </exclusion>\n");
                sb.append("      </exclusions>\n");
            }
            sb.append("    </dependency>\n");
        }
        sb.append("  </dependencies>\n\n");

        sb.append("  <build>\n");
        sb.append("    <plugins>\n");
        String[] pluginNames = {
            "compiler", "surefire", "jar", "install", "deploy",
            "site", "clean", "resources", "source", "javadoc",
            "shade", "assembly", "dependency", "enforcer", "failsafe"
        };
        for (int i = 0; i < 15; i++) {
            sb.append("      <plugin>\n");
            sb.append("        <groupId>org.apache.maven.plugins</groupId>\n");
            sb.append("        <artifactId>maven-")
                    .append(pluginNames[i % pluginNames.length])
                    .append("-plugin</artifactId>\n");
            sb.append("        <version>3.").append(i).append(".0</version>\n");
            sb.append("        <configuration>\n");
            for (int j = 0; j < 5; j++) {
                sb.append("          <config")
                        .append(j)
                        .append(">value-")
                        .append(j)
                        .append("</config")
                        .append(j)
                        .append(">\n");
            }
            sb.append("        </configuration>\n");
            if (i % 3 == 0) {
                sb.append("        <executions>\n");
                sb.append("          <execution>\n");
                sb.append("            <id>exec-").append(i).append("</id>\n");
                sb.append("            <phase>compile</phase>\n");
                sb.append("            <goals>\n");
                sb.append("              <goal>run</goal>\n");
                sb.append("            </goals>\n");
                sb.append("          </execution>\n");
                sb.append("        </executions>\n");
            }
            sb.append("      </plugin>\n");
        }
        sb.append("    </plugins>\n");
        sb.append("  </build>\n\n");

        sb.append("  <profiles>\n");
        for (int i = 0; i < 5; i++) {
            sb.append("    <profile>\n");
            sb.append("      <id>profile-").append(i).append("</id>\n");
            sb.append("      <activation>\n");
            sb.append("        <property>\n");
            sb.append("          <name>env</name>\n");
            sb.append("          <value>").append(i % 2 == 0 ? "prod" : "dev").append("</value>\n");
            sb.append("        </property>\n");
            sb.append("      </activation>\n");
            sb.append("      <dependencies>\n");
            for (int j = 0; j < 5; j++) {
                sb.append("        <dependency>\n");
                sb.append("          <groupId>com.profile").append(i).append("</groupId>\n");
                sb.append("          <artifactId>profile-dep-").append(j).append("</artifactId>\n");
                sb.append("          <version>1.0.0</version>\n");
                sb.append("        </dependency>\n");
            }
            sb.append("      </dependencies>\n");
            sb.append("    </profile>\n");
        }
        sb.append("  </profiles>\n");
        sb.append("</project>\n");
        return sb.toString();
    }

    @Test
    void runBenchmarks() throws Exception {
        Options opt = new OptionsBuilder()
                .include(ParserBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ParserBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
