/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.logging.YawlLogServletInterface;

/**
 * 
 * @hibernate.class table = "workitem_event"
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 * 
 */
@Entity
public class YWorkItem {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
    private static final long serialVersionUID = 2006030080l;

    public enum Status {
        Enabled,Fired,Executing,
        Complete,IsParent,Deadlocked, Cancelled}

//    public static final String statusEnabled = "Enabled";
//    public static final String statusFired = "Fired";
//    public static final String statusExecuting = "Executing";
//    public static final String statusComplete = "Complete";
//    public static final String statusIsParent = "Is parent";
//    public static final String statusDeadlocked = "Deadlocked";
//    public static final String statusDeleted = "Cancelled";

    private YWorkItemID _workItemID;
    private String _specificationID;
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;

    private Status _status;
    private String _whoStartedMe;
    private boolean _allowsDynamicCreation;
    private Element _dataList;

    private YWorkItem _parent;
    private Set<YWorkItem> _children = new HashSet<YWorkItem>();

    private static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private static DateFormat _df = new SimpleDateFormat("MMM:dd H:mm:ss");


    /**********************************/
    /* INSERTED VARIABLES AND METHODS */
    /*********************************/
    private String lastevent = "0";
    public String thisId = null;
    private String data_string = null;



    /**
     * Constructor<P>
     *
     * Default constructor is required for use by Hibernate (reflection?)
     */
    public YWorkItem() {
        super();
    }

    public void addToRepository() {

        Logger.getLogger(this.getClass()).debug("--> addToRepository");
        if ((_workItemRepository.getWorkItem(this.getWorkItemID().toString())) != null) {

            YWorkItem work = _workItemRepository.getWorkItem(this.getWorkItemID().toString());

        }
        _workItemRepository.addNewWorkItem(this);
    }
    public void setWorkItemID(YWorkItemID workitemid) {
        this._workItemID = workitemid;
    }

    private Long _id;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    protected Long getId() {
		return _id;
	}
    
	protected void setId( Long id ) {
		_id = id;
	}
    
    @Basic
    public String getThisId() {
        return thisId;
    }

    public void setThisId(String a) {
        thisId = a;
    }

    public void setInitData(Element data) {
        _dataList = data;
        data_string = getDataString();
    }

    public void completeData(Document output) throws YPersistenceException {

        Element root = output.getRootElement();

        java.util.List list = root.getChildren();
        Iterator iter = list.listIterator();

        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlLogServletInterface.getInstance().logData(child.getName(), child.getValue(), lastevent, "o");
        }

    }


    /***********************************/


    public YWorkItem(String specificationID, YWorkItemID workItemID,
                     boolean allowsDynamicCreation, boolean isDeadlocked) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specificationID + " WorkItem=" + workItemID.getTaskID());
        _workItemID = workItemID;
        _specificationID = specificationID;
        _enablementTime = new Date();
        _status = isDeadlocked ? Status.Deadlocked : Status.Enabled;
        _allowsDynamicCreation = allowsDynamicCreation;
        _workItemRepository.addNewWorkItem(this);

        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/

        try {

            YawlLogServletInterface.getInstance().logWorkItemEvent(workItemID.getCaseID().toString(),
                    workItemID.getTaskID()
                    , _status, _whoStartedMe, _specificationID);
	    setThisId(_workItemID.toString() + "!" + _workItemID.getUniqueID());
//        YPersistance.getInstance().storeData(this);
// TODO           if (pmgr != null) {
//                pmgr.storeObject(this);
//            }

            /*******************************/
        } catch (YPersistenceException e) {
            throw e;
        } catch (RuntimeException e) {
            // igtnore
        } catch (Exception e2) {
            // ignore
        }
    }


    /*
     * Creates a fired WorkItem.  Private method.
     */
    private YWorkItem(YWorkItemID workItemID, String specificationID,
                      Date workItemCreationTime, YWorkItem parent,
                      boolean allowsDynamicInstanceCreation) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specificationID + " WorkItem=" + workItemID.getTaskID());

        _workItemID = workItemID;
        _specificationID = specificationID;
        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        _status = Status.Fired;
        _workItemRepository.addNewWorkItem(this);
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/
        YawlLogServletInterface.getInstance().logWorkItemEvent(workItemID.getCaseID().toString(),
                workItemID.getTaskID()
                , _status, _whoStartedMe, _specificationID);
	setThisId(_workItemID.toString() + "!" + _workItemID.getUniqueID());

