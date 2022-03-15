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
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.microstran.core.engine.AbstractScreenSaver;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.engine.EngineHelper;
import com.microstran.core.engine.util.ExecEnvironment;
import com.microstran.core.event.ApplicationEvent;
import com.microstran.core.event.ApplicationEventListener;
import com.microstran.core.event.ApplicationEventServer;
import com.microstran.core.event.clock.WindowChangeEvent;
import com.microstran.core.viewer.AboutDialog;

//import net.mysrc.gui.aot.AlwaysOnTop;
//import net.mysrc.gui.aot.WindowAccess;



public class VMCCScreenSaver implements KeyListener, MouseListener, MouseMotionListener, ApplicationEventListener
{

	/**
     * the pointer to the current screen saver
     */
    private AbstractScreenSaver currentScreenSaver;
    
    /**
     * These represent the AWT window frame components that will be built. These
     * are more important for some of the screen savers than others since they basically
     * represent the backdrop on which the frames will be painted
     */
    protected Frame[] mainWindowFrame;
    protected Window[] mainWindow;
    
    
    /**
     * Array list of all screen savers
     */
    protected List<String> screenSaverList;
    
    
    public static final String SINGLE_CLOCK = "Single Full Screen";
    public static final String MULTI_CLOCK  = "Multi-Clock Full Screen";
    public static final String VERT_SCROLL  = "Vertical-Scroll";
    public static final String GRID_SLIDE   = "Grid Slide";
    
    /**
     * array of screen savers to cycle through
     */
    public static Map<String, String> allScreenSaverList = new HashMap<String, String>();
    static 
    {
        allScreenSaverList.put(SINGLE_CLOCK, "com.microstran.implementation.screensaver.CenterClockScreenSaver");
        allScreenSaverList.put(MULTI_CLOCK, "com.microstran.implementation.screensaver.VerticalClockScreenSaver");
        allScreenSaverList.put(VERT_SCROLL, "com.microstran.implementation.screensaver.TimeZoneVerticalScrollingTripletScreenSaver");
        allScreenSaverList.put(GRID_SLIDE, "com.microstran.implementation.screensaver.GridPuzzleScreenSaver");
    }
 
    /**
     * maximum number of screen savers
     */
    public static final int MAX_SCREENSAVER = 4;
   
    /**
     * timer for changing Screen Savers
     */
    protected static Timer changeScreenSaverEventTimer;

    /**
     * the default screen saver to use
     */
    public static final String DEFAULT_SCREEN_SAVER = SINGLE_CLOCK;
    
    /**
     * The current clock group
     */
    protected static int currentClockGroup;

    protected boolean ssInitialized  = false;
    
    /**
     * Current screen saver we are on
     */
    protected static int currentScreenSaverIndex = 0;
    
   //  KeyListener
	public void keyPressed(KeyEvent e)  { System.exit(0); }
	public void keyReleased(KeyEvent e) {  System.exit(0); }
	public void keyTyped(KeyEvent e)    {  System.exit(0); }
	// MouseListener
	public void mouseClicked(MouseEvent e) { System.exit(0); } 
	public void mouseEntered(MouseEvent e)
    {  
        if (ssInitialized)
            System.exit(0); 
    }
	public void mouseExited(MouseEvent e)
    {  
        if (ssInitialized)
            System.exit(0); 
    }
	public void mousePressed(MouseEvent e) { System.exit(0); }  
	public void mouseReleased(MouseEvent e) { System.exit(0); }  
	
	// MouseMotionListener
	public void mouseDragged(MouseEvent e) { System.exit(0); }
	public void mouseMoved(MouseEvent e)
    {  
        if (ssInitialized)
            System.exit(0); 
    }
	
	
	/**
	 * Normal Java Entry Point
	 * @param args
	 */
	public static void main(String[] args) 
	{
	    try
	    {
	        String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
	        //String crossPlatformLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); 
	        //if it's not windows then force the metal look and feel by default to avoid problems in dialog boxes
	        if (!systemLookAndFeel.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))
	            systemLookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
	        
	        UIManager.setLookAndFeel(systemLookAndFeel);
	        ClockGUI.frameIcon = new ImageIcon(new File(ClockGUI.resourcesPath + "/images/Icon.png").toURI().toURL());
	    }
	    catch(Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
        VMCCScreenSaver saver = new VMCCScreenSaver();
	}


