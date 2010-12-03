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
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
 * create a directory with a sub-directory structure that mirrors the package hierarchy for the
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
 * ~/test-eeproperties-configurations/net/olioinfo/eeproperties/development-ee.properties
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
 * net.olioinfo.eeproperties.extendedPropertiesSyntax.enabled = true
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
 * <h3>Variable substitution</h3>
 *
 * <p>EEProperties supports variable substitution in both property names and values. Variable substitutions are
 * indicated using the standard syntax ${variable_name}. This value will be replaced at load time by the corresponding
 * value from the system environment (System.getEnv()) and the system properties (System.getProperty()) and any defined
 * property value in that order. First value fourd is used.
 * Thus, system properties always override environment settings alsways override property settings.</p>
 * 
 * <h3>Extended syntax</h3>
 *
 * <p>(Starting in version 2.1) Extended syntax support allows basic object types to be specified in properties files.
 * At load time, EEProperties will attempt to convert the supplied property to an object of the indicated
 * type.  This feature is enabled by default.</p>
 *
 * <p>It may be controlled by the bootstrap setting:</p>
 *
 * <pre>
 * net.olioinfo.eeproperties.extendedPropertiesSyntax.enabled
 * </pre>
 *
 * <p>The syntax in the Properties file prefixes the property value with an object type designator in brackets.
 * The supported object types are:</p>
 * <ul>
 * <li>Integer</li>
 * <li>Short</li>
 * <li>Long</li>
 * <li>Byte</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Boolean</li>
 * <li>Date</li>
 * <li>ArrayList&lt;String&gt;</li>
 * </ul>
 *
 * <p>For the Date type, the following two formats will be parsed automatically.
 * The format strings are those supported in SimpleDateFormat.</p>
 * <ul>
 * <li>"yyyy-MM-dd'T'HH:mm:ssZ"</li>
 * <li>"yyyy-MM-dd"</li>
 * </ul>
 *
 * <p>A set of accessor methods for each type is also available.
 * For the default EEProperties instance, <em>sGet[object type]Property</em> and <em>sPut[object type[</em>.
 * For any EEProperties instance, <em>get[object type]Property</em> and <em>put[object type]</em>. Arguments to all methods are
 * typed. If a particular value didn't convert correctly to the specified type during load , a null will be returned
 * for the requested typed property. The unparsed String value will remain accessible using the standard String-based
 * property calls.</p>
 *
 * <p>Example</p>
 *
 * <pre>
 * In a Properties file
 *
 * net.olioinfo.eeproperties.test.value.10 = [Integer] 123
 *
 * Applicable methods
 *
 * EEProperties.sGetIntegerProperty("net.olioinfo.eeproperties.test.value.10");
 * (new EEProperties()).getIntegerProperty("net.olioinfo.eeproperties.test.value.10");
 * EEProperties.sPutInteger("net.olioinfo.eeproperties.test.value.10",new Integer(123));
 * (new EEProperties()).putInteger("net.olioinfo.eeproperties.test.value.10",new Integer(123));
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
 * <h3>Maven dependency</h3>
 *
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.olioinfo&lt;/groupId&gt;
 *   &lt;artifactId&gt;eeproperties&lt;/artifactId&gt;
 *   &lt;version&gt;2.9&lt;/version&gt;
 * &lt;/dependency&gt;
 *
 * &lt;repository&gt;
 *   &lt;id&gt;net-oilinfo&lt;/id&gt;
 *   &lt;url&gt;http://myowndemo.com:8080/nexus/content/repositories/net-olioinfo&lt;/url&gt;
 * &lt;/repository&gt;
 * </pre>
 *
 * @author Tracy Flynn
 * @version 2.9
 * @since 2.0
 */
@SuppressWarnings("unchecked")
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
     * Property value key for a typed property
     */
    private static final String TYPED_PROPERTY_VALUE = "value";


    /**
     * Property type key for a typed property
     */
    private static final String TYPED_PROPERTY_TYPE = "type";

    
    /**
     * Singleton instance of EEProperites
     */
    private static EEProperties singletonInstance = null;


    /**
     * EEPropertiesAvailableLogger instance
     */
    private EEPropertiesAvailableLogger logger = new EEPropertiesAvailableLogger();


    /**
     * Properties object that holds all conventional i.e. String - properties
     */
    private Properties coreProperties = new Properties();


    /**
     * HashMap object that holds all object-typed properties
     */
    private HashMap<String,HashMap<String,Object>> typedCoreProperties = new HashMap<String,HashMap<String,Object>>();
    
    /*
     * Default environment if not specified is 'development'
     */
    private String runtimeEnvironment = "development";


    /**
     *   Extended properties syntax
     */
    private boolean extendedPropertiesSyntax = true;
    
    /*
     * Paths to search for external configuration files
     */
    private ArrayList<String> searchPathsList = new ArrayList<String>();


    private String uniqueId = null;

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

        if (EEProperties.testSystemProperty("net.olioinfo.eeproperties.consoleTracing","true")) {
            String[] parts = this.toString().split("@");
            System.out.println(String.format("consoleTrace: (%s) EEProperties: Creating instance of EEProperties",parts[1]));
            System.out.println(String.format("consoleTrace: (%s) EEProperties: Runtime environment set to %s",parts[1],this.runtimeEnvironment));
        }
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
        if (EEProperties.singletonInstance == null) {
            if (EEProperties.testSystemProperty("net.olioinfo.eeproperties.consoleTracing","true")) {
                System.out.println(String.format("consoleTrace: EEProperties: Creating singleton instance of EEProperties"));
            }
            EEProperties.singletonInstance = new EEProperties();
        }
        return EEProperties.singletonInstance;
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
        //TODO Substitution needs to happen after all files are loaded
        
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
        String currentRuntimeEnvironemnt = null;
//        if (options.get("net.olioinfo.eeproperties.runtime.environment") != null) {
//            currentRuntimeEnvironemnt = (String) options.get("net.olioinfo.eeproperties.runtime.environment");
//        }
        if (currentRuntimeEnvironemnt == null) {
            currentRuntimeEnvironemnt = this.runtimeEnvironment;
        }
        names.add(currentRuntimeEnvironemnt);

        loadAndMergeConfigurations(names,klass,properties,options);

    }


    /**
     * List all the properties and values to the designated output stream (singleton version)
     *
     * @param printStream PrintStream to output properties to
     */
    public static void sListProperties(PrintStream printStream) {
        EEProperties.singleton().listProperties(printStream);

    }

    /**
     * List all the properties and values to the designated output stream
     *
     * @param printStream PrintStream to output properties to
     */
    public void listProperties(PrintStream printStream) {
        this.coreProperties.list(printStream);
    }


    /**
     * <p>Get an unordered list of all the property names as Strings (Singleton instance)</p>
     *
     * </p>Any property key that doesn't convert to a String will be skipped.</p>
     *
     * @return Unordered list of property names
     *
     * @since 2.8
     */
    public static ArrayList<String> sPropertyNames() {
        return EEProperties.singleton().propertyNames();

    }

    /**
     * <p>Get an unordered list of all the property names as Strings</p>
     *
     * </p>Any property key that doesn't convert to a String will be skipped.</p>
     *
     * @return Unordered list of property names
     *
     * @since 2.8
     */
    public ArrayList<String> propertyNames() {
        ArrayList<String> propertyNames = new ArrayList<String>();
        for (Object key : this.coreProperties.keySet()) {
            try {
                String propertyName = (String) key;
                propertyNames.add(propertyName);
            }
            catch (Exception ex) {
                // Do nothing - just skip

            }
        }
        return propertyNames;
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
        setTypedPropertyValueAndType(propertyName,propertyValue,"String");
    }

    /**
     * Get an Integer property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Integer sGetIntegerProperty(String propertyName) {
        return EEProperties.singleton().getIntegerProperty(propertyName);
    }

    /**
     * Get an Integer property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Integer getIntegerProperty(String propertyName) {
        return (Integer) getTypedPropertyValue(propertyName);
    }


    /**
     * Put an Integer property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutInteger(String propertyName, Integer propertyValue) {
        EEProperties.singleton().putInteger(propertyName,propertyValue);
    }

    /**
     * Put an Integer property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putInteger(String propertyName, Integer propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Integer");
    }

    /**
     * Get a Hhort property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Short sGetShortProperty(String propertyName) {
        return EEProperties.singleton().getShortProperty(propertyName);
    }

    /**
     * Get a Short property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Short getShortProperty(String propertyName) {
        return (Short) getTypedPropertyValue(propertyName);
    }

    /**
     * Put a Short property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutShort(String propertyName, Short propertyValue) {
        EEProperties.singleton().putShort(propertyName,propertyValue);
    }

    /**
     * Put a Short property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putShort(String propertyName, Short propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Short");
    }


    /**
     * Get a Long property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Long sGetLongProperty(String propertyName) {
        return EEProperties.singleton().getLongProperty(propertyName);
    }

    /**
     * Get a Long property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Long getLongProperty(String propertyName) {
        return (Long) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Long property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutLong(String propertyName, Long propertyValue) {
        EEProperties.singleton().putLong(propertyName,propertyValue);
    }

    /**
     * Put a Long property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putLong(String propertyName, Long propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Long");
    }

    /**
     * Get a Byte property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Byte sGetByteProperty(String propertyName) {
        return EEProperties.singleton().getByteProperty(propertyName);
    }

    /**
     * Get a Byte property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Byte getByteProperty(String propertyName) {
        return (Byte) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Byte property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutByte(String propertyName, Byte propertyValue) {
        EEProperties.singleton().putByte(propertyName,propertyValue);
    }

    /**
     * Put a Byte property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putByte(String propertyName, Byte propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Byte");
    }

    /**
     * Get a Float property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Float sGetFloatProperty(String propertyName) {
        return EEProperties.singleton().getFloatProperty(propertyName);
    }

    /**
     * Get a Float property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Float getFloatProperty(String propertyName) {
        return (Float) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Float property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutFloat(String propertyName, Float propertyValue) {
        EEProperties.singleton().putFloat(propertyName,propertyValue);
    }

    /**
     * Put a Float property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putFloat(String propertyName, Float propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Float");
    }

    /**
     * Get a Double property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Double sGetDoubleProperty(String propertyName) {
        return EEProperties.singleton().getDoubleProperty(propertyName);
    }

    /**
     * Get a Double property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Double getDoubleProperty(String propertyName) {
        return (Double) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Double property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutDouble(String propertyName, Double propertyValue) {
        EEProperties.singleton().putDouble(propertyName,propertyValue);
    }

    /**
     * Put a Double property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putDouble(String propertyName, Double propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Double");
    }

    /**
     * Get a Boolean property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Boolean sGetBooleanProperty(String propertyName) {
        return EEProperties.singleton().getBooleanProperty(propertyName);
    }

    /**
     * Get a Boolean property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Boolean getBooleanProperty(String propertyName) {
        return (Boolean) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Boolean property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutBoolean(String propertyName, Boolean propertyValue) {
        EEProperties.singleton().putBoolean(propertyName,propertyValue);
    }

    /**
     * Put a Boolean property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putBoolean(String propertyName, Boolean propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Boolean");
    }
 
    /**
     * Get a Date property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static Date sGetDateProperty(String propertyName) {
        return EEProperties.singleton().getDateProperty(propertyName);
    }

    /**
     * Get a Date property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public Date getDateProperty(String propertyName) {
        return (Date) getTypedPropertyValue(propertyName);
    }


    /**
     * Put a Date property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutDate(String propertyName, Date propertyValue) {
        EEProperties.singleton().putDate(propertyName,propertyValue);
    }

    /**
     * Put a Date property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putDate(String propertyName, Date propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"Date");
    }

    /**
     * Get an ArrayList<String> property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static ArrayList<String> sGetArrayListStringProperty(String propertyName) {
        return EEProperties.singleton().getArrayListStringProperty(propertyName);
    }

    /**
     * Get an ArrayList<String> property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public ArrayList<String> getArrayListStringProperty(String propertyName) {
        return (ArrayList<String>) getTypedPropertyValue(propertyName);
    }


    /**
     * Put an ArrayList<String> property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutArrayListString(String propertyName, ArrayList<String> propertyValue) {
        EEProperties.singleton().putArrayListString(propertyName,propertyValue);
    }

    /**
     * Put an ArrayList<String> property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putArrayListString(String propertyName, ArrayList<String> propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"ArrayList<String>");
    }


    /**
     * Get an ArrayList<Integer> property setting  (for the singleton class)
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public static ArrayList<Integer> sGetArrayListIntegerProperty(String propertyName) {
        return EEProperties.singleton().getArrayListIntegerProperty(propertyName);
    }

    /**
     * Get an ArrayList<Integer> property setting
     *
     * @param propertyName Property Name to retrieve
     * @return Property value or null if not found
     */
    public ArrayList<Integer> getArrayListIntegerProperty(String propertyName) {
        return (ArrayList<Integer>) getTypedPropertyValue(propertyName);
    }


    /**
     * Put an ArrayList<Integer> property (for the singleton class)
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public static void sPutArrayListInteger(String propertyName, ArrayList<Integer> propertyValue) {
        EEProperties.singleton().putArrayListInteger(propertyName,propertyValue);
    }

    /**
     * Put an ArrayList<Integer property
     *
     * @param propertyName Property Name to set
     * @param propertyValue Value for property
     *
     */
    public void putArrayListInteger(String propertyName, ArrayList<Integer> propertyValue) {
        setTypedPropertyValueAndType(propertyName,propertyValue,"ArrayList<Integer>");
    }

    /**
     * Substitute all the variable expressions in a given string with system environment settings and system properties and existing properties
     * in that order. If no match is found, the variable is left unmodified
     *
     * @param inputString String with possible variable substitution patterns
     * @return String with substitutions or null if no substitutions
     * @since 2.5
     */
    public static String substituteVariables(String inputString, Properties properties) {

        // Returned String builder
        StringBuffer returnedStringBuf = new StringBuffer();

        int matchPos;
        int startPos = 0;
        int endPos = inputString.length() - 1;
        boolean anySubstitution = false;
        while ( ( matchPos = inputString.indexOf("${",startPos)) != -1 ) {
            String beforeVariable = inputString.substring(startPos,matchPos);
            int varNameEnd = inputString.indexOf("}",matchPos + 2);
            String varName = inputString.substring(matchPos + 2, varNameEnd);
            String substitutionValue = null;
            if (System.getProperty(varName) != null) {
                substitutionValue = System.getProperty(varName);
                anySubstitution = true;
            }
            else if (System.getenv(varName) != null ) {
                substitutionValue = System.getenv(varName);
                anySubstitution = true;
            }
            else if (properties != null && properties.getProperty(varName) != null) {
                substitutionValue = properties.getProperty(varName);
                anySubstitution = true;
            }
            if (substitutionValue == null) {
                substitutionValue = String.format("${%s}",varName);
            }
            returnedStringBuf.append(beforeVariable).append(substitutionValue);
            startPos = varNameEnd + 1;
        }
        if (startPos <= endPos) {
           returnedStringBuf.append(inputString.substring(startPos));
        }
        if (anySubstitution) {
            return returnedStringBuf.toString();
        }
        else {
            return null;
        }
    }

    /**
     * Reload all the existing configuration definitions in the order they were originally loaded
     *
     * <p>Only intended for internal use. Requires setup. See explanation in sReloadConfigurations.</p>
     *
     * @param loadDefinitions Used if supplied. If null use existing (singleton) values
     *
     * @since 2.6
     */
    public void reloadConfigurations(ArrayList<EEPropertiesLoadDefinition> loadDefinitions) {

        if (loadDefinitions == null) {
            loadDefinitions = EEPropertiesLoadDefinition.getRegisteredDefinitions();
        }
        for (EEPropertiesLoadDefinition loadDefinition : loadDefinitions ) {
           if (loadDefinition.getEntryType() == EEPropertiesLoadDefinition.DEFINITION_TYPE_ABSOLUTE_PATH) {
                loadPropertiesFromFileOrClass(this.coreProperties,loadDefinition.getEntryPath(),null);
           }
           else if (loadDefinition.getEntryType() == EEPropertiesLoadDefinition.DEFINITION_TYPE_CLASS_RELATIVE) {
               loadPropertiesFromFileOrClass(this.coreProperties,loadDefinition.getEntryPath(),loadDefinition.getEntryClass());
           }
        }
    }

    /**
     * Reload all the existing configuration definitions in the order they were originally loaded
     *
     * <p>Only intended for internal use. Requires setup. See explanation in sReloadConfigurations.</p>
     *
     * @since 2.6
     */
    public void reloadConfigurations() {
        reloadConfigurations(null);
    }



    /**
     * Reload all the existing configuration definitions in the order they were originally loaded (singleton instance)
     *
     * <p>Sequence</p>
     * <ul>
     * <li>Get existing load definitions</li>
     * <li>Initialize new (singleton) instance</li>
     * <li>Reload existing load definitions against new (singleton) instance</li>
     * </ul>
     *
     * @since 2.6
     */
    public static void sReloadConfigurations() {

        synchronized (EEProperties.class) {
            // This sequence avoids an infinite loop when reloading as EEProperties.registerDefinition is called during reload

            ArrayList<EEPropertiesLoadDefinition> existingLoadDefinitions = EEPropertiesLoadDefinition.getRegisteredDefinitions();
            EEPropertiesLoadDefinition.sResetRegisteredDefinitions();

            // This call does the basic initialization for EEProperties itself - including rereading the bootstrap file and internal logging settings
            EEProperties.singletonInstance = new EEProperties();
            
            // Now load the previous definitions in order
            EEProperties.singleton().reloadConfigurations(existingLoadDefinitions);
        }
    }

    /**
     * Get the value of a (typed) Property Value
     *
     * <p>Note - this call is only intended for internal use. It should not be used directly.</p>
     *
     * @param propertyName Property name of the (typed) Property to retrieve
     * @return value of the property as an Object
     * @since 2.9
     */
    public Object getTypedPropertyValue(String propertyName) {
        HashMap<String,Object> typedEntry = this.typedCoreProperties.get(propertyName);
        if (typedEntry == null) {
            return null;
        }
        return typedEntry.get(EEProperties.TYPED_PROPERTY_VALUE);
    }

    /**
     * Set the value of a (typed) Property Value
     *
     * <p>Note - this call is only intended for internal use. It should not be used directly.</p>
     *
     * @param propertyName Property name of the (typed) Property to set
     * @param propertyValue Value of the (typed) Property to set
     * @since 2.9
     */
    public void setTypedPropertyValue(String propertyName,Object propertyValue) {
        HashMap<String,Object> typedEntry = this.typedCoreProperties.get(propertyName);
        if (typedEntry == null) {
            typedEntry = new HashMap<String,Object>();
        }
        typedEntry.put(EEProperties.TYPED_PROPERTY_VALUE,propertyValue);
        this.typedCoreProperties.put(propertyName,typedEntry);
    }

    /**
     * Get the type of a (typed) Property Value
     *
     * @param propertyName Property name of the (typed) Property to retrieve
     * @return type of the property as an String
     * @since 2.9
     */
    public String getTypedPropertyType(String propertyName) {
        HashMap<String,Object> typedEntry = this.typedCoreProperties.get(propertyName);
        if (typedEntry == null) {
            return null;
        }
        return (String) typedEntry.get(EEProperties.TYPED_PROPERTY_TYPE);
    }


    /**
     * Set the type of a (typed) Property Value
     *
     * <p>Note - this call is only intended for internal use. It should not be used directly.</p>
     *
     * @param propertyName Property name of the (typed) Property to set
     * @param propertyType Type of the (typed) Property to set
     * @since 2.9
     */
    public void setTypedPropertyType(String propertyName,String propertyType) {
        HashMap<String,Object> typedEntry = this.typedCoreProperties.get(propertyName);
        if (typedEntry == null) {
            typedEntry = new HashMap<String,Object>();
        }
        typedEntry.put(EEProperties.TYPED_PROPERTY_TYPE,propertyType);
        this.typedCoreProperties.put(propertyName,typedEntry);
    }

    /**
     * Set the value and type of a (typed) Property Value
     *
     * <p>Note - this call is only intended for internal use. It should not be used directly.</p>
     *
     * @param propertyName Property name of the (typed) Property to set
     * @param propertyValue Property value for (typed) Property
     * @param propertyType Type of the (typed) Property to set
     * @since 2.9
     */
    public void setTypedPropertyValueAndType(String propertyName,Object propertyValue, String propertyType) {
        HashMap<String,Object> typedEntry = this.typedCoreProperties.get(propertyName);
        if (typedEntry == null) {
            typedEntry = new HashMap<String,Object>();
        }
        typedEntry.put(EEProperties.TYPED_PROPERTY_TYPE,propertyType);
        typedEntry.put(EEProperties.TYPED_PROPERTY_VALUE,propertyValue);
        this.typedCoreProperties.put(propertyName,typedEntry);
    }

    /**
     * Get the type of a (typed) Property Value (for the singleton class)
     *
     * @param propertyName Property name of the (typed) Property to retrieve
     * @return type of the property as an String
     * @since 2.9
     */
    public static String sGetTypedPropertyType(String propertyName) {
        return EEProperties.singleton().getTypedPropertyType(propertyName);
    }


    /**
     * Initialize console tracing
     *
     * @param options Hash of options
     */
    private void initializeConsoleTracing(HashMap<String,String> options) {
        boolean consoleTracing = false;

        if (EEProperties.testSystemProperty("net.olioinfo.eeproperties.consoleTracing","true")) {
            consoleTracing = true;
        }
        if (EEProperties.testOption(options,"net.olioinfo.eeproperties.consoleTracing","true")) {
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

        this.logger.debug("EEproperties.initializeLogging: Entering... ");

        boolean bootstrapLogging = false;

        if (EEProperties.testSystemProperty("net.olioinfo.eeproperties.bootstrapLogging","true")) {
            bootstrapLogging = true;
        }
        if (EEProperties.testOption(options,"net.olioinfo.eeproperties.bootstrapLogging","true")) {
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


        this.logger.debug("EEProperties.loadBootstrapFile: Entering... ");

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
            this.logger.info("EEProperties.loadBootstrapFile bootstrap file loaded successfully.");
        }
        else {
            this.logger.error("EEProperties.loadBootstrapFile bootstrap file failed to load");
        }


        this.runtimeEnvironment = getPropertyFromOptionsOrSystemOrPropertiesWithDefault(
            "net.olioinfo.eeproperties.runtime.environment",options,this.coreProperties,this.runtimeEnvironment);
        this.coreProperties.setProperty("net.olioinfo.eeproperties.runtime.environment",this.runtimeEnvironment);

        if (this.logger.isConsoleTracing()) {
            this.logger.trace("EEProperties.loadBootstrapFile bootstrap file contents.");
            this.logger.dumpProperties("trace",this.coreProperties);
            this.logger.trace(String.format("EEProperties.loadBootstrapFile effective environment now set to %s",this.runtimeEnvironment));
        }


        String extendedPropertiesSyntaxSetting =  coreProperties.getProperty("net.olioinfo.eeproperties.extendedPropertiesSyntax.enabled");
        if (extendedPropertiesSyntaxSetting != null && extendedPropertiesSyntaxSetting.equals("true")) {
            this.extendedPropertiesSyntax = true;
        }

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
    public static boolean testSystemProperty(String propertyName, String propertyValue) {
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
    public static boolean testOption(HashMap<String,String> options , String optionName,String optionValue) {
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
    private String getPropertyFromOptionsOrSystemOrPropertiesWithDefault(
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
                    this.logger.info(String.format("EEProperties.loadPropertiesFromFileOrClass no file name specified"));
                }
                else {
                    if (fileName.startsWith("/")) {
                        if ((new File(fileName).exists())) {
                            is = new FileInputStream(fileName);
                            EEPropertiesLoadDefinition.createFromAbsolutePath(fileName);
                        }
                    }
                    else if (klass != null) {
                        URL url = klass.getResource(fileName);
                        if (url != null) {
                            is = url.openStream();
                            EEPropertiesLoadDefinition.createFromClass(klass,fileName);
                        }
                    }
                }
                if (is == null ) {
                    this.logger.error(String.format("EEProperties.loadPropertiesFromFileOrClass input stream not created. Check file name and location"));
                }
                else {
                    Properties newProperties = new Properties();
                    newProperties.load(is);
                    newProperties = stripAllLeadingTrailingWhiteSpace(newProperties);
                    newProperties = EEProperties.substituteAll(newProperties);
                    convertToObjectInstances(newProperties);
                    addAll(properties,newProperties);
                    is.close();
                    returnStatus = true;
                    this.logger.dumpProperties("debug",properties);
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
            String fullFileName = null;
            fullFileName = String.format("%s/%s/%s",location,klass.getPackage().getName().replaceAll("\\.","/"),fileName);
            if (! (new File(fullFileName).exists())) {
                fullFileName = String.format("%s/%s",location,fileName);
                if (! (new File(fullFileName).exists())) {
                    fullFileName = null;
                }
            }
            if (fullFileName != null) {
                InputStream is = null;
                try {
                    is = new FileInputStream(fullFileName);
                    Properties newProperties = new Properties();
                    newProperties.load(is);
                    newProperties = stripAllLeadingTrailingWhiteSpace(newProperties);
                    newProperties = EEProperties.substituteAll(newProperties);
                    convertToObjectInstances(newProperties);
                    properties = addAll(properties,newProperties);
                    is.close();
                    fileFound = true;
                    EEPropertiesLoadDefinition.createFromAbsolutePath(fullFileName);
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
                    Properties newProperties = new Properties();
                    newProperties.load(is);
                    newProperties = stripAllLeadingTrailingWhiteSpace(newProperties);
                    newProperties = EEProperties.substituteAll(newProperties);
                    convertToObjectInstances(newProperties);
                    addAll(properties,newProperties);
                    is.close();
                    fileFound = true;
                    EEPropertiesLoadDefinition.createFromClass(klass,fileName);
                    logger.debug(String.format("EEProperties.loadPropertiesFromLocationsOrClass Loaded file %s relative to class %s",fileName,klass.getName()));
                }
                catch (Exception ex) {
                    logger.info(String.format("EEProperties.loadPropertiesFromLocationsOrClass Unable to load file %s relative to class %s",fileName,klass.getName()));
                }
            }
        }
        if (fileFound) {
            this.logger.dumpProperties("debug",properties);
        }
        else {
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
     * Add all properties from one Properties instance to snother
     *
     * @param existingProperties Existing properties (this is the instance to which properties are added)
     * @param newProperties New properties (this is the instance from which properties are copied)
     * @return Merged properties object
     */
    private Properties addAll(Properties existingProperties, Properties newProperties) {

        Set<String> propertyNames = newProperties.stringPropertyNames();
        Iterator<String> propertyNamesItr = propertyNames.iterator();
        while (propertyNamesItr.hasNext()) {
            String propertyName = propertyNamesItr.next();
            existingProperties.setProperty(propertyName,removeLeadingTrailingWhiteSpace(newProperties.getProperty(propertyName)));
        }
        return existingProperties;

    }


    /**
     * Convert properties to object instances
     *
     * <p>Internal storage is updated. If errors occur during conversion no entry is made in the interance storage.</p>
     *
     * @param newProperties
     *
     */
    private void convertToObjectInstances(Properties newProperties) {

        Pattern objectTypeRegex = Pattern.compile("^\\[([^]]+)\\](.+)$", Pattern.CASE_INSENSITIVE);

        if (this.extendedPropertiesSyntax) {
            Set<String> propertyNames = newProperties.stringPropertyNames();
            Iterator<String> propertyNamesItr = propertyNames.iterator();
            while (propertyNamesItr.hasNext()) {
                String propertyName = propertyNamesItr.next();
                String propertyValue = newProperties.getProperty(propertyName);
                Matcher matcher = objectTypeRegex.matcher(propertyValue);
                if (matcher.matches()) {
                    String objectType = matcher.group(1);
                    String objectTypeLowerCase = objectType.toLowerCase();
                    String stringValue = removeLeadingTrailingWhiteSpace(matcher.group(2));
                    Object returnedInstance = convertToObjectInstance(objectType,stringValue);
                    if (returnedInstance != null) {
                        try {
                            HashMap<String,Object> valueAndType = new HashMap<String,Object>();
                            if (objectTypeLowerCase.startsWith("arraylist")) {
                                ArrayList<Object> returnedList  = (ArrayList<Object>) returnedInstance;
                                String arrayListType = (String) returnedList.get(0);
                                if (arrayListType.equals("string")) {
                                    valueAndType.put(EEProperties.TYPED_PROPERTY_TYPE,"ArrayList<String>");
                                    valueAndType.put(EEProperties.TYPED_PROPERTY_VALUE, returnedList.get(1));
                                }
                                else if (arrayListType.equals("integer")) {
                                    valueAndType.put(EEProperties.TYPED_PROPERTY_TYPE,"ArrayList<Integer>");
                                    valueAndType.put(EEProperties.TYPED_PROPERTY_VALUE, returnedList.get(1));
                                }
                                this.typedCoreProperties.put(propertyName,valueAndType);
                            }
                            else {
                                valueAndType.put(EEProperties.TYPED_PROPERTY_TYPE,objectType);
                                valueAndType.put(EEProperties.TYPED_PROPERTY_VALUE,returnedInstance);
                                this.typedCoreProperties.put(propertyName,valueAndType);
                            }

                            
                        }
                        catch (Exception ex) {
                            logger.debug(String.format("Failed to cast %s to an object instance of type %s",stringValue,objectType),ex);
                        }

                    }
                }
                else {
                    // If it's'nothing we recognize, it's a String
                    HashMap<String,Object> valueAndType = new HashMap<String,Object>();
                    valueAndType.put(EEProperties.TYPED_PROPERTY_TYPE,"String");
                    valueAndType.put(EEProperties.TYPED_PROPERTY_VALUE, propertyValue);
                    this.typedCoreProperties.put(propertyName,valueAndType);
                }
            }
        }
        

    }


    /**
     * Convert a String value to an object value
     *
     * @param objectType Type of object to convert to
     * @param stringValue String value to convert
     * @return Object instance with the specified value or null if conversion failed
     */
    private Object convertToObjectInstance(String objectType, String stringValue ) {

        Pattern arrayListRegex = Pattern.compile("^[^<]+<([^>]+)>$", Pattern.CASE_INSENSITIVE);

        Object returnedInstance = null;
        String objectTypeLowerCase = objectType.toLowerCase();
        try {
            if (objectTypeLowerCase.equals("integer")) {
                returnedInstance = new Integer(stringValue);
            }
            else if (objectTypeLowerCase.equals("short")) {
                returnedInstance = new Short(stringValue);
            }
            else if (objectTypeLowerCase.equals("long")) {
                returnedInstance = new Long(stringValue);
            }
            else if (objectTypeLowerCase.equals("byte")) {
                returnedInstance = new Byte(stringValue);
            }
            else if (objectTypeLowerCase.equals("float")) {
                returnedInstance = new Float(stringValue);
            }
            else if (objectTypeLowerCase.equals("double")) {
                returnedInstance = new Double(stringValue);
            }
            else if (objectTypeLowerCase.equals("boolean")) {
                if (stringValue.equalsIgnoreCase("true")) {
                    returnedInstance = Boolean.TRUE;                    
                }
                else if (stringValue.equalsIgnoreCase("false")) {
                    returnedInstance = Boolean.FALSE;
                }
            }
            else if (objectTypeLowerCase.equals("date")) {
                if (stringValue.indexOf("T") > -1) {
                    SimpleDateFormat dateAndTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                    returnedInstance = dateAndTime.parse(stringValue);
                }
                else {
                    SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
                    returnedInstance = dateOnly.parse(stringValue);
                }
            }
            else if (objectTypeLowerCase.startsWith("arraylist")) {
                ArrayList<Object> returnedArray = new ArrayList<Object>();
                String[] rawStringValues = stringValue.split(",");
                Matcher arrayListMatcher = arrayListRegex.matcher(objectTypeLowerCase);
                if (arrayListMatcher.matches()) {
                    String arrayListType = arrayListMatcher.group(1);
                    if (arrayListType.equals("string")) {
                        ArrayList<String> stringArrayList = new ArrayList<String>();
                        for (String rawString : rawStringValues ) {
                            stringArrayList.add(removeLeadingTrailingWhiteSpace(rawString));
                        }
                        returnedArray.add(arrayListType);
                        returnedArray.add(stringArrayList);
                    }
                    else if (arrayListType.equals("integer")) {
                        ArrayList<Integer> integerArrayList = new ArrayList<Integer>();
                        for (String rawString : rawStringValues ) {
                            integerArrayList.add(new Integer(removeLeadingTrailingWhiteSpace(rawString)));
                        }
                        returnedArray.add(arrayListType);
                        returnedArray.add(integerArrayList);
                    }


                    returnedInstance = returnedArray;

                }

            }

        }
        catch (Exception ex) {
            logger.debug(String.format("Failed to convert %s to an object of type %s",stringValue,objectType),ex);
            returnedInstance = null;
        }

        return returnedInstance;

    }



    /**
     * Perform variable substitution for all properties
     *
     * @param existingProperties Existing properties (this is the instance to which properties are added)
     * @return Properties object with all properties substituted
     */
    public static Properties substituteAll(Properties existingProperties) {
        Integer totalPasses = 0;
        boolean anySubstitutionFound = true;
        while (anySubstitutionFound ) {
            anySubstitutionFound = false;
            totalPasses = totalPasses + 1;
            Set<String> propertyNames = existingProperties.stringPropertyNames();
            for (String propertyName : propertyNames) {
                String propertyValue =  existingProperties.getProperty(propertyName);
                String substitutedName = EEProperties.substituteVariables(propertyName,existingProperties);
                if (substitutedName == null ) {
                  substitutedName = propertyName;
                }
                else {
                  anySubstitutionFound = true;
                }
                existingProperties.setProperty(substitutedName,propertyValue);
                String substitutedValue = EEProperties.substituteVariables(propertyValue,existingProperties);
                if (substitutedValue == null) {
                    substitutedValue = propertyValue;
                }
                else {
                    anySubstitutionFound = true;
                }
                existingProperties.setProperty(substitutedName,substitutedValue);
            }
        }
        return existingProperties;
        
    }

    /**
     * Strip leading and trailing blanks from all properties
     *
     * @param existingProperties Existing properties (this is the instance to which properties are added)
     * @return Properties object with all properties stripped
     */
    private Properties stripAllLeadingTrailingWhiteSpace(Properties existingProperties) {

        Set<String> propertyNames = existingProperties.stringPropertyNames();
        Iterator<String> propertyNamesItr = propertyNames.iterator();
        while (propertyNamesItr.hasNext()) {
            String propertyName = propertyNamesItr.next();
            existingProperties.setProperty(propertyName,removeLeadingTrailingWhiteSpace(existingProperties.getProperty(propertyName)));
        }
        return existingProperties;

    }


    /**
     * Remove leading and trailing whitespace from a string
     *
     * @param inputString Input string
     * @return String with leading and trailing whitespace removed
     */
    private String removeLeadingTrailingWhiteSpace(String inputString) {
        String outputString = inputString.replaceFirst("^\\s*","");
        outputString = outputString.replaceFirst("\\s*$","");
        return outputString;
    }

    /**
     * Run standalone for testing purposes
     */
    public static void main(String[] args) {
        EEProperties.singleton();

//        HashMap<String,String> options = new HashMap<String,String>();
//        options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
//        EEProperties.singleton().loadPackageConfiguration(EEProperties.class,options);
//        System.out.println("test value should be 'value1' but is " + EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.1"));
//
//        EEProperties.sPut("test.reload.property.1","one");
//
//        EEProperties.sReloadConfigurations();
//        options = new HashMap<String,String>();
//        options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
//        EEProperties.singleton().loadPackageConfiguration(EEProperties.class,options);
//
//        System.out.println("test value should be 'value1' but is " + EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.1"));
//        String value =  EEProperties.sGetProperty("test.reload.property.1");
//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXX");
//        System.out.println("Value is " + (value == null ? "null" : "not null" )) ;

    }
}