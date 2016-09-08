package com.example.heitorcolangelo.espressotests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.heitorcolangelo.espressotests.network.Api;
import com.example.heitorcolangelo.espressotests.network.UsersApi;
import com.example.heitorcolangelo.espressotests.ui.activity.MainActivity;
import com.example.heitorcolangelo.espressotests.ui.activity.UserDetailsActivity;

import net.vidageek.mirror.dsl.Mirror;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Thiago Braga on 9/8/16.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private MockWebServer server;


    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        this.server = new MockWebServer();
        this.server.start();
        setupServerUrl();

    }

    @After
    public void tearDown() throws IOException {
        this.server.shutdown();
    }

    private void setupServerUrl() {
        String url = server.url("/").toString();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        final UsersApi usersApi = UsersApi.getInstance();

        final Api api = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(UsersApi.GSON))
                .client(client)
                .build()
                .create(Api.class);

        setField(usersApi, "api", api);
    }

    private void setField(Object target, String fieldName, Object value) {
        new Mirror()
                .on(target)
                .set()
                .field(fieldName)
                .withValue(value);
    }

    @Test
    public void whenResultIsOk_shouldDisplayListWithUsers(){

        this.server.enqueue(new MockResponse().setResponseCode(200).setBody(Mocks.SUCCESS));
        mActivityTestRule.launchActivity(new Intent());
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        onView(allOf( withId(R.id.user_view_image),hasSibling(withText("Eddie Dunn")))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.user_view_name),hasSibling(withText("Eddie Dunn")))).check(matches(isDisplayed()));

    }

    @Test
    public void whenResultIs500_shouldDisplayDialogError(){

        this.server.enqueue(new MockResponse().setResponseCode(400).setBody(Mocks.ERROR));
        mActivityTestRule.launchActivity(new Intent());
        onView(withId(R.id.error_view)).check(matches(isDisplayed()));


    }

    @Test
    public void whenClickOnItemList_shouldStartUserDetailsActivity_withExtra(){

        this.server.enqueue(new MockResponse().setResponseCode(200).setBody(Mocks.SUCCESS));
        mActivityTestRule.launchActivity(new Intent());
        Intents.init();
        Matcher<Intent> matcher = allOf(
                hasComponent(UserDetailsActivity.class.getCanonicalName()),
                hasExtraWithKey(UserDetailsActivity.CLICKED_USER)
        );

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK,null);

        intending(matcher).respondWith(result);

        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition(0,click()));

        intended(matcher);

        Intents.release();



    }


}
