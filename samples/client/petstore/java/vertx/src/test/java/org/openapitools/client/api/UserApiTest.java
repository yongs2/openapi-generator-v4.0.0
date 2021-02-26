/*
 * OpenAPI Petstore
 * This spec is mainly for testing Petstore server and contains fake endpoints, models. Please do not use this for any other purpose. Special characters: \" \\
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.openapitools.client.api;

import org.openapitools.client.model.User;

import org.openapitools.client.Configuration;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for UserApi
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class UserApiTest {

    private UserApi api;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @BeforeClass
    public void setupApiClient() {
        JsonObject config = new JsonObject();
        Vertx vertx = rule.vertx();
        Configuration.setupDefaultApiClient(vertx, config);

        api = new UserApiImpl();
    }
    
    /**
     * Create user
     * This can only be done by the logged in user.
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void createUserTest(TestContext context) {
        Async async = context.async();
        User user = null;
        api.createUser(user, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Creates list of users with given input array
     * 
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void createUsersWithArrayInputTest(TestContext context) {
        Async async = context.async();
        List<User> user = null;
        api.createUsersWithArrayInput(user, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Creates list of users with given input array
     * 
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void createUsersWithListInputTest(TestContext context) {
        Async async = context.async();
        List<User> user = null;
        api.createUsersWithListInput(user, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Delete user
     * This can only be done by the logged in user.
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void deleteUserTest(TestContext context) {
        Async async = context.async();
        String username = null;
        api.deleteUser(username, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Get user by user name
     * 
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void getUserByNameTest(TestContext context) {
        Async async = context.async();
        String username = null;
        api.getUserByName(username, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Logs user into the system
     * 
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void loginUserTest(TestContext context) {
        Async async = context.async();
        String username = null;
        String password = null;
        api.loginUser(username, password, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Logs out current logged in user session
     * 
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void logoutUserTest(TestContext context) {
        Async async = context.async();
        api.logoutUser(result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
    /**
     * Updated user
     * This can only be done by the logged in user.
     *
     * @param context Vertx test context for doing assertions
     */
    @Test
    public void updateUserTest(TestContext context) {
        Async async = context.async();
        String username = null;
        User user = null;
        api.updateUser(username, user, result -> {
            // TODO: test validations
            async.complete();
        });
    }
    
}