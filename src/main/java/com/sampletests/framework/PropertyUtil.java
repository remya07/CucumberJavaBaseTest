package com.sampletests.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * Created by remyagoldie 04/04/19 .
 */
public class PropertyUtil 
{
    private Properties props = null;
    private String woringDirectory = System.getProperty("user.dir");
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);
    private String fileName = "uat";
    
    public PropertyUtil() 
    {
    }

    public Properties getPropertiesForEnvironment(String envName)
    {
    	if(props == null)
    	{
    		loadPropertiesForEnvironent(envName);
    	}	
    	
    	return props;
    }
    
    public void loadPropertiesForEnvironent(String env) 
    {
        props = new Properties();
        try 
        {
        	if(env != null)
        	{
        		fileName = env;
        	}	
        	File propFile = new File(woringDirectory+"/src/test/resources/" + fileName + ".properties");
            FileInputStream fis = new FileInputStream(propFile);
            props.load(fis);
        } 
        catch (Exception e) 
        {
            LOGGER.info("Failed to read properties file for env: " + fileName);
        }
    }

    /*


    // get property value by name
    public String propertyValue(String key)
    {
        return  getProperties().getProperty(key);
    }
    */





}
