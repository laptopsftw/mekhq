/*
 * EditPersonnelLogDialog.java
 *
 * Copyright (c) 2009 - Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
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

import java.awt.BorderLayout;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import megamek.client.ui.preferences.JWindowPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.logging.MMLogger;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.gui.control.EditPersonnelLogControl;

/**
 * @author Taharqa
 */
public class EditPersonnelLogDialog extends JDialog {
    private static final MMLogger logger = MMLogger.create(EditPersonnelLogDialog.class);

    private JFrame frame;
    private Campaign campaign;
    private Person person;

    private EditPersonnelLogControl editPersonnelLogControl;
    private JButton btnOK;

    /** Creates new form EditPersonnelLogDialog */
    public EditPersonnelLogDialog(JFrame parent, boolean modal, Campaign c, Person p) {
        super(parent, modal);
        Objects.requireNonNull(c);
        Objects.requireNonNull(p);

        this.frame = parent;
        campaign = c;
        person = p;

        initComponents();
        setLocationRelativeTo(parent);
        setUserPreferences();
    }

    private void initComponents() {
        final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.EditPersonnelLogDialog",
                MekHQ.getMHQOptions().getLocale());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName(resourceMap.getString("dialog.name"));
        setTitle(resourceMap.getString("dialog.title") + " " + person.getFullName());
        getContentPane().setLayout(new BorderLayout());

        editPersonnelLogControl = new EditPersonnelLogControl(frame, campaign, person);
        getContentPane().add(editPersonnelLogControl, BorderLayout.CENTER);

        btnOK = new JButton();
        btnOK.setText(resourceMap.getString("btnOK.text"));
        btnOK.setName("btnOK");
        btnOK.addActionListener(x -> this.setVisible(false));
        getContentPane().add(btnOK, BorderLayout.PAGE_END);

        pack();
    }

    @Deprecated // These need to be migrated to the Suite Constants / Suite Options Setup
    private void setUserPreferences() {
        try {
            PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(EditPersonnelLogDialog.class);
            this.setName("dialog");
            preferences.manage(new JWindowPreference(this));
        } catch (Exception ex) {
            logger.error("Failed to set user preferences", ex);
        }
    }
}
