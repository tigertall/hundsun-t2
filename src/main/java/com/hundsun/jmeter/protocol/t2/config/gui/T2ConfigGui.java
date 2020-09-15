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

package com.hundsun.jmeter.protocol.t2.config.gui;

import java.awt.*;

import javax.swing.*;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FilePanelEntry;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hundsun.jmeter.protocol.t2.sampler.T2Sampler;

public class T2ConfigGui extends AbstractConfigGui {

    private static final long serialVersionUID = 1L;
    
    private final FilePanelEntry configFile = new FilePanelEntry("t2sdk-config:", ".xml"); // $NON-NLS-1$

    private JCheckBox keepServer = new JCheckBox("保持T2连接？"); //$NON-NLS-1$;

    private boolean displayName;

    private static final Logger log = LoggerFactory.getLogger(T2Sampler.class);

    public T2ConfigGui() {
        this(true);
    }

    public T2ConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

	@Override
	public String getLabelResource() {
		// TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public String getStaticLabel() {
        return "T2设置"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
    	super.configure(element); // TODO - should this be done for embedded usage?
        // Note: the element is a ConfigTestElement when used standalone, so we cannot use FTPSampler access methods
        configFile.setFilename(element.getPropertyAsString(T2Sampler.CONFIG_FILE));
        keepServer.setSelected(element.getPropertyAsBoolean(T2Sampler.KEEPSERVER, false));
    }

    @Override
    public TestElement createTestElement() {
    	ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
    	configureTestElement(element);
        // Note: the element is a ConfigTestElement, so cannot use FTPSampler access methods
        element.setProperty(T2Sampler.CONFIG_FILE, configFile.getFilename());
        element.setProperty(T2Sampler.KEEPSERVER, keepServer.isSelected());
        T2Sampler.setConfigFile(configFile.getFilename());
        T2Sampler.setKeepServer(keepServer.isSelected());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        configFile.clearGui(); //$NON-NLS-1$
        keepServer.setSelected(false);
    }
    
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    	setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();
        
        JPanel serverPanel = new HorizontalPanel(20, Component.CENTER_ALIGNMENT);
        serverPanel.setBorder(BorderFactory.createTitledBorder(
        		BorderFactory.createEtchedBorder(),
                "连接设置")); // $NON-NLS-1$

        //configFile.setPreferredSize(new Dimension(500, 0));

        serverPanel.add(configFile);
        serverPanel.add(keepServer);

        mainPanel.add(serverPanel);
        
        add(mainPanel, BorderLayout.CENTER);      
    }
}
