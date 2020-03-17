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

import java.lang.reflect.Constructor;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.markup.html.WebPage;

/*
 * 
 */

@AuthorizeInstantiation("ADMIN")
public class WizardPage extends WebPage
{
	
	public WizardPage(){
		Constructor<? extends Wizard> ctor;
		try {
			ctor = AddTaskWizard.class.getConstructor(String.class);
			Wizard wizard = ctor.newInstance("wizard");
			add(wizard);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
