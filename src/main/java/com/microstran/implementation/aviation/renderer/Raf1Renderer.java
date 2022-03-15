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

package com.microstran.implementation.aviation.renderer;

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.Renderer;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

public class Raf1Renderer extends Renderer
{
	private Element TimerSecondHand;
	protected GraphicsNode gNodeTimerSecondHand;
	private Element TimerMinuteHand;
	protected GraphicsNode gNodeTimerMinuteHand;
	
	protected float timerMinuteHandX = 161;
	protected float timerMinuteHandY = 105;
	protected float timerSecondHandX = 162;
	protected float timerSecondHandY = 191;

	/**
	 * Default constructor.
	 */
	public Raf1Renderer( )
	{
	}
	
	public void setDocument(Document document)
	{
		super.setDocument(document);
		try
	    {
			this.TimerSecondHand = document.getElementById("TimerSecondHand");
			this.TimerMinuteHand = document.getElementById("TimerMinuteHand");
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ClockError.ExceptionSettingDocument");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
	
	
	public void endRenderer()
	{
	    super.endRenderer();
	    this.TimerSecondHand = null;
	    this.gNodeTimerSecondHand = null;
	    this.TimerMinuteHand = null;
	    this.gNodeTimerMinuteHand = null;
	}
	
	
	/**
	 * Recieve notification and advance time
	 * derived class so will implement this
	 * 
	 * @param applicationEvent The application event to process
	 * 
	 */
	public void render()
	{
	    if (!enabled)
	        return;
		try 
		{
		    if (queue == null)
	        {
		        SVG_SWTViewerFrame frame = this.clock.getFrame(); 
		        if (frame == null)
		            return;
		        queue = frame.getJSVGCanvas().getUpdateManager().getUpdateRunnableQueue(); 
		        BridgeContext context = frame.getJSVGCanvas().getUpdateManager().getBridgeContext();
		        gNodeSecondHand = context.getGraphicsNode(secondHand);
		        gNodeMinuteHand = context.getGraphicsNode(minuteHand);
		        gNodeHourHand = context.getGraphicsNode(hourHand);
		        gNodeTimerSecondHand = context.getGraphicsNode(TimerSecondHand);
		        gNodeTimerMinuteHand = context.getGraphicsNode(TimerMinuteHand);
	        }
		    queue.invokeAndWait(new Runnable() 
			{
				public void run() 
				{
				    rotateHands();
				}
		    });
		}
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
	
	public void rotateHands()
	{
	    try
	    {
	        if(clock==null)
	            return;
		    //in absolute terms NOT relative
		    ClockDate date = clock.getDate();
		    int fractsec = date.getFractionalSeconds();
		    int seconds  = date.getSecond();
		    int minutes  = date.getMinute();
			int hours    = date.getHour();
			int day      = date.getDayOfMonth();
			
			//don't do rotations if they aren't needed
			if (seconds%2 == 0)
			{
			    //rotate minute hand every 2 sec
			    float minuteRot = (((float)minutes * clock.getDegreePerMinute())+((float)seconds * clock.getDegreePerSecondPerHour()));
    	        double mRot = degreeToRadians(minuteRot);
    		    AffineTransform mt = AffineTransform.getRotateInstance(mRot,clock.getMinHandX(),clock.getMinHandY());
    		    gNodeMinuteHand.setTransform(mt);
    		    
    		    AffineTransform mht = AffineTransform.getRotateInstance(mRot,timerMinuteHandX,timerMinuteHandY);
    		    gNodeTimerMinuteHand.setTransform(mht);
	    	}
			if (minutes != previousMinutes)
			{
				previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
	    		//move the hour hand a bit to allow for quarter of the hour incriments
	    		float hourRot = (((float)hours * clock.getDegreePerHour())+((float)minutes * clock.getDegreePerMinutePerHour()));
    	        double hRot = degreeToRadians(hourRot);
    	        AffineTransform ht = AffineTransform.getRotateInstance(hRot,clock.getHourHandX(),clock.getHourHandY());
    		    gNodeHourHand.setTransform(ht);
			}
			//always render this
			if (fractsec%2 == 0)
			{
				float secondRot = ((float)(((float)(seconds * clock.getDegreePerSecond()))));	        
				double sRot = degreeToRadians(secondRot);
			    AffineTransform st = AffineTransform.getRotateInstance(sRot,clock.getSecHandX(),clock.getSecHandY());
			    gNodeSecondHand.setTransform(st);
			}
			
		    float tSecondRot = ((float)(((float)(seconds * clock.getTimerDegreePerSecond())) + ((float)(fractsec *  clock.getTimerDegreePerFractionalSecond()))));
	        double tSRot = degreeToRadians(tSecondRot);
		    AffineTransform sht = AffineTransform.getRotateInstance(tSRot,timerSecondHandX,timerSecondHandY);
		    gNodeTimerSecondHand.setTransform(sht);
		}
	    catch (Exception e) 
	    {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
	        System.out.println(errStr + " " + e.getMessage());		
	    }
	}

}
