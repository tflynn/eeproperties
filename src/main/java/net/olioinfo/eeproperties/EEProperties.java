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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;


/**
 * <p>EEProperties is the main class for the EEProperties package. The package provides a simple, consistent
 * approach to providing multiple environments and to per-component configuration while allowing configuration files to be managed or overridden externally to the
 * component or application.</p>
 *
 * <p>The simplest usage is:</p>
 * <ul>
 *   <li>EEProperties.loadAll(&lt;path list&gt;); - to load all the configuration files from the list of paths specified</li>
 *     <ul><li>Path list is an ArrayList&lt;String&gt; object</li></ul>
 *   <li>EEProperties.get("com.mysite.some.property.name");</li>
 * </ul>
 *
 * <p>For configurations in a servlet container, the easiest alternative is the following:</p>
 * <ul>
 *   <li>Create an external configuration tree and bootstrap configuration file</lI>
 *   <li>Set the additional load paths in the boostrap configuration to point to the external configuration tree</li>
 *   <li>Place all the additional configuration information - usually just production settings - in the configuration tree</li>
 *   <li>See EEPropertiesConfiguration documentation for further information on the boostrap configuration file</li>
 * </ul>
 *
 * <p>The model supports standard Java properties file syntax. Some of the standard Java Properties class methods
 * may be accessed via EEProperties.getInstance(). E.g. EEProperties.getInstance().getProperties(setting,default). </p>
 *
 * <p>EEProperties provides a mechanism for loading multiple set of Java properties files based on certain conventions.<p>
 * <ul>
 *   <li>Managed files have names of the form [environment]-ee.properties</li>
 *   <li>Environment represents one of the strings 'defaults','production','development','test'</li>
 *   <li>In any set of files, the 'defaults' file is always guaranteed to be loaded before any other</li>
 *   <li>Then the environmet-specific files are loaded, usually based on the setting in the bootstrap configuration file
 *       or 'development' by default</li>
 *   <li>Later values of particular properties override earlier values</li>
 *   <li>Properties loaded through this mechanism all share the same root (namespace)</li>
 *   <li>A bootstrap configuration is loaded first to get things off the ground (this can be overridden).
 *       See EEPropertiesConfiguration documentation for further information.</li>
 * </ul>
 *
 * <p>Note particularly:</p>
 * <ul>
 *  <li>Since all properties share the same root, the names of properties themselves must be distinct</li>
 * </ul>
 *
 * <p>The following JVM options are available for use in debugging and isolating problems during initialization.
 * They should not be used in other cases or in production, since they cause performance degradation and
 * may generate a lot of output. These options apply to the whole package.</p>
 *
 * <ul><li>-Dnet.olioinfo.eeproperties.consoleTracing</li></ul>
 *
 * <p>Provide detailed tracing to the System.out device. Does not use logging. </p>
 *
 * @author Tracy Flynn
 * @version 2.0
 * @since 2.0
 */
public class EEProperties {

    /**
     * Singleton Logger instance
     */
    private static final Logger logger = LoggerFactory.getLogger(EEProperties.class);


    /**
     * EEProperties bootstrap log4j settings
     */
    private static final String EEPROPERTIES_LOGJ4_BOOTSTRAP_PROPERTIES = "eeproperties_log4j_bootstrap.properties";

    /**
     * Singleton instance of EEProperites
     */
    private static EEProperties instance = new EEProperties();


    /**
     * Console tracing state
    */
    private boolean consoleTracing = false;

    
    /**
     * Get the singleton instance
     *
     * @return Singleton EEProperties instance
     */
    public static EEProperties singleton() {
        return EEProperties.instance;
    }

    /**
     * Load the configuration files associated with the (root of the) specified package only.
     *
     * <p>For instance, for the EEProperties class  / package itself, the configuration would be loaded by specifying:</p>
     * <pre>
     * EEproperties.SLoadPackageConfiguration(EEProperties.class)
     * </pre>
     *
     * @param klass Load the configuration associated with the specified class
     * @throws Exception if any errors occur during load
     */
    public static synchronized void sLoadPackageConfiguration(Class klass) throws Exception {
            

    }

    /**
     * Construct an instance of EEProperties
     */
    public EEProperties() {
        this(null);
    }

    /**
     * Construct an instance of EEProperties
     *
     * @param options Hash of options
     */
    public EEProperties(HashMap<String,String> options) {
        initializeConsoleTracing(options);
        initializeLogging(options);
    }

    /**
     * Initialize console tracing
     *
     * @param options Hash of options
     */
    private void initializeConsoleTracing(HashMap<String,String> options) {
        if (testSystemProperty("net.olioinfo.eeproperties.consoleTracing","true")) {
            this.consoleTracing = true;
        }
        if (testOption(options,"net.olioinfo.eeproperties.consoleTracing","true")) {
            this.consoleTracing = true;
        }
    }


    /**
     * Log a console tracing message (if console tracing is enabled)
     *
     * @param msg Message to trace
     */
    private void consoleTrace(String msg) {
        if (this.consoleTracing) System.out.println("consoleTrace: " + msg);
    }

    /**
     * Initialize bootstrap logging
     *
     * @param options Options hash
     */
    private void initializeLogging(HashMap<String,String> options) {
        consoleTrace("EEproperties.initializeLogging: Entering...");
        //eeproperties_log4j_bootstrap.properties
        Properties log4jBootstrapProperties = new Properties();
        try {
            URL url = EEProperties.class.getResource(EEProperties.EEPROPERTIES_LOGJ4_BOOTSTRAP_PROPERTIES);
            if (url != null) {
                InputStream is = url.openStream();
                log4jBootstrapProperties.load(is);
                is.close();
                org.apache.log4j.PropertyConfigurator.configure(log4jBootstrapProperties);
                consoleTrace(String.format("EEproperties.initializeLogging: Bootstrap logging successfully configured using log4j settings %s",EEProperties.EEPROPERTIES_LOGJ4_BOOTSTRAP_PROPERTIES));
                logger.debug("Bootstrap logging successfully initialized");
            }
        }
        catch (Exception ex) {
            consoleTrace(String.format("EEproperties.initializeLogging: exception %s",ex.toString()));
            if (this.consoleTracing) ex.printStackTrace(System.out);
        }
    }

    /**
     * Test a System property
     *
     * @param propertyName Property name
     * @param propertyValue Value to check against
     * @return boolean true if value matches, false otherwise
     */
    private boolean testSystemProperty(String propertyName, String propertyValue) {
        return (System.getProperty(propertyName) != null) && (System.getProperty(propertyName).equals(propertyValue));
    }

    /**
     * Test an option value
     *
     * @param options Hash of options
     * @param optionName Option Name
     * @param optionValue Value to check against
     * @return boolean true if value matches, false otherwise
     */
    private boolean testOption(HashMap<String,String> options , String optionName,String optionValue) {
        return ((options != null) && options.get(optionName) != null) && (options.get(optionName).equals(optionValue));
    }
}