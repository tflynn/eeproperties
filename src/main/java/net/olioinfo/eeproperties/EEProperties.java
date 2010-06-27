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


import java.io.FileInputStream;
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
 * <ul><li>-Dnet.olioinfo.eeproperties.consoleTracing=true</li></ul>
 *
 * <p>Provide detailed tracing to the System.out device. Does not use logging. </p>
 *
 * <ul><li>-Dnet.olioinfo.eeproperties.bootstrapLogging=true</li></ul>
 *
 * <p>Provide detailed logging during bootstrap phase</p>
 * 
 * <ul><li>-Dnet.olioinfo.eeproperties.bootstrapLogging.configurationFile=[fully qualified file name]</li></ul>
 *
 * <p>Override the default bootstrap logging settings by providing a log4j configuration file at the specified location</p>

 * <h3>Exception handling</h3>
 * <p>No methods return exceptions. Instead, exceptions will be logged. So, if something doesn't appear to be working
 * correctly, enable some of the options listed above, and enable general logging to isolate the problem.<p>
 *
 *
 *
 * @author Tracy Flynn
 * @version 2.0
 * @since 2.0
 */
public class EEProperties {

    /**
     * EEProperties bootstrap log4j settings
     */
    private static final String LOGJ4_BOOTSTRAP_PROPERTIES = "eeproperties_log4j_bootstrap.properties";

    /**
     * <p>Fullly qualified name of core properties file</p>
     *
     * <p>This setting may be overridden with the system property
     * net.olioinfo.eeproperties.coreConfigurationFileName</p>
     */
    private static final String CORE_CONFIGURATION_FILE_NAME_FQ = "net/olioinfo/eeproperties/eeproperties-bootstrap.properties";


    /**
     * Singleton instance of EEProperites
     */
    private static EEProperties instance = new EEProperties();


    /**
     * AvailableLogger instance
     */
    private AvailableLogger logger = new AvailableLogger();


    /**
     * Properties object that hold all properties
     */
    private Properties AllProperties = new Properties();
    
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
    public static synchronized void sLoadPackageConfiguration(Class klass) {
            

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
        loadBootstrapFile(options);
    }

    /**
     * Initialize console tracing
     *
     * @param options Hash of options
     */
    private void initializeConsoleTracing(HashMap<String,String> options) {
        boolean consoleTracing = false;

        if (testSystemProperty("net.olioinfo.eeproperties.consoleTracing","true")) {
            consoleTracing = true;
        }
        if (testOption(options,"net.olioinfo.eeproperties.consoleTracing","true")) {
            consoleTracing = true;
        }
        logger.setConsoleTracing(consoleTracing);
    }


    /**
     * Initialize bootstrap logging
     *
     * @param options Options hash
     */
    private void initializeLogging(HashMap<String,String> options) {
        logger.debug("EEproperties.initializeLogging: Entering...");

        boolean bootstrapLogging = false;

        if (testSystemProperty("net.olioinfo.eeproperties.bootstrapLogging","true")) {
            bootstrapLogging = true;
        }
        if (testOption(options,"net.olioinfo.eeproperties.bootstrapLogging","true")) {
            bootstrapLogging = true;
        }

        String bootstrapPropertiesFileName =  EEProperties.LOGJ4_BOOTSTRAP_PROPERTIES;
        if (bootstrapLogging) {
            String overrideBootstrapPropertiesFileName = System.getProperty("net.olioinfo.eeproperties.bootstrapLogging.configurationFile");
            if (overrideBootstrapPropertiesFileName != null) {
                bootstrapPropertiesFileName = overrideBootstrapPropertiesFileName;
            }
            if ( (options != null ) && (options.get("net.olioinfo.eeproperties.bootstrapLogging.configurationFile") != null )) {
                bootstrapPropertiesFileName = options.get("net.olioinfo.eeproperties.bootstrapLogging.configurationFile");
            }
        }


        if (bootstrapLogging) {
            Properties log4jBootstrapProperties = new Properties();
            boolean loaded = loadPropertiesFromFileOrClass(log4jBootstrapProperties,bootstrapPropertiesFileName,EEProperties.class);
            if (loaded) {
                org.apache.log4j.PropertyConfigurator.configure(log4jBootstrapProperties);
                logger.debug(String.format("EEproperties.initializeLogging: Bootstrap logging successfully configured using log4j settings %s",bootstrapPropertiesFileName));
                logger.dumpProperties("trace",log4jBootstrapProperties);
                logger.setBootstrapLogging(true);
                logger.debug("EEproperties.initializeLogging: Bootstrap logging successfully initialized");
            }
            else {
                logger.error("EEproperties.initializeLogging: error initializing bootstrap logging");
            }
        }
    }

    /**
     * Load the EEProperties bootstrap file
     *
     * @param options Hash of options
     */
    private void loadBootstrapFile(HashMap<String,String> options) {
                

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


    /**
     * Load a properties file from a file or class relative
     *
     * @param properties Properties file to update.
     * @param fileName File name to load. Ignored if null
     * @param klass Class to load file relative to. Ignored if null
     * @return boolean true if loaded, false otherwise
     */
    private boolean loadPropertiesFromFileOrClass(Properties properties,String fileName, Class klass) {
        boolean returnStatus = false;
        if (properties == null) {
            logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass no Properties instance specified"));
        }
        else {
            try {
                InputStream is = null;
                if (fileName == null) {
                    logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass no file name specified"));
                }
                else {
                    if (fileName.startsWith("/")) {
                        is = new FileInputStream(fileName);
                    }
                    else if (klass != null) {
                        URL url = klass.getResource(fileName);
                        is = url.openStream();
                    }
                }
                if (is == null ) {
                    logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass input stream not created. Check file name and location"));
                }
                else {
                    properties.load(is);
                    is.close();
                    returnStatus = true;
                }
            }
            catch (Exception ex) {
                returnStatus = false;
                logger.setBootstrapLogging(false);
                logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass: exception %s",ex.toString()),ex);
            }
        }
        return returnStatus;
    }
}