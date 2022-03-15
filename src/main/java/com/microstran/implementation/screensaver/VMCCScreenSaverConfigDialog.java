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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.microstran.core.engine.AbstractScreenSaver;
import com.microstran.core.engine.ClockGUI;
import com.toedter.components.JSpinField;

/**
 * @author Mstran
 *
 * 
 */
public class VMCCScreenSaverConfigDialog extends JDialog
{
    // OK Cancel Buttons Panel
	private JButton moveDown;
	private JButton moveUp;
	private JPanel orderPanel;
	private JCheckBox frameTimeDateCheckBox;
	private JCheckBox frameClockCheckBox;
	private JLabel groupLabel;
	private JComboBox groupComboBox;
	private JPanel groupPanel;
	private JCheckBox limitToGroupCheckBox;
	private JLabel maxClocksPerWindowLabel;
	private JSpinField maxClocksPerWindowSpinner;
	private JLabel selectedScreenSaversLabel;
	private JLabel availableScreenSaversLabel;
	private JList selectedScreenSavers;
	private JButton removeCurrent;
	private JButton removeAll;
	private JButton addAll;
	private JButton addCurrent;
	private JPanel moveSaversPanel;
	private JLabel rotateScreenSaverLabel;
	private JList availableScreenSavers;
	private JSpinField rotateScreenSaverSpinner;
	private JLabel rotateClocksLabel;
	private JSpinField rotateClockSpinner;
	private JPanel configPanel;
	private JPanel oKCancelPanel;
	private JButton okButton;
	private JButton cancelButton;
	
	private boolean startingUp;
	private boolean okButtonState;
	
	private static final int MAX_WIDTH=500;
	private static final int MAX_PANEL_HEIGHT = 470;
	
	private DefaultListModel availableSSListModel;
	private DefaultListModel selectedSSListModel;
	
	/**
     * Main entry point
     * @param args
     */
    public static void main(String args[])
    {
        VMCCScreenSaverConfigDialog dialog = new VMCCScreenSaverConfigDialog(null, null, null);
        
    }

