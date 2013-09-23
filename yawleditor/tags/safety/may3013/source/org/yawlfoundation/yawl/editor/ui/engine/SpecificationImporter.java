/*
 * Created on 09/02/2006
 * YAWLEditor v1.4 
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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationFileHandler;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.ui.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMetaData;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpecificationImporter extends EngineEditorInterpretor {

    private static final Point DEFAULT_LOCATION = new Point(100,100);
    private static List<String> _invalidResourceReferences;

    private static SpecificationModel _model;
    private static YSpecificationHandler _handler;


    public SpecificationImporter() {
        _model = SpecificationModel.getInstance();
        _handler = SpecificationModel.getHandler();
        _invalidResourceReferences = new ArrayList<String>();
    }


    public void importSpecificationFromFile(String fileName) {
        if (! loadFile(fileName)) return;

        _model.setLoadInProgress(true);
        createEditorObjects();
        layoutEditorObjects();
        finaliseLoad();
        _model.setLoadInProgress(false);
    }


    private boolean loadFile(String fileName) {
        try {
            _model.loadFromFile(fileName);
        }
        catch (IOException ioe) {
            String errorMsg = ioe.getMessage();
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Failed to load specification.\n" +
                            (errorMsg.length() > 0 ? "Reason: " + errorMsg : ""),
                    "Specification File Load Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    private boolean layoutEditorObjects() {
        YLayout layout = SpecificationModel.getHandler().getLayout();
        if (layout != null) {
            try {
                LayoutImporter.importAndApply(layout);
                return true;
            }
            catch (YLayoutParseException ylpe) {
                // fall through to below
            }
        }
        removeUnnecessaryDecorators(_model);
        DefaultLayoutArranger.layoutSpecification();
        return false;
    }

    private void finaliseLoad() {
        Publisher.getInstance().publishOpenFileEvent();
        SpecificationUndoManager.getInstance().discardAllEdits();

        if (! _invalidResourceReferences.isEmpty()) {
            showInvalidResourceReferences();
            if (! YConnector.isResourceConnected()) {
                if (showDisconnectedResourceServiceWarning() == JOptionPane.YES_OPTION) {
                    SpecificationFileHandler.getInstance().processCloseRequest();
                }
            }
        }

        ConfigurationImporter.ApplyConfiguration();
        reset();
    }


    private void createEditorObjects() {
        initialise();
        convertEngineMetaData();
        importNets();
        populateEditorNets();
    }

    private void convertEngineMetaData() {
        YMetaData metaData = _handler.getSpecification().getMetaData();

        _model.setVersionNumber(metaData.getVersion());

        // reset version change for file open
        _model.setVersionChanged(false);
    }


    private void importNets() {
        YNet rootNet = _handler.getControlFlowHandler().getRootNet();
        importNet(rootNet);

        // import sub-nets
        for (YNet net : _handler.getControlFlowHandler().getNets()) {
            if (! net.equals(rootNet)) {
                importNet(net);
            }
        }
    }


    private NetGraphModel importNet(YNet engineNet) {
        NetGraph editorNet = new NetGraph(engineNet);
        editorNet.setName(engineNet.getID());
        _model.addNetNotUndoable(editorNet.getNetModel());

        YAWLEditorDesktop.getInstance().openNet(editorNet);
        engineToEditorNetMap.put(engineNet, editorNet.getNetModel());
        return editorNet.getNetModel();
    }


    private void populateEditorNets() {
        for (NetGraphModel netModel : _model.getNets()) {
            populateEditorNet((YNet) netModel.getDecomposition(), netModel);
        }
    }


    private void populateEditorNet(YNet engineNet, NetGraphModel editorNet) {
        EngineNetElementSummary engineNetElementSummary = new EngineNetElementSummary(engineNet);

        InputCondition inputCondition = new InputCondition(DEFAULT_LOCATION,
                engineNet.getInputCondition());
        editorNet.getGraph().addElement(inputCondition);
        editorNet.getGraph().setElementLabel(inputCondition, inputCondition.getName());
        engineToEditorElementMap.put(engineNet.getInputCondition(), inputCondition);

        OutputCondition outputCondition = new OutputCondition(DEFAULT_LOCATION,
                engineNet.getOutputCondition());
        editorNet.getGraph().addElement(outputCondition);
        editorNet.getGraph().setElementLabel(outputCondition, outputCondition.getName());
        engineToEditorElementMap.put(engineNet.getOutputCondition(), outputCondition);

        populateElements(engineNetElementSummary, editorNet);
        populateFlows(engineNetElementSummary.getFlows(), editorNet);
        removeImplicitConditions(engineNetElementSummary.getConditions(), editorNet);
        populateCancellationSetDetail(engineNetElementSummary.getTasksWithCancellationSets());
    }


    private void populateElements(EngineNetElementSummary engineNetSummary,
                                  NetGraphModel editorNet) {
        populateAtomicTasks(engineNetSummary.getAtomicTasks(), editorNet);
        populateCompositeTasks(engineNetSummary.getCompositeTasks(), editorNet);
        populateConditions(engineNetSummary.getConditions(), editorNet);
    }


    private void populateAtomicTasks(Set<YAtomicTask> engineAtomicTasks,
                                     NetGraphModel editorNet) {
        for (YAtomicTask engineAtomicTask : engineAtomicTasks) {
            YAWLAtomicTask editorAtomicTask;
            if (engineAtomicTask.isMultiInstance()) {
                editorAtomicTask = new MultipleAtomicTask(DEFAULT_LOCATION, engineAtomicTask);
            }
            else {
                editorAtomicTask = new AtomicTask(DEFAULT_LOCATION, engineAtomicTask);
            }
            updateGraph(editorNet.getGraph(), (YAWLTask) editorAtomicTask, engineAtomicTask);

            setTaskDecorators(engineAtomicTask, (YAWLTask) editorAtomicTask, editorNet);
            setTaskResources(engineAtomicTask, editorAtomicTask, editorNet);

            if (engineAtomicTask.getConfigurationElement() != null) {
                ConfigurationImporter.CTaskList.add((YAWLTask) editorAtomicTask);
                ConfigurationImporter.map.put(editorAtomicTask,
                        engineAtomicTask.getConfigurationElement());
                ConfigurationImporter.NetTaskMap.put(editorAtomicTask, editorNet);
            }

            engineToEditorElementMap.put(engineAtomicTask, editorAtomicTask);
        }
        finaliseRetainFamiliarMappings(editorNet);
    }


    private void updateGraph(NetGraph editorNet, YAWLVertex vertex,
                             YExternalNetElement netElement) {
        editorNet.addElement(vertex);

        String label = null;
        if (netElement.getName() != null) {
            label = netElement.getName();
        }
        else if ((netElement instanceof YTask) &&
                ((YTask) netElement).getDecompositionPrototype() != null) {
            label = ((YTask) netElement).getDecompositionPrototype().getID();
        }
        if (label != null) editorNet.setElementLabel(vertex, label);
    }


    private void setTaskDecorators(YTask engineTask, YAWLTask editorTask,
                                   NetGraphModel editorNet) {
        editorNet.setJoinDecorator(editorTask, engineToEditorJoin(engineTask),
                JoinDecorator.getDefaultPosition());
        editorNet.setSplitDecorator(editorTask, engineToEditorSplit(engineTask),
                SplitDecorator.getDefaultPosition());
    }


    private void populateCompositeTasks(Set<YCompositeTask> engineCompositeTasks,
                                        NetGraphModel editorNet) {
        for (YCompositeTask engineCompositeTask : engineCompositeTasks) {
            YAWLCompositeTask editorCompositeTask;
            if (engineCompositeTask.getMultiInstanceAttributes() == null) {
                editorCompositeTask = new CompositeTask(DEFAULT_LOCATION, engineCompositeTask);
            }
            else {
                editorCompositeTask = new MultipleCompositeTask(
                        DEFAULT_LOCATION, engineCompositeTask);
            }
            updateGraph(editorNet.getGraph(), (YAWLTask) editorCompositeTask, engineCompositeTask);

            setTaskDecorators(engineCompositeTask, (YAWLTask) editorCompositeTask, editorNet);

            if (engineCompositeTask.getConfigurationElement() != null) {
                ConfigurationImporter.CTaskList.add((YAWLTask) editorCompositeTask);
                ConfigurationImporter.map.put(editorCompositeTask,
                        engineCompositeTask.getConfigurationElement());
                ConfigurationImporter.NetTaskMap.put(editorCompositeTask, editorNet);
            }

            engineToEditorElementMap.put(engineCompositeTask, editorCompositeTask);
        }
    }


    /********************************************************************************/

    private void setTaskResources(YAtomicTask engineTask, YAWLAtomicTask editorTask,
                                  NetGraphModel editorNet) {
        Element rawResourceElement = engineTask.getResourcingSpecs();
        if (rawResourceElement != null) {
            ResourceMapping resourceMap = new ResourceMapping(editorTask, true);
            boolean badRef = resourceMap.parse(rawResourceElement, editorNet);
            if (badRef) {
                _invalidResourceReferences.add(editorNet.getName() + "::" + editorTask.getLabel());
            }
            editorTask.setResourceMapping(resourceMap);
        }
    }


    private void finaliseRetainFamiliarMappings(NetGraphModel editorNet) {
        Set<YAWLAtomicTask> taskSet = NetUtilities.getAtomicTasks(editorNet);
        for (YAWLAtomicTask task : taskSet) {
            ResourceMapping rMap = task.getResourceMapping();
            if (rMap != null) rMap.finaliseRetainFamiliarTasks(taskSet);
        }
    }



    private void showInvalidResourceReferences() {
        List<String> msgList = new ArrayList<String>();
        String template = "An invalid resource reference in Task '%s' of Net '%s' has been removed.";
        for (String ref : _invalidResourceReferences) {
            String[] split = ref.split("::");
            msgList.add(String.format(template, split[1], split[0]));
        }
        ProblemMessagePanel.getInstance().setProblemList("Invalid Resource References", msgList);
    }


    private void populateConditions(Set<YCondition> engineConditions,
                                    NetGraphModel editorNet) {
        for (YCondition engineCondition : engineConditions) {
            Condition editorCondition = new Condition(DEFAULT_LOCATION, engineCondition);
            updateGraph(editorNet.getGraph(), editorCondition, engineCondition);
            engineToEditorElementMap.put(engineCondition, editorCondition);
        }
    }


    private void populateFlows(Set<YFlow> engineFlows, NetGraphModel editorNet) {

        for (YFlow engineFlow : engineFlows) {
            YAWLVertex sourceEditorElement = (YAWLVertex) engineToEditorElementMap.get(
                    engineFlow.getPriorElement());
            YAWLVertex targetEditorElement = (YAWLVertex) engineToEditorElementMap.get(
                    engineFlow.getNextElement());
            YAWLFlowRelation editorFlow = editorNet.getGraph().connect(sourceEditorElement,
                    targetEditorElement);

            editorFlow.setPredicate(engineFlow.getXpathPredicate());
            if (engineFlow.getEvalOrdering() != null) {
                editorFlow.setPriority(engineFlow.getEvalOrdering());
            }

            // when a default flow is exported, it has no predicate or ordering recorded
            // (because it is the _default_ flow) - so when importing from that xml,
            // a default predicate and ordering need to be reinstated.
            if (engineFlow.isDefaultFlow()) {
                if (editorFlow.getPredicate() == null) {
                    editorFlow.setPredicate("true()");
                }
                editorFlow.setPriority(10000);        // ensure it's ordered last
            }
        }
    }

    private void populateCancellationSetDetail(Set<YTask> engineTasksWithCancellationSets) {
        for (YTask engineTask : engineTasksWithCancellationSets) {
            YAWLTask editorTask = (YAWLTask) engineToEditorElementMap.get(engineTask);

            CancellationSet editorTaskCancellationSet = new CancellationSet(editorTask);

            for (YExternalNetElement engineSetMember : engineTask.getRemoveSet()) {
                YAWLCell editorSetMember = (YAWLCell) engineToEditorElementMap.get(engineSetMember);

                if (editorFlowEngineConditionMap.get(engineSetMember) != null) {
                    YAWLFlowRelation replacementEditorFlow = (YAWLFlowRelation) engineToEditorElementMap.get(engineSetMember);
                    editorSetMember = replacementEditorFlow;
                }
                editorTaskCancellationSet.addMember(editorSetMember);
            }
            editorTask.setCancellationSet(editorTaskCancellationSet);
        }
    }

    private void removeImplicitConditions(Set<YCondition> engineConditions, NetGraphModel editorNet) {
        for (YCondition engineCondition : engineConditions) {
            if (engineCondition.isImplicit()) {
                Condition editorCondition = (Condition)
                        engineToEditorElementMap.get(engineCondition);

                YAWLFlowRelation sourceFlow = editorCondition.getOnlyIncomingFlow();
                YAWLFlowRelation targetFlow = editorCondition.getOnlyOutgoingFlow();
                if(sourceFlow != null && targetFlow != null) {
                    YAWLTask sourceTask = ((YAWLPort) sourceFlow.getSource()).getTask();
                    YAWLTask targetTask = ((YAWLPort) targetFlow.getTarget()).getTask();
                    if (sourceTask != null && targetTask != null) {
                        editorNet.getGraph().removeCellsAndTheirEdges(
                                new Object[] { editorCondition });

                        YAWLFlowRelation editorFlow =
                                editorNet.getGraph().connect(sourceTask, targetTask);

                        // map predicate & priority from removed condition to new flow
                        editorFlow.setPredicate(sourceFlow.getPredicate());
                        editorFlow.setPriority(sourceFlow.getPriority());

                        engineToEditorElementMap.put(engineCondition, editorFlow);
                    }
                }
            }
        }
    }


    private void removeUnnecessaryDecorators(SpecificationModel editorSpec) {
        for (NetGraphModel net : editorSpec.getNets())
            removeUnnecessaryDecorators(net);
    }


    private void removeUnnecessaryDecorators(NetGraphModel editorNet) {
        for (YAWLTask editorTask : NetUtilities.getAllTasks(editorNet)) {
            if (editorTask.hasJoinDecorator() && editorTask.getIncomingFlowCount() < 2) {
                editorNet.setJoinDecorator(
                        editorTask,
                        JoinDecorator.NO_TYPE,
                        JoinDecorator.NOWHERE
                );
            }
            if (editorTask.hasSplitDecorator() && editorTask.getOutgoingFlowCount() < 2) {
                editorNet.setSplitDecorator(
                        editorTask,
                        SplitDecorator.NO_TYPE,
                        SplitDecorator.NOWHERE
                );
            }
        }
    }

    private int engineToEditorJoin(YTask engineTask) {
        switch (engineTask.getJoinType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.XOR_TYPE;
    }

    private int engineToEditorSplit(YTask engineTask) {
        switch (engineTask.getSplitType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.AND_TYPE;
    }


    private int showDisconnectedResourceServiceWarning() {
        Object[] buttonText = {"Close", "Continue"};
        return JOptionPane.showOptionDialog(
                YAWLEditor.getInstance(),
                "The loaded specification contains resource settings, but the resource\n " +
                        "service is currently offline. This means that the settings cannot be\n "+
                        "validated and will be LOST if the specification is saved. It is\n " +
                        "suggested that the specification be closed, a valid connection to\n " +
                        "the resource service is established (via the Tools menu), then\n " +
                        "the specification be reloaded.\n\n" +
                        "Click the 'Close' button to close the loaded file (recommended)\n " +
                        "or the 'Continue' button to keep it loaded, but with resourcing\n " +
                        "settings stripped.",
                "Warning - read carefully",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                buttonText,
                buttonText[0]);
    }

}