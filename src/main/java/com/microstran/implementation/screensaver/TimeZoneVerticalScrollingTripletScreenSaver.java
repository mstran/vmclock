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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
public class TimeZoneVerticalScrollingTripletScreenSaver extends AbstractScreenSaver implements ScreenSaver, ApplicationEventListener
{

    private boolean scrollLock = false;
    private int timeZoneGroupCounter = 0;
    /**
     * manifest constants specific to this screen saver
     */
    private static final int NUMBER_OF_GROUPS = 24;
    private static final int NUMBER_PER_ROW = 3;
    private static final int NUMBER_ACTIVE_ROWS = 2;
    private static final int NUMBER_INITIAL_ROWS = 3;
    private static final long SLIDE_CLOCK_TIME_EVENT_VALUE = 250;
    private ArrayList allScreenLists = new ArrayList();
    
    /**
     * available time zone triplets
     */
    private static String [][] timeZoneGroup = new String[NUMBER_OF_GROUPS][NUMBER_PER_ROW];
    static
    {
       int i = 0;
       timeZoneGroup[i][0] = "New_York";
       timeZoneGroup[i][1] = "London";
       timeZoneGroup[i][2] = "Paris";
       
       timeZoneGroup[++i][0] = "Tokyo";
       timeZoneGroup[i][1] = "New_York";
       timeZoneGroup[i][2] = "Los_Angeles";
       
       timeZoneGroup[++i][0] = "Algiers";
       timeZoneGroup[i][1] = "Cairo";
       timeZoneGroup[i][2] = "Casablanca";
       
       timeZoneGroup[++i][0] = "Amsterdam";
       timeZoneGroup[i][1] = "London";
       timeZoneGroup[i][2] = "Paris";

       timeZoneGroup[++i][0] = "Bangkok";
       timeZoneGroup[i][1] = "Calcutta";
       timeZoneGroup[i][2] = "Hong_Kong";
    
       timeZoneGroup[++i][0] = "New_York";
       timeZoneGroup[i][1] = "Chicago";
       timeZoneGroup[i][2] = "Los_Angeles";

       timeZoneGroup[++i][0] = "Vilnius";
       timeZoneGroup[i][1] = "Warsaw";
       timeZoneGroup[i][2] = "Zurich";

       timeZoneGroup[++i][0] = "Johannesburg";
       timeZoneGroup[i][1] = "Kigali";
       timeZoneGroup[i][2] = "Nairobi";

       timeZoneGroup[++i][0] = "Yellowknife";
       timeZoneGroup[i][1] = "Whitehorse";
       timeZoneGroup[i][2] = "Thunder_Bay";

       timeZoneGroup[++i][0] = "Boise";
       timeZoneGroup[i][1] = "Anchorage";
       timeZoneGroup[i][2] = "Indianapolis";

       timeZoneGroup[++i][0] = "Amman";
       timeZoneGroup[i][1] = "Baghdad";
       timeZoneGroup[i][2] = "Bahrain";

       timeZoneGroup[++i][0] = "Beirut";
       timeZoneGroup[i][1] = "Damascus";
       timeZoneGroup[i][2] = "Dubai";

       timeZoneGroup[++i][0] = "Oslo";
       timeZoneGroup[i][1] = "Prague";
       timeZoneGroup[i][2] = "Rome";

       timeZoneGroup[++i][0] = "Perth";
       timeZoneGroup[i][1] = "Sydney";
       timeZoneGroup[i][2] = "Queensland";

       timeZoneGroup[++i][0] = "Jerusalem";
       timeZoneGroup[i][1] = "Gaza";
       timeZoneGroup[i][2] = "Tel_Aviv";

       timeZoneGroup[++i][0] = "Jamaica";
       timeZoneGroup[i][1] = "Cancun";
       timeZoneGroup[i][2] = "Cayman";

       timeZoneGroup[++i][0] = "Athens";
       timeZoneGroup[i][1] = "Berlin";
       timeZoneGroup[i][2] = "Madrid";

       timeZoneGroup[++i][0] = "Mexico_City";
       timeZoneGroup[i][1] = "Mazatlan";
       timeZoneGroup[i][2] = "El_Salvador";

       timeZoneGroup[++i][0] = "Brussels";
       timeZoneGroup[i][1] = "Copenhagen";
       timeZoneGroup[i][2] = "Gibraltar";

       timeZoneGroup[++i][0] = "Tokyo";
       timeZoneGroup[i][1] = "Shanghai";
       timeZoneGroup[i][2] = "Melbourne";

       timeZoneGroup[++i][0] = "Saigon";
       timeZoneGroup[i][1] = "Seoul";
       timeZoneGroup[i][2] = "Singapore";

       timeZoneGroup[++i][0] = "Moscow";
       timeZoneGroup[i][1] = "Kiev";
       timeZoneGroup[i][2] = "Minsk";

       timeZoneGroup[++i][0] = "Denver";
       timeZoneGroup[i][1] = "Detroit";
       timeZoneGroup[i][2] = "Phoenix";

       timeZoneGroup[++i][0] = "Manila";
       timeZoneGroup[i][1] = "Phnom_Penh";
       timeZoneGroup[i][2] = "Rangoon";
    }
   
