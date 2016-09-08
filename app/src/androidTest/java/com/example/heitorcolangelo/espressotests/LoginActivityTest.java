package com.example.heitorcolangelo.espressotests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.heitorcolangelo.espressotests.ui.activity.LoginActivity;
import com.example.heitorcolangelo.espressotests.ui.activity.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by Thiago Braga on 9/8/16.
 */

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivittyRule = new ActivityTestRule<>(LoginActivity.class,false,true);


    @Test
    public void whenActivityIsLaunched_shouldDisplayInitialState(){
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_username)).check(matches(isDisplayed()));
        onView(withId(R.id.login_image)).check(matches(isDisplayed()));

    }

    @Test
    public void whenPasswordIsEmpty_andClickOnLoginButton_shouldDisplayDialog() {
        testEmptyFieldState(R.id.login_username);
    }

    @Test
    public void whenUserNameIsEmpty_andClickOnLoginButton_shouldDisplayDialog() {
        testEmptyFieldState(R.id.login_password);
    }

    @Test
    public void whenBothfieldsAreEmptys_shouldDisplayDialog(){

        onView(withId(R.id.login_username)).perform(typeText(""), ViewActions.closeSoftKeyboard());
        closeSoftKeyboard();
        onView(withId(R.id.login_password)).perform(typeText(""), ViewActions.closeSoftKeyboard());
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
        onView(withText(R.string.validation_message)).check(matches(isDisplayed()));
        onView(withText(R.string.ok)).perform(click());


    }

    private void testEmptyFieldState(int notEmptyFieldId){
        onView(withId(notEmptyFieldId)).perform(typeText("defaultText"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.login_button)).perform(click());
        onView(withText(R.string.validation_message)).check(matches(isDisplayed()));
        onView(withText(R.string.ok)).perform(click());
    }

    @Test
    public void whenBothFieldsAreFilled_andClickOnLoginButton_shouldOpenMainActivity(){

        Intents.init();
        onView(withId(R.id.login_username)).perform(typeText("thiagoBraga"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("123456"), ViewActions.closeSoftKeyboard());

        Matcher<Intent> matcher = hasComponent(MainActivity.class.getCanonicalName());
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK,null);


        intending(matcher).respondWith(result);

        onView(withId(R.id.login_button)).perform(click());
        intended(matcher);

        Intents.release();
    }

}
