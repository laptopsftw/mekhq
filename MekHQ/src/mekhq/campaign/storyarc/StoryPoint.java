/*
 * StoryPoint.java
 *
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved
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
package mekhq.campaign.storyarc;

import mekhq.MekHqXmlSerializable;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;


import java.util.*;
import java.util.List;

/**
 * The StoryPoint abstract class is the basic building block of a StoryArc. StoryPoints can do
 * different things when they are started. When they are completed they may start other story points as
 * determined by the specific class and user input. StoryPoints are started in one of the following ways:
 *  - By being selected as the next story point by a prior StoryPoint
 *  - By meeting the trigger conditions that are checked in various places in Campaign such as a specific date
 **/
public abstract class StoryPoint implements Serializable, MekHqXmlSerializable {

    /** The story arc that this story point is a part of **/
    private StoryArc storyArc;

    /** The UUID id of this story point */
    private UUID id;

    /** a name for this story point **/
    private String name;

    /** A boolean that tracks whether the story point is currently active **/
    private boolean active;

    /** A StorySplash image to display in a dialog. It can return a null image */
    private StorySplash storySplash;

    /**
     * The id of a personality who is associated with this StoryPoint. May be null.
     */
    private UUID personalityId;

    /**
     * The id of the next story point to start when this one is completed. It can be null if a new story point should not be
     * triggered. It can also be overwritten by a StoryOutcome
     * **/
    private UUID nextStoryPointId;

    /** A map of all possible StoryOutcomes **/
    protected Map<String, StoryOutcome> storyOutcomes;

    /** A list of StoryTriggers to execute on completion, can be overwritten by StoryOutcome */
    List<StoryTrigger> storyTriggers;

    protected static final String NL = System.lineSeparator();

    public StoryPoint() {
        active = false;
        storyOutcomes =  new LinkedHashMap<>();
        storyTriggers = new ArrayList<>();
        storySplash = new StorySplash();
    }

    public void setStoryArc(StoryArc a) {
        this.storyArc = a;
        //also apply it to any triggers
        for(StoryTrigger storyTrigger : storyTriggers) {
            storyTrigger.setStoryArc(a);
        }
        //also might need to apply it to triggers in storyOutcomes
        for (Map.Entry<String, StoryOutcome> entry : storyOutcomes.entrySet()) {
            entry.getValue().setStoryArc(a);
        }
    }

    protected StoryArc getStoryArc() { return storyArc; }

    public void setId(UUID id) { this.id = id; }

    protected UUID getId() { return id; }

    public Boolean isActive() { return active; }

    public abstract String getTitle();

    public String getName() {
        return name;
    }

    public Image getImage() {
        if(storySplash.isDefault()) {
            return null;
        }
        return storySplash.getImage();
    }

    /**
     * Do whatever needs to be done to start this story point. Specific story point types may need to override this
     */
    public void start() {
        active = true;
    }

    /**
     * Complete the storyp point. Specific story point types may need to override this.
     */
    public void complete() {
        active = false;
        processOutcomes();
        processTriggers();
        proceedToNextStoryPoint();
    }

    private void processOutcomes() {
        StoryOutcome chosenOutcome = storyOutcomes.get(getResult());
        if(null != chosenOutcome) {
            nextStoryPointId = chosenOutcome.getNextStoryPointId();
            storyTriggers = chosenOutcome.getStoryTriggers();
        }
    }

    private void processTriggers() {
        for(StoryTrigger storyTrigger : storyTriggers) {
            storyTrigger.execute();
        }
    }

    protected abstract String getResult();

    protected String getObjective() {
        return "";
    }

    /**
     * Gets the next story point and if it is not null, starts it
     */
    protected void proceedToNextStoryPoint() {
        // get the next story point
        StoryPoint nextStoryPoint = getNextStoryPoint();
        if(null != nextStoryPoint) {
            nextStoryPoint.start();
        }
    }

    /**
     * determine the next story point in the story arc based on the point. This could have been changed depending
     * on StoryOutcome
     **/
    protected StoryPoint getNextStoryPoint() {
        return storyArc.getStoryPoint(nextStoryPointId);
    }


    public Personality getPersonality() {
        if(null == personalityId) {
            return null;
        }
        return storyArc.getPersonality(personalityId);
    }

    public Campaign getCampaign() {
        return getStoryArc().getCampaign();
    }

    //region I/O
    @Override
    public abstract void writeToXml(PrintWriter pw1, int indent);

    protected void writeToXmlBegin(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLOpenTag(pw1, indent++, "storyPoint", "name", name,"type", this.getClass());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "id", id);
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "active", active);
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personalityId", personalityId);
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "nextStoryPointId", nextStoryPointId);
        if(!storyOutcomes.isEmpty()) {
            MekHqXmlUtil.writeSimpleXMLOpenTag(pw1, indent++, "storyOutcomes");
            for (Map.Entry<String, StoryOutcome> entry : storyOutcomes.entrySet()) {
                entry.getValue().writeToXml(pw1, indent);
            }
            MekHqXmlUtil.writeSimpleXMLCloseTag(pw1, --indent, "storyOutcomes");
        }
        if(!storyTriggers.isEmpty()) {
            for (StoryTrigger trigger : storyTriggers) {
                trigger.writeToXml(pw1, indent);
            }
        }
        storySplash.writeToXML(pw1, indent);
    }

    protected void writeToXmlEnd(PrintWriter pw1, int indent) {
        MekHqXmlUtil.writeSimpleXMLCloseTag(pw1, indent, "storyPoint");
    }

    protected abstract void loadFieldsFromXmlNode(Node wn, Campaign c) throws ParseException;

    public static StoryPoint generateInstanceFromXML(Node wn, Campaign c) {
        StoryPoint retVal = null;
        NamedNodeMap attrs = wn.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            // Instantiate the correct child class, and call its parsing
            // function.
            retVal = (StoryPoint) Class.forName(className).getDeclaredConstructor().newInstance();

            retVal.name = wn.getAttributes().getNamedItem("name").getTextContent().trim();

            retVal.loadFieldsFromXmlNode(wn, c);

            // Okay, now load specific fields!
            NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                if (wn2.getNodeName().equalsIgnoreCase("id")) {
                    retVal.id = UUID.fromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("nextStoryPointId")) {
                    retVal.nextStoryPointId = UUID.fromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("personalityId")) {
                    retVal.personalityId = UUID.fromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("active")) {
                    retVal.active = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if(wn2.getNodeName().equalsIgnoreCase("storyTrigger")) {
                    StoryTrigger trigger = StoryTrigger.generateInstanceFromXML(wn2, c);
                    retVal.storyTriggers.add(trigger);
                } else if (wn2.getNodeName().equalsIgnoreCase(StorySplash.XML_TAG)) {
                    retVal.storySplash = StorySplash.parseFromXML(wn2);
                } else if (wn2.getNodeName().equalsIgnoreCase("storyOutcomes")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE)
                            continue;

                        if (!wn3.getNodeName().equalsIgnoreCase("storyOutcome")) {
                            // Error condition of sorts!
                            // Errr, what should we do here?
                            LogManager.getLogger().error("Unknown node type not loaded in storyOutcomes nodes: " + wn3.getNodeName());

                            continue;
                        }
                        StoryOutcome s = StoryOutcome.generateInstanceFromXML(wn3, c);

                        if (null != s) {
                            retVal.storyOutcomes.put(s.getResult(), s);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogManager.getLogger().error(ex);
        }

        return retVal;
    }

}
