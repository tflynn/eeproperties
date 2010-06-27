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

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;


/**
 * <p>Logger that uses any available method to log a message.</p>
 *
 * <p>It checks in order for:</p>
 * <ul>
 * <li>General logger</ul>
 * <li>Bootstrap logger</ul>
 * <li>Console tracing</ul>
 * </ul>
 *
 * Using the first available method
 *
 * @author Tracy Flynn
 * @since Jun 27, 2010
 */
public class AvailableLogger {


    /**
     * Console tracing state
    */
    private boolean consoleTracing = false;

    /**
     * Bootstrap logging state
     */
    private boolean bootstrapLogging = false;

    /**
     * Bootstrap logger
     */
    private Logger bootstrapLogger = LoggerFactory.getLogger(EEPropertiesBootstrapLogger.class);

    /**
     * Generic logging state
     */
    private boolean genericLogging = false;

    /**
     * Generic logger
     */
    private Logger genericLogger = LoggerFactory.getLogger(EEProperties.class);


    /**
     * Create an AvailableLogger instance
     */
    public AvailableLogger() {

    }

    /**
     * Log a message at the DEBUG level
     *
     * @param msg the message string to be logged
     */
    public void debug(String msg) {
        if (genericLogging) {
            genericLogger.debug(msg);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.debug(msg);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
        }
    }


    /**
     * Log a message at the DEBUG level
     *
     * @param msg the message string to be logged
     * @param t the exception (throwable) to log
     */
    public void debug(String msg, Throwable t) {
        if (genericLogging) {
            genericLogger.debug(msg,t);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.debug(msg,t);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
            consoleTrace(t.toString());
            t.printStackTrace(System.out);
        }
    }

    /**
     * Log a message at the ERROR level
     *
     * @param msg the message string to be logged
     */
    public void error(String msg) {
        if (genericLogging) {
            genericLogger.error(msg);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.error(msg);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
        }
    }


    /**
     * Log a message at the ERROR level
     *
     * @param msg the message string to be logged
     * @param t the exception (throwable) to log
     */
    public void error(String msg, Throwable t) {
        if (genericLogging) {
            genericLogger.error(msg,t);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.error(msg,t);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
            consoleTrace(t.toString());
            t.printStackTrace(System.out);
        }
    }

    /**
     * Log a message at the INFO level
     *
     * @param msg the message string to be logged
     */
    public void info(String msg) {
        if (genericLogging) {
            genericLogger.info(msg);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.info(msg);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
        }
    }


    /**
     * Log a message at the INFO level
     *
     * @param msg the message string to be logged
     * @param t the exception (throwable) to log
     */
    public void info(String msg, Throwable t) {
        if (genericLogging) {
            genericLogger.info(msg,t);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.info(msg,t);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
            consoleTrace(t.toString());
            t.printStackTrace(System.out);
        }
    }

    /**
     * Log a message at the TRACE level
     *
     * @param msg the message string to be logged
     */
    public void trace(String msg) {
        if (genericLogging) {
            genericLogger.trace(msg);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.trace(msg);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
        }
    }


    /**
     * Log a message at the INFO level
     *
     * @param msg the message string to be logged
     * @param t the exception (throwable) to log
     */
    public void trace(String msg, Throwable t) {
        if (genericLogging) {
            genericLogger.trace(msg,t);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.trace(msg,t);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
            consoleTrace(t.toString());
            t.printStackTrace(System.out);
        }
    }

    /**
     * Log a message at the WARN level
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg) {
        if (genericLogging) {
            genericLogger.warn(msg);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.warn(msg);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
        }
    }


    /**
     * Log a message at the WARN level
     *
     * @param msg the message string to be logged
     * @param t the exception (throwable) to log
     */
    public void warn(String msg, Throwable t) {
        if (genericLogging) {
            genericLogger.warn(msg,t);
        }
        else if (bootstrapLogging) {
            bootstrapLogger.warn(msg,t);
        }
        else if (consoleTracing) {
            consoleTrace(msg);
            consoleTrace(t.toString());
            t.printStackTrace(System.out);
        }
    }


    /**
     * Dump all the properties in a Properties instance. Does not truncate property names or values
     *
     * @param level Logging level
     * @param properties Properties instance
     *
     */
    public void dumpProperties(String level, Properties properties) {
        level = level.toLowerCase();
        for (Enumeration e = properties.propertyNames() ; e.hasMoreElements() ; ) {
            String currentName = (String) e.nextElement();
            String msg = String.format("%s = %s",currentName ,properties.get(currentName));
            if (level.equals("debug")) {
                debug(msg);
            }
            else if (level.equals("error")) {
                error(msg);
            }
            else if (level.equals("info")) {
                info(msg);
            }
            else if (level.equals("trace")) {
                trace(msg);
            }
            else if (level.equals("warn")) {
                warn(msg);                                
            }
        }
    }


    public boolean isConsoleTracing() {
        return consoleTracing;
    }

    public void setConsoleTracing(boolean consoleTracing) {
        this.consoleTracing = consoleTracing;
    }

    public boolean isBootstrapLogging() {
        return bootstrapLogging;
    }

    public void setBootstrapLogging(boolean bootstrapLogging) {
        this.bootstrapLogging = bootstrapLogging;
    }

    public Logger getBootstrapLogger() {
        return bootstrapLogger;
    }

    public void setBootstrapLogger(Logger bootstrapLogger) {
        this.bootstrapLogger = bootstrapLogger;
    }

    public boolean isGenericLogging() {
        return genericLogging;
    }

    public void setGenericLogging(boolean genericLogging) {
        this.genericLogging = genericLogging;
    }

    public Logger getGenericLogger() {
        return genericLogger;
    }

    public void setGenericLogger(Logger genericLogger) {
        this.genericLogger = genericLogger;
    }

    /**
     * Log a console tracing message (if console tracing is enabled)
     *
     * @param msg Message to trace
     */
    private void consoleTrace(String msg) {
        if (this.consoleTracing) System.out.println("consoleTrace: " + msg);
    }
    

}