//	YPersistance.getInstance().storeData(this);
// TODO       if (pmgr != null) {
//            pmgr.storeObject(this);
//        }
        /*******************************/
    }


    public YWorkItem createChild(YIdentifier childCaseID) throws YPersistenceException {
        if (this._parent == null) {
            YIdentifier parentCaseID = getWorkItemID().getCaseID();
            if (childCaseID.getParent() != parentCaseID) {
                return null;
            }
            YWorkItem childItem = new YWorkItem(
                    new YWorkItemID(childCaseID, getWorkItemID().getTaskID()),
                    _specificationID,
                    getEnablementTime(),
                    this,
                    _allowsDynamicCreation
            );

            /*
              MODIFIED FOR PERSISTANCE
             */
            setStatus(Status.IsParent);
            _children.add(childItem);
            if (_children.size() == 1) {
                YawlLogServletInterface.getInstance().logWorkItemEvent(_workItemID.getCaseID().toString(),
                        _workItemID.getTaskID()
                        , _status, _whoStartedMe, _specificationID, null);

            }
            return childItem;
        }
        return null;
    }

    public void setStatusToDelete() throws YPersistenceException {
        /*MODIFIED FOR PERSISTANCE*/
        setStatus(Status.Cancelled);
        //_status = statusDeleted;
    }

    public void setStatusToStarted(String userName) throws YPersistenceException {
        if (_status != Status.Fired) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + Status.Executing + "\"]");
        }
        /*
          MODIFIED FOR PERSISTANCE
         */
        setStatus(Status.Executing);
        //_status = statusExecuting;

        _startTime = new Date();
        _whoStartedMe = userName;

        /*
          INSERTED FOR PERSISTANCE
         */
//	YPersistance.getInstance().updateData(this);
// TODO       if (pmgr != null) {
//            pmgr.updateObject(this);
//        }


        lastevent = YawlLogServletInterface.getInstance().logWorkItemEvent(_workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /****************************/
    }


    public void setStatusToComplete() throws YPersistenceException {
        if (_status != Status.Executing) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + Status.Complete + "\"]");
        }
        /*
          MODIFIED FOR PERSISTANCE
         */
        setStatus(Status.Complete);
        //_status = statusComplete;

        /*
         * Check if all siblings  are completed, if so then
         * the parent is completed too.
         * */
        boolean parentcomplete = true;
        Set<YWorkItem> siblings = _parent.getChildren();

        for (YWorkItem sibling : siblings) {
            if (sibling.getStatus() != Status.Complete)
                parentcomplete = false;
        }

        /********************************/
        /* INSERTED FOR PERSISTANCE */
        /**********************************/
