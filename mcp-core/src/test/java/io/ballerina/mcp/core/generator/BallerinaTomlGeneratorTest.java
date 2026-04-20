/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.mcp.core.generator;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Unit tests for {@link BallerinaTomlGenerator} version normalization behavior.
 */
public class BallerinaTomlGeneratorTest {

    @DataProvider(name = "versionCases")
    public Object[][] versionCases() {
        return new Object[][]{
                {"1", "1.0.0"},
                {"v2.3", "2.3.0"},
                {"3.4.5", "3.4.5"},
                {"1.2.3-alpha.1+build.7", "1.2.3-alpha.1+build.7"},
                {"invalid-version", Constants.DEFAULT_VERSION},
                {"", Constants.DEFAULT_VERSION},
                {null, Constants.DEFAULT_VERSION}
        };
    }

    @Test(dataProvider = "versionCases")
    public void testVersionNormalization(String inputVersion, String expectedVersion) throws Exception {
        BallerinaTomlGenerator generator = new BallerinaTomlGenerator();
        Method normalizeMethod = BallerinaTomlGenerator.class.getDeclaredMethod("normalizeVersion", String.class);
        normalizeMethod.setAccessible(true);
        String normalized = (String) normalizeMethod.invoke(generator, inputVersion);

        Assert.assertEquals(normalized, expectedVersion,
                "Unexpected normalized version for input: " + inputVersion);
    }
}
