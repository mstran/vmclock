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
public class W8464aRenderer extends Renderer
{
	
	
	//we will move in 1/4 second incriments
	
	//new york will be the standard hour/minute hand.
	
	private Element HourHandBombay;
	private Element MinuteHandBombay;
	private Element HourHandParis;
	private Element MinuteHandParis;
	private Element HourHandNewYork;
	private Element MinuteHandNewYork;
	private Element HourHandCapeTown;
	private Element MinuteHandCapeTown;
	private Element HourHandSydney;
	private Element MinuteHandSydney;

	private GraphicsNode gNodeHourHandBombay;
	private GraphicsNode gNodeMinuteHandBombay;
	private GraphicsNode gNodeHourHandParis;
	private GraphicsNode gNodeMinuteHandParis;
	private GraphicsNode gNodeHourHandNewYork;
	private GraphicsNode gNodeMinuteHandNewYork;
	private GraphicsNode gNodeHourHandCapeTown;
	private GraphicsNode gNodeMinuteHandCapeTown;
	private GraphicsNode gNodeHourHandSydney;
	private GraphicsNode gNodeMinuteHandSydney;
	
	
	private static final int bombayX = 507;
	private static final int bombayY = 540;
	private static final int bombayGMTHours = 5;
	private static final int bombayGMTMinutes = 30;
			
	private static final int parisX = 198;
	private static final int parisY = 540;
	private static final int parisGMTHours = 1;
	
	
	private static final int newYorkX = 352;
	private static final int newYorkY = 450;
	private static final int newYorkGMTHours = -5;
	
	private static final int capeTownX = 198;
	private static final int capeTownY = 721;
	private static final int capeTownGMTHours = 2;
	
	private static final int sydneyX = 507;
	private static final int sydneyY = 721;
	private static final int sydneyGMTHours = 11;
	
	private static final String mainTimeZone = "Europe/London";
	
	/**
	 * Default constructor.
	 */
	public W8464aRenderer( )
	{
		super();
	}
	