	/**
     * 	default constructor
     */
    public VMCCScreenSaver()
    {
        try
        {
	        ApplicationEventServer.instance().register(this, WindowChangeEvent.CHANGE_SCREENSAVER_EVENT);
	        currentScreenSaver = new AbstractScreenSaver();
	        //acquire all clock definitions from definitions.xml
	        EngineHelper.getAllClockDefinitions(currentScreenSaver);
	        //acquire saved screen saver confguration from the ss_configuration.xml file
	        screenSaverList = EngineHelper.getScreenSaverConfiguration(currentScreenSaver);
	        //get general system settings
			currentScreenSaver.getConfigurationSettings();
	        
			//for debugging purposes only -gives you time to setup second screen and move program 
			//Thread.sleep(30000);
			
			//create a viewing matrix for the screen savers to use
	        moveMouseOffScreensEdge();
	        //create the views     
	        startScreenSaverEngine();
		    ssInitialized = true;
        }
        catch(Exception ex)
        {
            System.out.println("error on startup = " + ex.getMessage());
        }
       
    }

    /**
     * There is a bug in the always on top code that WILL NOT allow you to hide the 
     * cursor in the viewer frames no matter what you do, to allow for this we will move
     * the mouse out of the way, just slightly off screen, this is system dependent and has
     * issues in some environments but the screen savers are primarily windows based anyways
     */
    private void moveMouseOffScreensEdge()
    {
        try
        {
	        //figure out what is the left most edge
            int right = 0;
            int bottom  = 0;
	        for (int i = 0; i < AbstractScreenSaver.numberofScreens; i++)
	        {
	            Rectangle sr = (Rectangle)ClockGUI.getScreenRes().get(i);
	            right = Math.max(right, (sr.x + sr.width));
	            bottom  = Math.max(bottom, (sr.y + sr.height)); 
	        }
	        Robot robot = new Robot();
	        robot.mouseMove(right, bottom);
        }
        catch(Exception e)
        {
            //unsupported on some systems (like X), just quietly ignore
        }
    }
    
 
    /**
     * Begin screen savers
     */
    public void startScreenSaverEngine()
    {
        try
        {
            currentScreenSaver = getScreenSaver();
        	//create the main window(s)
            mainWindowFrame = createMainFrames(currentScreenSaver.getBackgroundColor());
            AbstractScreenSaver.allScreenRectangles = createViewMatrix(currentScreenSaver.getMaxClocksPerScreen());
	        
            
            //now show progress splash screen
            ClockGUI.splashScreen = new AboutDialog();
	        ClockGUI.progressBar = new JProgressBar(0, 3); //initial setting
	        ClockGUI.splashScreen.getContentPane().add("South", ClockGUI.progressBar);
	        /*
	        if (ClockGUI.isWindowsPlatform)
	        {
	            WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
	            winacc.setAlwaysOnTop( ClockGUI.splashScreen, true );
	        }*/
	        Dimension ds = ClockGUI.splashScreen.setAbsoluteLocation();
	        //how much to increment the progress bar
	        ClockGUI.splashScreen.setSize(ds);
	        ClockGUI.splashScreen.setVisible(true);
	        
            //create and layout all the clocks
            prepareInitialScreenSaver();
            currentScreenSaver.startScreenSaver();
            startScreenSaverTimer();
        }
        catch(Exception e)
        {}
    }
	
    /**
     * Event handler for screen saver change event
     */
    private void changeScreenSaver()
    {
        try
        {
	        currentScreenSaverIndex++;
	        if (currentScreenSaverIndex >= screenSaverList.size())
	            currentScreenSaverIndex = 0;
	        //instantiate the next screen saver class
	        AbstractScreenSaver newScreenSaver = getScreenSaver(); 
	    	// now stop existing screen saver
	    	currentScreenSaver.stopScreenSaver();
	    	//save the new as current
	    	currentScreenSaver = newScreenSaver;
	    	
	    	//check the color and possibly change
	    	int numScreens = mainWindowFrame.length;
	    	for (int i = 0; i < numScreens; i++)
            {
	    	    Frame frame = mainWindowFrame[i];
	    	    frame.setBackground(currentScreenSaver.getBackgroundColor());
            }
	    	
	    	//create and layout all the clocks
        	prepareScreenSaver();
        	//startup the screen saver
        	currentScreenSaver.startScreenSaver();
	    }
        catch(Exception ex)
        {
            String errStr = ClockGUI.resources.getString("ClockError.ExceptionChangingScreenSaver");
            System.out.println(errStr + " " + ex.getMessage());
        }
    }
    
