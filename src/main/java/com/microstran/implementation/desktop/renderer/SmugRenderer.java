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

package com.microstran.implementation.desktop.renderer;

import java.awt.geom.AffineTransform;

import org.w3c.dom.Document;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.Renderer;
import com.microstran.core.engine.ClockGUI;

/**
 * A derived class for "smug" - eye tracks back and forth
 *
 * @author Mike Stran
 *
 */
public class SmugRenderer extends Renderer
{
	private static final int LEFT_BOUNDARY = 190;
	private static final int RIGHT_BOUNDARY = 224;
	
	private double currentLocation = 224;
	private double direction = -.25;
	private double cumulativeDist = 0;
	
	/**
	 * Default constructor.
	 */
	public SmugRenderer( )
	{
		super();
	}
	
	public void setDocument(Document document)
	{
	    try
	    {
	        this.document = document;
			this.minuteHand = document.getElementById("MinuteHand");
			this.hourHand = document.getElementById("HourHand");
			this.secondHand = document.getElementById("SecondHand");
			this.complication1 = document.getElementById("Eye");
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ClockError.ExceptionSettingDocument");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
	
	/**
	 * override base method so that we don't reevaluate the day
	 * for each minute but can respond when the image goes from 
	 * full window to regular window and back
	 */
	public void reset()
	{
	    super.reset();
	    previousHours = -1;
	}
	


	public void rotateHands()
	{
	    try{
	        if(clock==null)
	            return;
        	//in absolute terms NOT relative
		    ClockDate date  = clock.getDate();
		    int fractsec    = date.getFractionalSeconds();
		    int seconds     = date.getSecond();
		    int minutes     = date.getMinute();
    		int hours       = date.getHour();
    		
    		
   			currentLocation += direction;
   			cumulativeDist +=direction;
   			AffineTransform et = AffineTransform.getTranslateInstance(cumulativeDist, 0);
   			gNodeComplication1.setTransform(et);
   			if ( currentLocation <= LEFT_BOUNDARY) {
   				direction = .25;
   			}else if (currentLocation >= RIGHT_BOUNDARY){
   				direction = -.25;
   			}
    		if ((minuteHand != null) && (seconds%2 == 0)){
    				updateMinuteHandTransform(minutes, seconds);
    	    	}   
   			if ((hourHand != null) && (minutes != previousMinutes))	{
    				previousMinutes = minutes;
    	    		clock.getFrame().resetMessageBarMessages();
    	    		updateHourHandTransform(hours, minutes);
    			}
			if (secondHand != null) {
				updateSecondHandTransform(seconds, fractsec);
			}
		}catch (Exception e){
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}

}
