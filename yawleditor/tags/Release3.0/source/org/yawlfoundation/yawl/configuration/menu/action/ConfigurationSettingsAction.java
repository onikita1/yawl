/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created By Jingxin XU
 */

package org.yawlfoundation.yawl.configuration.menu.action;

import org.yawlfoundation.yawl.configuration.menu.ResourceLoader;
import org.yawlfoundation.yawl.configuration.net.NetConfiguration;
import org.yawlfoundation.yawl.configuration.net.NetConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.util.FileLocations;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class ConfigurationSettingsAction extends YAWLBaseAction
        implements FileStateListener {

    {
        Publisher.getInstance().subscribe(this);
    }

    {
        putValue(Action.SHORT_DESCRIPTION, "Process Configuration Settings");
        putValue(Action.NAME, "Preferences...");
        putValue(Action.LONG_DESCRIPTION, "Process Configuration Settings");
        putValue(Action.SMALL_ICON, getMenuIcon("settings"));
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
    }


    public void actionPerformed(ActionEvent event) {
        final NetGraph net = this.getGraph();
        net.getNetModel().beginUpdate();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConfigurationReferenceJDialog dialog =
                        new ConfigurationReferenceJDialog(new JFrame(), net);
                dialog.setLocationRelativeTo(YAWLEditor.getInstance());
                dialog.setVisible(true);
            }
        });
        net.getNetModel().endUpdate();
    }


    public void specificationFileStateChange(FileState state) {
        setEnabled(state == FileState.Open);
    }


    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconName + ".png");
    }


    /**
     *
     * @author jingxin
     */
    private class ConfigurationReferenceJDialog extends JDialog {

        private final NetGraph net;

        /** Creates new form ConfigurationReferenceJDialog */
        public ConfigurationReferenceJDialog(Frame parent, NetGraph net) {
            super(parent, true);
            this.net = net;
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            newElementConfig = new Checkbox();
            AotGreyOut = new Checkbox();
            denyblocking = new Checkbox();
            changDefault = new Checkbox();
            okButton = new JButton();
            fldWendy = new JTextField();

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("Process Configuration Settings");

            newElementConfig.setLabel("Set new elements configurable");

            AotGreyOut.setLabel("Preview process automatically");

            denyblocking.setLabel("Deny blocking input ports");

            changDefault.setLabel("Allow changing default configurations");

            okButton.setText("OK");
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    okButtonActionPerformed(evt);
                }
            });

            JPanel wendyPanel = getWendyPanel();

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                            .addComponent(denyblocking, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(newElementConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(AotGreyOut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(changDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(wendyPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(175, 175, 175)
                                                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap(19, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(newElementConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(AotGreyOut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(denyblocking, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(changDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(wendyPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                                    .addComponent(okButton)
                                    .addContainerGap())
            );

            NetConfiguration netConfiguration =
                    NetConfigurationCache.getInstance().getOrAdd(net.getNetModel());
            if(!netConfiguration.getSettings().isAllowBlockingInputPorts()){
                this.denyblocking.setState(true);
            }

            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.AotGreyOut.setState(true);
            }

            if(netConfiguration.getSettings().isNewElementsConfigurable()){
                this.newElementConfig.setState(true);
            }

            if(netConfiguration.getSettings().isAllowChangingDefaultConfiguration()){
                this.changDefault.setState(true);
            }

            fldWendy.setText(getWendyPath(netConfiguration));

            pack();
            setResizable(false);
        }// </editor-fold>

        private void okButtonActionPerformed(ActionEvent evt) {
            NetConfiguration netConfiguration =
                    NetConfigurationCache.getInstance().get(net.getNetModel());

            netConfiguration.getSettings().setApplyAutoGreyOut(this.AotGreyOut.getState());
            netConfiguration.getSettings().setAllowBlockingInputPorts(!this.denyblocking.getState());
            netConfiguration.getSettings().setNewElementsConfigurable(this.newElementConfig.getState());
            netConfiguration.getSettings().setAllowChangingDefaultConfiguration(this.changDefault.getState());
            netConfiguration.getSettings().setWendyPath(checkPath(fldWendy.getText()));
            this.setVisible(false);
        }

        private String getWendyPath(NetConfiguration netConfiguration) {
            String path = netConfiguration.getSettings().getWendyPath();
            return path != null ? path : FileLocations.getHomeDir() + "wendy";
        }


        private JPanel getWendyPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new TitledBorder("Wendy (Process Configuration) Folder"));
            panel.add(buildFileButton(), BorderLayout.EAST);
            fldWendy.setPreferredSize(new Dimension(350, 25));
            panel.add(fldWendy, BorderLayout.CENTER);
            return panel;
        }

        private JButton buildFileButton() {
            JButton button = new JButton("...");
            button.setPreferredSize(new Dimension(25, 15));
            button.setToolTipText(" Select File Dialog ");

            final JDialog thisPanel = this;
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser(getInitialDir());
                    fileChooser.setDialogTitle("Select Wendy (Process Configuration) Folder");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (fileChooser.showOpenDialog(thisPanel) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        if (file != null) {
                            setPath(file);
                        }
                    }
                }
            });
            return button;
        }


        private String getInitialDir() {
            String path = fldWendy.getText();
            return (path != null) ? path.substring(0, path.lastIndexOf(File.separator)) : null;
        }

        private void setPath(File file) {
            try {
                fldWendy.setText(file.getCanonicalPath());
            }
            catch (IOException ioe) {
                fldWendy.setText(file.getAbsolutePath());
            }
        }

        private String checkPath(String path) {
            if (path.endsWith("/") || path.endsWith("\\")) {
                path = path.substring(0, path.length() - 1);
            }
            return path;
        }



        // Variables declaration - do not modify
        private Checkbox AotGreyOut;
        private Checkbox changDefault;
        private Checkbox denyblocking;
        private Checkbox newElementConfig;
        private JTextField fldWendy;
        private JButton okButton;
        // End of variables declaration

    }

}