	public void setDocument(Document document)
	{
		super.setDocument(document);
		try
	    {
			HourHandNewYork = document.getElementById("HourHandNewYork");
			MinuteHandNewYork = document.getElementById("MinuteHandNewYork");
			HourHandBombay = document.getElementById("HourHandBombay");
			MinuteHandBombay = document.getElementById("MinuteHandBombay");
			HourHandParis = document.getElementById("HourHandParis");
			MinuteHandParis  = document.getElementById("MinuteHandParis");
			HourHandNewYork = document.getElementById("HourHandNewYork");
			MinuteHandNewYork = document.getElementById("MinuteHandNewYork");
			HourHandCapeTown = document.getElementById("HourHandCapeTown");
			MinuteHandCapeTown = document.getElementById("MinuteHandCapeTown");
			HourHandSydney = document.getElementById("HourHandSydney");
			MinuteHandSydney = document.getElementById("MinuteHandSydney");
	    
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
		this.previousDay = -1;
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
		    	ClockDate date = clock.getDate();
				if (!date.getTimeZone().equalsIgnoreCase(mainTimeZone)) {
					date.setTimeZone(mainTimeZone);
					date.configureTime();
				}
		    	SVG_SWTViewerFrame frame = this.clock.getFrame(); 
		        if (frame == null)
		            return;
		        canvas = frame.getJSVGCanvas();
		        queue = frame.getJSVGCanvas().getUpdateManager().getUpdateRunnableQueue(); 
		        BridgeContext context = frame.getJSVGCanvas().getUpdateManager().getBridgeContext();
		        gNodeSecondHand = context.getGraphicsNode(secondHand);
		        gNodeMinuteHand = context.getGraphicsNode(minuteHand);
		        gNodeHourHand = context.getGraphicsNode(hourHand);
		        gNodeHourHandBombay = context.getGraphicsNode(HourHandBombay);
		        gNodeMinuteHandBombay = context.getGraphicsNode(MinuteHandBombay);
		        gNodeHourHandNewYork = context.getGraphicsNode(HourHandNewYork);
		        gNodeMinuteHandNewYork = context.getGraphicsNode(MinuteHandNewYork);
		        gNodeHourHandParis = context.getGraphicsNode(HourHandParis);
		        gNodeMinuteHandParis = context.getGraphicsNode(MinuteHandParis);
		        gNodeHourHandCapeTown = context.getGraphicsNode(HourHandCapeTown);
		        gNodeMinuteHandCapeTown = context.getGraphicsNode(MinuteHandCapeTown);
		        gNodeHourHandSydney = context.getGraphicsNode(HourHandSydney);
		        gNodeMinuteHandSydney = context.getGraphicsNode(MinuteHandSydney);
	        }
		    queue.preemptAndWait(new Runnable()
			{
				public void run() 
				{
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
    		
    		//minute hands update
    		if (seconds%2 == 0)
    		{
    			//London
    			updateMinuteHandTransform(minutes, seconds, clock.getMinHandX(), clock.getMinHandY(), gNodeMinuteHand);
    			//NewYork
    			updateMinuteHandTransform(minutes, seconds, newYorkX, newYorkY, gNodeMinuteHandNewYork);
    			//Paris
    			updateMinuteHandTransform(minutes, seconds, parisX, parisY, gNodeMinuteHandParis);
    			//Bombay
    			updateMinuteHandTransform(minutes+bombayGMTMinutes, seconds, bombayX, bombayY, gNodeMinuteHandBombay);
    			//CapeTown
    			updateMinuteHandTransform(minutes, seconds, capeTownX, capeTownY, gNodeMinuteHandCapeTown);
    			//Sydney
    			updateMinuteHandTransform(minutes, seconds, sydneyX, sydneyY, gNodeMinuteHandSydney);
    		}
    		
    		//hour hands update
    		if (minutes != previousMinutes)
    		{
    			previousMinutes = minutes;
	    		clock.getFrame().resetMessageBarMessages();
    			//London
	    		updateHourHandTransform(hours, minutes, clock.getMinHandX(), clock.getMinHandY(), gNodeHourHand);
    			//NewYork
	    		updateHourHandTransform(hours + newYorkGMTHours, minutes, newYorkX, newYorkY, gNodeHourHandNewYork);
    			//Paris
	    		updateHourHandTransform(hours + parisGMTHours, minutes, parisX, parisY, gNodeHourHandParis);
    			//Bombay
	    		updateHourHandTransform(hours + bombayGMTHours, minutes+bombayGMTMinutes, bombayX, bombayY, gNodeHourHandBombay);
    			//CapeTown
	    		updateHourHandTransform(hours + capeTownGMTHours, minutes, capeTownX, capeTownY, gNodeHourHandCapeTown);
    			//Sydney
	    		updateHourHandTransform(hours + sydneyGMTHours, minutes, sydneyX, sydneyY, gNodeHourHandSydney);
	        }
	            
    		if (secondHand != null) {
				updateSecondHandTransform(seconds, fractsec);
			}	   	    
	    }
		catch (Exception e) 
		{
		    String errStr = ClockGUI.resources.getString("ClockError.ExceptionInRendering");
			System.out.println(errStr + " " + e.getMessage());		
		}
	}

	public void updateMinuteHandTransform(int minutes, int seconds, int x, int y, GraphicsNode gNodeMinute) {
		double minuteRot = ((minutes * clock.getDegreePerMinute())+(seconds * clock.getDegreePerSecondPerHour()));
        double mRot = degreeToRadians(minuteRot);
		AffineTransform mt = AffineTransform.getRotateInstance(mRot,x,y);
		gNodeMinute.setTransform(mt);
	}

	public void updateHourHandTransform(int hours, int minutes, int x, int y, GraphicsNode gNodeHour) {
		float hourRot = (hours * clock.getDegreePerHour())+(minutes * clock.getDegreePerMinutePerHour());
        double hRot = degreeToRadians(hourRot);
	    AffineTransform ht = AffineTransform.getRotateInstance(hRot,x,y);
	    gNodeHour.setTransform(ht);
	}
	
}
