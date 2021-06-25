/*
* MegaMek - Copyright (C) 2021 - The MegaMek Team
*
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free Software
* Foundation; either version 2 of the License, or (at your option) any later
* version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*/

package mekhq.gui.stratcon;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import mekhq.campaign.stratcon.StratconCampaignState;
import mekhq.campaign.stratcon.StratconTrackState;
import mekhq.gui.StratconTab;

/**
 * This class handles the UI for campaign VP/SP management 
 * @author NickAragua
 */
public class CampaignManagementDialog extends JDialog {
    private StratconCampaignState currentCampaignState;
    private StratconTab parent;
    JButton btnConvertVPToSP;
    JButton btnConvertSPtoBonusPart;
    JButton btnGMAddVP;
    JButton btnGMAddSP;
    JLabel lblTrackScenarioOdds;
    
    public CampaignManagementDialog(StratconTab parent) {
        this.parent = parent;
        this.setTitle("Manage SP/VP");
        initializeUI();
    }
    
    /**
     * Show the dialog for a given campaign state, and whether GM mode is on or not
     */
    public void display(StratconCampaignState campaignState, StratconTrackState currentTrack, boolean gmMode) {
        currentCampaignState = campaignState;
        
        btnConvertVPToSP.setEnabled(currentCampaignState.getVictoryPoints() > 0);
        btnConvertSPtoBonusPart.setEnabled(currentCampaignState.getSupportPoints() > 0);
        btnGMAddVP.setEnabled(gmMode);
        btnGMAddSP.setEnabled(gmMode);
        
        lblTrackScenarioOdds.setVisible(gmMode);
        if (gmMode) {
            lblTrackScenarioOdds.setText(String.format("Scenario Odds: %d%%", currentTrack.getScenarioOdds()));
        }
    }
    
    /**
     * One-time set up for all the buttons.
     */
    private void initializeUI() {
        GridLayout layout = new GridLayout();
        layout.setColumns(2);
        layout.setRows(0);
        layout.setHgap(1);
        layout.setVgap(1);
        
        getContentPane().removeAll();
        getContentPane().setLayout(layout);
        
        btnConvertVPToSP = new JButton();
        btnConvertVPToSP.setText("Convert VP to SP");
        btnConvertVPToSP.addActionListener(this::convertVPtoSPHandler);
        getContentPane().add(btnConvertVPToSP);
        
        btnConvertSPtoBonusPart = new JButton();
        btnConvertSPtoBonusPart.setText("Convert SP to bonus part");
        btnConvertSPtoBonusPart.addActionListener(this::convertSPtoBonusPartHandler);
        getContentPane().add(btnConvertSPtoBonusPart);
        
        btnGMAddVP = new JButton();
        btnGMAddVP.setText("Add VP (GM)");
        btnGMAddVP.addActionListener(this::gmAddVPHandler);
        getContentPane().add(btnGMAddVP);
        
        btnGMAddSP = new JButton();
        btnGMAddSP.setText("Add SP (GM)");
        btnGMAddSP.addActionListener(this::gmAddSPHandler);
        getContentPane().add(btnGMAddSP);
        
        lblTrackScenarioOdds = new JLabel();
        getContentPane().add(lblTrackScenarioOdds);
        
        pack();
    }
    
    private void convertVPtoSPHandler(ActionEvent e) {
        currentCampaignState.convertVictoryToSupportPoint();
        btnConvertVPToSP.setEnabled(currentCampaignState.getVictoryPoints() > 0);
        btnConvertSPtoBonusPart.setEnabled(currentCampaignState.getSupportPoints() > 0);
        parent.updateCampaignState();
    }
    
    private void convertSPtoBonusPartHandler(ActionEvent e) {
        currentCampaignState.useSupportPoint();
        currentCampaignState.getContract().addBonusParts(1);
        btnConvertSPtoBonusPart.setEnabled(currentCampaignState.getSupportPoints() > 0);
        parent.updateCampaignState();
    }
    
    private void gmAddVPHandler(ActionEvent e) {
        currentCampaignState.updateVictoryPoints(1);
        btnConvertVPToSP.setEnabled(currentCampaignState.getVictoryPoints() > 0);
        parent.updateCampaignState();
    }
    
    private void gmAddSPHandler(ActionEvent e) {
        currentCampaignState.addSupportPoints(1);
        btnConvertSPtoBonusPart.setEnabled(currentCampaignState.getSupportPoints() > 0);
        parent.updateCampaignState();
    }
}
