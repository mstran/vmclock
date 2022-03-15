/*
 * Copyright(c) 2022 Microstran Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microstran.implementation.pocketwatch;

import java.util.ArrayList;

import com.microstran.core.clock.Clock;

/**
 * A derived class for Pocketwatch
 *
 * @author Mike Stran
 *
 */
public class W8347a extends Clock 
{
	private ArrayList mouseElements;
	
	public W8347a()
	{
		super();
		
		//This clock has a stop watch feature, this 
		//is enabled 
		mouseElements = new ArrayList();
		String startStopWatch = "StopWatchSwitch";
		mouseElements.add(startStopWatch);
		
	}
	
	/**
	 * Does this clock support mouse clicks?
	 * @return
	 */
	public boolean hasMouseAlerts()
	{
	    return(true);
	}
	
	/**
	 * Return any mouse alert regions
	 * @return
	 */
	public ArrayList getMouseAlerts()
	{
	    return(mouseElements);
	}
	
	public void recieveMouseAlert(String elementID)
	{
	    this.renderer.recieveMouseAlert(elementID);
	}
}