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
import java.util.GregorianCalendar;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.MoonPhaseCalculator;
import com.microstran.core.clock.Renderer;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * Derived class for complicated version
 *
 * @author Mike Stran
 *
 * @version $Id: W6207aRenderer.java,v 1.1 2004/12/08 17:05:45 stranm Exp $
 */
public class W6207aRenderer extends Renderer
{
	
	//for day of month rotation
	private static final float degreePerDayOfMonth = 11.61F;
	
	//for month of year rotation
	private static final float degreePerMonthOfYear = 30.0F;
	
	//for day of week rotation
	private static final float degreePerDayOfWeek = 51.43F;
	
	//we will move in 1/4 second incriments
	private static final float degreePerQuarterSecond = 1.5F;
	private static final float degreePerHalfSecond = 3.0F;
	private static final float degreePerSecond = 6.0F;
	
	private static final float degreePerMoonPhase = -22.5F; //counter clockwise
	
	private int dayOfMonthX, dayOfMonthY;
	private int monthOfYearX, monthOfYearY;
	private int smallSecondHandX, smallSecondHandY;
	private int dayOfWeekX, dayOfWeekY;
	
	private Element dayOfMonthHand;
	private Element monthOfYearHand;
	private Element smallSecondHand;
	private Element dayOfWeekHand;

	private GraphicsNode gNodeDayOfMonthHand;
	private GraphicsNode gNodeMonthOfYearHand;
	private GraphicsNode gNodeSmallSecondHand;
	private GraphicsNode gNodeDayOfWeekHand;
	
	private static final int STOPWATCH_STOPPED = 1;
	private static final int STOPWATCH_STARTED = 2;

	private int stopwatchState = STOPWATCH_STOPPED;
	private int stopwatchSeconds = 0;
	private int stopwatchFractionalSeconds = 0;
	
	private int previousDay = -1;
	
	private MoonPhaseCalculator moonCalc = new MoonPhaseCalculator();
	
	/**
	 * Default constructor.
	 */
	public W6207aRenderer( )
	{
		super();
		dayOfMonthX 	 = 232;
		dayOfMonthY 	 = 132;
		monthOfYearX 	 = 314;
		monthOfYearY 	 = 211;
		smallSecondHandX = 232;
		smallSecondHandY = 290;
		dayOfWeekX       = 152;
		dayOfWeekY 	 	 = 211;
	}
	