//	YPersistance.getInstance().removeData(this);
//        if (parentcomplete) {
//              YPersistance.getInstance().removeData(_parent);
//        }
//  TODO      if (pmgr != null) {
//            pmgr.deleteObject(this);
//            if (parentcomplete) {
//                pmgr.deleteObject(_parent);
//            }
//        }


        YawlLogServletInterface.getInstance().logWorkItemEvent(_parent.getCaseID().toString(),
                _parent.getTaskID()
                , _status, _whoStartedMe, _specificationID);

        YawlLogServletInterface.getInstance().logWorkItemEvent(_workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /************************************/
    }


    public void rollBackStatus() throws YPersistenceException {
        if (!_status.equals(Status.Executing)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be rolledBack to \"" + Status.Fired + "\"]");
        }
        //_status = statusFired;
        /*
	  MODIFIED FOR PERSISTANCE
	 */
        setStatus(Status.Fired);
        YawlLogServletInterface.getInstance().logWorkItemEvent(_workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /*************************/

        _startTime = null;
        _whoStartedMe = null;

        /*
          INSERTED FOR PERSISTANCE
         */
//	YPersistance.getInstance().updateData(this);
//  TODO      if (pmgr != null) {
//            pmgr.updateObject(this);
//        }

    }


    public void setData(Element data) throws YPersistenceException {
        _dataList = data;

        /**********************/
        /* FOR PERSISTANCE */
        /*********************/
        data_string = getDataString();
//	YPersistance.getInstance().updateData(this);
//  TODO      if (pmgr != null) {
//            pmgr.updateObject(this);
//        }

        // XXX: FIXME: is this expected behavior?
        if( data == null ) {
        	throw new RuntimeException( "is this expected behavior?" );
        }
        java.util.List list = data.getChildren();
        Iterator iter = list.listIterator();
        /*
          FOR LOGGING
         */
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlLogServletInterface.getInstance().logData(child.getName(), child.getValue(), lastevent, "i");
        }

    }


    //#################################################################################
    //                              accessors
    //#################################################################################
    @Transient
    public YWorkItemID getWorkItemID() {
        return _workItemID;
    }

    @Basic
    public Date getEnablementTime() {
        return _enablementTime;
    }
    
    /**
     * Inserted for hibernate
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setEnablementTime(Date time) {
    	_enablementTime = time;
    }

    @Transient
    public String getEnablementTimeStr() {
        return _df.format(_enablementTime);
    }

    @Basic
    public Date getFiringTime() {
        return _firingTime;
    }
    
    /**
     * Inserted for hibernate 
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setFiringTime(Date time) {
    	_firingTime = time;
    }

    @Transient
    public String getFiringTimeStr() {
        return _df.format(_firingTime);
    }


    /**
     * @hibernate.property column="start_time"
     */
    @Basic
    public Date getStartTime() {
        return _startTime;
    }
    
    /**
     * Inserted for hibernate
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setStartTime(Date time) {
    	_startTime = time;
    }

    @Transient
    public String getStartTimeStr() {
        return _df.format(_startTime);
    }

    @Basic
    public Status getStatus() {
        return _status;
    }

    public void setStatus(Status status) {
        this._status = status;
    }

    @ManyToOne
    public YWorkItem getParent() {
        return _parent;
    }
    
    /**
     * Inserted for hibernate
     * @param item
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setParent(YWorkItem item) {
    	_parent = item;
    }

    @OneToMany(mappedBy="parent")
    public Set<YWorkItem> getChildren() {
        return _children;
    }

    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setChildren(Set<YWorkItem> children) {
    	_children = children;
    }


    @Transient
    public YIdentifier getCaseID() {
        return _workItemID.getCaseID();
    }

    @Transient
    public String getTaskID() {
        return _workItemID.getTaskID();
    }

    @Transient
    public String getIDString() {
        return _workItemID.toString();
    }


    public String toString() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1) + ":" + getIDString();
    }

    @Basic
    public String getUserWhoIsExecutingThisItem() {
        if (_status == Status.Executing) {
            return _whoStartedMe;
        } else
            return null;
    }

    //todo Q by LA: do we need this method 4 hibernate? otherwise delete
    private void setUserWhoIsExecutingThisItem(String person) {
    	_whoStartedMe = person;
    }

    @Basic
    public boolean allowsDynamicCreation() {
        return _allowsDynamicCreation;
    }

    //todo Q by LA: do we need this method 4 hibernate? otherwise delete
    private void setAllowsDynamicCreation(boolean b) {
    	_allowsDynamicCreation = b;
    }

    @Basic
    public String getDataString() {
        if (_dataList != null) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(_dataList);
        }
        return null;
    }
    
    /**
     * Inserted for hibernate
     * @param s
     */
    private void setDataString(String s) {
    	// FIXME TODO Do nothing?  Should this value be persisted at all?
    	// Note: the original code did nothing with the persisted value of data_string, it is persisted,
    	// but never read back in.  Is this the functionality that we want?
        //todo LA Comment: this got used in the system restore code but was a total hack.
        //i think we could live without it.
    }


    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<workItem>");
        xmlBuff.append("<taskID>").append(getTaskID()).append("</taskID>");
        xmlBuff.append("<caseID>").append(getCaseID()).append("</caseID>");
        xmlBuff.append("<uniqueID>").append(getUniqueID()).append("</uniqueID>");
        xmlBuff.append("<specID>").append(_specificationID).append("</specID>");
        xmlBuff.append("<status>").append(getStatus()).append("</status>");
        if (_dataList != null) {
            xmlBuff.append("<data>").append(getDataString())
                    .append("</data>");
        }
        xmlBuff.append("<enablementTime>")
                .append(_df.format(getEnablementTime()))
                .append("</enablementTime>");
        if (this.getFiringTime() != null) {
            xmlBuff.append("<firingTime>")
                    .append(_df.format(getFiringTime()))
                    .append("</firingTime>");
        }
        if (this.getStartTime() != null) {
            xmlBuff.append("<startTime>")
                    .append(_df.format(getStartTime()))
                    .append("</startTime>");
            xmlBuff.append("<assignedTo>")
                    .append(getUserWhoIsExecutingThisItem())
                    .append("</assignedTo>");
        }
        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

    @Transient
    private String getUniqueID() {
        return _workItemID.getUniqueID();
    }

    @Basic
    public String getSpecificationID() {
        return _specificationID;
    }

    //todo Q by LA: needed 4 hibernate ?
    private void setSpecificationID(String specificationID) {
    	_specificationID = specificationID;
    }
}
