/* Copyright 2009-2010 Tracy Flynn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.olioinfo.eeproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Bean to track where particular settings were loaded from
 *
 * @author Tracy Flynn
 * @since Aug 1, 2010
 */
public class EEPropertiesLoadDefinition {

    /**
     * Registered list of entries
     */
    private static ArrayList<EEPropertiesLoadDefinition> REGISTERED_ENTRIES = new  ArrayList<EEPropertiesLoadDefinition>();
    
    /**
     * List of environment names
     */
    private ArrayList<String> environmentNames = new ArrayList<String>();
    
    /**
     * Context class
     */
    private Class klass = null;
    
    /**
     * Properties
     */
    private Properties properties = new Properties();
    
    /**
     * Options
     */
    private HashMap<String,String> options = new HashMap<String,String>();
    
    /**
     * Create a new instance of a loader definition
     */
    public EEPropertiesLoadDefinition() {

    }

    /**
     * <p>Create and register a load definition</p> 
     * 
     * <p>Uses the same parameters as EEProperties.loadAndMergeConfigurations.</p>
     * 
     * @param environmentNames
     * @param klass
     * @param properties
     * @param options
     */
    public static void createAndRegisterLoadDefinition(ArrayList<String> environmentNames,Class klass, Properties properties,HashMap<String,String> options) {

    	EEPropertiesLoadDefinition loadDefinition = new EEPropertiesLoadDefinition();
    	
    	// Have to duplicate everything

    	ArrayList<String> duplicatedEnvironmentNames = new ArrayList<String>();
    	for (String environmentString : environmentNames) {
    		duplicatedEnvironmentNames.add(new String(environmentString));
    	}
    	loadDefinition.setEnvironmentNames(duplicatedEnvironmentNames);
    	loadDefinition.setClassContext(klass);
    	
    	Properties duplicatedProperties = new Properties();
    	for (Enumeration<?> e = properties.propertyNames() ; e.hasMoreElements();) {
    		String elementName = (String) e.nextElement();
    		if (elementName != null) {
    			if (properties.getProperty(elementName) != null) {
    				String elementValue = (String) properties.getProperty(elementName);
    				duplicatedProperties.put(new String(elementName), new String(elementValue));
    			}
    		}
    	 }
    	loadDefinition.setProperties(duplicatedProperties);     	
    	
    	HashMap<String,String> duplicatedOptions = new HashMap<String,String>();
    	for (String key : options.keySet() ) {
    		String value = options.get(key);
    		if (value != null) {
    			duplicatedOptions.put(new String(key), new String(value));
    		}
    	}
    	loadDefinition.setOptions(duplicatedOptions);
    	
    	EEPropertiesLoadDefinition.registerEntry(loadDefinition);
    }

    public void setEnvironmentNames(ArrayList<String> environmentNames) {
    	this.environmentNames = environmentNames;
    }
    
    public ArrayList<String> getEnvironmentNames() {
    	return this.environmentNames;
    	
    }

    public void setClassContext(Class klass) {
    	this.klass = klass;
    }
    
    public Class getClassContext() {
    	return this.klass;
    }
    
    public void setProperties(Properties properties) {
    	this.properties = properties;
    }
    
    public Properties getProperties() {
    	return this.properties;
    }
    
    public void setOptions(HashMap<String,String> options) {
    	this.options = options;
    }
    
    public HashMap<String,String> getOptions() {
    	return this.options;
    }
    
    /**
     * Register a definition
     *
     * @param definition Definition to register
     */
    public static void registerEntry(EEPropertiesLoadDefinition definition) {
        EEPropertiesLoadDefinition.REGISTERED_ENTRIES.add(definition);
    }

    /**
     * Get the list of registered definitions
     *
     * @return List of registered definitions
     */
    public static ArrayList<EEPropertiesLoadDefinition> getRegisteredDefinitions() {
        return EEPropertiesLoadDefinition.REGISTERED_ENTRIES;
    }

    /**
     * Reset existing definitions
     */
    public void resetRegisteredDefinitions() {
        EEPropertiesLoadDefinition.REGISTERED_ENTRIES = new  ArrayList<EEPropertiesLoadDefinition>();
    }

    /**
     * Reset existing definitions
     */
    public static void sResetRegisteredDefinitions() {
        EEPropertiesLoadDefinition.REGISTERED_ENTRIES = new  ArrayList<EEPropertiesLoadDefinition>();
    }

}