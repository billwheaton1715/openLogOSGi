package com.paulwithers.openLog;

/*

 <!--
 Copyright 2013 Paul Withers
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and limitations under the License
 -->

 */

import java.io.Serializable;
import java.util.LinkedHashSet;

import javax.faces.component.UIComponent;

import com.ibm.jscript.InterpretException;

/**
 * @author withersp
 * @since 1.0.0
 * 
 */
public class OpenLogErrorHolder implements Serializable {

	// Changed from using TreeSet because there is a bug with that. If compareTo
	// returns 0, because it implements TreeMap,
	// it can still consider them equal, and ignore them -
	// http://blog.tremend.ro/2007/05/17/problem-when-adding-elements-to-a-treesetcomparator-some-elements-are-not-added/
	private LinkedHashSet<EventError> errors;
	private LinkedHashSet<EventError> events;
	private LinkedHashSet<EventError> loggedErrors = null;
	private static final long serialVersionUID = 1L;

	public OpenLogErrorHolder() {

	}

	/**
	 * LinkedHashSet (array of EventError objects in the order they were
	 * inserted)
	 * 
	 * @return LinkedHashSet Errors or Events as a list
	 */
	public LinkedHashSet<EventError> getErrors() {
		return errors;
	}

	/**
	 * Loads a list of EventError objects, see {@link #getErrors()}
	 * 
	 * @return LinkedHashSet
	 */
	public LinkedHashSet<EventError> getEvents() {
		return events;
	}

	/**
	 * @return LinkedHashSet of current logged EventError objects
	 */
	public LinkedHashSet<EventError> getLoggedErrors() {
		return errors;
	}

	/**
	 * @param loggedErrors
	 *            LinkedHashSet of EventError objects to add to the log
	 */
	public void setLoggedErrors(LinkedHashSet<EventError> loggedErrors) {
		this.loggedErrors = loggedErrors;
	}

	/**
	 * @param ie
	 *            InterpretException or null
	 * @param msg
	 *            specific Event or Error message to be passed
	 * @param control
	 *            component the error is associated with
	 * @param severity
	 *            integer (1-7) of severity
	 * @param unid
	 *            document error is related to
	 * @return
	 */
	private EventError createEventError(InterpretException ie, String msg, UIComponent control, int severity, String unid) {
		EventError newErr = new EventError();
		newErr.setError(ie);
		newErr.setMsg(msg);
		newErr.setControl(control);
		newErr.setSeverity(severity);
		newErr.setUnid(unid);
		return newErr;
	}

