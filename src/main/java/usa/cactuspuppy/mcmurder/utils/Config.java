package usa.cactuspuppy.mcmurder.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom YML parser supporting #comments and indented key-value pairs
 *
 * @author CactusPuppy
 */
@NoArgsConstructor
public class Config implements Map<String, String> {
    /**
     * How many spaces each level should indent
     */
    @Getter @Setter
    private int spacesPerIndent = 2;

    @Getter
    private File configFile;

    /**
     * Pattern to find key-value pairs
     */
    private static final Pattern KEY_VALUE_MATCHER = Pattern.compile("^( *)([^:\\n]+):( *)([^:\\n]*)$");

    /**
     * Pattern to capture comments
     */
    private static final Pattern COMMENT_MATCHER = Pattern.compile("(?:[^#\\n]*)( *# *.*)");

    /**
     * Root node of the config. All nodes should be children of this node.
     */
    private Node rootNode = new BlankNode();

    /**
     * Flat map of all string keys. Does not include comments.
     */
    private Map<String, String> cache = new HashMap<>();

    public void load(File configFile) throws IllegalArgumentException, InvalidConfigurationException, IOException {
        if (configFile == null) {
            throw new IllegalArgumentException("Config file must not be null");
        }
        try (FileInputStream fIS = new FileInputStream(configFile)) {
            loadInputStream(fIS);
        }
    }

    public void load(String fileName) throws IllegalArgumentException, InvalidConfigurationException, IOException {
        if (fileName == null || fileName.equals("")) {
            throw new IllegalArgumentException("Filename must not be empty or null");
        }
        File configFile = new File(fileName);
        load(configFile);
    }

    public void loadFromString(String configString) throws IllegalArgumentException, InvalidConfigurationException {
        if (configString == null || configString.equals("")) {
            throw new IllegalArgumentException("Filename must not be empty or null");
        }
        loadInputStream(new ByteArrayInputStream(configString.getBytes()));
    }

    private void loadInputStream(InputStream stream) throws InvalidConfigurationException {
        int lineIndex = 0;
        try (Scanner scan = new Scanner(stream)) {
            //Track indent levels
            LinkedList<Integer> currIndents = new LinkedList<>();
            currIndents.addLast(0);

            Node currentParent = rootNode;
            Node previousKeyNode = null;
            int prevIndent = 0;

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                lineIndex++;

                //Comment handling
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            Logger.logSevere(this.getClass(), "Exception while parsing new config input stream at line " + lineIndex, e);
            throw new InvalidConfigurationException();
        }
    }

    public String saveToString() {
        //TODO
        return null;
    }

    public void save(File file) throws IllegalArgumentException, IOException {
        String config = saveToString();
        FileUtils.writeStringToFile(file, config, Charset.defaultCharset());
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return cache.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return cache.get(key);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public String getOrDefault(Object key, String def) {
        return cache.getOrDefault(key, def);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        //TODO
        return null;
    }

    @Override
    public String remove(Object key) {
        //TODO
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        for (String key : m.keySet()) {
            put(key, m.get(m));
        }
    }

    @Override
    public void clear() {
        rootNode.children.clear();
        cache.clear();
    }

    /**
     * Get an unmodifiable snapshot of the current cache which throws an {@link UnsupportedOperationException} exception
     * if any mutation is attempted.
     * @return An unmodifiable copy of the cache.
     */
    public Map<String, String> getCache() {
        return Collections.unmodifiableMap(cache);
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return cache.keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return cache.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return cache.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Map) {
            return cache.equals(obj);
        }
        return false;
    }

    /**
     * Represents one section of the config
     */
    @Getter @Setter
    private abstract class Node {
        private List<Node> children = new ArrayList<>();
    }

    /**
     * Represents a key-value pair
     */
    @Getter @Setter
    private class KeyNode extends Node {
        private String key = null;
        private int colonSpace;
        private String value = null;
    }

    /**
     * Represents a # prefixed comment
     */
    @Getter @Setter
    private class CommentNode extends Node {
        private String comment = null;
    }

    /**
     * Represents one or more blank lines
     */
    @Getter @Setter
    private class BlankNode extends Node {
        /**
         * Number of blank lines this node accounts for.
         */
        private int count = 0;
    }
}