/*
 * StoryArc.java
 *
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.storyarcs;

import megamek.common.annotations.Nullable;
import megamek.common.util.sorter.NaturalOrderComparator;
import mekhq.MekHQ;
import mekhq.MekHqConstants;
import mekhq.MekHqXmlSerializable;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.mission.Scenario;
import mekhq.campaign.Campaign;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * The Story Arc class manages a given story arc campaign
 */
public class StoryArc implements MekHqXmlSerializable {

    private String title;
    private String description;

    private Campaign campaign;

    /** Can this story arc be added to existing campaign or does it need to start fresh? **/
    private boolean startNew;

    /** A UUID for the initial event in this track  - can be null **/
    private UUID startingEventId;

    /** A hash of all possible StoryEvents in this StoryArc, referenced by UUID **/
    private Map<UUID, StoryEvent> storyEvents;

    /** A hash of all possible Missions in this StoryArc, referenced by UUID **/
    private Map<UUID, Mission> storyMissions;

    /** A hash of all possible Scenarios in this StoryArc, referenced by UUID **/
    private Map<UUID, Scenario> storyScenarios;

    /** A UUID for the currently active event. May be null **/
    private UUID currentEventId;

    /**
     * An integer that tracks the current mission id, so we know where to add new scenarios and complete the
     * mission. Eventually, I would like to allow multiple active missions, but figure that out later.
     */
    private int currentMissionId;


    public StoryArc() {
        startNew = true;
        storyEvents =  new LinkedHashMap<>();
        storyMissions = new LinkedHashMap<>();
        storyScenarios = new LinkedHashMap<>();
    }

    public void setCampaign(Campaign c) { this.campaign = c; }

    protected Campaign getCampaign() { return campaign; }

    private void setTitle(String t) { this.title = t; }

    public String getTitle() { return this.title; }

    public String getDescription() { return this.description; }

    private void setDescription(String d) { this.description = d; }

    private void setStartNew(Boolean b) { this.startNew = b; }

    private void setStartingEventId(UUID u) { this.startingEventId = u; }

    private UUID getStartingEventId() { return startingEventId; }

    public void setCurrentMissionId(int i) { this.currentMissionId = i; }

    public int getCurrentMissionId() { return this.currentMissionId; }

    public void setCurrentEventId(UUID id) { this.currentEventId = id; }

    public StoryEvent getStoryEvent(UUID id) {
        if (id == null) {
            return null;
        }
        return storyEvents.get(id);
    }

    public Mission getStoryMission(UUID id) {
        if (id == null) {
            return null;
        }
        return storyMissions.get(id);
    }

    public Scenario getStoryScenario(UUID id) {
        if (id == null) {
            return null;
        }
        return storyScenarios.get(id);
    }

    public void begin() {
        getStoryEvent(getStartingEventId()).startEvent();
    }

    public Mission getCurrentMission() {
        return getCampaign().getMission(getCurrentMissionId());
    }

    public StoryEvent getCurrentEvent() {
        if(null == currentEventId) {
            return null;
        }
        return storyEvents.get(currentEventId);
    }

    //region File I/O
    @Override
    public void writeToXml(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent);
        writeToXmlEnd(pw1, indent);
    }

    protected void writeToXmlBegin(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent++) + "<storyArc type=\"" + this.getClass().getName() + "\">");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "title", title);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "description", description);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "startNew", startNew);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "startingEventId", startingEventId);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "currentMissionId", currentMissionId);
    }

    protected void writeToXmlEnd(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, indent, "storyArc");
    }

    protected void parseStoryEvents(NodeList nl) {
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn = nl.item(x);
                if (wn.getNodeType() != Node.ELEMENT_NODE ||
                        wn.getNodeName()!="storyEvent") {
                    continue;
                }
                UUID id = UUID.fromString(wn.getAttributes().getNamedItem("uuid").getTextContent().trim());
                StoryEvent event = StoryEvent.generateInstanceFromXML(wn, getCampaign());
                if(null != event) {
                    event.setStoryArc(this);
                    event.setId(id);
                    storyEvents.put(id, event);
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
        }
    }

    protected void parseStoryMissions(NodeList nl) {
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn = nl.item(x);
                if (wn.getNodeType() != Node.ELEMENT_NODE ||
                        wn.getNodeName()!="mission") {
                    continue;
                }
                UUID id = UUID.fromString(wn.getAttributes().getNamedItem("uuid").getTextContent().trim());
                Mission mission = Mission.generateInstanceFromXML(wn, getCampaign(), null);
                if(null != mission) {
                    storyMissions.put(id, mission);
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
        }
    }

    protected void parseStoryScenarios(NodeList nl) {
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn = nl.item(x);
                if (wn.getNodeType() != Node.ELEMENT_NODE ||
                        wn.getNodeName()!="scenario") {
                    continue;
                }
                UUID id = UUID.fromString(wn.getAttributes().getNamedItem("uuid").getTextContent().trim());
                Scenario scenario = Scenario.generateInstanceFromXML(wn, getCampaign(), null);
                if(null != scenario) {
                    storyScenarios.put(id, scenario);
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
        }
    }

    public static @Nullable StoryArc parseFromXML(final NodeList nl) {
        final StoryArc storyArc = new StoryArc();
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn = nl.item(x);
                if (wn.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                switch (wn.getNodeName()) {
                    case "title":
                        storyArc.setTitle(wn.getTextContent().trim());
                        break;
                    case "description":
                        storyArc.setDescription(wn.getTextContent().trim());
                        break;
                    case "startNew":
                        storyArc.setStartNew(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    case "startingEventId":
                        storyArc.setStartingEventId(UUID.fromString(wn.getTextContent().trim()));
                        break;
                    case "storyEvents":
                        storyArc.parseStoryEvents(wn.getChildNodes());
                        break;
                    case "storyMissions":
                        storyArc.parseStoryMissions(wn.getChildNodes());
                        break;
                    case "storyScenarios":
                        storyArc.parseStoryScenarios(wn.getChildNodes());
                        break;


                    default:
                        break;
                }
            }
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            return null;
        }
        return storyArc;
    }



    //endregion File I/O

    /**
     * @return a list of all of the story arcs in the default and userdata folders
     */
    public static List<StoryArc> getStoryArcs() {
        final List<StoryArc> presets = loadStoryArcsFromDirectory(
                new File(MekHqConstants.STORY_ARC_DIRECTORY));
        presets.addAll(loadStoryArcsFromDirectory(
                new File(MekHqConstants.USER_STORY_ARC_DIRECTORY)));
        final NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();
        presets.sort((p0, p1) -> naturalOrderComparator.compare(p0.toString(), p1.toString()));
        return presets;
    }

    public static List<StoryArc> loadStoryArcsFromDirectory(final @Nullable File directory) {
        if ((directory == null) || !directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }

        final List<StoryArc> storyArcs = new ArrayList<>();
        for (final File file : Objects.requireNonNull(directory.listFiles())) {
            final StoryArc storyArc = parseFromFile(file);
            if (storyArcs != null) {
                storyArcs.add(storyArc);
            }
        }

        return storyArcs;
    }

    public static @Nullable StoryArc parseFromFile(final @Nullable File file) {
        final Document xmlDoc;
        try (InputStream is = new FileInputStream(file)) {
            xmlDoc = MekHqXmlUtil.newSafeDocumentBuilder().parse(is);
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            return null;
        }

        final Element element = xmlDoc.getDocumentElement();
        element.normalize();

        return parseFromXML(element.getChildNodes());
    }

}