    /**
     * 	default constructor
     */
    public TimeZoneVerticalScrollingTripletScreenSaver()
    {
        super();
    }

    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#registerForEvents()
     */
    public void registerForEvents()
    { 
        ApplicationEventServer.instance().register(this, WindowChangeEvent.SLIDE_VIEWER_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.SHOW_VIEWER_EVENT);
        ApplicationEventServer.instance().register(this, WindowChangeEvent.CREATE_VIEWER_ROW_EVENT);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#unregisterForEvents()
     */
    public void unregisterForEvents()
    { 
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.SLIDE_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.SHOW_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.CREATE_VIEWER_ROW_EVENT);
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
        changeClockEventTimer = new Timer();
        showClockEventTimer = new Timer();
        moveClockEventTimer = new Timer();
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
            changeClockEventTimer.cancel();
	        changeClockEventTimer = null;
	        moveClockEventTimer.cancel();
	        moveClockEventTimer = null;
	        
	        unregisterForEvents();
	        
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            LinkedList clocks = (LinkedList)allScreenLists.get(i);
	            for (int j = 0; j < clocks.size(); j++)
	            {
		                ClockDisplayElement element = (ClockDisplayElement)clocks.get(j);
		                SVG_SWTViewerFrame oldFrame = element.getFrame();
		                deactivateFrame(oldFrame,false);
		                element.setPrimaryCell(false);
                        element.setFrame(null);
                        element.setEmpty(true);
	            }
	            clocks.clear();
	        }
	        allScreenLists.clear();
	        allScreenLists = null;
        }
    }

    
    /* (non-Javadoc)
     * @see com.microstran.event.ApplicationEventListener#listen(com.microstran.event.ApplicationEvent)
     */
    public void listen( ApplicationEvent applicationEvent )
	{
        if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.SLIDE_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
	            if (scrollLock == true)
	                return;
	            if (changingScreenSaver == false)
	            {
	                scrollLock = true;
	                slideViewer();
	                scrollLock = false;
	            }
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CREATE_VIEWER_ROW_EVENT))
        {
            synchronized(lockObj)
            {
	            if (changingScreenSaver == false)
	            {
		            Integer screenIndex = (Integer)applicationEvent.getEventData();
		            LinkedList screenElements = (LinkedList)allScreenLists.get(screenIndex.intValue());
		            int startPosition = screenElements.size();
		            boolean yLocIsOffScreen = false;
		            boolean timeToCreateNext = false;
		            Rectangle sr = (Rectangle)screenRes.get(screenIndex.intValue());
		            int height = sr.height/3;
		            int y = 0;
		            createRow(screenElements, sr, y, height);
		            instantiateRow(screenElements, startPosition);
	            }
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.SHOW_VIEWER_EVENT))
        {
            //examine the total and determine if we're done
            synchronized(lockObj)
            {
	            if (changingScreenSaver == false)
	            {
	                showClockEventTimer.cancel();
	                showViewer();
	                moveClockEventTimer.schedule(generateMoveClockEvent, SLIDE_CLOCK_TIME_EVENT_VALUE, SLIDE_CLOCK_TIME_EVENT_VALUE);            

	            }
            }
        }
 	}
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#layoutClocks()
     */
    public void layoutClocks()
    {
        try
        {
            allScreenLists = new ArrayList();
	        LinkedList clocks = null;
	        totalClocks = numberofScreens*(NUMBER_ACTIVE_ROWS * NUMBER_PER_ROW);
	        
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            clocks = new LinkedList();
		        Rectangle sr = (Rectangle)screenRes.get(i);
	            int height = sr.height/3;
	            int y = height;
	            //create an extra buffer Row for first destroy event
	            //we fire create row event from then on, so we will always have 3 available really
	            for (int j = 0; j < NUMBER_INITIAL_ROWS; j++)  
	            {
	                createRow(clocks, sr, y, height);
	                y = 0;  //subsequent rows after the first are set to zero
	            }    
	            allScreenLists.add(clocks);
	        }
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
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            LinkedList clocks = (LinkedList)allScreenLists.get(i);
	            for (int j = 0; j < NUMBER_INITIAL_ROWS; j++)
	            {
	                instantiateRow(clocks, (j * NUMBER_PER_ROW));
	            }//for 2 rows
	        }//for # of screens
        }
        catch(Exception e)
        {
	        String errStr = resources.getString("ClockError.ExceptionInstantiatingViewers");
            System.out.println(errStr + " " + e.getMessage());
        }
    }

    /**
     * Starts the show clock timer
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
     * Start the move clock timer
     */
    private TimerTask generateMoveClockEvent = new TimerTask() 
    {
        public void run() 
        {
            try
            {
                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.SLIDE_VIEWER_EVENT,null));
            }
            catch(Exception e)
            {
                String errStr = resources.getString("ClockError.ExceptionGeneratingChangeEvent");
                System.out.println(errStr + " " + e.getMessage());
            }
        }
    };

    /**
     * Event driven method to move the viewers Down the screen
     */
    protected void slideViewer()
    {
        try
        {
            for (int i = 0; i < numberofScreens; i++)
	        {
	            LinkedList clocks = (LinkedList)allScreenLists.get(i);
	            boolean yLocIsOffScreen = false;
	            boolean timeToCreateNext = false;
                for (int j = 0; j < NUMBER_ACTIVE_ROWS; j++)
	            {
	                Rectangle sr = (Rectangle)screenRes.get(i);
		            for (int k = 0; k < NUMBER_PER_ROW; k++)
		            {
		                ClockDisplayElement clockElement = (ClockDisplayElement)clocks.get( ((j * NUMBER_PER_ROW) + k) );
            	        int xLocation = clockElement.getRectangle().x;
            	        int yLocation = clockElement.getRectangle().y + 1;
            	        clockElement.getRectangle().y += 1;
            	        clockElement.getFrame().setLocation(xLocation, yLocation);
            	        if (j == 0) //check if first row has scrolled off the screen or will soon
            	        {
            	            if (yLocation > (sr.y + sr.height))
            	                yLocIsOffScreen = true;
            	        }
		            }//for each row
		            if (yLocIsOffScreen)
		            {
		                //row 1 is off screen destroy it and create new row at top
		                destroyRow(clocks);
		                ClockDisplayElement clockElement = (ClockDisplayElement)clocks.get(NUMBER_PER_ROW);
		                int yPosition = ((clockElement.getRectangle().height * -1) + clockElement.getFrame().getMessageBarHeight());
		                showRow(clocks, NUMBER_PER_ROW, yPosition);
		                yLocIsOffScreen = false;
		                //publish an event to queue up the next row for this screen
		                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.CREATE_VIEWER_ROW_EVENT,new Integer(i)));
		            }
	            }//for rows
	        }//for each screen
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionMovingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
    /**
     * remove a row from the screen elements
     * @param screenElements
     */
    private void destroyRow(LinkedList clocks)
    {
    try
    	{
	        for (int i = 0; i < NUMBER_PER_ROW; i++)
	        {
	            ClockDisplayElement element = (ClockDisplayElement)clocks.remove(0);
	            SVG_SWTViewerFrame oldFrame = element.getFrame();
                deactivateFrame(oldFrame,false);
                element.setPrimaryCell(false);
                element.setFrame(null);
                element.setEmpty(true);
	        }
    	}
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionRemovingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
    /**
     * create a new row
     * @param screenElements
     * @param sr
     * @param y
     * @param height
     */
    private void createRow(LinkedList<ClockDisplayElement> clocks, Rectangle sr, int y, int height)
    {
        try
        {
	        int width = ((sr.width/3) - 1);
	        Map<String, Clock> clockGroup = EngineHelper.getAllDefinedClocks();
	        Set<String>keys = clockGroup.keySet();
	        for (int i = 0; i < NUMBER_PER_ROW; i++)
	        {
	            int offset = i > 0 ? 1 : 0;
	            Rectangle rect = new Rectangle((sr.x + (i * width) + offset), y, width, height);
	        
	            String clockID = EngineHelper.getRandomClockFromKeys(keys);
	            Clock clock = clockGroup.get(clockID);
	            ClockDisplayElement element = new ClockDisplayElement(rect, clock, i, 0, true, true);
	            element.setEmpty(false);
	            clocks.add(element);
	        }//number of screens for loop
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionCreatingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }        
    }
    
    /**
     * Instantiate a new row
     * @param screenElements
     * @param tzGroup
     */
    private void instantiateRow(LinkedList<ClockDisplayElement>  screenElements, int startPosition)
    {
        String timeZoneFromGroup = null;
        int timeZoneIndex = 0;
        timeZoneGroupCounter++;
        try
        {
            Clock newClock = null;
            SVG_SWTViewerFrame frame = null;
            
            startPosition = Math.max(0,startPosition);
            for (int k = 0; k < NUMBER_PER_ROW; k++)
            {
                ClockDisplayElement element = (ClockDisplayElement)screenElements.get(startPosition++);
                Clock clock = element.getClock();
                //get the 
                if (timeZoneGroupCounter >= NUMBER_OF_GROUPS)
                    timeZoneGroupCounter = 0;
                timeZoneFromGroup = timeZoneGroup[timeZoneGroupCounter][k];
                //create the time zone
                timeZoneIndex = findTimeZone(timeZoneFromGroup, timeZones);
                String timeZone = (timeZones[timeZoneIndex]);
                ClockDate date = new ClockDate();
                date.setTimeZone(timeZone);                    
                clock.setDate(date);
                clock.configureTime();
                //Create a caption for the time zone
                String caption = generateCaption(timeZoneFromGroup);
                clock.setCaption(caption);
                clock.setFullWindow(true);
                newClock = Clock.createClock(clock);
                Clock.copyRuntimeSettings(newClock, clock, newClock.getID());
    		    element.setClock(newClock);
    		    frame = CreateSSFrameSetClock(element.getRectangle(), newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
/*
                if(ClockGUI.isWindowsPlatform)
 
    	        {
    	        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
    		        winacc.setAlwaysOnTop( frame, true );
    	        }
*/
    		    element.setFrame(frame);
            }//for each in a row
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionCreatingViewerForRow");
            System.out.println(errStr + " " + e.getMessage() + " TimeZone Selected index = " + timeZoneIndex);
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
            for (int i = 0; i < numberofScreens; i++)
	        {
	            LinkedList<ClockDisplayElement>  screenElements = (LinkedList)allScreenLists.get(i);
	            int startPosition = 0;
	            for (int j = 0; j < NUMBER_ACTIVE_ROWS; j++)
	            {
	                ClockDisplayElement element = (ClockDisplayElement)screenElements.get(startPosition);
	                int yPosition = element.getRectangle().y;
	                if (j > 0)
	                    {
	                    yPosition = ((element.getRectangle().height * -1) + element.getFrame().getMessageBarHeight());
		                showRow(screenElements, startPosition, yPosition);
		                }
	                else
	                    showRow(screenElements, startPosition, yPosition);
	                startPosition += NUMBER_PER_ROW;
	            }
	        }
    	}
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionShowingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
    /**
     * Show a row
     * @param screenElements
     * @param startPosition
     */
    private void showRow(LinkedList screenElements, int startPosition, int yPosition)
    {
        Clock newClock = null;
        SVG_SWTViewerFrame newFrame = null;
        ClockDisplayElement element = null;
        for (int i = 0; i < NUMBER_PER_ROW; i++)
        {
            element = (ClockDisplayElement)screenElements.get(startPosition++);
            newClock = element.getClock();
            newFrame = element.getFrame();
            syncClockToFrame(newClock, newFrame);
            element.setVisible(true);
            newFrame.setLocation(element.getRectangle().x, yPosition);
            //set the position
            element.getRectangle().y = yPosition;
            newFrame.setVisible(true);
        }
    }
    
}
