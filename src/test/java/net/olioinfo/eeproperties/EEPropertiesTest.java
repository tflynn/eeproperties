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


import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.util.HashMap;


/**
 * TestNG test suite for EEProperties
 *
 * @author Tracy Flynn
 * @since Jun 27, 2010
 */
@Test
public class EEPropertiesTest {

    public void testInitialization() {
        // Force loading
        EEProperties.singleton();
        // No actual test involved yet
        assert true;
    }

    public void testLoad() {
        HashMap<String,String> options = new HashMap<String,String>();
        options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
        EEProperties.singleton().loadPackageConfiguration(EEProperties.class,options);
        assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.1").equals("value1");
        assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.2").equals("value3");
    }

    public void testDefaultLoad() {
        HashMap<String,String> options = new HashMap<String,String>();
        options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
        EEProperties.sLoadPackageConfiguration(EEProperties.class,options);
        assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.1").equals("value1");
        assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.2").equals("value3");
    }
    
    public void testLoadWithExternalPath() {
        String configurationDirectory = "/Users/tracy/Everything/Activities/test-eeproperties-configurations";
        // Fix this so test works on any machine
        if ((new File(configurationDirectory)).exists()) {
            HashMap<String,String> options = new HashMap<String,String>();
            options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
            options.put("net.olioinfo.eeproperties.runtime.additionalConfigurationPaths",configurationDirectory);
            EEProperties.singleton().loadPackageConfiguration(EEProperties.class,options);
            assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.1").equals("value1");
            assert EEProperties.sGetProperty("net.olioinfo.eeproperties.test.value.2").equals("value4");
        }
        else {
            assert true;
        }
    }



}