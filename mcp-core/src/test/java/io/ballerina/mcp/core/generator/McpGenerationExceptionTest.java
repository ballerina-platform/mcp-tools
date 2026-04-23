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
import org.testng.annotations.Test;

/**
 * Unit tests for {@link McpGenerationException}.
 */
public class McpGenerationExceptionTest {

    @Test
    public void testMessageConstructor() {
        McpGenerationException ex = new McpGenerationException("test error");
        Assert.assertEquals(ex.getMessage(), "test error");
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("root cause");
        McpGenerationException ex = new McpGenerationException("test error with cause", cause);
        Assert.assertEquals(ex.getMessage(), "test error with cause");
        Assert.assertEquals(ex.getCause(), cause);
    }
}