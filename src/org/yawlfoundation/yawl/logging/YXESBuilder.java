/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 21/04/2010
 */
public class YXESBuilder {

    protected boolean _ignoreUnknownEvents = false;
    Logger _log = LogManager.getLogger(this.getClass());

    public YXESBuilder() { }

    public YXESBuilder(boolean ignoreUnknownEvents) {
        _ignoreUnknownEvents = ignoreUnknownEvents;
    }

    public String buildLog(YSpecificationID specid, XNode events) {
        if (events != null) {
            XNode root = beginLogOutput(specid);
            processEvents(root, events);
            return root.toPrettyString(true);
        }
        return null;
    }


    protected void processEvents(XNode root, XNode yawlEvents) {
        for (XNode yawlEvent : yawlEvents.getChildren()) {
            XNode trace = root.addChild(traceNode(yawlEvent.getAttributeValue("id")));
            processCasePredicates(yawlEvent, trace);
            processCaseEvents(yawlEvent, trace);
            trace.sort(new XESTimestampComparator());
        }
    }


    protected String translateEvent(String yawlEvent) {
        String xesEvent;
        if (yawlEvent.equals(YWorkItemStatus.statusEnabled.toString())) {
            xesEvent = "schedule";
        } else if (yawlEvent.equals(YWorkItemStatus.statusExecuting.toString())) {
            xesEvent = "start";
        } else if (yawlEvent.equals(YWorkItemStatus.statusComplete.toString())) {
            xesEvent = "complete";
        } else if (yawlEvent.equals(YEventLogger.CASE_CANCEL)) {
            xesEvent = "pi_abort";
        } else if (yawlEvent.equals(YEventLogger.NET_START)) {
            xesEvent = "schedule";
        } else if (yawlEvent.equals(YEventLogger.NET_COMPLETE)) {
            xesEvent = "complete";
        } else if (yawlEvent.equals(YEventLogger.NET_CANCEL)) {
            xesEvent = "ate_abort";
        } else if (yawlEvent.equals(YWorkItemStatus.statusDeleted.toString())) {
            xesEvent = "ate_abort";
        } else if (yawlEvent.equals(YWorkItemStatus.statusCancelledByCase.toString())) {
            xesEvent = "pi_abort";
        } else if (yawlEvent.equals(YWorkItemStatus.statusFailed.toString())) {
            xesEvent = "ate_abort";
        } else if (yawlEvent.equals(YWorkItemStatus.statusForcedComplete.toString())) {
            xesEvent = "complete";
        } else if (yawlEvent.equals(YWorkItemStatus.statusSuspended.toString())) {
            xesEvent = "suspend";
        } else xesEvent = "unknown";

        return xesEvent;
    }


    protected XNode traceNode(String id) {
        XNode trace = new XNode("trace");
        trace.addChild(stringNode("concept:name", id));
        return trace;
    }


    protected XNode stringNode(String key, String value) {
        return entryNode("string", key, value);
    }


    protected XNode dateNode(String key, String value) {
        return entryNode("date", key, value);
    }


    protected XNode floatNode(String key, String value) {
        return entryNode("float", key, value);
    }


    protected XNode intNode(String key, String value) {
        return entryNode("int", key, value);
    }


    protected XNode booleanNode(String key, String value) {
        return entryNode("boolean", key, value);
    }


