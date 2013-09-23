package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class FlowConditionTableModel extends AbstractTableModel {

    protected List<YAWLFlowRelation> flows;
    protected boolean editable;

    private static final String[] COLUMN_LABELS = {"Target", "Condition"};
    private static final int NAME_COLUMN  = 0;
    private static final int CONDITION_COLUMN  = 1;


    public FlowConditionTableModel() {
        super();
    }


    public int getRowCount() {
        return (getFlows() != null) ? getFlows().size() : 0;
    }

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public String getColumnName(int column) {
        return COLUMN_LABELS[column];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int row, int column) {
        return column == CONDITION_COLUMN;
    }

    public Object getValueAt(int row, int col) {
        YAWLFlowRelation selected = getFlowAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                return selected.getTargetID();
            }
            case CONDITION_COLUMN:  {
                return selected.getPredicate();
            }
            default: {
                return null;
            }
        }
    }

    public void setValueAt(Object value, int row, int col) {
        YAWLFlowRelation selected = getFlowAtRow(row);
        if (col == CONDITION_COLUMN) {
            selected.setPredicate((String) value);
            fireTableRowsUpdated(row, row);
        }
    }


    public YAWLFlowRelation getFlowAtRow(int row) {
        if (getRowCount() == 0 || row < 0 || row >= getRowCount()) {
            return null;
        }
        return flows.get(row);
    }


    public void swapRows(int first, int second) {
        Collections.swap(flows, first, second);
        resetOrdering();
        fireTableRowsUpdated(first, second);
    }


    public void setFlows(List<YAWLFlowRelation> list) {
        flows = list;
        Collections.sort(flows);
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    public List<YAWLFlowRelation> getFlows() {
        return flows;
    }


    private void resetOrdering() {
        for (int i=0; i<getRowCount(); i++) {
            flows.get(i).setPriority(i);
        }
    }

}