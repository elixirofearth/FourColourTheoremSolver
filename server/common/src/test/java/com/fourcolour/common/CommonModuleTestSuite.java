package com.fourcolour.common;

import com.fourcolour.common.dto.ColoringRequestTest;
import com.fourcolour.common.dto.LoginRequestTest;
import com.fourcolour.common.dto.MapRequestTest;
import com.fourcolour.common.dto.RegisterRequestTest;
import com.fourcolour.common.dto.TokenResponseTest;
import com.fourcolour.common.service.LoggerClientTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all tests in the common module.
 * This suite runs all the tests for DTOs and services in the common module.
 */
@Suite
@SuiteDisplayName("Common Module Test Suite")
@SelectClasses({
    ColoringRequestTest.class,
    LoginRequestTest.class,
    MapRequestTest.class,
    RegisterRequestTest.class,
    TokenResponseTest.class,
    LoggerClientTest.class
})
public class CommonModuleTestSuite {
    // This class serves as a test suite container
    // All tests are automatically discovered and run
} 