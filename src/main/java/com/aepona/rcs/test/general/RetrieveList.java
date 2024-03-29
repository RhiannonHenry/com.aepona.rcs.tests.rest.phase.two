package com.aepona.rcs.test.general;

import java.util.Properties;

import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aepona.rcs.test.common.TestSubscriber;
import com.aepona.rcs.test.common.TestUtils;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/application-context.xml" })
public class RetrieveList {

    private final Logger LOGGER = LoggerFactory.getLogger(RetrieveList.class);

    @Value("${proxyURL}")
    protected String proxyURL;

    @Value("${proxyPort}")
    protected String proxyPort;

    @Value("${baseURI}")
    protected String baseURI;

    @Value("${apiVersion}")
    protected String apiVersion;

    @Value("${port}")
    protected int port;

    @Value("${applicationUsername}")
    protected String applicationUsername;

    @Value("${applicationPassword}")
    protected String applicationPassword;

    @Value("${user1}")
    protected String user1;

    @Value("${user2}")
    protected String user2;

    @Value("${invalidUser}")
    protected String invalidUser;

    @Value("${sessionSubscriptionURL}")
    protected String sessionSubscriptionURL;

    @Value("${validLongPoll}")
    protected String validLongPoll;

    @Value("${notificationChannelURL}")
    protected String notificationChannelURL;

    @Value("${sessionRequestData}")
    protected String sessionRequestData;

    @Value("${urlSplit}")
    protected String urlSplit;

    private Boolean initialised = false;

    private String lastTest = null;

    private TestSubscriber userOne;

    private TestSubscriber userTwo;

    @Before
    public void setup() {
        LOGGER.info("Proxy URL: " + proxyURL);
        Properties props = System.getProperties();
        props.put("http.proxyHost", proxyURL);
        props.put("http.proxyPort", proxyPort);
        start();
        userOne = new TestSubscriber();
        userOne.setUserID(user1);
        userTwo = new TestSubscriber();
        userTwo.setUserID(user2);
    }

    @After
    public void cleanup() {
        TestUtils.deleteNotificationChannel(userOne, applicationUsername, applicationPassword);
        TestUtils.deleteNotificationChannel(userTwo, applicationUsername, applicationPassword);
    }

    @Test
    public void listUser1Subscriptions() {
        String userID = user1;
        int i = 1;

        TestUtils.startNotificationChannel(userOne, notificationChannelURL, apiVersion, validLongPoll, applicationUsername, applicationPassword);
        TestUtils.subscribeToSession(userOne, sessionSubscriptionURL, sessionRequestData, apiVersion, applicationUsername, applicationPassword);


        String test = "List Session Subscription(s) for User 1";
        startTest(test);

        String cleanUserID = cleanPrefix(userID);
        String url = replace(sessionSubscriptionURL, apiVersion, userID);

        // RestAssured.authentication = RestAssured.basic(userID,
        // applicationPassword);
        RestAssured.authentication = RestAssured.basic(applicationUsername, applicationPassword);

        // Response response =
        // RestAssured.given().auth().preemptive().basic(applicationUsername,
        // applicationPassword).expect().log().ifError().statusCode(200).body(
        // "sessionSubscriptionList.resourceURL",
        // StringContains.containsString(cleanUserID),
        // "sessionSubscriptionList.sessionSubscription.size()", Matchers.is(1),
        // "sessionSubscriptionList.sessionSubscription[0].callbackReference.callbackData",
        // IsEqual.equalTo(userID),
        // "sessionSubscriptionList.sessionSubscription[0].callbackReference.notifyURL",
        // IsEqual.equalTo(callbackURL[1]),
        // "sessionSubscriptionList.sessionSubscription[0].callbackReference.notificationFormat",
        // IsEqual.equalTo("JSON")
        // ).get(url);
        Response response =
                RestAssured.given()
                           .auth()
                           .preemptive()
                           .basic(applicationUsername, applicationPassword)
                           .expect()
                           .log()
                           .ifError()
                           .statusCode(200)
                           .get(url);

        LOGGER.info("Response Received = " + response.getStatusCode());
        LOGGER.info("Body = " + response.asString());
        endTest(test);
    }