    /**
     * default constructor
     */
    public VMCCScreenSaverConfigDialog(AbstractScreenSaver saver, List allSavers, List selectedSavers)
    {
        super();
        getContentPane().setBounds(0, 0, 580, 390);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        setBounds(0, 0, 580, 390);
        
        setSize(new Dimension(580, 390));
        setName("ScreenSaverConfiguration");
        final GridBagConstraints gridBagConstraints;
        final GridBagConstraints gridBagConstraints_1;
        final GridBagConstraints gridBagConstraints_2;
        final GridBagConstraints gridBagConstraints_4;
        final GridBagConstraints gridBagConstraints_3;
        final GridBagConstraints gridBagConstraints_5;
        final Component top;
        final GridBagConstraints gridBagConstraints_6;
        final GridBagConstraints gridBagConstraints_7;
        final GridBagConstraints gridBagConstraints_8;
        final GridBagConstraints gridBagConstraints_9;
        final GridBagConstraints gridBagConstraints_10;
        final GridBagConstraints gridBagConstraints_11;
        final GridBagConstraints gridBagConstraints_12;
        final GridBagConstraints gridBagConstraints_13;
        final GridBagConstraints gridBagConstraints_14;
        final GridBagConstraints gridBagConstraints_15;
        setTitle("Screen Saver Configuration");
        startingUp = true;
		okButtonState = false;
		
		setResizable(false);
		setModal(true);

		//set the content pane to use gridbaglayout
		getContentPane().setLayout(new BorderLayout());

		//Setup OK Cancel Panel
		oKCancelPanel = new JPanel();
		oKCancelPanel.setMinimumSize(new Dimension(0, 0));
		oKCancelPanel.setPreferredSize(new Dimension(495, 35));
		final FlowLayout okCancelflowLayout;
		okCancelflowLayout = new FlowLayout();
		okCancelflowLayout.setAlignment(FlowLayout.RIGHT);
		oKCancelPanel.setLayout(okCancelflowLayout);
		
		getContentPane().add(oKCancelPanel, BorderLayout.SOUTH);
		okButton = new JButton();
		okButton.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_okButton_actionPerformed();
			}
		});
		okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		oKCancelPanel.add(okButton);
		okButton.setText(ClockGUI.resources.getString("ConfigDLG.save"));
		cancelButton = new JButton();
		cancelButton.setVerifyInputWhenFocusTarget(false);
		cancelButton.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent e) 
			{
				do_cancelButton_actionPerformed();
			}
		});
		cancelButton.setHorizontalAlignment(SwingConstants.RIGHT);
		oKCancelPanel.add(cancelButton);
		cancelButton.setText(ClockGUI.resources.getString("ConfigDLG.cancel"));

		configPanel = new JPanel();
		configPanel.setMaximumSize(new Dimension(400, 400));
		configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		configPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		configPanel.setLayout(new GridBagLayout());
		configPanel.setPreferredSize(new Dimension(400, 310));
		configPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        getContentPane().add(configPanel, BorderLayout.NORTH);

		rotateClockSpinner = new JSpinField();
		rotateClockSpinner.setMinimum(1);
		
		rotateClockSpinner.setPreferredSize(new Dimension(50, 25));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipadx = 38;
		gridBagConstraints.insets = new Insets(5, 0, 0, 0);
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridx = 0;
		configPanel.add(rotateClockSpinner, gridBagConstraints);

		rotateClocksLabel = new JLabel();
		gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.insets = new Insets(5, 5, 0, 0);
		gridBagConstraints_1.gridy = 2;
		gridBagConstraints_1.gridx = 1;
		configPanel.add(rotateClocksLabel, gridBagConstraints_1);
		rotateClocksLabel.setText("Rotate Clocks (minutes)");

		rotateScreenSaverSpinner = new JSpinField();
		rotateScreenSaverSpinner.setMinimum(1);
		
		rotateScreenSaverSpinner.setPreferredSize(new Dimension(50, 25));
		gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.ipadx = 26;
		gridBagConstraints_2.insets = new Insets(5, 0, 0, 0);
		gridBagConstraints_2.gridy = 2;
		gridBagConstraints_2.gridx = 2;
		configPanel.add(rotateScreenSaverSpinner, gridBagConstraints_2);

		availableSSListModel = new DefaultListModel();
		availableScreenSavers = new JList(availableSSListModel);
		availableScreenSavers.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        do_availableScreenSavers_valueChanged();
		    }
		});
		availableScreenSavers.setPreferredSize(new Dimension(150, 75));
		availableScreenSavers.setMinimumSize(new Dimension(150, 75));
		gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints_4.gridx = 1;
		gridBagConstraints_4.gridy = 1;
		configPanel.add(availableScreenSavers, gridBagConstraints_4);

		rotateScreenSaverLabel = new JLabel();
		gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridwidth = 2;
		gridBagConstraints_3.insets = new Insets(5, 5, 0, 0);
		gridBagConstraints_3.gridy = 2;
		gridBagConstraints_3.gridx = 3;
		configPanel.add(rotateScreenSaverLabel, gridBagConstraints_3);
		rotateScreenSaverLabel.setText("Rotate Screen Savers (minutes)");

		moveSaversPanel = new JPanel();
		moveSaversPanel.setLayout(new BoxLayout(moveSaversPanel, BoxLayout.Y_AXIS));
		gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.fill = GridBagConstraints.BOTH;
		gridBagConstraints_5.gridy = 1;
		gridBagConstraints_5.gridx = 2;
		configPanel.add(moveSaversPanel, gridBagConstraints_5);

		top = Box.createVerticalStrut(7);
		moveSaversPanel.add(top);

		addCurrent = new JButton();
		addCurrent.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_addCurrent_actionPerformed();
		    }
		});
		addCurrent.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveSaversPanel.add(addCurrent);
		addCurrent.setText(">");

		moveSaversPanel.add(Box.createVerticalStrut(14));

		addAll = new JButton();
		addAll.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_addAll_actionPerformed();
		    }
		});
		addAll.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveSaversPanel.add(addAll);
		addAll.setText(">>");
		moveSaversPanel.add(addAll);

		moveSaversPanel.add(Box.createRigidArea(new Dimension(40, 10)));

		removeAll = new JButton();
		removeAll.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_removeAll_actionPerformed();
		    }
		});
		removeAll.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveSaversPanel.add(removeAll);
		removeAll.setText("<<");

		moveSaversPanel.add(Box.createVerticalStrut(10));

		removeCurrent = new JButton();
		removeCurrent.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_removeCurrent_actionPerformed();
		    }
		});
		removeCurrent.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveSaversPanel.add(removeCurrent);
		removeCurrent.setText("<");

		selectedSSListModel = new DefaultListModel();
		selectedScreenSavers = new JList(selectedSSListModel);
		selectedScreenSavers.setMinimumSize(new Dimension(150, 75));
		selectedScreenSavers.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        do_selectedScreenSavers_valueChanged();
		    }
		});
		selectedScreenSavers.addHierarchyListener(new HierarchyListener() {
		    public void hierarchyChanged(HierarchyEvent e) {
		        do_selectedScreenSavers_hierarchyChanged();
		    }
		});
		selectedScreenSavers.setPreferredSize(new Dimension(150, 75));
		gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints_6.gridy = 1;
		gridBagConstraints_6.gridx = 3;
		configPanel.add(selectedScreenSavers, gridBagConstraints_6);

		availableScreenSaversLabel = new JLabel();
		gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.gridx = 1;
		gridBagConstraints_7.gridy = 0;
		configPanel.add(availableScreenSaversLabel, gridBagConstraints_7);
		availableScreenSaversLabel.setText("Available Screen Savers");

		selectedScreenSaversLabel = new JLabel();
		gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.gridy = 0;
		gridBagConstraints_8.gridx = 3;
		configPanel.add(selectedScreenSaversLabel, gridBagConstraints_8);
		selectedScreenSaversLabel.setText("Selected Screen Savers");

		maxClocksPerWindowSpinner = new JSpinField();
		maxClocksPerWindowSpinner.setMaximum(25);
		maxClocksPerWindowSpinner.setMinimum(1);
		
		maxClocksPerWindowSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
		maxClocksPerWindowSpinner.setPreferredSize(new Dimension(50, 25));
		gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_9.ipadx = 38;
		gridBagConstraints_9.insets = new Insets(5, 0, 0, 0);
		gridBagConstraints_9.gridy = 3;
		gridBagConstraints_9.gridx = 0;
		configPanel.add(maxClocksPerWindowSpinner, gridBagConstraints_9);

		maxClocksPerWindowLabel = new JLabel();
		gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.insets = new Insets(0, 5, 0, 0);
		gridBagConstraints_10.gridy = 3;
		gridBagConstraints_10.gridx = 1;
		configPanel.add(maxClocksPerWindowLabel, gridBagConstraints_10);
		maxClocksPerWindowLabel.setText("Max. Clocks Per Window");

		limitToGroupCheckBox = new JCheckBox();
		limitToGroupCheckBox.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_limitToGroupCheckBox_actionPerformed();
		    }
		});
		limitToGroupCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.anchor = GridBagConstraints.WEST;
		gridBagConstraints_11.insets = new Insets(5, 2, 0, 0);
		gridBagConstraints_11.gridwidth = 2;
		gridBagConstraints_11.gridy = 4;
		gridBagConstraints_11.gridx = 0;
		configPanel.add(limitToGroupCheckBox, gridBagConstraints_11);
		limitToGroupCheckBox.setText("Limit To Clock Group");

		groupPanel = new JPanel();
		gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.fill = GridBagConstraints.BOTH;
		gridBagConstraints_12.gridwidth = 2;
		gridBagConstraints_12.gridy = 4;
		gridBagConstraints_12.gridx = 2;
		configPanel.add(groupPanel, gridBagConstraints_12);

		groupComboBox = new JComboBox();
		groupComboBox.setPreferredSize(new Dimension(175, 25));
		groupPanel.add(groupComboBox);

		groupLabel = new JLabel();
		groupPanel.add(groupLabel);
		groupLabel.setText("Group");

		frameClockCheckBox = new JCheckBox();
		gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.insets = new Insets(5, 2, 0, 0);
		gridBagConstraints_13.anchor = GridBagConstraints.WEST;
		gridBagConstraints_13.gridwidth = 2;
		gridBagConstraints_13.gridy = 5;
		gridBagConstraints_13.gridx = 0;
		configPanel.add(frameClockCheckBox, gridBagConstraints_13);
		frameClockCheckBox.setText("Draw Frame Around Clock");

		frameTimeDateCheckBox = new JCheckBox();
		gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.insets = new Insets(5, 15, 0, 0);
		gridBagConstraints_14.anchor = GridBagConstraints.WEST;
		gridBagConstraints_14.gridwidth = 2;
		gridBagConstraints_14.gridy = 5;
		gridBagConstraints_14.gridx = 2;
		configPanel.add(frameTimeDateCheckBox, gridBagConstraints_14);
		frameTimeDateCheckBox.setText("Draw Frame Around Time/Date");

		orderPanel = new JPanel();
		orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
		gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.fill = GridBagConstraints.BOTH;
		gridBagConstraints_15.gridy = 1;
		gridBagConstraints_15.gridx = 4;
		configPanel.add(orderPanel, gridBagConstraints_15);

		orderPanel.add(Box.createVerticalStrut(40));

		moveUp = new JButton();
		moveUp.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_moveUp_actionPerformed();
		    }
		});
		moveUp.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveUp.setMargin(new Insets(2, 5, 2, 5));
		orderPanel.add(moveUp);
		moveUp.setText("Move Up");

		orderPanel.add(Box.createVerticalStrut(15));

		moveDown = new JButton();
		moveDown.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        do_moveDown_actionPerformed();
		    }
		});
		moveDown.setAlignmentX(Component.CENTER_ALIGNMENT);
		moveDown.setMargin(new Insets(2, 5, 2, 5));
		orderPanel.add(moveDown);
		moveDown.setText("Move Down");
    
		setScreenSaverConfig(saver, allSavers, selectedSavers);
		startingUp = false;
    }
    
    
    /**
     * This function will read from the screen saver but will not
     * modify it's internal structure
     * @param saver
     */
    private void setScreenSaverConfig(AbstractScreenSaver saver, List allSavers, List selectedSavers)
    {
        if (saver == null)
            return;
        for (int i = 0; i < selectedSavers.size(); i++)
            selectedSSListModel.insertElementAt(selectedSavers.get(i), i);

        for (int i = 0; i < allSavers.size(); i++)
        {
            //only put available entries that ARE NOT currently selected!
            String selection = (String)allSavers.get(i);
            if (findInListModel(selection, selectedSSListModel) == -1)
                availableSSListModel.addElement(selection);
            
        }
        
        String selectedGroup = (String)AbstractScreenSaver.getCurrentClockGroupName();
        List<String> groups = ClockGUI.clockCategories;
        for (int i = 0; i < groups.size(); i++)
        {
            String group = (String)groups.get(i);
            groupComboBox.addItem(group);
            if (selectedGroup.equals(group))
                groupComboBox.setSelectedIndex(i);
        }
        groupComboBox.setEnabled(saver.isLimitScreenToGroup());

        frameClockCheckBox.setSelected(saver.isFrameAroundClocks());
        frameTimeDateCheckBox.setSelected(saver.isBoxAroundCaption());
        limitToGroupCheckBox.setSelected(saver.isLimitScreenToGroup());
        
        maxClocksPerWindowSpinner.setValue(saver.getMaxClocksPerScreen());
        int clockRotatePeriod = (int)(saver.getClockRotationPeriod()/60000);
        rotateClockSpinner.setValue(clockRotatePeriod);
        int screenRotatePeriod = (int)(saver.getScreenSaverRotationPeriod()/60000);
        rotateScreenSaverSpinner.setValue(screenRotatePeriod);

        updateSelectButtons();
    }
    
    /**
     * This function will modify the passed in screen saver with the
     * current dialog box settings and return the selected screen savers list
     * @param saver
     */
    public ArrayList getNewScreenSaverConfig(AbstractScreenSaver saver)
    {
        saver.setMaxClocksPerScreen(maxClocksPerWindowSpinner.getValue());
        saver.setClockRotationPeriod((rotateClockSpinner.getValue() * 60000));
        saver.setScreenSaverRotationPeriod((rotateScreenSaverSpinner.getValue() * 60000));
        saver.setLimitScreenToGroup(limitToGroupCheckBox.isSelected());
        saver.setFrameAroundClocks(frameClockCheckBox.isSelected());
        saver.setBoxAroundCaption(frameTimeDateCheckBox.isSelected());
        if (limitToGroupCheckBox.isSelected())
        {
            AbstractScreenSaver.setCurrentClockGroupName((String)groupComboBox.getSelectedItem());
        }
        //store off all the selected screen savers
        ArrayList newScreenSaverList = new ArrayList();
        for (int i = 0; i < selectedSSListModel.size(); i++)
        {
            newScreenSaverList.add(selectedSSListModel.get(i));
        }
        return(newScreenSaverList);
    }
    
    /**
     * 	center this on the screen
     */
    public Dimension setAbsoluteLocation()
    {
        Dimension ss = getToolkit().getScreenSize();
        Dimension ds = getPreferredSize();
        setLocation((ss.width  - ds.width) / 2,(ss.height - ds.height) / 2);
        return(ds);
    }
	
    /**
	 * @return Returns the okButton.
	 */
	public boolean getOkButtonState() 
	{
		return okButtonState;
	}
	
    /**
     * update the selection buttons based on the state of the list boxes
     *
     */
    private void updateSelectButtons()
    {
        //availableSSListModel.g
        //selectedSSListModel
        
        addAll.setEnabled(availableSSListModel.size() > 0);
        removeAll.setEnabled(selectedSSListModel.size() > 0);
        
        int selectedIndex = availableScreenSavers.getSelectedIndex();
        if (selectedIndex != -1)
        {
            String availableSelection = (String)availableSSListModel.get(selectedIndex);
            addCurrent.setEnabled(findInListModel(availableSelection, availableSSListModel) != -1);
        }
        else
            addCurrent.setEnabled(false);
        
        selectedIndex = selectedScreenSavers.getSelectedIndex();
        if (selectedIndex != -1)
        {
            String selection = (String)selectedSSListModel.get(selectedIndex);
            int position = findInListModel(selection, selectedSSListModel);
            removeCurrent.setEnabled(position != -1);

            moveUp.setEnabled(position != 0);
            moveDown.setEnabled(position != (selectedSSListModel.size()-1));
        }
        else
            {
            removeCurrent.setEnabled(false);
            moveUp.setEnabled(false);
            moveDown.setEnabled(false);
            }
    }

    /**
     * test for string in list model
     * @param test
     * @param model
     * @return
     */
    private int findInListModel(String test, DefaultListModel model)
    {
        for (int i = 0; i < model.size(); i++)
        {
            if(model.get(i).equals(test))
                return(i);
        }    
        return(-1);
    }
    
