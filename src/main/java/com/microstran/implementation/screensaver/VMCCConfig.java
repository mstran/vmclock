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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import com.microstran.core.engine.AbstractScreenSaver;
import com.microstran.core.engine.EngineHelper;


/**
 * The  class for configuration details
 *
 * @author Mike Stran
 *
 */
public class VMCCConfig 
{

    private AbstractScreenSaver currentScreenSaver;
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
 
    
    public static void main(String[] args) 
	{
	    try
	    {
	        String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
	        String crossPlatformLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); 
	        //if it's not windows then force the metal look and feel by default to avoid problems in dialog boxes
	        if (!systemLookAndFeel.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))
	            systemLookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
	        
	        UIManager.setLookAndFeel(systemLookAndFeel);
	    }
	    catch(Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
		VMCCConfig screenSaverConfig = new VMCCConfig();
	    screenSaverConfig.editSettings();
	}


    public VMCCConfig()
    {
        currentScreenSaver = new AbstractScreenSaver();
        //acquire all clock definitions from definitions.xml
        EngineHelper.getAllClockDefinitions(currentScreenSaver);
        //acquire saved screen saver confguration from the ss_configuration.xml file
        screenSaverList = EngineHelper.getScreenSaverConfiguration(currentScreenSaver);
        //get general system settings
		currentScreenSaver.getConfigurationSettings();
		editSettings();
    }


    /**
     * Pull up the configuration settings dialog box
     */
    private void editSettings()
    {
        VMCCScreenSaverConfigDialog cfgDialog = null;
        List list = new ArrayList();
        try
        {
            Iterator it = allScreenSaverList.keySet().iterator();
            while (it.hasNext())
                list.add(it.next());
            
            cfgDialog = new VMCCScreenSaverConfigDialog(currentScreenSaver,list,screenSaverList);
			cfgDialog.setAbsoluteLocation();
			cfgDialog.setVisible(true);
			cfgDialog.toFront();
			if (cfgDialog.getOkButtonState() == true)
			{
			    screenSaverList.clear();
			    screenSaverList = cfgDialog.getNewScreenSaverConfig(currentScreenSaver);
			    //write out the saved settings
			    EngineHelper.removeScreenSaverSettings();
			    EngineHelper.writeScreenSaverSettings(currentScreenSaver,screenSaverList);
			}
        }
        catch(Exception ex)
		{
            System.out.println(ex.getMessage());
            ex.printStackTrace();
		}
		finally
		{
		    cfgDialog.dispose();
		    list.clear();
		    System.exit(0);
		}
    }
	

}