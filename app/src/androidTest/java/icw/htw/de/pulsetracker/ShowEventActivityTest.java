package icw.htw.de.pulsetracker;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class ShowEventActivityTest extends ActivityInstrumentationTestCase2<ShowEventActivity> {

    private ShowEventActivity showEventActivity;

    public ShowEventActivityTest(){
        super(ShowEventActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        showEventActivity = getActivity();
    }

    @Test
    public void testActionBarClickShouldStartActivity(){
        onView(withId(R.id.action_graph)).perform(click());
        onView(withId(R.id.lineChartPulse)).check(matches(isDisplayed()));
    }

    @Test
    public void testActionBarIconForShowEventShouldBeInactive(){

        onView(withId(R.id.action_showevent)).check(matches(not(isEnabled())));

    }

//    @Test
//    public void testActionBarClickObShowEventIconShouldDoNothing(){
//
//    }

}