    /**
     * Create one or more background windows to work in
     */
    protected Frame[] createMainFrames(Color color)
    {
        try
        {
            Frame[] frame;
            ArrayList list = ClockGUI.getScreenRes();
            int numScreens = list.size();
            frame = new Frame[numScreens];
            for (int i = 0; i < numScreens; i++)
            {
                Rectangle sr = (Rectangle)list.get(i);
                
                GraphicsDevice[] gs = ExecEnvironment.ge.getScreenDevices();
                GraphicsDevice gd = gs[i];
               	GraphicsConfiguration gc = gd.getDefaultConfiguration();
                
                //the section below is to account for linux where top or bottom insets on the screen from the shell will make the actual space less
               	//the code below subtracts those
               	Rectangle bounds = gd.getDefaultConfiguration().getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

                Rectangle safeBounds = new Rectangle(bounds);
                safeBounds.x += insets.left;
                safeBounds.y += insets.top;
                safeBounds.width -= (insets.left + insets.right);
                safeBounds.height -= (insets.top + insets.bottom);
                
                sr.setBounds(safeBounds.x, safeBounds.y, safeBounds.width, safeBounds.height);
                Frame localFrame = new Frame(gc);
                localFrame.setUndecorated(true);
                localFrame.setAlwaysOnTop(true);
                localFrame.setState(Frame.MAXIMIZED_BOTH);
                
                localFrame.setBounds(sr.x, sr.y, sr.width, sr.height);
                //localFrame.setBackground(color);
                localFrame.addMouseListener(this);
                localFrame.addMouseMotionListener(this);
                localFrame.addKeyListener(this);
                localFrame.setVisible(true);
                
                /*
                if(ClockGUI.isWindowsPlatform)
    	        {
    	        	WindowAccess winacc = AlwaysOnTop.getSystemDependentInstance();
    		        winacc.setAlwaysOnTop( localFrame, true );
    	        }*/
                frame[i]=localFrame;
            }
        return(frame);
        }
        catch(Exception e)
        {
            String errStr = ClockGUI.resources.getString("ClockError.ExceptionCreatingBackground");
            System.out.println(errStr + " " + e.getMessage());
        }
        return(null);
    }

    /**
     * Do this so we can obfuscate the screen saver classes, if we don't do this
     * and call class for name, we would have to expose these classes since you 
     * can't obfuscate classes that are dynamically instantiated by name
     * 
     * @return new screen saver
     */
    private AbstractScreenSaver getScreenSaver()
    {
        String className;
        if (screenSaverList.size() == 0)
        {
            return(new CenterClockScreenSaver());  //our default
        }
        else
        {
            className = (String)screenSaverList.get(currentScreenSaverIndex);
            
	        if (className.equals(SINGLE_CLOCK))
	            return(new CenterClockScreenSaver());
	        else if (className.equals(MULTI_CLOCK))
	            return(new VerticalClockScreenSaver());
	        else if (className.equals(VERT_SCROLL))
	            return(new TimeZoneVerticalScrollingTripletScreenSaver());
	        else if (className.equals(GRID_SLIDE))
            return(new GridPuzzleScreenSaver());
	        else
	            return(null);
        }
    }
    
    /**
     * Layout and instantiate the clocks, startup the screen saver
     */
    protected void prepareInitialScreenSaver()
    {
        currentScreenSaver.layoutClocks();
        //for the vertical clock screen saver this is not totally accurate
        int numActiveClocks = (currentScreenSaver.getTotalClocks());
		ClockGUI.progressBar.setMaximum(numActiveClocks);
		ClockGUI.pbTracker = 2;
        ClockGUI.progressBar.setValue(ClockGUI.pbTracker);
        currentScreenSaver.instantiateClocks();
    }

    /**
     * for all others
     */
    protected void prepareScreenSaver()
    {
        currentScreenSaver.layoutClocks();
        currentScreenSaver.instantiateClocks();
    }
    
