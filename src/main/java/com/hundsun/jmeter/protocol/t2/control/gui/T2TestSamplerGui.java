/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hundsun.jmeter.protocol.t2.control.gui;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hundsun.jmeter.protocol.t2.sampler.T2Sampler;

public class T2TestSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(T2Sampler.class);

    private ArgumentsPanel argsPanel;
    
    private JTextField functionNo;
    
    private JTextField systemNo;

    private JTextField parentName;
    
    private JCheckBox saveResponseData;
    
    public T2TestSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
    	super.configure(element);
        argsPanel.configure(((T2Sampler)element).getArguments());
        functionNo.setText(element.getPropertyAsString(T2Sampler.FUNCTION_NO));
        systemNo.setText(element.getPropertyAsString(T2Sampler.SYSTEM_NO));
        parentName.setText(element.getPropertyAsString(T2Sampler.PARENTNAME));
        saveResponseData.setSelected(element.getPropertyAsBoolean(T2Sampler.SAVE_RESPONSE, false));
    }

    @Override
    public TestElement createTestElement() {
    	T2Sampler sampler = new T2Sampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        sampler.setProperty(new TestElementProperty(T2Sampler.ARGUMENTS, argsPanel.createTestElement()));
        sampler.setProperty(T2Sampler.FUNCTION_NO, functionNo.getText());
        sampler.setProperty(T2Sampler.SYSTEM_NO, systemNo.getText());
        sampler.setProperty(T2Sampler.PARENTNAME, parentName.getText());
        sampler.setProperty(T2Sampler.SAVE_RESPONSE, saveResponseData.isSelected());
        super.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
    	super.clearGui();
        argsPanel.clearGui();
        functionNo.setText("");
        systemNo.setText("");
        parentName.setText("");
        saveResponseData.setSelected(false);
    }

	@Override
	public String getLabelResource() {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public String getStaticLabel() {
        return "T2 Sampler"; // $NON-NLS-1$
    }

    private JPanel CreateArgsPanel() {
    	argsPanel = new ArgumentsPanel("业务参数"); // $NON-NLS-1$
    	return argsPanel;
    }

    private JPanel createOptionsPanel(){
        JPanel optionsPanel = new HorizontalPanel(15, Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel("function_no:"); // $NON-NLS-1$
        functionNo = new JTextField();
        label.setLabelFor(functionNo);
        JPanel funcNopanel = new HorizontalPanel();
        funcNopanel.add(label, BorderLayout.WEST);
        funcNopanel.add(functionNo, BorderLayout.CENTER);
        //funcNopanel.setPreferredSize(new Dimension(100, 0));
        optionsPanel.add(funcNopanel);

        label = new JLabel("system_no:"); // $NON-NLS-1$
        systemNo = new JTextField();
        label.setLabelFor(systemNo);
        JPanel systemNopanel = new HorizontalPanel();
        systemNopanel.add(label);
        systemNopanel.add(systemNo);
        //systemNopanel.setPreferredSize(new Dimension(200, 0));
        optionsPanel.add(systemNopanel);

        label = new JLabel("parent_name:"); // $NON-NLS-1$
        parentName = new JTextField();
        label.setLabelFor(parentName);
        JPanel parentNamepanel = new HorizontalPanel();
        parentNamepanel.add(label);
        parentNamepanel.add(parentName);
        //parentNamepanel.setPreferredSize(new Dimension(100, 0));
        optionsPanel.add(parentNamepanel);

        saveResponseData = new JCheckBox("保存应答？"); //$NON-NLS-1$
        optionsPanel.add(saveResponseData);

        return optionsPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    	setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        // MAIN PANEL
        VerticalPanel ArgsPanel = new VerticalPanel();
        ArgsPanel.setPreferredSize(new Dimension(0,500));
        ArgsPanel.setBorder(BorderFactory.createTitledBorder(
        		BorderFactory.createEtchedBorder(),
                "接口设置")); // $NON-NLS-1$
        
        ArgsPanel.add(createOptionsPanel());
        ArgsPanel.add(CreateArgsPanel(), BorderLayout.CENTER);
        mainPanel.add(ArgsPanel);
        
        add(mainPanel, BorderLayout.CENTER);        
    }
}
