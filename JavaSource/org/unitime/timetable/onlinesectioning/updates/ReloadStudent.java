/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

import net.sf.cpsolver.studentsct.model.Student;

/**
 * @author Tomas Muller
 */
public class ReloadStudent extends ReloadAllData {
	private Collection<Long> iStudentIds = null;
	
	public ReloadStudent(Long... studentIds) {
		iStudentIds = new ArrayList<Long>();
		for (Long studentId: studentIds)
			iStudentIds.add(studentId);
	}
	
	public ReloadStudent(Collection<Long> studentIds) {
		iStudentIds = studentIds;
	}

	
	public Collection<Long> getStudentIds() { return iStudentIds; }
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.load", "true"))) return false;

		helper.info(getStudentIds().size() + " students changed.");

		helper.beginTransaction();
		try {
			for (Long studentId: getStudentIds()) {
				Lock lock = server.lockStudent(studentId, (List<Long>)helper.getHibSession().createQuery(
						"select distinct e.courseOffering.InstructionalOffering.uniqueId from StudentClassEnrollment e where "+
                		"e.student.uniqueId = :studentId").setLong("studentId", studentId).list(), false);
				try {
					
					// Unload student
					Student oldStudent = server.getStudent(studentId);
					if (oldStudent != null)
						server.remove(oldStudent);

					// Load student
					org.unitime.timetable.model.Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					Student newStudent = null;
					if (student != null) {
						newStudent = loadStudent(student, server, helper);
						if (newStudent != null)
							server.update(newStudent);
					}
					
					server.notifyStudentChanged(studentId, (oldStudent == null ? null : oldStudent.getRequests()), (newStudent == null ? null : newStudent.getRequests()));

				} finally {
					lock.release();
				}
					
			}
			
			helper.commitTransaction();
			return true;
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		}
	}
	
	@Override
    public String name() { return "reload-student"; }
}