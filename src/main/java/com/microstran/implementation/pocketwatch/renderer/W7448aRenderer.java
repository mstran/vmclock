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

package com.microstran.implementation.pocketwatch.renderer;

import java.awt.geom.AffineTransform;

import org.w3c.dom.Document;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.Renderer;
import com.microstran.core.engine.ClockGUI;

/**
 * A derived class for wrist watch style 1
 *
 * @author Mike Stran
 *
 */
public class W7448aRenderer extends Renderer
{
	
	/**
	 * Default constructor.
	 */
	public W7448aRenderer( )
	{
		super();
	}
	
	public void setDocument(Document document)
	{
	    try
	    {
	        this.document = document;
			this.minuteHand = document.getElementById("MinuteHand");
			this.hourHand = document.getElementById("HourWheel");
			this.secondHand = document.getElementById("SecondHand");
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
	    try
	    {
	        if(clock==null)
	            return;
        	//in absolute terms NOT relative
		    ClockDate date  = clock.getDate();
		    int fractsec    = date.getFractionalSeconds();
		    int seconds     = date.getSecond();
		    int minutes     = date.getMinute();
    		int hours       = date.getHour();
    		
    		//don't do rotations if they aren't needed
    		if (seconds%2 == 0)
    		{
    		    //rotate minute hand every 2 sec
    		    float minuteRot = (-91 + (minutes * clock.getDegreePerMinute())) + (seconds * clock.getDegreePerSecondPerHour());
    	        double mRot = degreeToRadians(minuteRot);
    		    AffineTransform at = AffineTransform.getRotateInstance(mRot,clock.getMinHandX(),clock.getMinHandY());
    		    gNodeMinuteHand.setTransform(at);
	    	}
    		if (minutes != previousMinutes){
    			previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
    		}

    		if (hours != previousHours){
    			updateHourHandTransform((hours * -1), 0);
    			previousHours = hours;
    		}
    		
	        float secondRot = (seconds * clock.getDegreePerSecond()) + (fractsec *  clock.getDegreePerFractionalSecond());
	        double sRot = degreeToRadians(secondRot);
		    AffineTransform at = AffineTransform.getRotateInstance(sRot,clock.getSecHandX(),clock.getSecHandY());
		    gNodeSecondHand.setTransform(at);
		    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}

}
