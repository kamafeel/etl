/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkage.ftpdrudgery.wicket;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * 显示当前时间的Label
 * @author run[zhangqi@lianchuang.com]
 * 4:03:32 PM May 20, 2009
 */
public class Clock extends Label {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1946832262318030693L;

	/**
	 * 
	 * @param id
	 * @param tz
	 */
	public Clock(String id, TimeZone tz) {
		super(id, new ClockModel(tz));

	}
	
	/**
	 * 
	 * @author run[zhangqi@lianchuang.com]
	 * 4:04:03 PM May 20, 2009
	 */
	private static class ClockModel extends AbstractReadOnlyModel<String> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5786155432989323520L;
		private final DateFormat df;

		/**
		 * @param tz
		 */
		public ClockModel(TimeZone tz) {
			df = DateFormat.getDateTimeInstance(DateFormat.FULL,
					DateFormat.LONG);
			df.setTimeZone(tz);
		}

		@Override
		public String getObject() {
			return df.format(new Date());
		}
	}
}