    /**
     * startup the timer
     */
    private void startScreenSaverTimer()
    {
        changeScreenSaverEventTimer = new Timer();
        changeScreenSaverEventTimer.schedule(generateChangeScreenSaverEvent, currentScreenSaver.getScreenSaverRotationPeriod(),currentScreenSaver.getScreenSaverRotationPeriod());
    }
    
    
    public void listen( ApplicationEvent applicationEvent )
	{
        if (applicationEvent.getApplicationEventType().equals(WindowChangeEvent.CHANGE_SCREENSAVER_EVENT))
        {
                changeScreenSaver();
        }
	}
    
    
    /**
     * Comment for <code>generateChangeScreenSaverEvent</code>
     */
    private TimerTask generateChangeScreenSaverEvent = new TimerTask() 
    {
        public void run() 
        {
            try
            {
                ApplicationEventServer.instance().publishEvent(new WindowChangeEvent(WindowChangeEvent.CHANGE_SCREENSAVER_EVENT,null));
            }
            catch(Exception e)
            {
                String errStr = ClockGUI.resources.getString("ClockError.ExceptionGeneratingChangeEvent");
                System.out.println(errStr + " " + e.getMessage());
            }
        }
    };
    
    
    /**
     * partition up the screen into a matrix of rectangles that will be filled 
     * in with clocks
     */
    public ArrayList createViewMatrix(int clocksPerScreen)
    {
        ArrayList allScreenRect = null;
        try
        {
	        int width,height,x,y;
	        
	        //what we want to do here is create a screen matrix or set of matrixs that 
	        //allow us to slice up the screen into sections 
	        allScreenRect = new ArrayList();
	        
	        ArrayList list = ClockGUI.getScreenRes();
            int numScreens = list.size();
            for (int i = 0; i < numScreens; i++)
	        {
                GraphicsDevice[] gs = ExecEnvironment.ge.getScreenDevices();
                GraphicsDevice gd = gs[i];
               	GraphicsConfiguration gc = gd.getDefaultConfiguration();
                
                //the section below is to account for linux where top or bottom insets on the screen from the shell will make the actual space less
               	//the code below subtracts those
               	Rectangle bounds = gd.getDefaultConfiguration().getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

                Rectangle safeBounds = new Rectangle(bounds);
                safeBounds.x += insets.left;
                safeBounds.y += insets.top;
                safeBounds.width -= (insets.left + insets.right);
                safeBounds.height -= (insets.top + insets.bottom);
                
                
	        	//take the amount of screens we can draw / by 2 to get even
	            //horizontal vertical array 
	            int clocksEachWay = (int)Math.sqrt(clocksPerScreen);
	            
	            int numberHorizontal = clocksEachWay;
	            int targetWidthHorizontal = safeBounds.width/numberHorizontal;
	            //if remainder is < number clocks across -1 then pad left and right
	            int remainderHorizontal = (safeBounds.width%numberHorizontal);
	            
	            int numberVertical   = clocksEachWay;
	            int targetWidthVertical = (safeBounds.height/numberVertical) - ClockGUI.BOTTOM_MESSAGE_BAR_SIZE;  //MJS SUBTRACT OF MESSAGE BAR
	            //if remainder is < number clocks down -1 then pad top and bottom
	            int remainderVertical = (safeBounds.height%numberVertical);
	            
	            //now adjust the numbers based on the clocks per screen
	            int totalThisScreen = (numberHorizontal * numberVertical);
	            
	            System.out.println("Clocks Each Way: "+ numberHorizontal);
	            System.out.println("TargetWidthHorizontal: "+ targetWidthHorizontal);
	            System.out.println("RemainderHorizontal: "+ remainderHorizontal);
	            
	            System.out.println("targetWidthVertical: "+ targetWidthVertical);
	            System.out.println("remainderVertical: "+ remainderVertical);
	            
	            //for each row
	            Rectangle [][] screenRectangles;
	            screenRectangles = new Rectangle[numberHorizontal][numberVertical];
	            
	            //we create the matrix in column order, the whole first column will have
	            //either have some padding and the rest not or all columns will  pad
	            //however as we figure out Y positions, each first row will either have padding or not
	            boolean doNotApplyYRemainder = false;
	            int endX = safeBounds.x;
	            
	            for (int j = 0; j < numberHorizontal; j++)
	            {
	                int endY = safeBounds.y; //reset with each trip through loop
	                //if not enough to distribute across all then pad left & right -right padded by adding half to left only
	                if ((remainderHorizontal < (numberHorizontal + 1)) && (j == 0) )
	                {
	                 	x = (endX + (remainderHorizontal / 2));
	                   	remainderHorizontal = 0;
	                }
	                else  
	                    x = (endX + (remainderHorizontal / (numberHorizontal + 1)) );
	                
	                //for each row
	                for (int k = 0; k < numberVertical; k++)
	                {
	                    //if not enough to distribute across all then pad top and bottom -bottom padded by adding half to top only
	                    if ((remainderVertical < (numberVertical + 1)) && (k == 0) )   
	                    {
	                       y = (endY + (remainderVertical / 2));
	                       doNotApplyYRemainder = true;
	                    }
	                    else
	                    {
	                        if (doNotApplyYRemainder == true)
	                            y = endY; 
	                        else
	                            y = (endY + (remainderVertical / (numberVertical + 1)));
	                    }
	                    Rectangle rectangle = new Rectangle();
	                    //subtract 1 here to size rectangles so frames don't overlap 
	                    rectangle.setBounds(x,y,(targetWidthHorizontal-1),(targetWidthVertical-1));
	                    endX = x + targetWidthHorizontal;
	                    endY = y + targetWidthVertical;
	                    screenRectangles[j][k]=rectangle;
	                }//vertical
	            }//horizontal
	            
	            //add all the calculated rectangles to the collection now
	            allScreenRect.add(screenRectangles); 
	        }//number of screens
        }
        catch(Exception e)
        {
	        String errStr = ClockGUI.resources.getString("ClockError.ExceptionCreatingViewMatrix");
            System.out.println(errStr + " " + e.getMessage());
        }
        return(allScreenRect);
    }
}


  