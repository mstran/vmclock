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

package com.microstran.implementation.screensaver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;

import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.ClockDisplayElement;
import com.microstran.core.engine.AbstractScreenSaver;
import com.microstran.core.engine.EngineHelper;
import com.microstran.core.engine.ScreenSaver;
import com.microstran.core.event.ApplicationEvent;
import com.microstran.core.event.ApplicationEventListener;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.viewer.SVG_SWTViewerFrame;

//import net.mysrc.gui.aot.AlwaysOnTop;
//import net.mysrc.gui.aot.WindowAccess;


/**
 * @author Mstran
 *	Screen Save class this class is intended to provide a full screen set of clocks that
 *	basically will cycle through a vertical pattern, it has several cycles and loops back through in
 *  repetition. 
 * 
 * 	It will first use the clock base class utilities to determine how much memory is allocated, what
 *  the screen dimensions are and how many screens are available. From that it will determine how best
 *  to partition up the screen saver.
 *  
 */
public class VerticalClockScreenSaver extends AbstractScreenSaver implements ScreenSaver, ApplicationEventListener
{

    /**
     * 	default constructor
     */
    public VerticalClockScreenSaver() 
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#registerForEvents()
     */
    public void registerForEvents()
    {   
        ApplicationEventServer.instance().register(this, WindowChangeEvent.SHOW_VIEWER_EVENT);
    	ApplicationEventServer.instance().register(this, WindowChangeEvent.CHANGE_VIEWER_EVENT);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#unregisterForEvents()
     */
    public void unregisterForEvents()
    {   
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.SHOW_VIEWER_EVENT);
    	ApplicationEventServer.instance().unregister(this, WindowChangeEvent.CHANGE_VIEWER_EVENT);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#getBackgroundColor()
     */
    public Color getBackgroundColor() 
    {
        return(Color.LIGHT_GRAY);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#startScreenSaver()
     */
    public void startScreenSaver()
    {
        showClockEventTimer = new Timer();
        registerForEvents();
        showClockEventTimer.schedule(generateShowClockEvent, SHOW_CLOCK_TIME_EVENT_VALUE, SHOW_CLOCK_TIME_EVENT_VALUE);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#stopScreenSaver()
     */
    public void stopScreenSaver()
    {
        super.stopScreenSaver();
        synchronized(lockObj)
        {
            changingScreenSaver = true;
            unregisterForEvents();
	        changeClockEventTimer.cancel();
	        changeClockEventTimer = null;
	        
	        SVG_SWTViewerFrame frame = null;
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            for (int j = 0; j < screenElements.length; j++) 
	            {
	                for(int k = 0; k < screenElements[j].length; k++)
	                {
	                    ClockDisplayElement element = screenElements[j][k];
	                    if (element.isPrimaryCell())
	                    {
	                        SVG_SWTViewerFrame oldFrame = element.getFrame();
	                        deactivateFrame(oldFrame,false);
	        		        element.setPrimaryCell(false);
	                        element.setFrame(null);
	                        element.setEmpty(true);
	                    }
	                }
	            }
	        }
	        allScreenClocks.clear();
	        allScreenClocks = null;
        }
    }
    
    /* (non-Javadoc)
     * @see com.microstran.event.ApplicationEventListener#listen(com.microstran.event.ApplicationEvent)
     */
    public void listen( ApplicationEvent applicationEvent )
	{
        if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.SHOW_VIEWER_EVENT))
        {
            //examine the total and determine if we're done
            synchronized(lockObj)
            {
                if (totalClocks == 0)
                    return;
                
                totalClocks--;
                showViewer();
                if (totalClocks == 0)
                {
                    showClockEventTimer.cancel();
                    showClockEventTimer = null;
                    changeClockEventTimer = new Timer();
                    changeClockEventTimer.schedule(generateChangeClockEvent, clockRotationPeriod, clockRotationPeriod);

                }
            }    
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CHANGE_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                if (changingScreenSaver == false)
                    changeViewer();
            }
        }
	}
    
       
    /**
     * Create the layout for all the clocks and assign them their positions
     * in either single cells or across multiple cells
      */
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#layoutClocks()
     */
    public void layoutClocks()
    {
        try
        {
            Map<String, Clock> clockGroup = null;
	        if (limitScreenToGroup)
                clockGroup = EngineHelper.getClocksForGroup(currentClockGroupName);
            else
	            clockGroup = EngineHelper.getAllDefinedClocks();
	        
	        int numberOfClocksInGroup = clockGroup.size();
	        Set<String>keys = clockGroup.keySet();
	        Random generator = new Random();
	        int index = generator.nextInt((numberOfClocksInGroup - 1));
	        
	        List<ClockDisplayElement [][]> clocks = new ArrayList<ClockDisplayElement [][]>();
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            Rectangle[][] screenRectangles = (Rectangle[][])allScreenRectangles.get(i);
	            int numberHorizontal = screenRectangles.length; 
	            int numberVertical = screenRectangles[0].length; 
	
	            ClockDisplayElement [][] screenElements = new ClockDisplayElement[numberHorizontal][numberVertical];
	            for (int j = 0; j < numberHorizontal; j++)
	            {
	                for (int k = 0; k < numberVertical; k++)
	                {
	                    Rectangle rect = screenRectangles[j][k];
	                    
	                    if ((index > numberOfClocksInGroup - 1) || (index < 0))
	                        index = 0;
	                    String key = EngineHelper.getRandomClockFromKeys(keys);
	                    Clock clock = clockGroup.get(key);
	                    
	                    /*						Algorithm
	                     * _________________________________________________________________________
	                     *	If the rendering hint is square 
	                     *		If there is no clock alreay using this space 
	                     *			then place a clock here. 
	                     *  -------------------------------------------------------	
	                     *	Else if the rendering hint favors width 
	                     *		if this is not in the last column 
	                     *			if the adjacent space to the right is empty
	                     *				then create  wide clock and set it to both spaces
	                     *		    else If adjacent space to right is full 
	                     *		    	if the space contains only a single clock
	                     *		    		then remove it and  place the wide clock in those 2 spaces.
	                     *		    	else find single clock to fill space	
	                     *      else if this is the last column
	                     *			if the space to the left contains only a single clock
	                     *		    		then remove it and  place the wide clock in those 2 spaces.
	                     *		    	else find single clock to fill space	
	                     *  -------------------------------------------------------	
	                     *  Else if the rendering hint favors height 
	                     *   	if this is not the last row 
	                     *   		if the space below is empty. 
	                     *   			then create long clock and set it to both spaces
	                     *           else if space below is full
	                     *           	if space contains only a single clock
	                     *           		then remove it and place long clock in those 2 spaces
	                     *           	else find single clock to fill space
	                     *   	else if this is the last row 
	                     *   		 else if space above contains only a single clock
	                     *           		then remove it and place long clock in those 2 spaces
	                     *           	else find single clock to fill space
	                     */
	                    
	                    int hint = clock.getRenderingHint();
	                    switch(hint)
	                    {
		                    case Clock.SQUARE:
		                    {
		                        if (screenElements[j][k] == null)
		                        {
		                            //create a new screen element
		                            ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, true, true);
		                            screenElements[j][k] = displayElement;
		                            totalClocks++;
		                    	}
		                        else
		                        {   
		                            //space was filled, probably by a long or wide clock, 
		                            //try to reuse this clock on the next spin through
		                            index--; 
		                        }
		                        break;
		                    }
		                    case Clock.FAVORS_WIDTH:
		                    {
		                        if (screenElements[j][k] == null)
		                        {
			                        
			                        boolean success = false;
			                        //is this the last column? 
			                        if ((j + 1) < numberHorizontal)
			                        {
			                            //see what's in the column to the right if empty or single spaced clock use it
			                            if ((screenElements[j+1][k] == null) || (screenElements[j+1][k].isSingleSpace()))    
			                            {
			                                //can use this position
				                            ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, false, true);
				                            displayElement.setEndingHorizontalIndex(j+1);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j][k] = displayElement;
			                                
				                            rect = screenRectangles[j+1][k];
				                            displayElement = new ClockDisplayElement(rect, clock, j+1, k, false, false);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j+1][k] = displayElement;
				                            totalClocks++;
				                            success = true;
				                        }
			                        }
			                        else 
			                        {
			                            //this is the last column try to push left instead of right
			                            //see what's in the column to the left if empty or single spaced clock use it
			                            if ((screenElements[j-1][k] == null) || (screenElements[j-1][k].isSingleSpace()))    
			                            {
			                                //can use this position
				                            ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, false, true);
				                            displayElement.setEndingHorizontalIndex(j-1);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j][k] = displayElement;
			                                
				                            rect = screenRectangles[j-1][k];
				                            displayElement = new ClockDisplayElement(rect, clock, j-1, k, false, false);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j-1][k] = displayElement;
				                            totalClocks++;
				                            success = true;
				                        }
			                        }
			                        if (!success)
			                        {
			                            //Create the first single clock you can NOTE if no clocks in group are single this will fail!
		                                int tmpIndex = index;
		                                int localHint = -1;
		                                while (localHint != Clock.SQUARE)
		                                {
		                                    key = EngineHelper.getNextClockIDForCollection(clock.getID(), clockGroup);
		                                    clock = clockGroup.get(key);
		                                    
		                                    localHint = clock.getRenderingHint();
		                                    if (tmpIndex == numberOfClocksInGroup)
			                                    tmpIndex = 0;
			                            }
		                                ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, true, true);
			                            screenElements[j][k] = displayElement;
			                            totalClocks++;
		                            }
		                    	}
		                        else
		                        {   
		                            //space was filled, probably by a long or wide clock, 
		                            //try to reuse this clock on the next spin through
		                            index--; 
		                        }
		                        break;
		                    }
		                    case Clock.FAVORS_HEIGHT:
		                    {
		                        if (screenElements[j][k] == null)
		                        {
			                        
			                        boolean success = false;
			                        //is this the last row? 
			                        if ((k + 1) < numberVertical)
			                        {
			                            //see what's in the next row if empty or single spaced clock use it
			                            if ((screenElements[j][k+1] == null) || (screenElements[j][k+1].isSingleSpace()))    
			                            {
			                                //can use this position
				                            ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, false, true);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k+1);
				                            screenElements[j][k] = displayElement;
			                                
				                            rect = screenRectangles[j][k+1];
				                            displayElement = new ClockDisplayElement(rect, clock, j, k+1, false, false);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j][k+1] = displayElement;
				                            totalClocks++;
				                            success = true;
				                        }
			                        }
			                        else 
			                        {
			                            //this is the last row try to push up instead of down
			                            //see what's in the row above if empty or single spaced clock use it
			                            if ((screenElements[j][k-1] == null) || (screenElements[j][k-1].isSingleSpace()))    
			                            {
			                                //can use this position
				                            ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, false, true);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k-1);
				                            screenElements[j][k] = displayElement;
			                                
				                            rect = screenRectangles[j][k-1];
				                            displayElement = new ClockDisplayElement(rect, clock, j, k-1, false, false);
				                            displayElement.setEndingHorizontalIndex(j);
				                            displayElement.setEndingVerticalIndex(k);
				                            screenElements[j][k-1] = displayElement;
				                            totalClocks++;
				                            success = true;
				                        }
			                        }
			                        if (!success)
			                        {
			                            //Create the first single clock you can NOTE if no clocks in group are single this will fail!
		                                int tmpIndex = index;
		                                int localHint = -1;
		                                while (localHint != Clock.SQUARE)
		                                {
		                                	key = EngineHelper.getNextClockIDForCollection(clock.getID(), clockGroup);
		                                    clock = clockGroup.get(key);
		                                    localHint = clock.getRenderingHint();
		                                    if (tmpIndex == numberOfClocksInGroup)
			                                    tmpIndex = 0;
			                            }
		                                ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, true, true);
			                            screenElements[j][k] = displayElement;
			                            totalClocks++;
		                            }
		                    	}
		                        else
		                        {   
		                            //space was filled, probably by a long or wide clock, 
		                            //try to reuse this clock on the next spin through
		                            index--; 
		                        }
		                        break;
		                    }//favors height
	                    }//switch statement
	                }//vertical loop
	            }//horizontal loop
	            clocks.add(screenElements);
	        }//number of screens for loop
	        allScreenClocks = clocks;
        }
        catch(Exception e)
        {
	        String errStr = resources.getString("ClockError.ExceptionLayingOutViewers");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#instantiateClocks()
     */
    public void instantiateClocks()
    {
        try
        {
        //must use 1 random number generator
        //since the seed is the time and rapid calls will generate the same sequence
        Clock newClock = null;
        SVG_SWTViewerFrame frame = null;
        for (int i = 0; i < numberofScreens; i++)
        {
            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
            for (int j = 0; j < screenElements.length; j++) 
            {
                for(int k = 0; k < screenElements[j].length; k++)
                {
                    //get the clock, if it's primary then set it up, if it crosses horiz/vert boundaries
                    //then ensure that you set it's size correctly
                    ClockDisplayElement element = screenElements[j][k];
                    if (element.isPrimaryCell())
                    {
                        Clock clock = element.getClock();
                        //create the time zone
                        int val = timeZoneGenerator.nextInt(timeZones.length);
                        String timeZone = (timeZones[val]);
                        ClockDate date = new ClockDate();
                        date.setTimeZone(timeZone);                    
                        clock.setDate(date);
                        clock.configureTime();
                        //Create a caption for the time zone
                        String caption = generateCaption(timeZone);
                        clock.setCaption(caption);
                        clock.setFullWindow(true);
                        newClock = Clock.createClock(clock);
                        Clock.copyRuntimeSettings(newClock, clock, newClock.getID());
            		    element.setClock(newClock);
    
            		    //if this is not a single we need to get the full rectangle
                        Rectangle rectPrimary = element.getRectangle();
                        if (element.isSingleSpace()==false)
                        {
                            //get the other element and find it's rectangle
                            int h = element.getEndingHorizontalIndex();
                            int v = element.getEndingVerticalIndex();
                            ClockDisplayElement secondaryElement = screenElements[h][v];
                            Rectangle rectSecondary = secondaryElement.getRectangle();
                            Rectangle union = rectPrimary.union(rectSecondary);
                            newClock.setPosX(union.x);
                            newClock.setPosY(union.y);
                            newClock.setCurrentWidth(union.width);
                            newClock.setCurrentHeight(union.height);
    	                    frame = CreateSSFrameSetClock(union, newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
    	    	            secondaryElement.setFrame(frame);
    	                    secondaryElement.setClock(newClock);
                        }
                        else
                        {
                            newClock.setPosX(rectPrimary.x);
                            newClock.setPosY(rectPrimary.y);
                            newClock.setCurrentWidth(rectPrimary.width);
                            newClock.setCurrentHeight(rectPrimary.height);
    	                    frame = CreateSSFrameSetClock(rectPrimary, newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
                        }
/*   
                        if(ClockGUI.isWindowsPlatform)
        		        {
        		        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
        			        winacc.setAlwaysOnTop( frame, true );
        		        }
*/
                        element.setFrame(frame);
                    }
                }
            }
        }//number of screens
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionInstantiatingViewers");
            System.out.println(errStr + " " + e.getMessage());
        }
    }

  
    /**
     * Comment for <code>generateShowClockEvent</code>
     */
    private TimerTask generateShowClockEvent = new TimerTask() 
    {
        public void run() 
        {
            try
            {
                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.SHOW_VIEWER_EVENT,null));
            }
            catch(Exception e)
            {
                String errStr = resources.getString("ClockError.ExceptionGeneratingShowEvent");
                System.out.println(errStr + " " + e.getMessage());
            }
        }
    };

    /**
     * Comment for <code>generateChangeClockEvent</code>
     */
    private TimerTask generateChangeClockEvent = new TimerTask() 
    {
        public void run() 
        {
            try
            {
                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.CHANGE_VIEWER_EVENT,null));
            }
            catch(Exception e)
            {
                String errStr = resources.getString("ClockError.ExceptionGeneratingChangeEvent");
                System.out.println(errStr + " " + e.getMessage());
            }
        }
    };

    /**
     * Event driven method to change a viewer out
     */
    protected void changeViewer()
    {
        try
        {
	        Map<String, Clock> clockGroup = null;
	        if (limitScreenToGroup)
                clockGroup = EngineHelper.getClocksForGroup(currentClockGroupName);
            else
	            clockGroup = EngineHelper.getAllDefinedClocks();
	        
	        int numberOfClocksInGroup = clockGroup.size();

	        int arraySize = clockGroup.size();
	        int index = changeGenerator.nextInt((arraySize-1));
	        Clock clock = (Clock)clockGroup.get(index);
	        ClockDisplayElement primaryCell = null;
	        ClockDisplayElement secondaryCell = null;
            
	        //for each screen
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
                
	            //pick one at random untill you find one that isn't used
                int numberHorizontal = screenElements.length; 
                int numberVertical = screenElements[0].length; 

                int hIndex;
                int vIndex;
                synchronized(lockObj)
                {
                    hIndex = changeGenerator.nextInt(numberHorizontal);
                    vIndex   = changeGenerator.nextInt(numberVertical);
                }
                
                ClockDisplayElement element = screenElements[hIndex][vIndex];
                SVG_SWTViewerFrame oldFrame = element.getFrame();
                //find a clock with the same orientation
                
                Rectangle displayRect;
                if (element.isPrimaryCell() == true)
                {
                    primaryCell = element;
                    displayRect = element.getRectangle();
                    if (element.isSingleSpace() == false)
                    {
                        int h = element.getEndingHorizontalIndex();
                        int v = element.getEndingVerticalIndex();
                        secondaryCell= screenElements[h][v];
                        Rectangle rect1 = primaryCell.getRectangle();
                        Rectangle rect2 = secondaryCell.getRectangle();
                        displayRect = rect1.union(rect2);
                     }
                }
                else //must be secondary so this must be using more than 1 spot
                {
                    int h = element.getEndingHorizontalIndex();
                    int v = element.getEndingVerticalIndex();
                    secondaryCell = element;
                    primaryCell = screenElements[h][v];
                    Rectangle rect1 = primaryCell.getRectangle();
                    Rectangle rect2 = secondaryCell.getRectangle();
                    displayRect = rect1.union(rect2);
                }
                
                int currentClockHint = primaryCell.getClock().getRenderingHint();
                int selectedClockHint = clock.getRenderingHint();
                
                if (currentClockHint != selectedClockHint)
                {
                    clock = EngineHelper.findClockWithOrientation(clockGroup, currentClockHint, clock.getID());
                }
                Clock newClock = Clock.createClock(clock);
                //copying the runtime settings includes the date and hense new TimeZone
                Clock.copyRuntimeSettings(newClock, clock, newClock.getID());
    		    
                //new random time zone
                int val;
                synchronized(lockObj)
                {
                    val = timeZoneGenerator.nextInt(timeZones.length);
                }
                String timeZone = (timeZones[val]);
                ClockDate date = new ClockDate();
                date.setTimeZone(timeZone);                    
                newClock.setDate(date);
                String caption = generateCaption(timeZone);
                newClock.setCaption(caption);
                
                SVG_SWTViewerFrame newFrame = CreateSSFrameSetClock(displayRect, newClock, true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
                if (secondaryCell != null)
                {
                    secondaryCell.setFrame(newFrame);
                    secondaryCell.setClock(newClock);
                }
                primaryCell.setFrame(newFrame);
                primaryCell.setClock(newClock);
                
                syncClockToFrame(newClock, newFrame);
/*                
	            if(ClockGUI.isWindowsPlatform)
		        {
		        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
			        winacc.setAlwaysOnTop( newFrame, true );
		        }
*/
	            deactivateFrame(oldFrame, false);
                
                newFrame.isResizingNow();
    	        for(int k = 0; k < 3; k++)
    	        {
    	            newFrame.setRenderedOnce(false);
    	            newFrame.isRenderedOnce();
    	        }
                
                newFrame.setVisible(true);
	        }//for each screen
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionChangingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
    
    /**
     * Event driven method to make viewer visible
     * @return
     */
    protected void showViewer()
    {
        try
        {
            //pick at random
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            if (hasClocksToDisplay(screenElements) == true)
	            {
	                //pick one at random untill you find one that isn't used
	                int numberHorizontal = screenElements.length; 
	                int numberVertical = screenElements[0].length; 
	
	                boolean notDone = true;
	                while (notDone)
	                {
	                    int  hIndex = showGenerator.nextInt(numberHorizontal);
	                    int  vIndex = showGenerator.nextInt(numberVertical);

	                    ClockDisplayElement element = screenElements[hIndex][vIndex];
	                    if (element.isVisible() == false)
	                    {
	                        Clock newClock = element.getClock();
	                        SVG_SWTViewerFrame newFrame = element.getFrame();
	            	        Dimension curDimFrame = newFrame.getSize();
	          			    
	            	        //set document into the renderer 
	            	        Document document = newFrame.getSVGDocument();
	            	        
	            	        syncClockToFrame(newClock, newFrame);
	            	        element.setVisible(true);
	                        
	                        newFrame.isResizingNow();
	            	        for(int k = 0; k < 3; k++)
	            	        {
	            	            newFrame.setRenderedOnce(false);
	            	            newFrame.isRenderedOnce();
	            	        }
	            	        newFrame.setVisible(true);
	                        if (element.isSingleSpace()==false)
	                        {
	                            int h = element.getEndingHorizontalIndex();
	                            int v = element.getEndingVerticalIndex();
	                            element = screenElements[h][v];
	                            element.setVisible(true);        
	                        }
	                        notDone = false;
	                    }
	                }
	            }
	        }
    	}
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionShowingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }

    
    
}
