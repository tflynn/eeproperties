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
 * <p>The EEProperties package provides a simple, consistent
 * approach to providing multiple environments and per-component configuration while allowing configuration files to be
 * managed or overridden externally to the component or application.</p>
 *
 * <h3>Basic usage</h3>
 * <pre>
 * EEProperties.sLoadPackageConfiguration(some.class);
 * </pre>
 *
 * <p>This will load the files "defaults-ee.properties' and 'development-ee.properties' if present in that order from the same
 * location as the class 'some.class' loaded. The merged properties will then be accessible using a call similar to the following:</p>
 * <pre>
 * EEProperties.get("com.mysite.some.property.name");
 * </pre>
 *
 * <p>The default names for the configuration files are:</p>
 * <ul>
 * <li>defaults-ee.properties</ii>
 * <li>production-ee.properties</li>
 * <li>development-ee.properties</li>
 * <li>test-ee.properties</li>
 * </ul>
 *
 * The 'defaults-ee.properties' is always loaded first if available. Then one of the other files depending on the
 * setting for the runtime environment. The default environment 'development' is set in the (internal) bootstrap file. See the discussion
 * about the bootstrap file below for the recommended way to override the runtime environment settings.</p>
 *  
 * <h3>External configuration</h3>
 *
 * <p>To use the external configuration option (the real reason for the existence of EEProperties),
 * create a directory structure with a sub-directory structure that mirrors the package hierarchy for the
 * package(s) whose  configurations are to be managed.</p>
 *
 *
 * <p>For example, the directory structure used to test the net.olioinfo.eeproperties package looks like:</p>
 * <pre>
 *
 * Top level directory
 *
 * ~/test-eeproperties-configurations
 *
 * Directory tree
 *
 * ~/test-eeproperties-configurations/net
 * ~/test-eeproperties-configurations/net/olioinfo/eeproperties
 * ~/test-eeproperties-configurations/net/olioinfo/eeproperties/eeproperties-bootstrap.properties
 * ~/test-eeproperties-configurations/net/olioinfo/eeproperties/tdevelopment-ee.properties
 * ~/test-eeproperties-configurations/net/olioinfo/eeproperties/production-ee.properties
 * </pre>
 *
 * <p>Then, tell EEProperties to bootstrap using the specified 'eeproperties-bootstrap.properties' file.
 * Specify the JVM option as below:</p>
 * <pre>
 * -Dnet.olioinfo.eeproperties.bootstrap.fileName=/Users/johndoe/test-eeproperties-configurations/net/olioinfo/eeproperties/eeproperties-bootstrap.properties
 * </pre>
 *
 * <p>Finally, in the bootstrap file itself ("eeproperties-bootstrap.properties"), tell EEProperties where to find
 * all of the other configuration files. This will usually be the same directory tree containing the bootstrap
 * file, but need not be.</p>
 *
 * <pre>
 *
 * Sample "eeproperties-bootstrap.properties" contents
 *
 * net.olioinfo.eeproperties.runtime.environment = development
 * net.olioinfo.eeproperties.runtime.additionalConfigurationPaths = /Users/johndoe/test-eeproperties-configurations
 * </pre>
 *
 *
 * <h3>Servlet configuration</h3>
 *
 * <p>Servlet configuration is no different from standard usage. The loading relative to classes in a JAR functions
 * as expected, and configuration settings can be overridden as explained above, by using the appropriate JVM
 * option when starting the servlet container. For example, for Tomcat:</p>
 *
 * <pre>
 * export CATALINA_OPTS="-Dnet.olioinfo.eeproperties.bootstrap.fileName=/Users/johndoe/test-eeproperties-configurations/net/olioinfo/eeproperties/eeproperties-bootstrap.properties ${CATALINA_OPTS}"
 * </pre>
 *
 * <h3>Caution</h3>
 * <p>All properties are loaded in the same context, so the names of the properties themselves must be distinct.</p>
 *
 * <h3>Additional information</h3>
 *
 * <p>The load process implied by a phrase similar to "EEProperties.sLoadPackageConfiguration(some.class)" is
 * thread-safe and can be invoked as many times as needed to reload settings for one or more packages.</p>
 *
 * <p>The following JVM options are available for use in debugging and isolating problems during initialization.
 * They should not be used in other cases or in production, since they cause performance degradation and
 * may generate a lot of output. These options apply to the whole package.</p>
 *
 * <p>To provide detailed tracing to the System.out device, specify the following: (Does not use logging) </p>
 *
 * <ul><li>-Dnet.olioinfo.eeproperties.consoleTracing=true</li></ul>
 *
 * <p>To provide detailed logging during the bootstrap phase specify the following:</p>
 *
 * <ul><li>-Dnet.olioinfo.eeproperties.bootstrapLogging=true</li></ul>
 *
 * <p>To override the default bootstrap logging settings by providing a log4j configuration file at the specified
 * location, specify the following:</p>
 *
 * <ul><li>-Dnet.olioinfo.eeproperties.bootstrapLogging.configurationFile=[fully qualified file name]</li></ul>
 *

 * <h3>Exception handling</h3>
 * <p>No methods throw exceptions. Instead, exceptions will be logged. So, if something doesn't appear to be working
 * correctly, enable some of the logging options listed above to isolate the problem.</p>
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
     * @param klass
     * @param properties
     * @param options
     */
    public void loadAndMergeConfigurations(Class klass, Properties properties,HashMap<String,String> options) {


        ArrayList<String> names = new ArrayList<String>();
        names.add("defaults");
        names.add(this.runtimeEnvironment);

        loadAndMergeConfigurations(names,klass,properties,options);

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