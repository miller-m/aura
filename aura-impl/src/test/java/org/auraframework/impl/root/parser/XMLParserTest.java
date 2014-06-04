/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.root.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.auraframework.def.AttributeDef;
import org.auraframework.def.ComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.EventDef;
import org.auraframework.def.EventType;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.root.event.EventDefImpl;
import org.auraframework.impl.source.file.FileSource;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.system.Location;
import org.auraframework.system.Parser.Format;
import org.auraframework.system.Source;
import org.auraframework.throwable.AuraRuntimeException;
import org.mockito.Mockito;

public class XMLParserTest extends AuraImplTestCase {

    private DefDescriptor<ComponentDef> descriptor;
    private ComponentDef def;

    public XMLParserTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            XMLParser parser = XMLParser.getInstance();
            descriptor = DefDescriptorImpl.getInstance("test:parser", ComponentDef.class);
            Source<?> source = getSource(descriptor);
            def = parser.parse(descriptor, source);
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    public void testParseDescriptor() throws Exception {
        assertEquals("Unexpected Descriptor", descriptor, def.getDescriptor());
    }

    public void testParseInvalid() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        descriptor = DefDescriptorImpl.getInstance("test:parserInvalid", ComponentDef.class);
        Source<?> source = getSource(descriptor);
        try {
            parser.parse(descriptor, source);
            fail("Parsing invalid source should throw exception");
        } catch (AuraRuntimeException e) {
            Location location = e.getLocation();
            assertTrue("Wrong filename.", location.getFileName().endsWith("parserInvalid.cmp"));
            assertEquals(19, location.getLine());
            assertEquals(5, location.getColumn());
        }
    }

    public void testParseFragment() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        descriptor = DefDescriptorImpl.getInstance("test:parserFragment", ComponentDef.class);
        Source<?> source = getSource(descriptor);
        try {
            parser.parse(descriptor, source);
            fail("Parsing invalid source should throw exception");
        } catch (AuraRuntimeException e) {
            Location location = e.getLocation();
            assertTrue("Wrong filename.", location.getFileName().endsWith("parserFragment.cmp"));
            assertEquals(18, location.getLine());
            assertEquals(15, location.getColumn());
        }
    }

    public void testParseNonexistent() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        File tmpFile = Mockito.mock(File.class);
        Mockito.when(tmpFile.exists()).thenReturn(true);
        Mockito.when(tmpFile.lastModified()).thenReturn(0L);
        Mockito.when(tmpFile.getCanonicalPath()).thenReturn("");
        Mockito.when(tmpFile.getPath()).thenReturn("");
        try {
            Source<?> source = new FileSource<ComponentDef>(descriptor, tmpFile, Format.XML);
            parser.parse(null, source);
            fail("Parsing nonexistent source should throw exception");
        } catch (AuraRuntimeException e) {
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

    public void testParseNull() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        descriptor = DefDescriptorImpl.getInstance("test:parserNonexistent", ComponentDef.class);
        Source<?> source = null;
        try {
            parser.parse(descriptor, source);
            fail("Parsing null source should throw exception");
        } catch (NullPointerException e) {
            // good!
        }
    }

    public void testGetLocationNull() throws Exception {
        assertNull(XMLParser.getLocation(null, null));
    }

    public void testParseEvent() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        DefDescriptor<EventDef> eventDescriptor = DefDescriptorImpl.getInstance("test:anevent", EventDef.class);
        Source<?> source = getSource(eventDescriptor);
        EventDefImpl eventDef = (EventDefImpl) parser.parse(eventDescriptor, source);
        assertNotNull(eventDef);
        assertEquals("Unexpected Descriptor", eventDescriptor, eventDef.getDescriptor());
        assertEquals("Wrong event type", EventType.COMPONENT, eventDef.getEventType());
        Map<DefDescriptor<AttributeDef>, AttributeDef> atts = eventDef.getDeclaredAttributeDefs();
        assertEquals("Wrong number of attributes", 3, atts.size());
    }

    /**
     * Positive test: Parse a component with comments, new line character after
     * the end tag.
     * 
     * @throws Exception
     */
    public void testParseComments() throws Exception {
        XMLParser parser = XMLParser.getInstance();
        descriptor = DefDescriptorImpl.getInstance("test:test_Parser_Comments", ComponentDef.class);
        Source<?> source = getSource(descriptor);
        def = parser.parse(descriptor, source);
        assertEquals("Unexpected Descriptor", descriptor, def.getDescriptor());
    }
}
