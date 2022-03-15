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

package com.microstran.implementation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.microstran.core.clock.Clock;
import com.microstran.core.clock.ClockDate;
import com.microstran.core.engine.ClockGUI;
import com.microstran.core.engine.EngineHelper;
import com.microstran.core.viewer.AboutDialog;
import com.microstran.core.viewer.SVG_SWTViewerFrame;


/**
 * @author Mstran
 *	Class to create single Clocks from
 */
public class LGClockSwingSingle extends ClockGUI
{
	/**
	 * Static main program entry point
	 * @param args
	 */
	public static void main(String[] args) 
	{
	    try
	    {
	    	
	    	String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
	        if (systemLookAndFeel.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))
	        {
	        	UIManager.setLookAndFeel(systemLookAndFeel);
	        }else {
	        	try {
	        		boolean foundNimbus = false;
	        	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	        	        if ("Nimbus".equals(info.getName())) {
	        	            UIManager.setLookAndFeel(info.getClassName());
	        	            foundNimbus = true;
	        	            break;
	        	        }
	        	    }
	        	    if (!foundNimbus) {
	    	        	systemLookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
	    	        	UIManager.setLookAndFeel(systemLookAndFeel);
	    		        	    }
	        	} catch (Exception e) {
	        	    System.out.println("Error getting look and feel: "+e.getMessage());
	        	}
	        }
	        frameIcon = new ImageIcon(new File(ClockGUI.resourcesPath + "/images/Icon.png").toURI().toURL());
	    }
	    catch(Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
	    new LGClockSwingSingle();
	}
    
    /**
     * 	default constructor
     */
    public LGClockSwingSingle()
    {
        super();
        runSingleClockApplication();
    }

    /**
     * Load all clocks that are configured for this Application
     */
    public void runSingleClockApplication() 
    {
        try
        {
	        //show the splash screen
	        splashScreen = new AboutDialog();
	        progressBar = new JProgressBar(0, 3); //initial setting
	        splashScreen.getContentPane().add("South", progressBar);
            
	        Dimension ds = splashScreen.setAbsoluteLocation();
	        //how much to increment the progress bar
	        splashScreen.setSize(ds);
	        splashScreen.setVisible(true);
	        LoadDefinitionsAndClocks(this);
			progressBar.setValue(++ClockGUI.pbTracker);
			
			int numActiveClocks = activeClocks.size();
		    int numTotalClocks = EngineHelper.getAllDefinedClocks().size();
		    System.out.println("Total Clocks = " + numTotalClocks);
			System.out.println("Active Clocks = " + numActiveClocks);
		    
			if (activeClocks.size() > 0)
			{
			    //we set the progress bar to include the engine start 
				progressBar.setMaximum(numActiveClocks + 1);
	
				//create all the viewers that we need based on the stored settings
				Iterator<Clock> it = activeClocks.iterator();
				while (it.hasNext()){
					Clock clock = it.next();
	    		    clock.configureTime();
					CreateClockFrameSetClock(clock,true,true,true,Color.LIGHT_GRAY, Color.WHITE);
	    		}
	    		//all created and ready, now display them onto the screen all at once
	    		for(int k = 0; k < numActiveClocks; k++) {
	    		    SVG_SWTViewerFrame newFrame = (SVG_SWTViewerFrame)clockViewers.get(k);
	                newFrame.setVisible(true);
	    		}
			}
			else
			{
			    progressBar.setMaximum(2);
				//create a default one
			    Iterator<Clock> it = activeClocks.iterator();
			    Clock clock = it.next();
		        Clock newClock = Clock.createClock(clock);
		        newClock.setCurrentWidth(clock.getSrcWidth());
				newClock.setCurrentHeight(clock.getSrcHeight());
				
			    ClockDate date = new ClockDate();
			    TimeZone dtz = (TimeZone) TimeZone.getDefault();
			    String ID = dtz.getID();
			    String caption = ClockGUI.generateCaption(ID);
			    newClock.setCaption(caption);
			    date.setTimeZone(ID);
			    newClock.setDate(date);
			    newClock.setParentID(clock.getID());

		        //middle of screen 
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			    newClock.setPosX((screenSize.width/2) - (newClock.getSrcWidth()/2));
				newClock.setPosY((screenSize.height/2) - (newClock.getSrcHeight()/2));
			    
				//now create it
				SVG_SWTViewerFrame newFrame = CreateClockFrameSetClock(newClock,true,true,true,Color.LIGHT_GRAY, Color.WHITE);
				newFrame.setVisible(true);
	            newFrame.getSvgCanvas().requestFocusInWindow();
			}
        } 
        catch (Exception e) 
        {
	        String errStr = resources.getString("ClockError.ExceptionLoading");
            System.out.println(errStr + " " + e.getMessage());
        }
    }


}