    @Test
    public void listUser2Subscriptions() {
        String userID = user2;
        int i = 2;

        TestUtils.startNotificationChannel(userTwo, notificationChannelURL, apiVersion, validLongPoll, applicationUsername, applicationPassword);
        TestUtils.subscribeToSession(userTwo, sessionSubscriptionURL, sessionRequestData, apiVersion, applicationUsername, applicationPassword);

        String test = "List Session Subscription(s) for User 2";
        startTest(test);

        String cleanUserID = cleanPrefix(userID);
        String url = replace(sessionSubscriptionURL, apiVersion, userID);

        // RestAssured.authentication = RestAssured.basic(userID,
        // applicationPassword);
        RestAssured.authentication = RestAssured.basic(applicationUsername, applicationPassword);

        Response response =
                RestAssured.given()
                           .auth()
                           .preemptive()
                           .basic(applicationUsername, applicationPassword)
                           .expect()
                           .log()
                           .ifError()
                           .statusCode(200)
                           .body("sessionSubscriptionList.resourceURL", StringContains.containsString(cleanUserID),
                                 "sessionSubscriptionList.sessionSubscription.size()", Matchers.is(1),
                                 "sessionSubscriptionList.sessionSubscription[0].callbackReference.callbackData",
                                 IsEqual.equalTo(userID),
                                 "sessionSubscriptionList.sessionSubscription[0].callbackReference.notificationFormat",
                                 IsEqual.equalTo("JSON"))
                           .get(url);

        LOGGER.info("Response Received = " + response.getStatusCode());
        LOGGER.info("Body = " + response.asString());
        endTest(test);
    }

    @Test
    public void listUserWithNoSubscriptions() {
        userTwo.setUserID("+15554000004");

        TestUtils.startNotificationChannel(userTwo, notificationChannelURL, apiVersion, validLongPoll, applicationUsername, applicationPassword);

        // subscribeToSessionNotifications(userID, i);

        String test = "List Session Subscription(s) for Unsubscribed User";
        startTest(test);
        String url = replace(sessionSubscriptionURL, apiVersion, userTwo.getUserID());

        // RestAssured.authentication = RestAssured.basic(userID,
        // applicationPassword);
        RestAssured.authentication = RestAssured.basic(applicationUsername, applicationPassword);

        // Response response =
        // RestAssured.given().auth().preemptive().basic(applicationUsername,
        // applicationPassword).expect().log().ifError().statusCode(400).get(url);
        Response response =
                RestAssured.given()
                           .auth()
                           .preemptive()
                           .basic(applicationUsername, applicationPassword)
                           .expect()
                           .log()
                           .ifError()
                           .statusCode(401)
                           .get(url);

        LOGGER.info("Response Received = " + response.getStatusCode());

        // JsonPath jsonData = response.jsonPath();
        // String errorCode =
        // jsonData.get("requestError.serviceException.messageId");
        // String errorMessage =
        // jsonData.get("requestError.serviceException.variables[0]");
        // LOGGER.info("Response Received = " + response.getStatusCode());
        // LOGGER.info("Error Code = " + errorCode);
        // LOGGER.info("Error Message = " + errorMessage);

        endTest(test);
    }

    public void start() {
        if (!initialised) {
            RestAssured.baseURI = baseURI;
            RestAssured.port = port;
            RestAssured.basePath = "";
            RestAssured.urlEncodingEnabled = true;
            initialised = true;
        }
    }

    private void startTest(String test) {
        if (lastTest != null) {
            LOGGER.info("Ending the test: '" + lastTest + "' premeturely...");
        }
        LOGGER.info("Starting the test: '" + test + "'");
    }

    private void endTest(String test) {
        LOGGER.info("End of test: '" + test + "'");
    }

    private String replace(String sessionSubscriptionURL, String apiVersion, String userID) {
        return sessionSubscriptionURL.replace("{apiVersion}", apiVersion).replace("{userID}", userID);
    }

    private String requestDataClean(String sessionRequestData, String userID, String callback) {
        String clean = sessionRequestData.replace("{CALLBACK}", callback).replace("{USERID}", userID);
        return clean;
    }

    public String cleanPrefix(String userID) {
        return userID.replaceAll("tel\\:\\+", "").replaceAll("tel\\:", "").replaceAll("\\+", "").replaceAll("\\:", "");
    }

    public void setProxyURL(String proxyURL) {
        this.proxyURL = proxyURL;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setApplicationUsername(String applicationUsername) {
        this.applicationUsername = applicationUsername;
    }

    public void setApplicationPassword(String applicationPassword) {
        this.applicationPassword = applicationPassword;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public void setInvalidUser(String invalidUser) {
        this.invalidUser = invalidUser;
    }

    public void setSessionSubscriptionURL(String sessionSubscriptionURL) {
        this.sessionSubscriptionURL = sessionSubscriptionURL;
    }

    public void setValidLongPoll(String validLongPoll) {
        this.validLongPoll = validLongPoll;
    }

    public void setNotificationChannelURL(String notificationChannelURL) {
        this.notificationChannelURL = notificationChannelURL;
    }

    public void setSessionRequestData(String sessionRequestData) {
        this.sessionRequestData = sessionRequestData;
    }

    public void setUrlSplit(String urlSplit) {
        this.urlSplit = urlSplit;
    }
}
