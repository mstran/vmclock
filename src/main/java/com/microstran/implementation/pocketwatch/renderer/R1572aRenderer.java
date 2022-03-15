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
public class R1572aRenderer extends Renderer
{
	private static final float strengthDegreePerHour = 9.5F;
	
	/**
	 * Default constructor.
	 */
	public R1572aRenderer( )
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
			this.strengthIndicator = document.getElementById("StrengthIndicator");
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
	}
	
	public void endRenderer()
	{
	    super.endRenderer();
	    this.strengthIndicator = null;
	    this.gNodeStrengthHand = null;
	}

	public void rotateHands()
	{
	    try
	    {
	        if (clock==null)
	            return;
	        //in absolute terms NOT relative
		    ClockDate date  = clock.getDate();
		    int fractsec    = date.getFractionalSeconds();
		    int seconds     = date.getSecond();
		    int minutes     = date.getMinute();
    		int hours       = date.getHour();

    		if (previousHours == -1)
			{
    		    previousHours = hours;
			    strengthHours = previousHours;
			}
    		
    		//don't do rotations if they aren't needed
    		if (seconds%2 == 0)
    		{
    		    //rotate minute hand every 2 sec
    		    float minuteRot = (minutes * clock.getDegreePerMinute()) + (seconds * clock.getDegreePerSecondPerHour());
    	        double mRot = degreeToRadians(minuteRot);
    		    AffineTransform mt = AffineTransform.getRotateInstance(mRot,clock.getMinHandX(),clock.getMinHandY());
    		    gNodeMinuteHand.setTransform(mt);
	    	}
    		if (minutes != previousMinutes)
    		{
    			previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
	    		//move the hour hand a bit to allow for quarter of the hour incriments
	    		float hourRot = (hours * clock.getDegreePerHour()) + (minutes * clock.getDegreePerMinutePerHour());
    	        double hRot = degreeToRadians(hourRot);
    		    AffineTransform ht = AffineTransform.getRotateInstance(hRot,clock.getHourHandX(),clock.getHourHandY());
    		    gNodeHourHand.setTransform(ht);
	    		
    		    //move the strength indicator down, when we reach zero reset it!
	    		if (hours != previousHours)
	    		{
	    		    strengthHours++;
		    		previousHours = hours;
	    		}
	    		if (strengthHours > 36) //if 24 go back to zero
	    		{
	    		    strengthHours = 0;
	    		}
		        double iRotation = (clock.getStrengthBaseDegrees() + (strengthHours*strengthDegreePerHour))+(minutes * clock.getDegreePerMinutePerHour());
	    		double iRot = degreeToRadians(iRotation);
			    AffineTransform it = AffineTransform.getRotateInstance(iRot,clock.getStrengthIndX(),clock.getStrengthIndY());
			    gNodeStrengthHand.setTransform(it);
	        }
    		updateSecondHandTransform(seconds, fractsec);
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
}
