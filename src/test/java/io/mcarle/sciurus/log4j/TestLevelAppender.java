package io.mcarle.sciurus.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Plugin(name = TestLevelAppender.NAME, category = "Core", elementType = "appender", printObject = true)
public class TestLevelAppender extends AbstractAppender {

    public final static String NAME = "TestLevelAppender";

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 8047713135100613185L;
    private final List<Map.Entry<Level, String>> messages = Collections.synchronizedList(new ArrayList<>());

    protected TestLevelAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    @SuppressWarnings("unused")
    public static TestLevelAppender createAppender(@PluginAttribute("name") String name,
                                                   @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                   @PluginElement("Filter") final Filter filter,
                                                   @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for TestAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new TestLevelAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        messages.add(new AbstractMap.SimpleEntry<>(event.getLevel(), event.getMessage().getFormattedMessage()));
    }

    public List<String> getMessages() {
        return messages.stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<Map.Entry<Level, String>> getLogs() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }
}
