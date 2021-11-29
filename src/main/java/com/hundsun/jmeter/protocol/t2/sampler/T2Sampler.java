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

package com.hundsun.jmeter.protocol.t2.sampler;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hundsun.t2sdk.common.core.context.ContextUtil;
import com.hundsun.t2sdk.common.share.dataset.DatasetService;
import com.hundsun.t2sdk.impl.client.T2Services;
import com.hundsun.t2sdk.interfaces.IClient;
import com.hundsun.t2sdk.interfaces.T2SDKException;
import com.hundsun.t2sdk.interfaces.share.dataset.IDataset;
import com.hundsun.t2sdk.interfaces.share.dataset.IDatasets;
import com.hundsun.t2sdk.interfaces.share.event.EventReturnCode;
import com.hundsun.t2sdk.interfaces.share.event.EventTagdef;
import com.hundsun.t2sdk.interfaces.share.event.EventType;
import com.hundsun.t2sdk.interfaces.share.event.IEvent;

/**
 * A sampler which understands FTP file requests.
 *
 */
public class T2Sampler extends AbstractSampler implements TestStateListener,Interruptible {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(T2Sampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
            		"com.hundsun.jmeter.protocol.t2.config.gui.T2ConfigGui",
                    "com.hundsun.jmeter.protocol.t2.control.gui.T2TestSamplerGui"
            ));
    
    public static final String CONFIG_FILE = "T2Sampler.config_file"; // $NON-NLS-1$
    
    public static final String CONFIG_FILE_DEFAULT = "T2Sampler.config_file_default"; // $NON-NLS-1$

    public static final String KEEPSERVER = "T2Sampler.keepserver"; // $NON-NLS-1$

    public static final String PARENTNAME = "T2Sampler.parentname"; // $NON-NLS-1$
    
    public static final String PARENTNAME_DEFAULT = "T2Sampler.parentname_default"; // $NON-NLS-1$
    
    public static final String FUNCTION_NO = "T2Sampler.function_no"; // $NON-NLS-1$
    
    public static final String SYSTEM_NO = "T2Sampler.system_no"; // $NON-NLS-1$
    
    public static final String ASYNC_SEND = "T2Sampler.async_send"; // $NON-NLS-1$
    
    // Should the file data be saved in the response?
    public static final String SAVE_RESPONSE = "T2Sampler.saveresponse"; // $NON-NLS-1$
    
    public static final String ARGUMENTS = "T2Sampler.arguments"; // $NON-NLS-1$

    private IClient client;
    
    private static T2Services server = T2Services.getInstance();

    private static String t2sdkConfigFile;

    private static boolean keepServer = false;

    // T2Server初始化完成标志
    private static boolean warmed_up = false;

    private Map<String, Object> tagMap = new LinkedHashMap<>();
	
	private Map<Object, Object> dataMap = new LinkedHashMap<>();
    
	private IEvent rsp;

    private ObjectMapper mapper = new ObjectMapper();

    private StringBuilder requestData = new StringBuilder();

    private StringBuilder responeData = new StringBuilder();

    public T2Sampler() {
    }

    public static void setConfigFile(String newConfigFile) {
        // this.setProperty(CONFIG_FILE, newConfigFile);
        if (!newConfigFile.equals(t2sdkConfigFile)) {
            warmed_up = false;
        }

        t2sdkConfigFile = newConfigFile;
    }

    public static String getConfigFile() {
        // return getPropertyAsString(CONFIG_FILE);
        return  t2sdkConfigFile;
    }

    public static void setKeepServer(boolean newKeepServer) {
        if(newKeepServer != keepServer) {
            warmed_up = false;
        }
        keepServer = newKeepServer;
    }

    public String getParentName() {
        return getPropertyAsString(PARENTNAME); // $NON-NLS-1$
    }

    public String getFunctionNo() {
        return getPropertyAsString(FUNCTION_NO); // $NON-NLS-1$
    }

    public int getSystemNo() {
        return getPropertyAsInt(SYSTEM_NO, 0); // $NON-NLS-1$
    }

    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }
    
    public boolean isSaveResponse(){
        return getPropertyAsBoolean(SAVE_RESPONSE,false);
    }
    
    /**
     * Returns a formatted string label describing this sampler Example output:
     * ftp://ftp.nowhere.com/pub/README.txt
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        requestData.setLength(0);
        tagMap.clear();
        tagMap.put(EventTagdef.TAG_FUNCTION_ID, getFunctionNo());
        tagMap.put(EventTagdef.TAG_SYSTEM_NO, getSystemNo());
        tagMap.put(EventTagdef.TAG_EVENT_TYPE, EventTagdef.REQUEST_PACKET);

        try {
            requestData.append("{\"t2_tag\":").append(mapper.writeValueAsString(tagMap));
            requestData.append(",\"t2_data\":").append(mapper.writeValueAsString(dataMap)).append("}");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return requestData.toString();
    }
    
    public synchronized void  testStarted() {
        if (warmed_up) {
            log.info("t2 already warmed up");
            return;
        }

        log.info("init t2, keepserver: " + keepServer);
		server.setT2sdkConfigString(getConfigFile());
		try {
			server.init();
		} catch (T2SDKException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		server.start();

		// 等待创建完成，防止获取连接失败
        // 不需要了，通过 t2sdk配置的 connectionWaitTimes 来控制
        /*
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */

        warmed_up = true;
    }
    
    public void testStarted(String host) {
    	testStarted();
    }

    public synchronized void testEnded() {
        // 如果不保持连接，测试完成时主动释放连接
        if (!keepServer && warmed_up) {
            warmed_up = false;
            server.stop();
        }
    }
    
    public void testEnded(String host) {
    	testEnded();
    }
    
    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSuccessful(false); // Assume failure
        res.setSampleLabel(getName());
        boolean saveResponse = isSaveResponse();

        // 有数据才进行数据填充
        dataMap.clear();
		Arguments args = getArguments();
		if (args.getArgumentCount() > 0)
		{
	        for (int i=0;i<args.getArgumentCount();i++) {
	            Argument arg = args.getArgument(i);
	            dataMap.put(arg.getName(), arg.getPropertyAsString(Argument.VALUE));
	        }
		}

        //log.debug("samp " + getParentName() + ' ' + getThreadName() + ' ' + getFunctionNo() + ' ' +System.identityHashCode(this));
        IEvent event = ContextUtil.getServiceContext().getEventFactory()
                .getEventByAlias(getFunctionNo(), EventType.ET_REQUEST);

        // 设置系统节点号
        event.setIntegerAttributeValue(EventTagdef.TAG_SYSTEM_NO, getSystemNo());

        IDataset dataset = DatasetService.getDefaultInstance().getDataset(dataMap);

        event.putEventData(dataset);

        if (saveResponse) {
            res.setSamplerData(getLabel());
        }

        res.sampleStart();

        try
        {
            // 实例初始化一次client，已经创建过证明T2已经初始化过，不需要再初始化
            if (client == null) {
                log.info("get client, parent_name: " + getParentName() + ' ' + getThreadName());
                client = server.getClient(getParentName());
            }
			rsp = client.sendReceive(event);
        }catch (T2SDKException ex) {
            res.setResponseCode("999");
            res.setResponseMessage(ex.toString());
            log.error(String.format("sampler exception, func: %s, info: %s", getFunctionNo(), ex.getErrorInfo()));
            return res;
        }
			
        res.latencyEnd();
        res.sampleEnd();
        //先判断返回值
        String retErrorNo = rsp.getErrorNo();
        int iReturnCode = rsp.getReturnCode();
        String respCode = "0";

        if (!retErrorNo.equals("0")) {  // 先判断T2错误
            saveResponse = true; // 失败的不管是否选择了记录，都要记录应答，方便查错误
            respCode = retErrorNo;
        } else if(iReturnCode != EventReturnCode.I_OK) { // 再判断业务错误
            saveResponse = true;
            respCode = Integer.toString(iReturnCode);
        } else {
            res.setSuccessful(true);
        }

        res.setResponseCode(respCode);
        res.setResponseMessage(rsp.getErrorInfo());

        if (saveResponse) {
            saveResponse(res, rsp);
        }

        return res;
    }
    
    private void saveResponse(SampleResult res, IEvent rsp) {
    	responeData.setLength(0);
		//获得结果集
		IDatasets result = rsp.getEventDatas();
		//获得结果集总数
		int datasetCount = result.getDatasetCount();
		//遍历所有的结果集
		for (int i = 0; i < datasetCount; i++) {
            responeData.append("\n").append("dataset - ").append(i).append("\n");
			// 开始读取单个结果集的信息
			IDataset ds = result.getDataset(i);
			int columnCount = ds.getColumnCount();
			
			// 遍历单个结果集列信息
			for (int j = 1; j <= columnCount; j++) {
                responeData.append(ds.getColumnName(j));
                responeData.append("|");
                //responeData.append(ds.getColumnType(j));
                //responeData.append(",");
			}
            responeData.append("\n");
			
			// 遍历单个结果集记录，遍历前首先将指针置到开始
			ds.beforeFirst();
			while (ds.hasNext()) {
                responeData.append("\n");
				ds.next();
				for (int j = 1; j <= columnCount; j++) {
                    responeData.append(ds.getString(j));
                    responeData.append("|");
				}
			}
		}
    	res.setResponseData(responeData.toString(), null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean interrupt() {
        log.info("interrupt, release t2");
        server.stop();
        client = null;
        warmed_up = false;
        return true;
    }
    
    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
