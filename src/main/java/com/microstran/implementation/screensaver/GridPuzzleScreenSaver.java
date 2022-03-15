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
public class GridPuzzleScreenSaver extends AbstractScreenSaver implements ScreenSaver, ApplicationEventListener
{

	/**
	 * manifest constants for this screen saver
	 */
	private static final int TOP = 0;
    private static final int BOTTOM  = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int MOVE_ANIMATION = 20;
    private static final int MOVE_DELAY     = 50;
    
    private static final long SHOW_GRID_CLOCK_TIME_EVENT_VALUE = 250;  //1/4 second
    private static final long NEXT_CLOCK_TIME_EVENT_VALUE = 1000;  //1 second
    private static final long CHANGE_TIME_ZONE_EVENT_VALUE = 300000; //5 minutes
    
    /**
     * 	default constructor
     */
    public GridPuzzleScreenSaver()
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
	    ApplicationEventServer.instance().register(this, WindowChangeEvent.CHANGE_TIMEZONE_EVENT);
    }
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#unregisterForEvents()
     */
    public void unregisterForEvents()
    {
	    ApplicationEventServer.instance().unregister(this, WindowChangeEvent.SLIDE_VIEWER_EVENT);
	    ApplicationEventServer.instance().unregister(this, WindowChangeEvent.SHOW_VIEWER_EVENT);
        ApplicationEventServer.instance().unregister(this, WindowChangeEvent.CHANGE_TIMEZONE_EVENT);
    }
    

    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#getBackgroundColor()
     */
    public Color getBackgroundColor() 
    {
        return(Color.BLUE);
    }
    
    
    /* (non-Javadoc)
     * @see com.microstran.engine.ScreenSaver#startScreenSaver()
     */
    public void startScreenSaver()
    {
        showClockEventTimer = new Timer();
        registerForEvents();
        showClockEventTimer.schedule(generateShowClockEvent, 0, SHOW_GRID_CLOCK_TIME_EVENT_VALUE);
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
	        moveClockEventTimer =  null;
	        unregisterForEvents();
	        try
	        {
	            //give it time to stop moving and calm down
	            Thread.sleep(1000);
	        }
	        catch(Exception e)
	        {}
	        
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            for (int j = 0; j < screenElements.length; j++) 
	            {
	                for(int k = 0; k < screenElements[j].length; k++)
	                {
	                    ClockDisplayElement element = screenElements[j][k];
	                    if (element.isEmpty()  == false)
	                    {
	                        SVG_SWTViewerFrame oldFrame = element.getFrame();
	                        deactivateFrame(oldFrame,false);
	                    }    	                   
                        element.setPrimaryCell(false);
                        element.setFrame(null);
                        element.setEmpty(true);
	                }
	            }
	        }
	        allScreenClocks.clear();
	        allScreenClocks=null;
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
                    
                    moveClockEventTimer = new Timer();
                    changeClockEventTimer = new Timer();
                    moveClockEventTimer.schedule(generateMoveClockEvent, NEXT_CLOCK_TIME_EVENT_VALUE, NEXT_CLOCK_TIME_EVENT_VALUE);
                    changeClockEventTimer.schedule(generateChangeClockTimeZoneEvent, CHANGE_TIME_ZONE_EVENT_VALUE, CHANGE_TIME_ZONE_EVENT_VALUE);
                }
            }    
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.SLIDE_VIEWER_EVENT))
        {
            synchronized(lockObj)
            {
                if (changingScreenSaver == false)
                    slideViewer();
            }
        }
        else if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CHANGE_TIMEZONE_EVENT))
        {
            synchronized(lockObj)
            {
                if (changingScreenSaver == false)
                    changeTimeZone();
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
	        Map<String, Clock> clockGroup = EngineHelper.getClocksForGroup(currentClockGroupName);
	        Random generator = new Random();
	        List<ClockDisplayElement [][]> clocks = new ArrayList<ClockDisplayElement [][]>();
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            Rectangle[][] screenRectangles = (Rectangle[][])allScreenRectangles.get(i);
	            int numberHorizontal = screenRectangles.length; 
	            int numberVertical = screenRectangles[0].length; 
	            
	            ClockDisplayElement [][] screenElements = new ClockDisplayElement[numberHorizontal][numberVertical];

	            int emptyX = generator.nextInt((numberHorizontal - 1));
	            int emptyY = generator.nextInt((numberVertical - 1));
                Rectangle rect = screenRectangles[emptyX][emptyY];
                ClockDisplayElement element = new ClockDisplayElement(rect, null, emptyX, emptyY, true, true);
                element.setEmpty(true);
                screenElements[emptyX][emptyY] = element;
                Set<String>keys = clockGroup.keySet();
	            for (int j = 0; j < numberHorizontal; j++)
	            {
	                for (int k = 0; k < numberVertical; k++)
	                {
	                    rect = screenRectangles[j][k];
	                    String key = EngineHelper.getRandomClockFromKeys(keys);
	                    Clock clock = clockGroup.get(key);
	                    if (screenElements[j][k] == null)
		                {
		                    ClockDisplayElement displayElement = new ClockDisplayElement(rect, clock, j, k, true, true);
		                    screenElements[j][k] = displayElement;
		                    totalClocks++;
		                }
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
                    ClockDisplayElement element = screenElements[j][k];
                    if (element.isEmpty()  == false)
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
            		    Rectangle r1 = element.getRectangle();
            		    Rectangle r2 = new Rectangle();
            		    r2.setBounds((int)r1.getX(), (int)r1.getY(), r1.width, r1.height);
            		    frame = CreateSSFrameSetClock(r2, newClock,true, false, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
/*
            		    if(ClockGUI.isWindowsPlatform)
        		        {
        		        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
        			        winacc.setAlwaysOnTop( frame, true );
        		        }
*/
                        element.setFrame(frame);
                    }
                    totalClocks++;
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
    private TimerTask generateChangeClockTimeZoneEvent = new TimerTask() 
    {
        public void run() 
        {
            try
            {
                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.CHANGE_TIMEZONE_EVENT,null));
            }
            catch(Exception e)
            {
                String errStr = resources.getString("ClockError.ExceptionGeneratingChangeTimeZoneEvent");
                System.out.println(errStr + " " + e.getMessage());
            }
        }
    };
    
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
     * method to change time zone
     */
    protected void changeTimeZone()
    {
        try
        {
            //for each screen
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            int numberHorizontal = screenElements.length; 
                int numberVertical = screenElements[0].length; 
                ClockDisplayElement element = null;
                int x = -1;
                int y = -1;
                boolean notDone = true;
                while (notDone)
                {
                    x = changeGenerator.nextInt(numberHorizontal);
                    y = changeGenerator.nextInt(numberVertical);
                    element = screenElements[x][y];
                    if (element.isEmpty() == false)
                        notDone = false;
                }
        
                int val = timeZoneGenerator.nextInt(timeZones.length);
                String timeZone = (timeZones[val]);
                ClockDate date = new ClockDate();
                date.setTimeZone(timeZone);                    
                Clock clock = element.getClock();
                
                clock.pauseClock();
                
                clock.setDate(date);
                String caption = generateCaption(timeZone);
                clock.setCaption(caption);
                clock.configureTime();
                
                clock.resumeClock();
	        }
        } 
	    catch(Exception e)
	    {
	        String errStr = resources.getString("ClockError.ExceptionChangingTimeZones");
	        System.out.println(errStr + " " + e.getMessage());
	    }    
	}
    
    
    /**
     * Event driven method to move the viewers around
     */
    protected void slideViewer()
    {
        try
        {
 	        //for each screen
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            int numberHorizontal = screenElements.length; 
                int numberVertical = screenElements[0].length; 
                int selectX = 0;
                int selectY = 0;
                ClockDisplayElement element = null;
                for (int j = 0; j < numberHorizontal; j++) 
                {
                    for(int k = 0; k < numberVertical; k++)
                    {
                        element = screenElements[j][k];
                        if (element.isEmpty())
                        {
                            selectX = j;
                            selectY = k;
                            j = numberHorizontal;
                            k = numberVertical;
                        }
                    }
                }
                //we found the empty one
                //find a new cell for it adjacent to this one
                boolean success = false;
                while (!success)
                {
                    int newCell = -1;
                    boolean notDone = true;
                    //we want to go the other direction
                    while (notDone)
                    {
                        newCell = changeGenerator.nextInt(4);
                        switch(element.getPreviousMoveFrom())
                        {
                        	case TOP:
                        	    if ((newCell != TOP) && (newCell != BOTTOM))
                        	        notDone = false;
                        	    break;
                        	case BOTTOM:
                        	    if ((newCell != TOP) && (newCell != BOTTOM))
                        	        notDone = false;
                        	    break;
                        	case LEFT:
                        	    if ((newCell != RIGHT) && (newCell != LEFT)) 
                        	        notDone = false;
                        	    break;
                        	case RIGHT:
                        	    if ((newCell != RIGHT) && (newCell != LEFT)) 
                        	        notDone = false;
                        	    break;
                        }
                    }
                    
                    ClockDisplayElement moveElement = null;
                    
                    switch(newCell)
	                {
	                	case TOP:
	                	{
	                	    if (selectY != 0)
	                	    {
	                	        moveElement = screenElements[selectX][selectY - 1];
	                	        //swap positions
	                	        screenElements[selectX][selectY] = moveElement;
	                	        screenElements[selectX][selectY - 1] = element;
	                	        
	                	        int moveToY = element.getRectangle().y;
	                	        int moveFromY = moveElement.getRectangle().y;
	                	        int moveTotalAmount = moveFromY - moveToY; 
	                	        int moveAmountPerMove = (moveTotalAmount / MOVE_ANIMATION);
	                	        int remainderPerMove = ((moveTotalAmount - (moveAmountPerMove * MOVE_ANIMATION))/MOVE_ANIMATION);
	                	        
	                	        //see if we need to apply any at the end
	                	        int finalRemainder = (moveTotalAmount - ((moveAmountPerMove * MOVE_ANIMATION) + (remainderPerMove * MOVE_ANIMATION)));
	                	        int xLocation = moveElement.getRectangle().x;
	                	        int yLocation = moveElement.getRectangle().y;
	                	        //loop and move
	                	        for (int l = 0; l < MOVE_ANIMATION; l++)
	                	        {
	                	            yLocation -= moveAmountPerMove + remainderPerMove;
		                	        if (l + 1 == MOVE_ANIMATION)
		                	            yLocation -= finalRemainder;
	                	            moveElement.getFrame().setLocation(xLocation, yLocation);
	                	            Thread.sleep(MOVE_DELAY);
	                	        }
	                	        success = true;
	                	    }
	                	    break;
	                	}
	                	case BOTTOM:
	                	{
	                	    if (selectY != (numberVertical - 1))
	                	    {
	                	        moveElement = screenElements[selectX][selectY + 1];
	                	        //swap positions
	                	        screenElements[selectX][selectY] = moveElement;
	                	        screenElements[selectX][selectY + 1] = element;
	                	        
	                	        int moveToY = element.getRectangle().y;
	                	        int moveFromY = moveElement.getRectangle().y;
	                	        int moveTotalAmount = moveToY - moveFromY; 
	                	        int moveAmountPerMove = (moveTotalAmount / MOVE_ANIMATION);
	                	        int remainderPerMove = ((moveTotalAmount - (moveAmountPerMove * MOVE_ANIMATION))/MOVE_ANIMATION);
	                	        
	                	        //see if we need to apply any at the end
	                	        int finalRemainder = (moveTotalAmount - ((moveAmountPerMove * MOVE_ANIMATION) + (remainderPerMove * MOVE_ANIMATION)));
	                	        int xLocation = moveElement.getRectangle().x;
	                	        int yLocation = moveElement.getRectangle().y;
	                	        //loop and move
	                	        for (int l = 0; l < MOVE_ANIMATION; l++)
	                	        {
	                	            yLocation += moveAmountPerMove + remainderPerMove;
		                	        if (l + 1 == MOVE_ANIMATION)
		                	            yLocation += finalRemainder;
	                	            moveElement.getFrame().setLocation(xLocation, yLocation);
	                	            Thread.sleep(MOVE_DELAY);
	                	        }
	                	        success = true;
	                	    }
	                	    break;
	                	}
	                	case LEFT:
	                	{
	                	    if (selectX != 0)
	                	    {
	                	        moveElement = screenElements[selectX - 1][selectY];
	                	        //swap positions
	                	        screenElements[selectX][selectY] = moveElement;
	                	        screenElements[selectX - 1][selectY] = element;
	                	        
	                	        int moveToX = element.getRectangle().x;
	                	        int moveFromX = moveElement.getRectangle().x;
	                	        int moveTotalAmount = moveFromX - moveToX; 
	                	        int moveAmountPerMove = (moveTotalAmount / MOVE_ANIMATION);
	                	        int remainderPerMove = ((moveTotalAmount - (moveAmountPerMove * MOVE_ANIMATION))/MOVE_ANIMATION);
	                	        
                	        	//see if we need to apply any at the end
	                	        int finalRemainder = (moveTotalAmount - ((moveAmountPerMove * MOVE_ANIMATION) + (remainderPerMove * MOVE_ANIMATION)));
	                	        int xLocation = moveElement.getRectangle().x;
	                	        int yLocation = moveElement.getRectangle().y;
	                	        //loop and move
	                	        for (int l = 0; l < MOVE_ANIMATION; l++)
	                	        {
	                	            xLocation -= moveAmountPerMove + remainderPerMove;
		                	        if (l + 1 == MOVE_ANIMATION)
		                	            xLocation -= finalRemainder;
	                	            moveElement.getFrame().setLocation(xLocation, yLocation);
	                	            Thread.sleep(MOVE_DELAY);
	                	        }
	                	        success = true;
	                	    }
	                	    break;
	                	}
	                	case RIGHT:
	                	{
	                	    if (selectX != (numberHorizontal - 1))
	                	    {
	                	        moveElement = screenElements[selectX + 1][selectY];
	                	        //swap positions
	                	        screenElements[selectX][selectY] = moveElement;
	                	        screenElements[selectX + 1][selectY] = element;
	                	        
	                	        int moveToX = element.getRectangle().x;
	                	        int moveFromX = moveElement.getRectangle().x;
	                	        int moveTotalAmount = moveToX - moveFromX; 
	                	        int moveAmountPerMove = (moveTotalAmount / MOVE_ANIMATION);
	                	        int remainderPerMove = ((moveTotalAmount - (moveAmountPerMove * MOVE_ANIMATION))/MOVE_ANIMATION);
	                	        
	                	        //see if we need to apply any at the end
	                	        int finalRemainder = (moveTotalAmount - ((moveAmountPerMove * MOVE_ANIMATION) + (remainderPerMove * MOVE_ANIMATION)));
	                	        int xLocation = moveElement.getRectangle().x;
	                	        int yLocation = moveElement.getRectangle().y;
	                	        //loop and move
	                	        for (int l = 0; l < MOVE_ANIMATION; l++)
	                	        {
	                	            xLocation += moveAmountPerMove + remainderPerMove;
		                	        if (l + 1 == MOVE_ANIMATION)
		                	            xLocation += finalRemainder;
	                	            moveElement.getFrame().setLocation(xLocation, yLocation);
	                	            Thread.sleep(MOVE_DELAY);
	                	        }
	                	        success = true;
	                	    }
	                	    break;
	                	}
	                }
                    if (success)
                    {
                        Rectangle saveRectangle = moveElement.getRectangle();
	        	        moveElement.setRectangle(element.getRectangle());
	        	        element.setRectangle(saveRectangle);
	        	        moveElement.getStartingHorizontalIndex();
	        	        moveElement.getStartingVerticalIndex();
	        	        moveElement.setStartingHorizontalIndex(element.getStartingHorizontalIndex());
	        	        moveElement.setStartingVerticalIndex(element.getStartingVerticalIndex());
	        	        element.setPreviousMoveFrom(newCell);
                    }
                }//while !success    
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
            Clock newClock = null;
            SVG_SWTViewerFrame newFrame = null;
            ClockDisplayElement element = null;
            //pick at random
	        for (int i = 0; i < numberofScreens; i++)
	        {
	            ClockDisplayElement[][] screenElements = (ClockDisplayElement[][])allScreenClocks.get(i);
	            if (hasClocksToDisplay(screenElements) == true)
	            {
	                //pick one at random untill you find one that isn't used
	                int numberHorizontal = screenElements.length; 
	                int numberVertical = screenElements[0].length; 
	                for (int j = 0; j < numberHorizontal; j++) 
	                {
	                    for(int k = 0; k < numberVertical; k++)
	                    {
	                        element = screenElements[j][k];
	                        if (element.isVisible() == false)
	                        {
	                            if (element.isEmpty() == false)
	                            {
			                        newClock = element.getClock();
			                        newFrame = element.getFrame();
			            	        Dimension curDimFrame = newFrame.getSize();
			            	        syncClockToFrame(newClock, newFrame);
			            	        element.setVisible(true);
			                        newFrame.setVisible(true);
			                        return;
	                            }    
	                        }
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