    protected String getComment() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return "Generated by the YAWL Engine " + df.format(new Date(System.currentTimeMillis()));
    }


    /**
     * *********************************************************************
     */

    private XNode beginLogOutput(YSpecificationID specid) {
        XNode log = new XNode("log");

        log.addComment(getComment());

        log.addAttribute("xes.version", "1.0");
        log.addAttribute("xes.features", "arbitrary-depth");
        log.addAttribute("openxes.version", "1.5");
        log.addAttribute("xmlns", "http://code.deckfour.org/xes");

        log.addChild(extensionNode("Lifecycle", "lifecycle",
                "http://code.fluxicon.com/xes/lifecycle.xesext"));
        log.addChild(extensionNode("Time", "time",
                "http://code.fluxicon.com/xes/time.xesext"));
        log.addChild(extensionNode("Concept", "concept",
                "http://code.fluxicon.com/xes/concept.xesext"));
        log.addChild(extensionNode("Semantic", "semantic",
                "http://code.fluxicon.com/xes/semantic.xesext"));
        log.addChild(extensionNode("Organizational", "org",
                "http://code.fluxicon.com/xes/org.xesext"));

        XNode gTrace = log.addChild(globalNode("trace"));
        gTrace.addChild(stringNode("concept:name", "UNKNOWN"));

        XNode gEvent = log.addChild(globalNode("event"));
        gEvent.addChild(dateNode("time:timestamp", "1970-01-01T00:00:00.000+01:00"));
        gEvent.addChild(stringNode("concept:name", "UNKNOWN"));
        gEvent.addChild(stringNode("lifecycle:transition", "UNKNOWN"));
        gEvent.addChild(stringNode("concept:instance", "UNKNOWN"));

        XNode classifier = log.addChild(new XNode("classifier"));
        classifier.addAttribute("name", "Activity classifier");
        classifier.addAttribute("keys",
                "concept:name concept:instance lifecycle:transition");

        log.addChild(stringNode("concept:name", specid.toString()));

        return log;
    }


    private XNode extensionNode(String name, String prefix, String uri) {
        XNode extn = new XNode("extension");
        extn.addAttribute("name", name);
        extn.addAttribute("prefix", prefix);
        extn.addAttribute("uri", uri);
        return extn;
    }


    private XNode globalNode(String scope) {
        XNode global = new XNode("global");
        global.addAttribute("scope", scope);
        return global;
    }


    private XNode entryNode(String name, String key, String value) {
        XNode entry = new XNode(name);
        entry.addAttribute("key", key);
        entry.addAttribute("value", value);
        return entry;
    }


    private XNode eventNode(String timestamp, String taskName, String transition, String instanceID) {
        XNode eventNode = new XNode("event");
        eventNode.addChild(dateNode("time:timestamp", timestamp));
        eventNode.addChild(stringNode("concept:name", taskName));
        eventNode.addChild(stringNode("lifecycle:transition", transition));
        eventNode.addChild(stringNode("concept:instance", instanceID));
        return eventNode;
    }


    private void addDataEvents(XNode eventNode, XNode dataItems) {
        if (dataItems != null) {
            for (XNode dataItem : dataItems.getChildren()) {
                String value = dataItem.getChildText("value");
                if ((value != null) && value.startsWith("<")) {
                    processComplexTypeDataEvent(dataItem, eventNode);
                } else {
                    eventNode.addChild(formatDataNode(dataItem));
                }
            }
        }
    }


    private void processComplexTypeDataEvent(XNode dataItem, XNode eventNode) {
        String typeDef = dataItem.getChildText("typeDefinition");
        Map<String, String> typeMap = parseComplexTypeDefinition(typeDef);
        String rootName = dataItem.getChildText("name");
        XNode valueNode = new XNodeParser().parse(dataItem.getChildText("value"));
        processComplexTypeDataValues(eventNode, valueNode, typeMap, rootName);
    }


    private void processComplexTypeDataValues(XNode eventNode, XNode valueNode,
                                              Map<String, String> typeMap, String rootName) {
        if (valueNode.hasChildren()) {
            for (XNode subValueNode : valueNode.getChildren()) {
                String subName = concatName(rootName, subValueNode.getName());
                String subValue = subValueNode.getText();
                String typeName = typeMap.get(subValueNode.getName());

                // a node with no subnodes and no text gets a default value of ""
                if ((subValue == null) && (!subValueNode.hasChildren())) {
                    subValue = "";
                }
                if (subValue != null) {
                    eventNode.addChild(formatDataNode(subName, subValue, typeName));
                }

                // recurse
                processComplexTypeDataValues(eventNode, subValueNode, typeMap, subName);
            }
        }
    }


    private String concatName(String rootName, String subName) {
        StringBuilder s = new StringBuilder(rootName);
        s.append('/').append(subName);
        return s.toString();
    }


    private Map<String, String> parseComplexTypeDefinition(String typeDef) {
        return parseComplexTypeDefinition(new XNodeParser().parse(typeDef));
    }


    private Map<String, String> parseComplexTypeDefinition(XNode typeNode) {
        Map<String, String> element2TypeMap = new Hashtable<String, String>();
        if (typeNode.hasChildren()) {
            for (XNode child : typeNode.getChildren()) {
                if (child.getName().endsWith("element")) {
                    String name = child.getAttributeValue("name");
                    String type = child.getAttributeValue("type");
                    if ((name != null) && (type != null)) {
                        element2TypeMap.put(name, type);
                    }
                }
                element2TypeMap.putAll(parseComplexTypeDefinition(child));    // recurse
            }
        }
        return element2TypeMap;
    }


    private XNode formatDataNode(XNode node) {
        return formatDataNode(node.getChildText("name"), node.getChildText("value"),
                node.getChildText("typeDefinition"));
    }


    private XNode formatDataNode(String name, String value, String typeDefinition) {
        String tag = getTagType(typeDefinition);
        value = tag.equals("date") ? formatDateValue(typeDefinition, value) :
                JDOMUtil.encodeEscapes(value);
        return entryNode(tag, name, value);
    }


    private String formatDateValue(String type, String value) {
        if (value == null || value.length() == 0) {
            return "1970-01-01T00:00:00";
        }
        String formattedDateTime = value;                   // default (if datetime)
        if (type.endsWith("date")) {
            String blankTime = "T00:00:00";

            // a date type is of the form CCYY-MM-DD - but the year can have more than
            // 4 chars and may start with '-', while the date may be followed by a Z or
            // timezone (like '+10:00'). We need to find the insertion position
            // immediately after the 2 day characters.
            int insertPos = value.indexOf('-', 4) + 6;
            if (insertPos == value.length()) {
                formattedDateTime = value + blankTime;
            } else {
                formattedDateTime = value.substring(0, insertPos) + blankTime +
                        value.substring(insertPos);
            }
        } else if (type.endsWith("time")) {
            formattedDateTime = "1970-01-01T" + value;
        }
        return formattedDateTime;
    }


    private String cdataIfComplex(String s) {
        if (s.startsWith("<")) {
            return "<![CDATA[" + s + "]]>";
        }
        return s;
    }


    private String getTagType(String typeDef) {
        if (typeDef == null) return "string";

        if (typeDef.contains(":")) {
            typeDef = typeDef.substring(typeDef.indexOf(':') + 1);
        }
        if (XSDType.isBooleanType(typeDef)) {
            return "boolean";
        } else if (XSDType.isDateType(typeDef)) {
            return "date";
        } else if (XSDType.isFloatType(typeDef)) {
            return "float";
        } else if (XSDType.isIntegralType(typeDef)) {
            return "int";
        } else return "string";
    }


    private void processCasePredicates(XNode yawlEvent, XNode trace) {
        List<XNode> predNodes = yawlEvent.getChildren("predicate");
        for (XNode predNode : predNodes) {
            String key = predNode.getChildText("key");
            trace.addChild(stringNode(key, predNode.getChildText("value")));
            yawlEvent.removeChild(predNode);
        }
    }


    private void processCaseEvents(XNode yawlEvent, XNode trace) {
        for (XNode netInstance : yawlEvent.getChildren()) {
            for (XNode taskInstance : netInstance.getChildren()) {
                processTaskEvents(taskInstance, trace);
            }
        }
    }

    private void processTaskEvents(XNode taskInstance, XNode trace) {
        String taskName = taskInstance.getChildText("taskname");
        String instanceID = taskInstance.getChildText("engineinstanceid");
        XNode dataChanges = extractDataChangeEvents(taskInstance);
        for (XNode event : taskInstance.getChildren("event")) {
            String descriptor = getDescriptor(event);
            if (!descriptor.equals("DataValueChange")) {
                String transition = translateEvent(descriptor);
                if (_ignoreUnknownEvents && "unknown".equals(transition)) {
                   continue;
                }
                String timestamp = event.getChildText("timestamp");
                XNode node = eventNode(timestamp, taskName, transition, instanceID);
                if (descriptor.equals("Executing")) {
                    addDataEvents(node, dataChanges.getChild("input"));
                } else if (descriptor.equals("Complete")) {
                    addDataEvents(node, dataChanges.getChild("output"));
                }
                addLogPredicates(node, event.getChild("dataItems"));
                trace.addChild(node);
            }
        }
    }

    private void addLogPredicates(XNode eventNode, XNode dataItems) {
        if (dataItems != null) {
            for (XNode item : dataItems.getChildren()) {
                if ("Predicate".equals(item.getChildText("descriptor"))) {
                    eventNode.addChild(formatDataNode("logpredicate",
                            item.getChildText("value"), "string"));
                }
            }
        }
    }


    private XNode extractDataChangeEvents(XNode taskInstance) {
        XNode node = new XNode("dataItems");
        XNode inputs = node.addChild("input");
        XNode outputs = node.addChild("output");

        for (XNode event : taskInstance.getChildren("event")) {
            if (getDescriptor(event).equals("DataValueChange")) {
                XNode items = event.getChild("dataItems");
                for (XNode item : items.getChildren()) {
                    String descriptor = getDescriptor(item);
                    if (descriptor.startsWith("Input")) {
                        inputs.addChild(item);
                    }
                    else if (descriptor.startsWith("Output")) {
                        outputs.addChild(item);
                    }
                    else if (descriptor.equals("Predicate")) {
                        String name = item.getChildText("name");

                        // reset name to generic key with var name attached
                        item.getChild("name").setText(name.replaceFirst(".*(?=#)",
                                "logpredicate"));

                        if (name.startsWith("Start")) {
                            inputs.addChild(item);
                        }
                        else if (name.startsWith("Completion")){
                            outputs.addChild(item);
                        }
                    }
                }
            }
        }
        return node;
    }


    private String getDescriptor(XNode node) {
        return node.getChildText("descriptor");
    }

}
