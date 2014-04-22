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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.AbstractDataBindingDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.InputBindingDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindingDialog;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class VariableTablePanel extends JPanel
        implements ActionListener, ListSelectionListener {

    private VariableTable table;
    private final DataVariableDialog parent;
    private JToolBar toolbar;
    private final TableType tableType;
    private boolean isEditing;

    // toolbar buttons
    private JButton btnUp;
    private JButton btnDown;
    private JButton btnAdd;
    private JButton btnDel;
    private JButton btnInMapping;
    private JButton btnOutMapping;
    private JButton btnAutoMapping;
    private JButton btnMIVar;
    private JButton btnExAttributes;
    private StatusPanel status;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public VariableTablePanel(java.util.List<VariableRow> rows, TableType tableType,
                              String netElementName, DataVariableDialog parent) {
        this.parent = parent;
        this.tableType = tableType;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10,10,0,10));
        JScrollPane scrollPane = new JScrollPane(createTable(rows, tableType, netElementName));
        scrollPane.setSize(new Dimension(tableType.getPreferredWidth(), 180));
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        if (tableType == TableType.Net) {
            btnInMapping.setVisible(false);
            btnOutMapping.setVisible(false);
            btnAutoMapping.setVisible(false);
            btnMIVar.setVisible(false);
            btnExAttributes.setVisible(false);
        }
        table.getSelectionModel().addListSelectionListener(this);
        enableButtons(true);
    }


    public void valueChanged(ListSelectionEvent event) {
        enableButtons(! isEditing());
    }


    public VariableTable getTable() { return table; }


    public void showErrorStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.ERROR, more);
    }


    public void showOKStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.OK, more);
    }


    public void clearStatus() {
        status.clear();
     }


    public void showMIButton(boolean show) { btnMIVar.setVisible(show); }


    public java.util.List<String> getScopeNames() {
        java.util.List<String> names = YDataHandler.getScopeNames();
        if (tableType != TableType.Net) {
            names.remove("Local");
        }
        return names;
    }


    public void actionPerformed(ActionEvent event) {
        clearStatus();
        String action = event.getActionCommand();
        if (action.equals("Add")) {
            table.addRow();
            setEditMode(true);
        }
        else if (action.equals("Del")) {
            table.removeRow();
            enableButtons(true);
        }
        else if (action.equals("Up")) {
            table.moveSelectedRowUp();
        }
        else if (action.equals("Down")) {
            table.moveSelectedRowDown();
        }
        else if (action.equals("InBinding")) {
            showBindingDialog(YDataHandler.INPUT);
        }
        else if (action.equals("OutBinding")) {
            showBindingDialog(YDataHandler.OUTPUT);
        }
        else if (action.equals("Autobind")) {
            autobind();
        }
        else if (action.equals("MarkMI")) {
            int row = table.getSelectedRow();
            String error = parent.setMultiInstanceRow(table.getSelectedVariable());
            if (error != null) showErrorStatus(error, null);
            table.selectRow(row);
        }
        else if (action.equals("ExAt")) {
            VariableRow row = table.getSelectedVariable();
            if (row != null) {
                new ExtendedAttributesDialog(parent, row.getAttributes(), row.getName())
                        .setVisible(true);
                table.getTableModel().setTableChanged(true);     // to flag update
                parent.enableApplyButton();
            }
        }
    }


    private void showBindingDialog(int scope) {
        int selectedRow = table.getSelectedRow();
        java.util.List<VariableRow> netVars =
                parent.getNetTablePanel().getTable().getVariables();
        java.util.List<VariableRow> taskVars = table.getVariables();
        String taskID = parent.getTask().getID();
        AbstractDataBindingDialog dialog = null;

        if (scope == YDataHandler.INPUT) {
            dialog = new InputBindingDialog(taskID, table.getSelectedVariable(),
                    netVars, taskVars);
        }
        else if (scope == YDataHandler.OUTPUT) {
            dialog = new OutputBindingDialog(taskID, table.getSelectedVariable(),
                    netVars, taskVars, parent.getOutputBindings());
        }
        if (dialog != null) {
            if (table.hasMultiInstanceRow()) {
                dialog.setMultiInstanceHandler(parent.getMultiInstanceHandler());
            }
            dialog.setVisible(true);
            if (dialog.hasChanges()) parent.enableApplyButton();
            table.getTableModel().fireTableDataChanged();
        }
        table.selectRow(selectedRow);
    }


    public VariableRow getVariableAtRow(int row) {
        return table.getVariables().get(row);
    }

    public boolean isEditing() {return isEditing; }

    public void showToolBar(boolean show) { toolbar.setVisible(show); }


    public VariableTablePanel copy() {
        return new VariableTablePanel(table.getVariables(), tableType,
                table.getNetElementName(), parent);
    }

    public DataVariableDialog getVariableDialog() { return parent; }

    private JTable createTable(java.util.List<VariableRow> rows, TableType tableType,
                               String netElementName) {
        table = new VariableTable(tableType.getModel());
        table.setVariables(rows);
        table.setNetElementName(netElementName);
        VariableRowUsageEditor usageEditor = new VariableRowUsageEditor(this);
        table.setDefaultEditor(Integer.class, usageEditor);
        VariableRowStringEditor stringEditor = new VariableRowStringEditor(this);
        table.setDefaultEditor(String.class, stringEditor);
        VariableRowUsageRenderer usageRenderer = new VariableRowUsageRenderer();
        table.setDefaultRenderer(Integer.class, usageRenderer);
        VariableRowStringRenderer stringRenderer =
                new VariableRowStringRenderer(parent.getOutputBindings());
        table.setDefaultRenderer(String.class, stringRenderer);
        fixSelectorColumn(table);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar() {
        toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        btnAdd = createToolBarButton("plus", "Add", " Add ");
        toolbar.add(btnAdd);
        btnDel = createToolBarButton("minus", "Del", " Remove ");
        toolbar.add(btnDel);
        btnUp = createToolBarButton("arrow_up", "Up", " Move up ");
        toolbar.add(btnUp);
        btnDown = createToolBarButton("arrow_down", "Down", " Move down ");
        toolbar.add(btnDown);
        btnInMapping = createToolBarButton("inMapping", "InBinding", " Input Bindings ");
        toolbar.add(btnInMapping);
        btnOutMapping = createToolBarButton("outMapping", "OutBinding", " Output Bindings ");
        toolbar.add(btnOutMapping);
        btnAutoMapping = createToolBarButton("generate", "Autobind", " Smart Data Bindings ");
        toolbar.add(btnAutoMapping);
        btnExAttributes = createToolBarButton("exat", "ExAt", " Ext. Attributes ");
        toolbar.add(btnExAttributes);
        btnMIVar = createToolBarButton("miVar", "MarkMI", " Mark as MI ");
        toolbar.add(btnMIVar);
        status = new StatusPanel(parent);
        toolbar.add(status);
        return toolbar;
    }


    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }

    private void fixSelectorColumn(JTable table) {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(15);
        column.setMaxWidth(15);
        column.setResizable(false);
    }

    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    protected void enableButtons(boolean enable) {
        VariableRow row = table.getSelectedVariable();
        boolean hasRowSelected = table.getSelectedRow() > -1;
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && hasRowSelected);
        btnUp.setEnabled(enable && hasRowSelected);
        btnDown.setEnabled(enable && hasRowSelected);
        if (btnInMapping.isVisible()) {
            btnInMapping.setEnabled(enable && hasRowSelected &&
                    (row.isInput() || row.isInputOutput()));
        }
        if (btnOutMapping.isVisible()) {
            btnOutMapping.setEnabled(enable && hasRowSelected &&
                    (row.isOutput() || row.isInputOutput()));
        }
        if (btnExAttributes.isVisible()) {
            btnExAttributes.setEnabled(enable && hasRowSelected);
        }
        if (btnMIVar.isVisible()) btnMIVar.setEnabled(enable && shouldEnableMIButton());
        if (btnAutoMapping.isVisible()) {
            btnAutoMapping.setEnabled(enable && shouldEnableAutoBindingButton());
        }
    }


    private boolean shouldEnableMIButton() {
        VariableRow row = table.getSelectedVariable();
        YDataHandler handler = SpecificationModel.getHandler().getDataHandler();

        // MI button can enable if the row is already MI (to allow toggling) or
        // there's no current MI row AND the row's data type is MI valid
        return row != null && (row.isMultiInstance() ||
                ( ! table.hasMultiInstanceRow() &&
                handler.getMultiInstanceItemNameAndType(row.getDataType()) != null));
    }


    private boolean shouldEnableAutoBindingButton() {
        if (parent.getNetTablePanel().getTable().getRowCount() == 0) return false;
        for (VariableRow row : table.getVariables()) {
            if (! hasBinding(row)) return true;
        }
        return false;
    }


    private boolean hasBinding(VariableRow row) {
        return (row.isInput() && row.getMapping() != null) ||
               (row.isOutput() && parent.getOutputBindings().hasBinding(row.getName()));
    }


    private void autobind() {
        boolean changed = false;
        for (VariableRow row : table.getVariables()) {
            if (! hasBinding(row)) {
                changed = parent.createAutoBinding(row) || changed;
            }
        }
        if (changed) table.getTableModel().fireTableDataChanged();
    }


    protected void setEditMode(boolean editing) {
        isEditing = editing;
        parent.setInserting(editing);
        enableButtons(!editing);
    }

}