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

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.Renderer;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * Derived class for complicated version
 *
 * @author Mike Stran
 *
 */
public class W8347aRenderer extends Renderer
{
	
	private static final int STOPWATCH_STOPPED = 1;
	private static final int STOPWATCH_STARTED = 2;

	private int stopwatchState = STOPWATCH_STOPPED;
	private int stopwatchSeconds = 0;
	private int stopwatchHalfMinute = 0;
	private int stopwatchMinutes = 0;
	private int stopwatchFractionalSeconds = 0;
	
	private Element timerTenthsHand;
	private Element timerSecondsHand;
	private Element timerMinutesHand;
	
	private GraphicsNode gNodeTimerTenthsHand;
	private GraphicsNode gNodeTimerSecondsHand;
	private GraphicsNode gNodeTimerMinutesHand;
	
	private float timerDegreesPerFracSec;
	private float timerDegreesPerSec;
	private float timerDegreesPerHalfSec;
	private float timerDegreesPerMinute;
	
	private int previousStopwatchMinutes;
	private float secHandX,secHandY;
	private float minHandX, minHandY;
	/**
	 * Default constructor.
	 */
	public W8347aRenderer( )
	{
		super();
	}
	
	
	public void setDocument(Document document)
	{
		super.setDocument(document);
		try
	    {
			
			timerTenthsHand = document.getElementById("TimerTenthsHand");
			timerSecondsHand = document.getElementById("TimerSecondsHand");
			timerMinutesHand = document.getElementById("TimerMinutesHand");
			timerDegreesPerFracSec = clock.getTimerDegreePerFractionalSecond();
			timerDegreesPerSec = clock.getTimerDegreePerSecond();
			timerDegreesPerHalfSec = timerDegreesPerSec *2;
			timerDegreesPerMinute = clock.getDegreePerMinute();
					
			secHandX = clock.getTimerSecondHandX();
			secHandY = clock.getTimerSecondHandY();
			
			minHandX = clock.getTimerMinuteHandX();
			minHandY = clock.getTimerMinuteHandY();
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
	
	/* (non-Javadoc)
	 * @see com.lgcdc.clock.Renderer#recieveMouseAlert(java.lang.String)
	 */
	public void recieveMouseAlert(String elementID)
	{
	    //deal with the alert to turn on/off/reset the stop watch
	    if (stopwatchState == STOPWATCH_STOPPED)
	    {
	        stopwatchSeconds = 0;
	        stopwatchMinutes = 0;
	        stopwatchFractionalSeconds = 0;
	        stopwatchState = STOPWATCH_STARTED;
	    }    
	    else 
	    {
	        stopwatchState = STOPWATCH_STOPPED;
	    }
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
		        canvas = frame.getJSVGCanvas();
		        queue = frame.getJSVGCanvas().getUpdateManager().getUpdateRunnableQueue(); 
		        BridgeContext context = frame.getJSVGCanvas().getUpdateManager().getBridgeContext();
		        gNodeSecondHand = context.getGraphicsNode(secondHand);
		        gNodeMinuteHand = context.getGraphicsNode(minuteHand);
		        gNodeHourHand = context.getGraphicsNode(hourHand);

		        gNodeTimerTenthsHand = context.getGraphicsNode(timerTenthsHand);
		        gNodeTimerSecondsHand = context.getGraphicsNode(timerSecondsHand);
		        gNodeTimerMinutesHand = context.getGraphicsNode(timerMinutesHand);
	        }
		    queue.preemptAndWait(new Runnable()
			{
				public void run() 
				{
				    //System.out.println("Rendering W6207a");
				    rotateHands();
		        }
		    });
		    canvas.immediateRepaint();
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
	        if (clock==null)
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
    		    float minuteRot = (minutes * clock.getDegreePerMinute())+(seconds * clock.getDegreePerSecondPerHour());
    	        double mRot = degreeToRadians(minuteRot);
    		    AffineTransform mt = AffineTransform.getRotateInstance(mRot, clock.getMinHandX(), clock.getMinHandY());
    		    gNodeMinuteHand.setTransform(mt);
	    	}
    		if ((minuteHand != null) && (seconds%2 == 0)){
    			updateMinuteHandTransform(minutes, seconds);
    	    }   

    		if ((hourHand != null) && (minutes != previousMinutes))
			{
				previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
	    		updateHourHandTransform(hours, minutes);
			}
    		
    		if (stopwatchState == STOPWATCH_STARTED)
	        {
	            stopwatchFractionalSeconds++;
	            if (stopwatchFractionalSeconds == 10){
	                stopwatchFractionalSeconds = 0;
	                stopwatchSeconds++;
	                if (stopwatchSeconds%30 == 0) {
	                	 stopwatchHalfMinute++;
		                 if (stopwatchSeconds == 60) {   
		                	 stopwatchSeconds = 0;
		                 }
	                    if (stopwatchHalfMinute == 2) {
	                    	stopwatchHalfMinute=0;
		                	stopwatchMinutes++;
	                    }
	                }
	                if (stopwatchMinutes == 60) {	
	                	stopwatchMinutes = 0;
	                }
	            }
	            float secondRot = ((stopwatchSeconds * timerDegreesPerSec) + (stopwatchFractionalSeconds * timerDegreesPerFracSec));
	            double sRot = degreeToRadians(secondRot);
	    	    AffineTransform st = AffineTransform.getRotateInstance(sRot, clock.getSecHandX(), clock.getSecHandY());
	    	    gNodeTimerTenthsHand.setTransform(st);
	    	    
	    	    if (fractsec%10 == 0) {
		    	    float secondRot1 = ((stopwatchSeconds+1) * timerDegreesPerHalfSec);
		    	    double sRot1 = degreeToRadians(secondRot1);
		    	    AffineTransform sh = AffineTransform.getRotateInstance(sRot1, secHandX, secHandY);
		    	    gNodeTimerSecondsHand.setTransform(sh);
	    	    }    	    
	    	    if (previousStopwatchMinutes != stopwatchMinutes) {
	    	    	previousStopwatchMinutes = stopwatchMinutes;
	    	    	float secondRot2 = (stopwatchMinutes * timerDegreesPerMinute);
	    	    	double sRot2 = degreeToRadians(secondRot2);
		    	    AffineTransform mh = AffineTransform.getRotateInstance(sRot2, minHandX, minHandY);
		    	    gNodeTimerMinutesHand.setTransform(mh);
		    	}
	        }
	            
    		if (fractsec%10 == 0){
				updateSecondHandTransform(seconds, 0);
			}		    		    
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
}
