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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public void testConvertObjectTypes() {
        HashMap<String,String> options = new HashMap<String,String>();
        options.put("net.olioinfo.eeproperties.configurationFile.prefix","test-");
        EEProperties.sLoadPackageConfiguration(EEProperties.class,options);
        Integer testInteger = EEProperties.sGetIntegerProperty("net.olioinfo.eeproperties.test.value.10");
        if (testInteger != null) {
            assert testInteger.equals(new Integer(123));
        }
        Short testShort = EEProperties.sGetShortProperty("net.olioinfo.eeproperties.test.value.11");
        if (testShort != null) {
            assert testShort.equals(new Short("321"));
        }
        Long testLong = EEProperties.sGetLongProperty("net.olioinfo.eeproperties.test.value.12");
        assert testLong.equals(12345678901L);
        Byte testByte = EEProperties.sGetByteProperty("net.olioinfo.eeproperties.test.value.13");
        if  (testByte != null) {
            assert testByte.equals(new Byte((byte) 27));
        }
        Float testFloat = EEProperties.sGetFloatProperty("net.olioinfo.eeproperties.test.value.14");
        if (testFloat != null) {
            assert testFloat.equals(new Float("123.456"));
        }
        Double testDouble = EEProperties.sGetDoubleProperty("net.olioinfo.eeproperties.test.value.15");
        if (testDouble != null) {
            assert testDouble.equals(new Double("234.567"));          
        }
        Boolean testBoolean;
        testBoolean = EEProperties.sGetBooleanProperty("net.olioinfo.eeproperties.test.value.16");
        if (testBoolean != null) {
            assert testBoolean.equals(Boolean.TRUE);
            assert testBoolean.equals(new Boolean("true"));
        }
        testBoolean = EEProperties.sGetBooleanProperty("net.olioinfo.eeproperties.test.value.17");
        if (testBoolean != null) {
            assert testBoolean.equals(Boolean.FALSE);
            assert testBoolean.equals(new Boolean("false"));
        }

        Date testDate;
        Date dateToMatch;
        testDate = EEProperties.sGetDateProperty("net.olioinfo.eeproperties.test.value.18");
        if (testDate != null) {
            try {
                dateToMatch = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).parse("2001-07-04T12:08:56-0500");
                assert testDate.equals(dateToMatch);
            }
            catch (Exception ex) {
                assert false;
            }
        }

        testDate = EEProperties.sGetDateProperty("net.olioinfo.eeproperties.test.value.19");
        if (testDate != null) {
            try {
                dateToMatch = (new SimpleDateFormat("yyyy-MM-dd")).parse("2001-07-04");
                assert testDate.equals(dateToMatch);
            }
            catch (Exception ex) {
                assert false;
            }
        }

        ArrayList<String> stringList = EEProperties.sGetArrayListStringProperty("net.olioinfo.eeproperties.test.value.20");
        if (stringList != null) {
            assert stringList.get(0).equals("string 1");
            assert stringList.get(1).equals("string 2");
            assert stringList.get(2).equals("string 3");
        }


        ArrayList<Integer> integerList = EEProperties.sGetArrayListIntegerProperty("net.olioinfo.eeproperties.test.value.21");
        if (integerList != null) {
            assert (integerList.get(0) == 3);
            assert (integerList.get(1) == 17);
            assert (integerList.get(2) == 89);
        }

    }

}