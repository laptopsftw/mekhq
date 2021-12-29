/*
 * ScenarioStoryPoint.java
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
package mekhq.campaign.storyarc.storypoint;

import mekhq.MekHqXmlSerializable;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.mission.Scenario;
import mekhq.campaign.mission.enums.ScenarioStatus;
import mekhq.campaign.storyarc.StoryPoint;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.util.UUID;

/**
 * Adds a scenario to the identified mission. Note that it will also create an id on the given scenario in campaign and
 * that scenario will trigger complete upon completion.
 */
public class ScenarioStoryPoint extends StoryPoint implements Serializable, MekHqXmlSerializable {

    /** track the scenario itself **/
    private Scenario scenario;

    /** The UUID of the MissionStoryPoint that this ScenarioStoryPoint is a part of **/
    private UUID missionStoryPointId;

    public ScenarioStoryPoint() {
        super();
    }

    @Override
    public String getTitle() {
        if(null != scenario) {
            return scenario.getName();
        }
        return "";
    }

    @Override
    public void start() {
        super.start();
        StoryPoint missionStoryPoint = getStoryArc().getStoryPoint(missionStoryPointId);
        if(null != missionStoryPoint && missionStoryPoint instanceof MissionStoryPoint) {
            Mission m = ((MissionStoryPoint) missionStoryPoint).getMission();
            if (null != m & null != scenario) {
                getStoryArc().getCampaign().addScenario(scenario, m);
            }
        }
    }

    private void setScenario(Scenario s) {
        this.scenario = s;
    }

    public Scenario getScenario() { return scenario; }

    @Override
    protected String getResult() {
        if(null == scenario || scenario.getStatus().isCurrent()) {
            return "";
        }
        ScenarioStatus status = scenario.getStatus();

        //the StoryOutcomes may not include this particular outcome so if this is the case we want to find the next
        //highest enum that is present.

        //if storyOutcomes are empty, then it doesn't really matter. Also if this status has an entry
        //in storyOutcomes then return it
        if(storyOutcomes.isEmpty() || null != storyOutcomes.get(status.name())) {
            return status.name();
        }

        //ok if we are here then we have storyOutcomes but not an exact match. We want to compare ordinals to get
        //the next best choice
        for(ScenarioStatus nextStatus : ScenarioStatus.values()) {
            if(nextStatus == ScenarioStatus.CURRENT) {
                //shouldn't happen, but ok
                continue;
            }
            if(null != storyOutcomes.get(nextStatus.name())) {
                if(status.ordinal() <= nextStatus.ordinal()) {
                    return nextStatus.name();
                }
            }
        }

        //if we are still here, return nothing because we probably want defaults
        return "";
    }

    @Override
    public String getObjective() {
        return "Complete " + scenario.getName() + " scenario";
    }

    @Override
    public void writeToXml(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent++);
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "missionStoryPointId", missionStoryPointId);
        if(null != scenario) {
            //if the scenario has a valid id, then just save this because the scenario is saved
            //and loaded elsewhere so we need to link it
            if (scenario.getId() > 0) {
                MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "scenarioId", scenario.getId());
            } else {
                scenario.writeToXml(pw1, indent);
            }
        }
        writeToXmlEnd(pw1, --indent);
    }

    @Override
    public void loadFieldsFromXmlNode(Node wn, Campaign c) throws ParseException {
        NodeList nl = wn.getChildNodes();

        for (int x = 0; x < nl.getLength(); x++) {
            Node wn2 = nl.item(x);

            try {
                if (wn2.getNodeName().equalsIgnoreCase("scenarioId")) {
                    int scenarioId = Integer.parseInt(wn2.getTextContent().trim());
                    if(null != c) {
                        Scenario s = c.getScenario(scenarioId);
                        this.setScenario(s);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("scenario")) {
                    Scenario s = Scenario.generateInstanceFromXML(wn2, c, null);
                    if(null != s) {
                        this.setScenario(s);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("missionStoryPointId")) {
                    missionStoryPointId = UUID.fromString(wn2.getTextContent().trim());
                }
            } catch (Exception e) {
                LogManager.getLogger().error(e);
            }
        }
    }
}
