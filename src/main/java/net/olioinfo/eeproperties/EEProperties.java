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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
    private static final String CORE_CONFIGURATION_FILE_NAME_FQ = "eeproperties-bootstrap.properties";


    /**
     * Singleton instance of EEProperites
     */
    private static EEProperties instance = new EEProperties();


    /**
     * EEPropertiesAvailableLogger instance
     */
    private EEPropertiesAvailableLogger logger = new EEPropertiesAvailableLogger();


    /**
     * Properties object that hold all properties
     */
    private Properties coreProperties = new Properties();

    /*
     * Default environment if not specified is 'development'
     */
    private String runtimeEnvironment = "development";
    

    /*
     * Paths to search for external configuration files
     */
    private ArrayList<String> searchPathsList = new ArrayList<String>();

    
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
     * Get the singleton instance
     *
     * @return Singleton EEProperties instance
     */
    public static EEProperties singleton() {
        return EEProperties.instance;
    }

    /**
     * Load the configuration files associated with the (root of the) specified package only (for the singleton class).
     *
     * <p>For instance, for the EEProperties class  / package itself, the configuration would be loaded by specifying:</p>
     * <pre>
     * EEproperties.sLoadPackageConfiguration(EEProperties.class)
     * </pre>
     * 
     * Also see the documentation for loadPackageConfiguration
     *
     * @param klass Load the configuration associated with the specified class
     */
    public static synchronized void sLoadPackageConfiguration(Class klass) {
       EEProperties.singleton().loadPackageConfiguration(klass,null);
    }

    /**
     * Load the configuration files associated with the (root of the) specified package only (for the singleton class).
     *
     * See the documentation for loadPackageConfiguration
     *
     * @param klass Load the configuration associated with the specified class
     * @param options Hash of options
     */
    public static synchronized void sLoadPackageConfiguration(Class klass,HashMap<String,String> options) {
       EEProperties.singleton().loadPackageConfiguration(klass,options);
    }

    /**
     * Load the configuration files associated with the (root of the) specified package only.
     *
     *
     * @param klass Load the configuration associated with the specified class
     * @param options Hash of options
     */
    public synchronized void loadPackageConfiguration(Class klass,HashMap<String,String> options) {

        ArrayList<String> names = new ArrayList<String>();
        names.add("defaults");
        names.add(this.runtimeEnvironment);
        HashMap<String,String> combinedOptions = new HashMap<String,String>();
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.prefix",null);
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.suffix","-ee");
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.extension","properties");
        if (options != null) combinedOptions.putAll(options);

        loadAndMergeConfigurations(names,klass,this.coreProperties,combinedOptions);
    }


    /**
     * Load and merge configurations based on environment names, class location and other options
     *
     * <p>Environment names are a list of environment names, usually from the list [defaults,production,development,test[.
     * File names will be constructed, and the files searched for and loaded if present in the order specified.</p>
     *
     * </p>The klass parameter specifies the klass used as the locator for the file within the search paths already configured.</p>
     *
     * <p>The properties object contains any configuration options already loaded.</p>
     *
     * <p>Options are optional and can specify rules for forming the name of the properties file. Options are:</p>
     * <ul>
     * <li>net.olioinfo.eeproperties.configurationFile.prefix</li>
     * <li>net.olioinfo.eeproperties.configurationFile.suffix</li>
     * <li>net.olioinfo.eeproperties.configurationFile.extension</li>
     * </ul>
     *
     * <p>The format of the file name for the properties flle is '[prefix][environment][suffix].[extension]'.
     * If any element is absent or null it is skipped when constructing the name.</p>
     *
     *
     * @param environmentNames
     * @param klass
     * @param properties
     * @param options
     */
    public void loadAndMergeConfigurations(ArrayList<String> environmentNames,Class klass, Properties properties,HashMap<String,String> options) {

        //Check for addtional search paths in the options
        if (options != null && options.containsKey("net.olioinfo.eeproperties.runtime.additionalConfigurationPaths")) {
            this.searchPathsList.addAll(parseSearchPaths(options.get("net.olioinfo.eeproperties.runtime.additionalConfigurationPaths")));
        }

        

        String prefix = options.containsKey("net.olioinfo.eeproperties.configurationFile.prefix") ? options.get("net.olioinfo.eeproperties.configurationFile.prefix") : null;
        String suffix = options.containsKey("net.olioinfo.eeproperties.configurationFile.suffix") ? options.get("net.olioinfo.eeproperties.configurationFile.suffix") : null;
        String extension = options.containsKey("net.olioinfo.eeproperties.configurationFile.extension") ? options.get("net.olioinfo.eeproperties.configurationFile.extension") : null;

        for (String environmentName : environmentNames ) {
            StringBuffer environmentFileNameBuf = new StringBuffer();
            if (prefix != null ) environmentFileNameBuf.append(prefix);
            if (environmentName != null ) environmentFileNameBuf.append(environmentName);
            if (suffix != null) environmentFileNameBuf.append(suffix);
            if (extension != null) environmentFileNameBuf.append(".").append(extension);
            String environmentFileName = environmentFileNameBuf.toString();
            logger.debug(String.format("EEProperties.loadAndMergeConfigurations checking for file %s.",environmentFileName));
            loadPropertiesFromLocationsOrClass(properties,this.searchPathsList,environmentFileName,klass);
        }
        
    }

    /**
     * Get a property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static String sGetProperty(String propertyName) {
        return EEProperties.singleton().getProperty(propertyName);
    }

    /**
     * Get a property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public String getProperty(String propertyName) {
        return this.coreProperties.getProperty(propertyName);
    }

    /**
     * Get a property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @param defaultValue Default value if property not found
     * @return Property value or null if not found
     */
    public static String sGetProperty(String propertyName, String defaultValue) {
        return EEProperties.singleton().getProperty(propertyName,defaultValue);
    }

    /**
     * Get a property setting
     *
     * @param propertyName Property Name to retrieve
     * @param defaultValue Default value if property not found
     * @return Property value or null if not found
     */
    public String getProperty(String propertyName, String defaultValue) {
        return this.coreProperties.getProperty(propertyName,defaultValue);
    }


    /**
     * Put a property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPut(String propertyName, String propertyValue) {
        EEProperties.singleton().put(propertyName,propertyValue);
    }

    /**
     * Put a property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void put(String propertyName, String propertyValue) {
        this.coreProperties.put(propertyName,propertyValue);
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
        this.logger.setConsoleTracing(consoleTracing);
    }


    /**
     * Initialize bootstrap logging
     *
     * @param options Options hash
     */
    private void initializeLogging(HashMap<String,String> options) {
        this.logger.debug("EEproperties.initializeLogging: Entering...");

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
            if ( (options != null ) && options.containsKey("net.olioinfo.eeproperties.bootstrapLogging.configurationFile") ) {
                bootstrapPropertiesFileName = options.get("net.olioinfo.eeproperties.bootstrapLogging.configurationFile");
            }
        }


        if (bootstrapLogging) {
            Properties log4jBootstrapProperties = new Properties();
            boolean loaded = loadPropertiesFromFileOrClass(log4jBootstrapProperties,bootstrapPropertiesFileName,EEProperties.class);
            if (loaded) {
                org.apache.log4j.PropertyConfigurator.configure(log4jBootstrapProperties);
                this.logger.debug(String.format("EEproperties.initializeLogging: Bootstrap logging successfully configured using log4j settings %s",bootstrapPropertiesFileName));
                this.logger.dumpProperties("trace",log4jBootstrapProperties);
                this.logger.setBootstrapLogging(true);
                this.logger.debug("EEproperties.initializeLogging: Bootstrap logging successfully initialized");
            }
            else {
                this.logger.error("EEproperties.initializeLogging: error initializing bootstrap logging");
            }
        }
    }

    /**
     * Load the EEProperties bootstrap file
     *
     * @param options Hash of options
     */
    private void loadBootstrapFile(HashMap<String,String> options) {

        // Figure out what the name of the bootstrap configuration file is

        String corePropertiesFileName = EEProperties.CORE_CONFIGURATION_FILE_NAME_FQ;

        String systemProperty = System.getProperty("net.olioinfo.eeproperties.bootstrap.fileName");
        if ( systemProperty != null ) {
            corePropertiesFileName = systemProperty;
        }

        if (options != null && options.containsKey("net.olioinfo.eeproperties.bootstrap.fileName")) {
            corePropertiesFileName = options.get("net.olioinfo.eeproperties.bootstrap.fileName");
        }
        
        this.logger.debug(String.format("EEProperties.loadBootstrapFile bootstrap file name %s",corePropertiesFileName));
        
        boolean loaded = loadPropertiesFromFileOrClass(this.coreProperties,corePropertiesFileName,EEProperties.class);
        if (loaded) {
            this.logger.info("EEProperties.loadBootstrapFile bootstrap file loaded successfully. Contents follow.");
            this.logger.dumpProperties("debug",this.coreProperties);
        }
        else {
            this.logger.error("EEProperties.loadBootstrapFile bootstrap file failed to load");
        }


        this.runtimeEnvironment = getPropertyFromOptionsOrSystemOrPropertiesWithDefault(
            "net.olioinfo.eeproperties.runtime.environment",options,this.coreProperties,this.runtimeEnvironment);

        String additionalPathsAsString = getPropertyFromOptionsOrSystemOrPropertiesWithDefault(
            "net.olioinfo.eeproperties.runtime.additionalConfigurationPaths",options,this.coreProperties,null);

        this.searchPathsList.addAll(parseSearchPaths(additionalPathsAsString));
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
     * Get a property if it exists from the following in order:
     *
     * <ul>
     * <li> options hash</li>
     * <li> System property</li>
     * <li> Existing (i.e. loaded already) property</li>
     * </ul>
     *
     * @param propertyName Property Name
     * @param options Options hash
     * @param properties Properties instance
     * @param defaultValue default value
     * @return value
     */
    public String getPropertyFromOptionsOrSystemOrPropertiesWithDefault(
            String propertyName, HashMap<String,String> options, Properties properties, String defaultValue  ) {

        String returnValue = defaultValue;

        if (this.coreProperties.containsKey(propertyName)) {
            returnValue = (String) properties.get(propertyName);
        }

        if (System.getProperty(propertyName) != null) {
            returnValue = System.getProperty(propertyName);
        }

        if (options != null && options.containsKey(propertyName)) {
            returnValue = options.get(propertyName);
        }
        return returnValue;
    }

    /**
     * Load a properties file from a file or relative to a class
     *
     * @param properties Properties file to update.
     * @param fileName File name to load. Ignored if null
     * @param klass Class to load file relative to. Ignored if null
     * @return boolean true if loaded, false otherwise
     */
    private boolean loadPropertiesFromFileOrClass(Properties properties,String fileName, Class klass) {
        boolean returnStatus = false;
        if (properties == null) {
            this.logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass no Properties instance specified"));
        }
        else {
            try {
                InputStream is = null;
                if (fileName == null) {
                    this.logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass no file name specified"));
                }
                else {
                    if (fileName.startsWith("/")) {
                        if ((new File(fileName).exists())) {
                            is = new FileInputStream(fileName);
                        }
                    }
                    else if (klass != null) {
                        URL url = klass.getResource(fileName);
                        if (url != null) {
                            is = url.openStream();
                        }
                    }
                }
                if (is == null ) {
                    this.logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass input stream not created. Check file name and location"));
                }
                else {
                    properties.load(is);
                    is.close();
                    returnStatus = true;
                }
            }
            catch (Exception ex) {
                returnStatus = false;
                this.logger.setBootstrapLogging(false);
                this.logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass: exception %s",ex.toString()),ex);
            }
        }
        return returnStatus;
    }


    /**
     * Load a properties file from a list of locations or relative to a class
     *
     * @param properties Properties file to update.
     * @param locations An array of absolute directory names
     * @param fileName File name to load. Ignored if null
     * @param klass Class to load file relative to. Ignored if null
     * @return boolean true if loaded, false otherwise
     */
    private boolean loadPropertiesFromLocationsOrClass(Properties properties,ArrayList<String> locations, String fileName, Class klass) {

        boolean fileFound = false;
        for (String location : locations ) {
            String fullFileName = String.format("%s/%s/%s",location,klass.getPackage().getName().replaceAll("\\.","/"),fileName);
            if ((new File(fullFileName).exists())) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fullFileName);
                    properties.load(is);
                    is.close();
                    fileFound = true;
                    logger.debug(String.format("EEProperties.loadPropertiesFromLocationsOrClass Loaded class %s from %s",fileName,fullFileName));
                }
                catch (Exception ex) {
                    logger.info(String.format("EEProperties.loadPropertiesFromLocationsOrClass Unable to load file %s",fileName));
                }
            }
            if (fileFound) break;
        }
        if (! fileFound) {
            if (klass != null) {
                InputStream is = null;
                try {
                    URL url = klass.getResource(fileName);
                    if (url != null) {
                        is = url.openStream();
                    }
                    properties.load(is);
                    is.close();
                    fileFound = true;
                    logger.debug(String.format("EEProperties.loadPropertiesFromLocationsOrClass Loaded file %s relative to class %s",fileName,klass.getName()));
                }
                catch (Exception ex) {
                    logger.info(String.format("EEProperties.loadPropertiesFromLocationsOrClass Unable to load file %s relative to class %s",fileName,klass.getName()));
                }
            }
        }
        if (!fileFound) {
            logger.error(String.format("EEProperties.loadPropertiesFromLocationsOrClass Unable to load file %s from anywhere",fileName));
        }
        return fileFound;
    }
    


    /**
     * Parse a list of colon-delimited search paths returning a list
     *
     * @param searchPaths Colon-delimeted list of search paths
     * @return A list of search paths
     */
    private ArrayList<String> parseSearchPaths(String searchPaths) {
        ArrayList<String> searchPathList = new ArrayList<String>();

        if (searchPaths != null && searchPaths.length() > 0 ) {
            String[] searchPathsArray = searchPaths.split(":");
            for (int i = 0 ; i < searchPathsArray.length ; i++ ) {
                searchPathsList.add(searchPathsArray[i]);
                logger.debug(String.format("EEProperties.parseSearchPaths adding search path %s",searchPathsArray[i]));
            }
        }

        return searchPathList;
    }
    
    /**
     * Run standalone for testing purposes
     */
    public static void main(String[] args) {
        EEProperties.singleton();
    }
}