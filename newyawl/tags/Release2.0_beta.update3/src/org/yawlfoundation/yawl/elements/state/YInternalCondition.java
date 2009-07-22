/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.state;

import org.yawlfoundation.yawl.elements.YConditionInterface;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.util.YIdentifierBag;

import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/04/2003
 * Time: 10:17:37
 * 
 */
public class YInternalCondition extends YNetElement implements YConditionInterface {
    private YIdentifierBag _bag;
    public YTask _myTask;
    public static String _mi_active = "mi_active";
    public static String _mi_entered = "mi_entered";
    public static String _mi_executing = "mi_executing";
    public static String _mi_complete = "mi_complete";


    public YInternalCondition(String id, YTask myTask) {
        super(id);
        _bag = new YIdentifierBag(this);
        _myTask = myTask;
    }

    /**
     * Adds an identifier to the collection.
     * @param identifier
     */
    public void add(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        _bag.addIdentifier(pmgr, identifier);
    }

    /**
     * @param identifier
     * @return true iff this contains identifier.
     */
    public boolean contains(YIdentifier identifier) {
        return _bag.contains(identifier);
    }

    /**
     * @return true iff this contains one or more identifier.
     */
    public boolean containsIdentifier() {
        return _bag.getIdentifiers().size() > 0;
    }

    /**
     * Get the number of identifier matching the the one passed in.
     * @param identifier
     * @return the number of equal identifiers in this.
     */
    public int getAmount(YIdentifier identifier) {
        return _bag.getAmount(identifier);
    }

    /**
     * @return a List of the identifiers in the condition numbering 1 or more.
     */
    public List getIdentifiers() {
        return _bag.getIdentifiers();
    }

    /**
     * Removes one YIdentifier from this condition.  If there are none
     * inside then make no change to the state of this.
     */
    public YIdentifier removeOne(YPersistenceManager pmgr) throws YPersistenceException {
        YIdentifier id = (YIdentifier) getIdentifiers().get(0);
        _bag.remove(pmgr, id, 1);
        return id;
    }

    /**
     * Removes one YIdentifier equal to identifier from the condition. If there are none
     * inside then make no change to the state of this.
     * @param identifier
     */
    public void removeOne(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        _bag.remove(pmgr, identifier, 1);
    }

    /**
     * Remove from this amount YIdentifiers equal to identifier.
     * @param identifier
     * @param amount the amount to remove.
     * @throws YStateException iff amount is greater than the number of YIdentifiers
     * held inside this, and further more no change will be made to the state of this.
     */
    public void remove(YPersistenceManager pmgr, YIdentifier identifier, int amount) throws YStateException, YPersistenceException {
        _bag.remove(pmgr, identifier, amount);
    }

    /**
     * Removes all the YIdentifiers equal to identifier.
     * @param identifier
     */
    public void removeAll(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        _bag.remove(pmgr, identifier, _bag.getAmount(identifier));
    }

    public void removeAll(YPersistenceManager pmgr) {
        _bag.removeAll();
    }

    public String toString() {
        return getID() + "[" + _myTask.toString() + "]";
    }
}