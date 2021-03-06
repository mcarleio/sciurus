package io.mcarle.sciurus.log4j;

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


@Plugin(name = TestDateAppender.NAME, category = "Core", elementType = "appender", printObject = true)
public class TestDateAppender extends AbstractAppender {

    public final static String NAME = "TestDateAppender";

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 8047713132100613185L;
    private final List<Map.Entry<Date, String>> messages = Collections.synchronizedList(new ArrayList<>());

    protected TestDateAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    @SuppressWarnings("unused")
    public static TestDateAppender createAppender(@PluginAttribute("name") String name,
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
        return new TestDateAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        messages.add(new AbstractMap.SimpleEntry<>(new Date(), event.getMessage().getFormattedMessage()));
    }

    public List<Map.Entry<Date, String>> getLogs() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }
}