///////////////////////////// ACTIONS PERFORMED /////////////////////////////////////////
    
    protected void do_okButton_actionPerformed() 
	{
	    okButtonState = true;
		this.setVisible(false);
	}
	
	protected void do_cancelButton_actionPerformed() 
	{
		okButtonState = false;
		this.setVisible(false);
	}
    
    protected void do_addCurrent_actionPerformed() 
    {
        //take the current selection and move it to the other side
        int selectedIndex = availableScreenSavers.getSelectedIndex();
        selectedSSListModel.addElement(availableSSListModel.get(selectedIndex));
        availableSSListModel.remove(selectedIndex);
        availableScreenSavers.setSelectedIndex(-1);
        updateSelectButtons();
    }
    
    protected void do_addAll_actionPerformed() 
    {
        int size = availableSSListModel.size();
        for (int i= 0; i < size; i++)
        {
            selectedSSListModel.addElement(availableSSListModel.get(i));
        }
        for (int i= 0; i < size; i++)
        {
            availableSSListModel.remove(0);
        }
        updateSelectButtons();
    }
    
    protected void do_removeCurrent_actionPerformed() 
    {
        int selectedIndex = selectedScreenSavers.getSelectedIndex();
        availableSSListModel.addElement(selectedSSListModel.get(selectedIndex));
        selectedSSListModel.remove(selectedIndex);
        selectedScreenSavers.setSelectedIndex(-1);
        updateSelectButtons();
    }

    protected void do_removeAll_actionPerformed() 
    {
        int size  = selectedSSListModel.size();
        for (int i= 0; i < size;  i++)
        {
            availableSSListModel.addElement(selectedSSListModel.get(i));
        }
        for (int i= 0; i < size; i++)
        {
            selectedSSListModel.remove(0);
        }
        updateSelectButtons();
    }
    
    protected void do_moveUp_actionPerformed() 
    {
        int selectedIndex = selectedScreenSavers.getSelectedIndex();
        String selection = (String)selectedSSListModel.remove(selectedIndex);
        selectedSSListModel.add((selectedIndex - 1), selection);
        selectedScreenSavers.setSelectedIndex(selectedIndex - 1);
    }
    
    protected void do_moveDown_actionPerformed() 
    {
        int selectedIndex = selectedScreenSavers.getSelectedIndex();
        String selection = (String)selectedSSListModel.remove(selectedIndex);
        selectedSSListModel.add((selectedIndex + 1), selection);
        selectedScreenSavers.setSelectedIndex(selectedIndex + 1);
   }
    
    protected void do_limitToGroupCheckBox_actionPerformed() 
    {
        groupComboBox.setEnabled(limitToGroupCheckBox.isSelected());
    }
    
    protected void do_selectedScreenSavers_hierarchyChanged() 
    {
        if (!startingUp)
            updateSelectButtons();
    }
   
    protected void do_availableScreenSavers_valueChanged() 
    {
        if (!startingUp)
            updateSelectButtons();
    }
    
    protected void do_selectedScreenSavers_valueChanged() 
    {
        if (!startingUp)
            updateSelectButtons();
    }

}






