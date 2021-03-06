/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


import org.cpsolver.studentsct.model.Course;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCourse.XCourseSerializer.class)
public class XCourse extends XCourseId {
	private static final long serialVersionUID = 1L;
    private String iSubjectArea = null;
    private String iCourseNumber = null;
    private String iDepartment = null;
    private String iConsentLabel = null, iConsentAbbv = null;
    private String iNote = null;
    private String iDetails = null;
    private int iLimit = 0;
    private int iProjected = 0;
    private Integer iWkEnroll = null, iWkChange = null, iWkDrop = null;
    private String iCreditAbbv = null, iCreditText = null;

    public XCourse() {
    	super();
    }
    
    public XCourse(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
    
    public XCourse(CourseOffering course, OnlineSectioningHelper helper) {
    	this(course);
    }

    public XCourse(CourseOffering course) {
    	super(course);
		iSubjectArea = course.getSubjectAreaAbbv().trim();
		iCourseNumber = course.getCourseNbr().trim();
		iNote = course.getScheduleBookNote();
		iDepartment = (course.getSubjectArea().getDepartment().getDeptCode() == null ? course.getSubjectArea().getDepartment().getAbbreviation() : course.getSubjectArea().getDepartment().getDeptCode());
        boolean unlimited = false;
        iLimit = 0;
        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
        	if (config.isUnlimitedEnrollment()) unlimited = true;
        	iLimit += config.getLimit();
        }
        if (course.getReservation() != null)
        	iLimit = course.getReservation();
        if (iLimit >= 9999) unlimited = true;
        if (unlimited) iLimit = -1;
        iProjected = (course.getProjectedDemand() != null ? course.getProjectedDemand().intValue() : course.getDemand() != null ? course.getDemand().intValue() : 0);
		iWkEnroll = course.getInstructionalOffering().getLastWeekToEnroll();
		iWkChange = course.getInstructionalOffering().getLastWeekToChange();
		iWkDrop = course.getInstructionalOffering().getLastWeekToDrop();
		if (course.getConsentType() != null) {
			iConsentLabel = course.getConsentType().getLabel();
			iConsentAbbv = course.getConsentType().getAbbv();
		}
        if (course.getCredit() != null) {
        	iCreditAbbv = course.getCredit().creditAbbv();
        	iCreditText = course.getCredit().creditText();
        }
    }
    
    public XCourse(Course course) {
    	super(course);
		iSubjectArea = course.getSubjectArea();
		iCourseNumber = course.getCourseNumber();
		iNote = course.getNote();
        iLimit = course.getLimit();
        iProjected = course.getProjected();
        if (course.getCredit() != null) {
        	int split = course.getCredit().indexOf('|');
        	if (split >= 0) {
        		iCreditAbbv = course.getCredit().substring(0, split);
        		iConsentLabel = course.getCredit().substring(split + 1);
        	}
        }
    }

    /** Subject area */
    public String getSubjectArea() {
        return iSubjectArea;
    }

    /** Course number */
    public String getCourseNumber() {
        return iCourseNumber;
    }

    /** Course offering limit */
    public int getLimit() {
        return iLimit;
    }
    
    public int getProjected() { return iProjected; }
    
	public Integer getLastWeekToEnroll() { return iWkEnroll; }
	public Integer getLastWeekToChange() { return iWkChange; }
	public Integer getLastWeekToDrop() { return iWkDrop; }
	public String getDepartment() { return iDepartment; }
	public String getConsentLabel() { return iConsentLabel; }
	public String getConsentAbbv() { return iConsentAbbv; }

    /** Course note */
    public String getNote() { return iNote; }
    
	public String getDetails(AcademicSessionInfo session, CourseDetailsProvider provider) throws SectioningException {
		if (iDetails == null && provider != null)
			iDetails = provider.getDetails(session, getSubjectArea(), getCourseNumber());
		return iDetails;
	}
	
    /**
     * Get credit (Online Student Scheduling only)
     */
    public String getCreditAbbv() { return iCreditAbbv; }
    public String getCreditText() { return iCreditText; }
    public String getCredit() { return getCreditAbbv() == null ? null : getCreditAbbv() + "|" + getCreditText(); }
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		iSubjectArea = (String)in.readObject();
		iCourseNumber = (String)in.readObject();
		iDepartment = (String)in.readObject();
		iConsentLabel = (String)in.readObject();
		iConsentAbbv = (String)in.readObject();
		iNote = (String)in.readObject();
		iDetails = (String)in.readObject();
		iLimit = in.readInt();
		iProjected = in.readInt();
		iWkEnroll = (in.readBoolean() ? in.readInt() : null);
		iWkChange = (in.readBoolean() ? in.readInt() : null);
		iWkDrop = (in.readBoolean() ? in.readInt() : null);
		iCreditAbbv = (String)in.readObject();
		iCreditText = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(iSubjectArea);
		out.writeObject(iCourseNumber);
		out.writeObject(iDepartment);
		out.writeObject(iConsentLabel);
		out.writeObject(iConsentAbbv);
		out.writeObject(iNote);
		out.writeObject(iDetails);
		out.writeInt(iLimit);
		out.writeInt(iProjected);
		out.writeBoolean(iWkEnroll != null);
		if (iWkEnroll != null)
			out.writeInt(iWkEnroll);
		out.writeBoolean(iWkChange != null);
		if (iWkChange != null)
			out.writeInt(iWkChange);
		out.writeBoolean(iWkDrop != null);
		if (iWkDrop != null)
			out.writeInt(iWkDrop);
		out.writeObject(iCreditAbbv);
		out.writeObject(iCreditText);
	}
	
	public static class XCourseSerializer implements Externalizer<XCourse> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCourse object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCourse readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCourse(input);
		}
	}
}