	public void setDocument(Document document)
	{
		super.setDocument(document);
		try
	    {
			this.dayOfMonthHand = document.getElementById("DayOfMonthHand");
			this.monthOfYearHand = document.getElementById("MonthHand");
			this.smallSecondHand = document.getElementById("SmallSecondHand");
			this.dayOfWeekHand = document.getElementById("DayOfWeekHand");
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
		this.dayOfMonthHand = null;
		this.monthOfYearHand = null;
		this.smallSecondHand = null;
		this.dayOfWeekHand = null;
		this.moonPhaseDial = null;
		this.gNodeSecondHand = null;
		this.gNodeMinuteHand = null;
		this.gNodeHourHand = null;
		this.gNodeDayOfMonthHand = null;
		this.gNodeMonthOfYearHand = null;
		this.gNodeSmallSecondHand = null;
		this.gNodeDayOfWeekHand = null;
		this.gNodeMoonPhaseDial = null;
		this.moonCalc = null;
	}
	
	
	/**
	 * override base method so that we don't reevaluate the day
	 * for each minute but can respond when the image goes from 
	 * full window to regular window and back
	 */
	public void reset()
	{
	    super.reset();
		this.previousDay = -1;
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
		        gNodeDayOfMonthHand = context.getGraphicsNode(dayOfMonthHand);
		        gNodeMonthOfYearHand = context.getGraphicsNode(monthOfYearHand);
		        gNodeSmallSecondHand = context.getGraphicsNode(smallSecondHand);
		        gNodeDayOfWeekHand = context.getGraphicsNode(dayOfWeekHand);
		        gNodeMoonPhaseDial = context.getGraphicsNode(moonPhaseDial);
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
    		int dayOfWeek   = date.getDayOfWeek();
    		int monthOfYear = date.getMonth();
    		int dayOfMonth  = date.getDayOfMonth();
    		int year        = date.getYear();
    		int AmPm = date.getAmPm();
    		
    		//don't do rotations if they aren't needed
    		if (seconds%2 == 0)
    		{
    		    //rotate minute hand every 2 sec
    		    float minuteRot = (minutes * clock.getDegreePerMinute())+(seconds * clock.getDegreePerSecondPerHour());
    	        double mRot = degreeToRadians(minuteRot);
    		    AffineTransform mt = AffineTransform.getRotateInstance(mRot, clock.getMinHandX(), clock.getMinHandY());
    		    gNodeMinuteHand.setTransform(mt);
	    	}
    		if (minutes != previousMinutes)
    		{
    			previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
	    		//move the hour hand a bit to allow for quarter of the hour incriments
	    		float hourRot = (hours *  clock.getDegreePerHour())+(minutes *  clock.getDegreePerMinutePerHour());
		        double hRot = degreeToRadians(hourRot);
			    AffineTransform ht = AffineTransform.getRotateInstance(hRot, clock.getHourHandX(), clock.getHourHandY());
			    gNodeHourHand.setTransform(ht);
    		
	    		float monthOfYearRot = (float)((float)(monthOfYear-1) * degreePerMonthOfYear);
		        double mYRot = degreeToRadians(monthOfYearRot);
			    AffineTransform myt = AffineTransform.getRotateInstance(mYRot,monthOfYearX,monthOfYearY);
			    gNodeMonthOfYearHand.setTransform(myt);
    		
	            float dayOfWeekRot = (float)((float)dayOfWeek * degreePerDayOfWeek);
	            if (AmPm == GregorianCalendar.PM) //add 1/2
	                dayOfWeekRot += (degreePerDayOfWeek/2);

		        double dWRot = degreeToRadians(dayOfWeekRot);
			    AffineTransform dwt = AffineTransform.getRotateInstance(dWRot,dayOfWeekX,dayOfWeekY);
			    gNodeDayOfWeekHand.setTransform(dwt);

	            float dayOfMonthRot = (float)((float)dayOfMonth * degreePerDayOfMonth);
	            double dWMRot = degreeToRadians(dayOfMonthRot);
			    AffineTransform dwm = AffineTransform.getRotateInstance(dWMRot,dayOfMonthX,dayOfMonthY);
			    gNodeDayOfMonthHand.setTransform(dwm);
	        }
	        if (dayOfMonth != previousDay)
	        {
	            previousDay = dayOfMonth;
	            int phaseValue = moonCalc.calcPhaseOfMoon(year, monthOfYear,dayOfMonth, hours, minutes, seconds);
	            float moonPhaseRotation = (phaseValue * degreePerMoonPhase);
	            double mpRot = degreeToRadians(moonPhaseRotation);
			    AffineTransform mpt = AffineTransform.getRotateInstance(mpRot, clock.getMoonPhaseDialX(), clock.getMoonPhaseDialY());
			    gNodeMoonPhaseDial.setTransform(mpt);
	        }
    		if (stopwatchState == STOPWATCH_STARTED)
	        {
	            stopwatchFractionalSeconds++;
	            if (stopwatchFractionalSeconds == 4)
	                {
	                stopwatchFractionalSeconds = 0;
	                stopwatchSeconds++;
	                if (stopwatchSeconds == 60)
	                    stopwatchSeconds = 0;
	                }
	            float secondRot = ((stopwatchSeconds * degreePerSecond) + (stopwatchFractionalSeconds *  degreePerQuarterSecond));
	            double sRot = degreeToRadians(secondRot);
	    	    AffineTransform st = AffineTransform.getRotateInstance(sRot, clock.getSecHandX(), clock.getSecHandY());
	    	    gNodeSecondHand.setTransform(st);
	        }
	            
    		if (fractsec%4 == 0)
    		{
	    		float secondRot = ((float)((float)(seconds * degreePerSecond)));
	            double sRot = degreeToRadians(secondRot);
	    	    AffineTransform st = AffineTransform.getRotateInstance(sRot,smallSecondHandX,smallSecondHandY);
	    	    gNodeSmallSecondHand.setTransform(st);
    		}		    		    
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}
}
