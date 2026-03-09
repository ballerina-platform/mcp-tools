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

package io.ballerina.mcp.core;

import io.ballerina.mcp.core.generator.NameSanitizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link NameSanitizer}.
 */
public class NameSanitizerTest {

    @DataProvider(name = "typeNameData")
    public Object[][] typeNameData() {
        return new Object[][] {
                {"Pet",           "Pet"},
                {"NewPet",        "NewPet"},
                {"my-type",       "my_type"},
                {"123Type",       "_123Type"},
                {"string",        "string_"},    // reserved keyword
                {"int",           "int_"},        // reserved keyword
                {"my type",       "my_type"},
                {"",              "_"},
        };
    }

    @Test(dataProvider = "typeNameData")
    public void testSanitizeTypeName(String input, String expected) {
        Assert.assertEquals(NameSanitizer.sanitizeTypeName(input), expected);
    }

    @DataProvider(name = "identData")
    public Object[][] identData() {
        return new Object[][] {
                {"petId",         "petId"},
                {"pet_id",        "petId"},
                {"pet-id",        "petId"},
                {"123field",      "_123field"},
                {"string",        "string_"},    // reserved keyword
                {"",              "field"},
        };
    }

    @Test(dataProvider = "identData")
    public void testSafeIdent(String input, String expected) {
        Assert.assertEquals(NameSanitizer.safeIdent(input), expected);
    }

    @DataProvider(name = "toolNameData")
    public Object[][] toolNameData() {
        return new Object[][] {
                {"get",    "/pets",          "getPets"},
                {"post",   "/pets",          "postPets"},
                {"get",    "/pets/{petId}",  "getPetsPetId"},
                {"delete", "/users/{id}",    "deleteUsersId"},
        };
    }

    @Test(dataProvider = "toolNameData")
    public void testBuildToolName(String method, String path, String expected) {
        Assert.assertEquals(NameSanitizer.buildToolName(method, path), expected);
    }
}
