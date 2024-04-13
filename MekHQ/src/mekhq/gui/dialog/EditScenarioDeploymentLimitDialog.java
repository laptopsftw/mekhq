/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog;

import megamek.client.ui.baseComponents.MMComboBox;
import mekhq.campaign.mission.BotForceRandomizer;
import mekhq.campaign.mission.ScenarioDeploymentLimit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditScenarioDeploymentLimitDialog extends JDialog {

    private JFrame frame;
    private ScenarioDeploymentLimit deploymentLimit;
    private boolean newLimit;

    private JSpinner spnQuantity;
    private MMComboBox<ScenarioDeploymentLimit.QuantityType> choiceQuantityType;
    private MMComboBox<ScenarioDeploymentLimit.CountType> choiceCountType;

    public EditScenarioDeploymentLimitDialog(JFrame parent, boolean modal, ScenarioDeploymentLimit limit) {
        super(parent, modal);
        this.frame = parent;
        if(limit == null) {
            deploymentLimit = new ScenarioDeploymentLimit();
            newLimit = true;
        } else {
            deploymentLimit = limit;
            newLimit = false;
        }
        initComponents();
        setLocationRelativeTo(parent);
        pack();
    }

    private void initComponents() {
        //final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.EditScenarioDeploymentLimitsDialog",
        //        MekHQ.getMHQOptions().getLocale());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        setTitle("Edit Scenario Deployment Limits");

        getContentPane().setLayout(new BorderLayout());
        JPanel panMain = new JPanel(new GridBagLayout());
        JPanel panButtons = new JPanel(new GridLayout(0, 2));

        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.gridy = 1;
        leftGbc.gridwidth = 1;
        leftGbc.weightx = 0.0;
        leftGbc.weighty = 0.0;
        leftGbc.insets = new Insets(0, 0, 5, 10);
        leftGbc.fill = GridBagConstraints.NONE;
        leftGbc.anchor = GridBagConstraints.NORTHWEST;

        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 1;
        rightGbc.gridy = 1;
        rightGbc.gridwidth = 1;
        rightGbc.weightx = 1.0;
        rightGbc.weighty = 0.0;
        rightGbc.insets = new Insets(0, 10, 5, 0);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.NORTHWEST;

        JLabel lblQuantityType = new JLabel("Quantity Type:");
        panMain.add(lblQuantityType, leftGbc);
        choiceQuantityType = new MMComboBox("choiceQuantityType", ScenarioDeploymentLimit.QuantityType.values());
        choiceQuantityType.setSelectedItem(deploymentLimit.getQuantityType());
        panMain.add(choiceQuantityType, rightGbc);


        JLabel lblCountType = new JLabel("Maximum Type:");
        leftGbc.gridy++;
        panMain.add(lblCountType, leftGbc);
        choiceCountType = new MMComboBox("choiceCountType", ScenarioDeploymentLimit.CountType.values());
        choiceCountType.setSelectedItem(deploymentLimit.getCountType());
        rightGbc.gridy++;
        panMain.add(choiceCountType, rightGbc);


        JLabel lblQuantity = new JLabel("Maximum Quantity:");
        leftGbc.gridy++;
        panMain.add(lblQuantity, leftGbc);
        spnQuantity = new JSpinner(new SpinnerNumberModel(deploymentLimit.getQuantityLimit(),
                1, 100, 1));
        rightGbc.gridy++;
        panMain.add(spnQuantity, rightGbc);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(this::complete);
        JButton btnClose = new JButton("Cancel");
        btnClose.addActionListener(this::cancel);
        panButtons.add(btnOk);
        panButtons.add(btnClose);

        getContentPane().add(panMain, BorderLayout.CENTER);
        getContentPane().add(panButtons, BorderLayout.PAGE_END);
    }

    private void complete(ActionEvent evt) {
        deploymentLimit.setQuantityLimit((int) spnQuantity.getValue());
        deploymentLimit.setQuantityType(choiceQuantityType.getSelectedItem());
        deploymentLimit.setCountType(choiceCountType.getSelectedItem());
        this.setVisible(false);
    }

    private void cancel(ActionEvent evt) {
        if(newLimit) {
            deploymentLimit = null;
        }
        this.setVisible(false);
    }

    public ScenarioDeploymentLimit getDeploymentLimit() {
        return deploymentLimit;
    }
}
