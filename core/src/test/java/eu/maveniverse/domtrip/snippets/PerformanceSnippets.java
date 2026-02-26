package eu.maveniverse.domtrip.snippets;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Snippet tests for the Performance documentation.
 */
public class PerformanceSnippets extends BaseSnippetTest {

    @Test
    public void demonstrateMemoryUsage() throws DomTripException {
        // START: memory-usage
        // Memory-efficient document handling
        String xmlContent = createTestXml("root");
        Document doc = Document.of(xmlContent);

        // Only modified nodes consume additional memory
        Editor editor = new Editor(doc);
        editor.addElement(editor.root(), "newElement", "content");

        // Original content remains in efficient representation
        String result = editor.toXml();
        // END: memory-usage

        Assertions.assertTrue(result.contains("newElement"));
    }

    @Test
    public void demonstrateParsingPerformance() throws DomTripException {
        // START: parsing-performance
        // Benchmark parsing performance
        String xmlContent = createTestXml("root");
        long startTime = System.nanoTime();

        Document doc = Document.of(xmlContent);
        Editor editor = new Editor(doc);

        long parseTime = System.nanoTime() - startTime;
        System.out.printf("Parsed %d characters in %.2f ms%n", xmlContent.length(), parseTime / 1_000_000.0);
        // END: parsing-performance

        Assertions.assertNotNull(doc);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateModificationPerformance() throws DomTripException {
        // START: modification-performance
        // Efficient bulk modifications
        String xmlContent = createTestXml("root");
        Document document = Document.of(xmlContent);
        Editor editor = new Editor(document);
        Element root = editor.root();

        // Batch operations are optimized
        long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) { // Reduced for testing
            editor.addElement(root, "item" + i, "value" + i);
        }

        long modifyTime = System.nanoTime() - startTime;
        System.out.printf("Added 100 elements in %.2f ms%n", modifyTime / 1_000_000.0);
        // END: modification-performance

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("item99"));
    }

    @Test
    public void demonstrateLargeDocumentProcessing() throws DomTripException, IOException {
        // START: large-document-processing
        // Strategy 1: Process in sections
        String xmlContent = createTestXml("root");
        processLargeDocument(xmlContent);
        // END: large-document-processing

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    public void processLargeDocument(String xmlContent) throws DomTripException, IOException {
        // Check content size first
        long contentSize = xmlContent.length();

        if (contentSize > 50_000) { // Reduced threshold for testing
            processInChunks(xmlContent);
        } else {
            processNormally(xmlContent);
        }
    }

    private void processInChunks(String xmlContent) throws DomTripException {
        // For very large files, consider streaming approach
        Document doc = Document.of(xmlContent);
        Editor editor = new Editor(doc);

        // Process specific elements without loading entire tree
        Element root = editor.root();

        // Use streaming iteration for large collections
        root.childElements().forEach(child -> {
            // Process each child individually
            processElement(child);

            // Optional: Clear processed elements to free memory
            if (shouldClearMemory()) {
                // child.clearCache(); // Conceptual - actual API may vary
            }
        });
    }

    private void processNormally(String xmlContent) throws DomTripException {
        Document doc = Document.of(xmlContent);
        Editor editor = new Editor(doc);
        // Normal processing
    }

    private void processElement(Element element) {
        // Simulate element processing
        String content = element.textContent();
    }

    private boolean shouldClearMemory() {
        // Simple heuristic for memory management
        return false;
    }

    @Test
    public void demonstrateBatchOperations() throws DomTripException {
        // START: batch-operations
        // Efficient batch modifications
        String xmlContent = createTestXml("root");
        Document document = Document.of(xmlContent);
        Editor editor = new Editor(document);

        // Simulate batch updates
        List<ElementData> updates = List.of(
                new ElementData(editor.root(), "item1", "value1"),
                new ElementData(editor.root(), "item2", "value2"),
                new ElementData(editor.root(), "item3", "value3"));

        optimizedBatchUpdate(editor, updates);
        // END: batch-operations

        String result = editor.toXml();
        Assertions.assertTrue(result.contains("item1"));
        Assertions.assertTrue(result.contains("item2"));
        Assertions.assertTrue(result.contains("item3"));
    }

    public void optimizedBatchUpdate(Editor editor, List<ElementData> updates) throws DomTripException {
        Element root = editor.root();

        // Group operations by parent for better performance
        Map<Element, List<ElementData>> groupedUpdates =
                updates.stream().collect(Collectors.groupingBy(ElementData::getParent));

        // Process each group
        groupedUpdates.forEach((parent, elementList) -> {
            // Batch add elements to same parent
            elementList.forEach(data -> editor.addElement(parent, data.getName(), data.getValue()));
        });
    }

    @Test
    public void demonstrateMemoryManagement() throws DomTripException {
        // START: memory-management
        // Optimize memory usage for long-running applications
        XmlProcessor processor = new XmlProcessor();
        String xmlContent = createTestXml("root");
        Document result = processor.processWithCaching(xmlContent);

        // Periodic cleanup
        processor.cleanup();
        // END: memory-management

        Assertions.assertNotNull(result);
    }

    @Test
    public void demonstratePerformanceTesting() throws IOException {
        // START: performance-testing
        DomTripBenchmark benchmark = new DomTripBenchmark();
        benchmark.benchmarkParsing();
        // END: performance-testing

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    @Test
    public void demonstrateMemoryProfiling() throws DomTripException {
        // START: memory-profiling
        profileMemoryUsage();
        // END: memory-profiling

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    public void profileMemoryUsage() throws DomTripException {
        Runtime runtime = Runtime.getRuntime();

        // Baseline memory
        runtime.gc();
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();

        // Load document
        String xmlContent = createTestXml("root");
        Document doc = Document.of(xmlContent);
        runtime.gc();
        long afterParseMemory = runtime.totalMemory() - runtime.freeMemory();

        // Create editor
        Editor editor = new Editor(doc);
        runtime.gc();
        long afterEditorMemory = runtime.totalMemory() - runtime.freeMemory();

        // Perform modifications
        for (int i = 0; i < 10; i++) { // Reduced for testing
            editor.addElement(editor.root(), "element" + i, "value" + i);
        }
        runtime.gc();
        long afterModifyMemory = runtime.totalMemory() - runtime.freeMemory();

        // Report results
        System.out.printf("Memory usage:%n");
        System.out.printf("  Baseline: %d KB%n", baselineMemory / 1024);
        System.out.printf(
                "  After parse: %d KB (+%d KB)%n", afterParseMemory / 1024, (afterParseMemory - baselineMemory) / 1024);
        System.out.printf(
                "  After editor: %d KB (+%d KB)%n",
                afterEditorMemory / 1024, (afterEditorMemory - afterParseMemory) / 1024);
        System.out.printf(
                "  After modify: %d KB (+%d KB)%n",
                afterModifyMemory / 1024, (afterModifyMemory - afterEditorMemory) / 1024);
    }

    @Test
    public void demonstrateConfigurationOptimization() throws DomTripException {
        // START: configuration-optimization
        // Optimize configuration for performance (conceptual)
        DomTripConfig performanceConfig =
                DomTripConfig.defaults().withCommentPreservation(false); // Skip comments if not needed

        String xmlContent = createTestXml("root");
        Document document = Document.of(xmlContent);
        Editor editor = new Editor(document);
        // END: configuration-optimization

        Assertions.assertNotNull(performanceConfig);
        Assertions.assertNotNull(editor);
    }

    @Test
    public void demonstrateStreamingForLargeFiles() throws DomTripException, IOException {
        // START: streaming-large-files
        // Stream processing for very large files
        String xmlContent = createTestXml("root");
        streamProcess(xmlContent);
        // END: streaming-large-files

        // Test passes if no exception is thrown
        Assertions.assertTrue(true);
    }

    public void streamProcess(String xmlContent) throws DomTripException, IOException {
        try (InputStream stream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
                BufferedInputStream buffered = new BufferedInputStream(stream, 64 * 1024)) {

            Document doc = Document.of(buffered);
            Editor editor = new Editor(doc);

            // Process incrementally
            processIncrementally(editor);
        }
    }

    private void processIncrementally(Editor editor) throws DomTripException {
        Element root = editor.root();

        // Process children one at a time
        Iterator<Element> children = root.childElements().iterator();
        while (children.hasNext()) {
            Element child = children.next();

            // Process this child
            processElement(child);

            // Optional: Remove processed child to free memory
            if (isMemoryConstrained()) {
                // children.remove(); // Conceptual - may not be supported
            }
        }
    }

    private boolean isMemoryConstrained() {
        // Simple heuristic for memory constraints
        return false;
    }

    @Test
    public void demonstratePerformanceMonitoring() throws DomTripException {
        // START: performance-monitoring
        PerformanceMonitor monitor = new PerformanceMonitor();
        String xmlContent = createTestXml("root");
        Document result = monitor.monitoredParse(xmlContent);
        monitor.printStatistics();
        // END: performance-monitoring

        Assertions.assertNotNull(result);
    }

    // Helper classes for snippets
    public static class ElementData {
        private final Element parent;
        private final String name;
        private final String value;

        public ElementData(Element parent, String name, String value) {
            this.parent = parent;
            this.name = name;
            this.value = value;
        }

        public Element getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public static class XmlProcessor {
        // Simulated document cache
        private final java.util.Map<String, Document> cache = new java.util.HashMap<>();

        public Document processWithCaching(String xmlContent) throws DomTripException {
            String contentHash = String.valueOf(xmlContent.hashCode());

            // Check cache first
            Document cached = cache.get(contentHash);
            if (cached != null) {
                return Document.of(cached.toXml()); // Return copy to avoid modification
            }

            // Parse and cache
            Document doc = Document.of(xmlContent);
            cache.put(contentHash, doc);

            return doc;
        }

        // Periodic cleanup
        public void cleanup() {
            cache.clear();
            System.gc(); // Suggest garbage collection
        }
    }

    public static class DomTripBenchmark {

        public void benchmarkParsing() {
            String[] testContents = {createTestXml("small"), createTestXml("medium")};

            for (int i = 0; i < testContents.length; i++) {
                benchmarkContent("test" + i, testContents[i]);
            }
        }

        private void benchmarkContent(String name, String content) {
            try {
                // Warm up JVM
                for (int i = 0; i < 3; i++) { // Reduced for testing
                    Document.of(content);
                }

                // Actual benchmark
                long totalTime = 0;
                int iterations = 10; // Reduced for testing

                for (int i = 0; i < iterations; i++) {
                    long start = System.nanoTime();
                    Document doc = Document.of(content);
                    Editor editor = new Editor(doc);
                    String result = editor.toXml();
                    long end = System.nanoTime();

                    totalTime += (end - start);
                }

                double avgTime = totalTime / (double) iterations / 1_000_000.0;
                double throughput = content.length() / avgTime * 1000.0; // chars/sec

                System.out.printf("%s: %.2f ms avg, %.0f chars/sec%n", name, avgTime, throughput);

            } catch (Exception e) {
                System.err.println("Failed to benchmark " + name + ": " + e.getMessage());
            }
        }

        private static String createTestXml(String type) {
            return "<" + type + "><child>content</child></" + type + ">";
        }
    }

    public static class PerformanceMonitor {
        private final AtomicLong parseCount = new AtomicLong();
        private final AtomicLong parseTime = new AtomicLong();

        public Document monitoredParse(String xml) throws DomTripException {
            long start = System.nanoTime();
            try {
                Document doc = Document.of(xml);
                return doc;
            } finally {
                long duration = System.nanoTime() - start;
                parseCount.incrementAndGet();
                parseTime.addAndGet(duration);
            }
        }

        public void printStatistics() {
            long totalParses = parseCount.get();
            long totalParseTime = parseTime.get();

            if (totalParses > 0) {
                double avgParseTime = totalParseTime / (double) totalParses / 1_000_000.0;
                System.out.printf("Parse statistics: %d operations, %.2f ms average%n", totalParses, avgParseTime);
            }
        }
    }
}
