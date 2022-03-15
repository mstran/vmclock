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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.clock.ClockDisplayElement;
import com.microstran.core.engine.AbstractScreenSaver;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.engine.EngineHelper;
import com.microstran.core.engine.ScreenSaver;
import com.microstran.core.event.ApplicationEvent;
import com.microstran.core.event.ApplicationEventListener;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.viewer.SVG_SWTViewerFrame;


/**
 * @author Mstran
 *	Screen Save class this class is intended to provide a full screen set of clocks that
 *	basically will cycle through a pattern with one large clock in the middle and satalite clocks 
 *  surrounding it.
 * 
 * 	It will use a fixed number of clocks.
 *  
 */
public class CenterClockScreenSaver extends AbstractScreenSaver implements ScreenSaver, ApplicationEventListener
{

    /**
     * 	default constructor
     */
    public CenterClockScreenSaver()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#registerForEvents()
     */
    public void registerForEvents()
    {
        ApplicationEventServer.instance().register(this, WindowChangeEvent.CHANGE_VIEWER_EVENT);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#unregisterForEvents()
     */
    public void unregisterForEvents()
    {
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
        changeClockEventTimer = new Timer();
        registerForEvents();
        changeClockEventTimer.schedule(generateChangeClockEvent, clockRotationPeriod, clockRotationPeriod);
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
	        unregisterForEvents();
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement element = (ClockDisplayElement)allScreenClocks.get(i);
                SVG_SWTViewerFrame oldFrame = element.getFrame();
                deactivateFrame(oldFrame,false);
                element.setPrimaryCell(false);
                element.setFrame(null);
                element.setEmpty(true);
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
        if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CHANGE_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                if (changingScreenSaver == false)
                    changeViewer();
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
            Map<String, Clock> clockGroup = null;
	        if (limitScreenToGroup)
                clockGroup = EngineHelper.getClocksForGroup(currentClockGroupName);
            else
	            clockGroup = EngineHelper.getAllDefinedClocks();
	        
	        List<ClockDisplayElement> clocks = new ArrayList<ClockDisplayElement>();
	        Set<String>keys = clockGroup.keySet();
	        totalClocks = numberofScreens;
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            Rectangle[][] screenRectangles = (Rectangle[][])allScreenRectangles.get(i);
	            int numberHorizontal = screenRectangles.length; 
	            int numberVertical = screenRectangles[0].length; 
	
	            //put one great big one in the center
	            ClockDisplayElement element = new ClockDisplayElement();
		        String clockID = EngineHelper.getRandomClockFromKeys(keys);
	            Clock clock = clockGroup.get(clockID);
                Rectangle fullRect = new Rectangle();
                element.setRectangle(fullRect);
                element.setClock(clock);
                element.setPrimaryCell(true);
                for (int j = 0; j < numberHorizontal; j++)
	            {
	                for (int k = 0; k < numberVertical; k++)
	                {
	                    Rectangle rect = screenRectangles[j][k];
	                    fullRect.add(rect);
	                }//vertical loop
	            }//horizontal loop
	            clocks.add(element);
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
            Clock newClock = null;
            SVG_SWTViewerFrame newFrame = null;
            for (int i = 0; i < numberofScreens; i++)
            {
                ClockDisplayElement element = (ClockDisplayElement)allScreenClocks.get(i);
                Clock clock = element.getClock();
    	        //create the time zone
    		    ClockDate date = new ClockDate();
    		    TimeZone dtz = (TimeZone) TimeZone.getDefault();
    		    String ID = dtz.getID();
    		    String caption = ClockGUI.generateCaption(ID);
    		    clock.setCaption(caption);
    		    date.setTimeZone(ID);
    		    clock.setDate(date);
    		    clock.configureTime();
                clock.setFullWindow(true);
                newClock = Clock.createClock(clock);
                Clock.copyRuntimeSettings(newClock, clock, newClock.getID());
    		    element.setClock(newClock);
                newClock.setPosX(element.getRectangle().x);
                newClock.setPosY(element.getRectangle().y);
                newClock.setCurrentWidth(element.getRectangle().width);
                newClock.setCurrentHeight(element.getRectangle().height);
                newFrame = CreateSSFrameSetClock(element.getRectangle(), newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
                element.setFrame(newFrame);
    	        syncClockToFrame(newClock, newFrame);
               
/*                if(ClockGUI.isWindowsPlatform)
    	        {
    	        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
    		        winacc.setAlwaysOnTop( newFrame, true );
    	        }
*/
                newFrame.isResizingNow();
    	        for(int k = 0; k < 3; k++)
    	        {
    	            newFrame.setRenderedOnce(false);
    	            newFrame.isRenderedOnce();
    	        }
    	        newFrame.setVisible(true);
    	        totalClocks++;
            }//number of screens
        }
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionInstantiatingViewers");
            System.out.println(errStr + " " + e.getMessage());
        }
    }

   
      
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
            Map<String, Clock>clockGroup = null;
	        if (limitScreenToGroup)
                clockGroup = EngineHelper.getClocksForGroup(currentClockGroupName);
            else
	            clockGroup = EngineHelper.getAllDefinedClocks();
	        
	        Set<String>keys = clockGroup.keySet();
	        String clockID = EngineHelper.getRandomClockFromKeys(keys);
            
	        Clock clock = (Clock)clockGroup.get(clockID);

	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement element = (ClockDisplayElement)allScreenClocks.get(i);
	            //create the time zone
			    ClockDate date = new ClockDate();
			    TimeZone dtz = (TimeZone) TimeZone.getDefault();
			    String ID = dtz.getID();
			    String caption = ClockGUI.generateCaption(ID);
			    clock.setCaption(caption);
			    date.setTimeZone(ID);
			    clock.setDate(date);
			    clock.configureTime();
	            clock.setFullWindow(true);
	            Clock newClock = Clock.createClock(clock);
	            Clock.copyRuntimeSettings(newClock, clock, newClock.getID());
			    element.setClock(newClock);
			    newClock.setPosX(element.getRectangle().x);
	            newClock.setPosY(element.getRectangle().y);
	            newClock.setCurrentWidth(element.getRectangle().width);
	            newClock.setCurrentHeight(element.getRectangle().height);
	            SVG_SWTViewerFrame newFrame = CreateSSFrameSetClock(element.getRectangle(), newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
		        SVG_SWTViewerFrame oldFrame = element.getFrame();
		        element.setFrame(newFrame);
		        syncClockToFrame(newClock, newFrame);
/*
		        if(ClockGUI.isWindowsPlatform)
    	        {
    	        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
    		        winacc.setAlwaysOnTop( newFrame, true );
    	        }
*/	
		        newFrame.isResizingNow();
    	        for(int k = 0; k < 3; k++)
    	        {
    	            newFrame.setRenderedOnce(false);
    	            newFrame.isRenderedOnce();
    	        }
    	        newFrame.setVisible(true);
                deactivateFrame(oldFrame,false);
                totalClocks++;
	        }
	    }//number of screens      
        catch(Exception e)
        {
            String errStr = resources.getString("ClockError.ExceptionChangingViewer");
            System.out.println(errStr + " " + e.getMessage());
        }
    }
    
}
