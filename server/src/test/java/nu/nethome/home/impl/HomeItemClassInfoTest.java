/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.impl;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by Stefan 2013-12-25
 */
public class HomeItemClassInfoTest {


    private HomeItemClassInfo creationInfoItem;

    class NoAnnotation extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    @HomeItemType("Ports")
    class PortsItem extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    @HomeItemType(value = "Controls", creationEvents = "NexaL_Message")
    class EventsItem extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    @HomeItemType(value = "Controls", creationInfo = TestAutoCreationInfo.class)
    class CreationInfoItem extends HomeItemAdapter{
        @Override
        public String getModel() {
            return null;
        }
    }

    HomeItemInfo noAnno;
    HomeItemInfo portsItem;
    HomeItemInfo eventsItem;
    InternalEvent event;

    @Before
    public void setUp() throws Exception {
        noAnno = new HomeItemClassInfo(NoAnnotation.class);
        portsItem = new HomeItemClassInfo(PortsItem.class);
        eventsItem = new HomeItemClassInfo(EventsItem.class);
        creationInfoItem = new HomeItemClassInfo(CreationInfoItem.class);
        event = new InternalEvent("Foo_Message", "Fie");
    }

    // No Annotation

    @Test
    public void noAnnotationHasClassName() {
        assertThat(noAnno.getClassName(), is("NoAnnotation"));
    }

    @Test
    public void noAnnotationGivesUnknownCategory() {
        assertThat(noAnno.getCategory(), is("Unknown"));
    }

    @Test
    public void noAnnotationGivesEmptyEventsList() {
        assertThat(noAnno.getCreationEventTypes().length, is(0));
    }

    @Test
    public void noAnnotationCannotBeCreatedByEvent() {
        assertThat(noAnno.canBeCreatedBy(event), is(false));
    }

    @Test
    public void noAnnotationGivesEmptyCreationInfo() {
        assertThat(noAnno.getCreationIdentification(event), is(""));
    }

    // With category annotation "Ports"

    @Test
    public void withAnnotationHasClassName() {
        assertThat(portsItem.getClassName(), is("PortsItem"));
    }

    @Test
    public void withAnnotationGivesSpecifiedCategory() {
        assertThat(portsItem.getCategory(), is("Ports"));
    }

    @Test
    public void annotationWithoutEventsGivesEmptyEventsList() {
        assertThat(portsItem.getCreationEventTypes().length, is(0));
    }

    @Test
    public void withOnlyCategoryAnnotationCannotBeCreatedByEvent() {
        assertThat(portsItem.canBeCreatedBy(event), is(false));
    }

    @Test
    public void withOnlyCategoryAnnotationGivesEmptyCreationInfo() {
        assertThat(portsItem.getCreationIdentification(event), is(""));
    }


    // With category and events annotation

    @Test
    public void withEventsAnnotationGivesSpecifiedCategory() {
        assertThat(eventsItem.getCategory(), is("Controls"));
    }

    @Test
    public void canReadEventsFromAnnotation() {
        assertThat(eventsItem.getCreationEventTypes().length, is(1));
        assertThat(eventsItem.getCreationEventTypes()[0], is("NexaL_Message"));
    }

    @Test
    public void withEventsAnnotationKnowsIfCanBeCreatedByEvent() {
        assertThat(eventsItem.canBeCreatedBy(event), is(false));
        InternalEvent correctEvent = new InternalEvent("NexaL_Message", "Fie");
        assertThat(eventsItem.canBeCreatedBy(correctEvent), is(true));
    }

    @Test
    public void withEventsAnnotationGivesGenericCreationId() {
        InternalEvent event = new InternalEvent("NexaL_Message", "");
        event.setAttribute("Foo", "FooValue");
        event.setAttribute("Fie", "FieValue");
        assertThat(eventsItem.getCreationIdentification(event), is("NexaL:Fie=FieValue,Foo=FooValue"));
    }

    @Test
    public void withEventsAnnotationGivesGenericCreationIdIgnoringStandardAttributes() {
        InternalEvent event = new InternalEvent("NexaL_Message", "");
        event.setAttribute("Foo", "FooValue");
        event.setAttribute("Fie", "FieValue");
        event.setAttribute("UPM.SequenceNumber", "XXX");
        event.setAttribute("Direction", "YYY");
        assertThat(eventsItem.getCreationIdentification(event), is("NexaL:Fie=FieValue,Foo=FooValue"));
    }

    // With CreationInfo annotation

    static class TestAutoCreationInfo implements AutoCreationInfo {
        static final String [] EVENTS = {"Foo_Message"};
        @Override
        public String[] getCreationEvents() {
            return EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("CanCreate").equals("true");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return e.getAttribute("CanCreate");
        }
    }


    @Test
    public void withCreationInfoAnnotationGivesSpecifiedCategory() {
        assertThat(creationInfoItem.getCategory(), is("Controls"));
    }

    @Test
    public void canReadEventsFromCreationInfo() {
        assertThat(creationInfoItem.getCreationEventTypes().length, is(1));
        assertThat(creationInfoItem.getCreationEventTypes()[0], is("Foo_Message"));
    }

    @Test
    public void withCreationInfoKnowsIfCanBeCreatedByEvent() {
        InternalEvent correctEvent = new InternalEvent("NexaL_Message", "Fie");
        correctEvent.setAttribute("CanCreate", "true");
        assertThat(creationInfoItem.canBeCreatedBy(event), is(false));
        assertThat(creationInfoItem.canBeCreatedBy(correctEvent), is(true));
    }

    @Test
    public void withCreationInfoCanGiveCreationIdInfo() {
        event.setAttribute("CanCreate", "Foo");
        assertThat(creationInfoItem.getCreationIdentification(event), is("Foo"));
    }
}