/*
 * Created on 28/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.actions;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * @author Lindsay Bradford
 *
 */
public class CutAction extends YAWLBaseAction
        implements TooltipTogglingWidget, SpecificationSelectionSubscriber  {

    private static final CutAction INSTANCE = new CutAction();

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Cut");
        putValue(Action.LONG_DESCRIPTION, "Cut the selected elements");
        putValue(Action.SMALL_ICON, getPNGIcon("cut"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("X"));
    }

    private CutAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.DeletableElementSelected));
    }

    public static CutAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        NetGraph graph = getGraph();
        YAWLTask task = graph.viewingCancellationSetOf();
        boolean cutCellsIncludeCancellationTask = false;

        Object[] selectedCells = graph.getSelectionCells();    // can return null
        if (selectedCells != null) {
            for (Object o : selectedCells) {
                if (o instanceof VertexContainer) {
                    o = ((VertexContainer) o).getVertex();
                }
                if (task.equals(o)) {
                    cutCellsIncludeCancellationTask = true;
                }
            }
        }

        graph.stopUndoableEdits();
        graph.changeCancellationSet(null);
        graph.startUndoableEdits();

        TransferHandler.getCutAction().actionPerformed(
                new ActionEvent(getGraph(), event.getID(), event.getActionCommand()));
        PasteAction.getInstance().setEnabled(true);

        if (! cutCellsIncludeCancellationTask) {
            graph.stopUndoableEdits();
            graph.changeCancellationSet(task);
            graph.startUndoableEdits();
        }
    }

    public String getEnabledTooltipText() {
        return " Cut the selected elements ";
    }

    public String getDisabledTooltipText() {
        return " You must have a number of net elements selected" +
                " to cut them ";
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.DeletableElementSelected);
    }
}