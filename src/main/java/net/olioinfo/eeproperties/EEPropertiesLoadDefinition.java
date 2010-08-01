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


/**
 * Bean to track where particular settings were loaded from
 *
 * @author Tracy Flynn
 * @since Aug 1, 2010
 */
public class EEPropertiesLoadDefinition {

    /**
     * Indicates entry was loaded from an absolute path
     */
    public static final Integer DEFINITION_TYPE_ABSOLUTE_PATH = 1;

    /**
     * Indicates entry was loaded path relative
     */
    public static final Integer DEFINITION_TYPE_CLASS_RELATIVE = 2;

    

    /**
     * Name for the entry
     */
    private String entryName = null;

    /**
     * Entry type
     */
    private Integer entryType = null;


    /**
     * Entry Path - for entry loaded from absolute path
     */
    private String entryPath = null;


    /**
     * Entry class - for entry loaded from class
     */
    private Class entryClass = null;


    /**
     * Registered list of entries
     */
    private static ArrayList<EEPropertiesLoadDefinition> REGISTERED_ENTRIES = new  ArrayList<EEPropertiesLoadDefinition>();
    

    /**
     * Create a new instance of a loader definition
     */
    public EEPropertiesLoadDefinition() {

    }



    /**
     * Convenience method to create and register an entry from an absolute path
     *
     * @parma absolutePath Absolute path for definition
     * @return new entry instance
     */
    public static EEPropertiesLoadDefinition createFromAbsolutePath(String absolutePath) {
        EEPropertiesLoadDefinition newInstance = new EEPropertiesLoadDefinition();
        newInstance.setEntryType(DEFINITION_TYPE_ABSOLUTE_PATH);
        newInstance.setEntryName(absolutePath);
        newInstance.setEntryPath(absolutePath);
        EEPropertiesLoadDefinition.registerEntry(newInstance);
        return newInstance;
    }


    /**
     * Convenience method to create and register an entry from an absolute path
     *
     * @param definitionName Definition Name
     * @parma absolutePath Absolute path for definition
     * @return new entry instance
     */
    public static EEPropertiesLoadDefinition createFromAbsolutePath(String definitionName, String absolutePath) {
        EEPropertiesLoadDefinition newInstance = new EEPropertiesLoadDefinition();
        newInstance.setEntryType(DEFINITION_TYPE_ABSOLUTE_PATH);
        newInstance.setEntryName(definitionName);
        newInstance.setEntryPath(absolutePath);
        EEPropertiesLoadDefinition.registerEntry(newInstance);
        return newInstance;
    }

    /**
     * Convenience method to create and register an entry that is class relative
     *
     * @param klass Class
     * @param fileName File Name
     * @return new entry instance
     */
    public static EEPropertiesLoadDefinition createFromClass(Class klass, String fileName) {
        EEPropertiesLoadDefinition newInstance = new EEPropertiesLoadDefinition();
        newInstance.setEntryType(DEFINITION_TYPE_CLASS_RELATIVE);
        newInstance.setEntryName(klass.getName());
        newInstance.setEntryPath(fileName);
        newInstance.setEntryClass(klass);
        EEPropertiesLoadDefinition.registerEntry(newInstance);
        return newInstance;
    }

    /**
     * Convenience method to create and register an entry that is class relative
     *
     * @param klass Class
     * @param fileName File Name
     * @return new entry instance
     */
    public static EEPropertiesLoadDefinition createFromClass(String definitionName, Class klass, String fileName) {
        EEPropertiesLoadDefinition newInstance = new EEPropertiesLoadDefinition();
        newInstance.setEntryType(DEFINITION_TYPE_CLASS_RELATIVE);
        newInstance.setEntryName(definitionName);
        newInstance.setEntryPath(fileName);
        newInstance.setEntryClass(klass);
        EEPropertiesLoadDefinition.registerEntry(newInstance);
        return newInstance;
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

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public Integer getEntryType() {
        return entryType;
    }

    public void setEntryType(Integer entryType) {
        this.entryType = entryType;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String entryPath) {
        this.entryPath = entryPath;
    }

    public Class getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(Class entryClass) {
        this.entryClass = entryClass;
    }
}