	public InterpretException getInterpretException(Object je) {
		try {
			String className = je.getClass().getName();
			if ("com.ibm.jscript.InterpretException".equals(className)) {
				return (InterpretException) je;
			} else {
				Throwable t = new Throwable(je.toString());
				InterpretException ie = new InterpretException(t);
				ie.setExpressionText(t.getMessage());
				return ie;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return new InterpretException(new Throwable(t.getMessage()));
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest.
	 * @param unid
	 *            This object is serializable to requestScope. But for safety,
	 *            it shouldn't include Domino objects. The code will look for
	 *            the document in the current database. If it can't be found, we
	 *            won't be able to get the document. So instead we'll just write
	 *            out the UNID in the error message.
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, this, 1, doc.getUniversalId());
	 * }
	 * </pre>
	 * 
	 *            The default level is 4.
	 */
	public void addError(Object je, UIComponent control, int severity, String unid) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, "", control, severity, unid);
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest.
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, this, 1);
	 * }
	 * </pre>
	 * 
	 *            The default level is 4.
	 */
	public void addError(Object je, UIComponent control, int severity) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, "", control, severity, "");
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, this);
	 * }
	 * </pre>
	 * 
	 *            To pass no control, call openLogBean.addError(e, null)
	 */
	public void addError(Object je, UIComponent control) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, "", control, 4, "");
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param msg
	 *            An additional message to pass to OpenLog.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest.
	 * @param unid
	 *            This object is serializable to requestScope. But for safety,
	 *            it shouldn't include Domino objects. The code will look for
	 *            the document in the current database. If it can't be found, we
	 *            won't be able to get the document. So instead we'll just write
	 *            out the UNID in the error message.
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, &quot;This is an extra error message&quot;, this, 1, doc.getUniversalId());
	 * }
	 * </pre>
	 * 
	 *            The default level is 4.
	 */
	public void addError(Object je, String msg, UIComponent control, int severity, String unid) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, msg, control, severity, unid);
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param msg
	 *            An additional message to pass to OpenLog.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest.
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, &quot;This is an extra error message&quot;, this, 1);
	 * }
	 * </pre>
	 * 
	 *            The default level is 4.
	 */
	public void addError(Object je, String msg, UIComponent control, int severity) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, msg, control, severity, "");
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param ie
	 *            InterpretException thrown from SSJS. In SSJS, add a
	 *            try...catch block.<br/>
	 *            The "catch" element passes an InterpretException.
	 * @param msg
	 *            An additional message to pass to OpenLog.
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addError(e, &quot;This is an extra error message&quot;, this);
	 * }
	 * </pre>
	 * 
	 *            To pass no control, call openLogBean.addError(e, null)
	 */
	public void addError(Object je, String msg, UIComponent control) {
		try {
			InterpretException ie = getInterpretException(je);
			EventError newErr = createEventError(ie, msg, control, 4, "");
			addToErrorsList(newErr);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param newErr
	 *            error to add to the list
	 */
	private void addToErrorsList(EventError newErr) {
		if (null == getErrors()) {
			errors = new LinkedHashSet<EventError>();
		}

		addFacesMessageForError(newErr);

		errors.add(newErr);
	}

	/**
	 * @param msg
	 *            String message to pass to the event logger
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest
	 * @param unid
	 *            This object is serializable to requestScope. But for safety,
	 *            it shouldn't include Domino objects. The code will look for
	 *            the document in the current database. If it can't be found, we
	 *            won't be able to get the document. So instead we'll just write
	 *            out the UNID in the error message.
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addEvent(&quot;This is an extra message&quot;, this, 1, doc.getUniversalId());
	 * }
	 * </pre>
	 * 
	 *            The default level is 4. To pass no UNID, pass "".
	 */
	public void addEvent(String msg, UIComponent control, int severity, String unid) {
		try {
			EventError newEv = createEventError(null, msg, control, severity, unid);
			addToEventsList(newEv);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param msg
	 *            String message to pass to the event logger
	 * @param control
	 *            Component the error occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * @param severity
	 *            Integer severity level from 1 to 7, corresponding to
	 *            java.util.logging Levels. 1 is severe, 7 is finest.
	 * 
	 *            EXAMPLE
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addEvent(&quot;This is an extra message&quot;, this, 1);
	 * }
	 * </pre>
	 * 
	 *            The default level is 4.
	 */
	public void addEvent(String msg, UIComponent control, int severity) {
		try {
			EventError newEv = createEventError(null, msg, control, severity, "");
			addToEventsList(newEv);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param msg
	 *            String message to pass to the event logger
	 * @param control
	 *            Component the event occurred on. To avoid hard-coding the
	 *            control, use "this" in a property or "this.getParent()" in an
	 *            event:
	 * 
	 *            EXAMPLE:
	 * 
	 *            <pre>
	 * try {
	 * 	*YOUR CODE*
	 * } catch(e) {
	 * 	openLogBean.addEvent(&quot;This is an extra message&quot;, this);
	 * }
	 * </pre>
	 */
	public void addEvent(String msg, UIComponent control) {
		try {
			EventError newEv = createEventError(null, msg, control, 4, "");
			addToEventsList(newEv);
		} catch (Throwable e) {
			OpenLogItem.logError(e);
		}
	}

	/**
	 * @param newEv
	 *            event to be added to the list
	 */
	private void addToEventsList(EventError newEv) {
		if (null == getEvents()) {
			events = new LinkedHashSet<EventError>();
		}
		events.add(newEv);
	}

	/**
	 * @author withersp
	 * @since 1.0.0
	 * 
	 */
	public class EventError implements Serializable {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((control == null) ? 0 : control.hashCode());
			result = prime * result + ((msg == null) ? 0 : msg.hashCode());
			result = prime * result + severity;
			result = prime * result + ((unid == null) ? 0 : unid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EventError other = (EventError) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (control == null) {
				if (other.control != null)
					return false;
			} else if (!control.equals(other.control))
				return false;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			if (severity != other.severity)
				return false;
			if (unid == null) {
				if (other.unid != null)
					return false;
			} else if (!unid.equals(other.unid))
				return false;

			String srcMsg = "";
			String srcText = "";
			String otherMsg = "";
			String otherText = "";
			if (null != this.getError()) {
				srcMsg = this.getError().getLocalizedMessage();
				srcText = this.getError().getExpressionText();
			}
			InterpretException otherErr = (other).getError();
			if (null != otherErr) {
				otherMsg = otherErr.getLocalizedMessage();
				otherText = otherErr.getExpressionText();
			}
			if (!srcMsg.equals(otherMsg)) {
				return false;
			}
			if (!srcText.equals(otherText)) {
				return false;
			}
			return true;
		}

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private UIComponent control;
		private InterpretException error;
		private String msg;
		private int severity;
		private String unid;

		public EventError() {

		}

		public UIComponent getControl() {
			return control;
		}

		public void setControl(UIComponent control) {
			this.control = control;
		}

		public InterpretException getError() {
			return error;
		}

		public void setError(InterpretException error) {
			this.error = error;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public int getSeverity() {
			return severity;
		}

		public void setSeverity(int severity) {
			this.severity = severity;
		}

		public String getUnid() {
			return unid;
		}

		public void setUnid(String unid) {
			this.unid = unid;
		}

		private OpenLogErrorHolder getOuterType() {
			return OpenLogErrorHolder.this;
		}

	}

	/**
	 * @param newErr
	 *            error to be added to facesMessage
	 */
	public void addFacesMessageForError(EventError newErr) {
		// Quit if getLoggedErrors() is null. Will be initialised during
		// beforePhase of renderResponse
		if (null == loggedErrors) {
			return;
		}

		// If not already added to facesMessage, add it
		if (!loggedErrors.contains(newErr)) {
			String dispErr = newErr.getError().getLocalizedMessage();
			String ctrlId = null;
			if (null != newErr.getControl()) {
				ctrlId = newErr.getControl().getId();
			}
			if ("Interpret exception".equals(dispErr)) {
				dispErr = newErr.getError().getExpressionText();
			}
			if (OpenLogItem.getDisplayError()) {
				if (null == ctrlId) {
					OpenLogItem.addFacesMessage(ctrlId, dispErr);
				} else {
					OpenLogItem.addFacesMessage(ctrlId, ctrlId.toUpperCase() + ": " + dispErr);
				}
			}

			loggedErrors.add(newErr);
		}

	}

}
