package ke.co.keronei.student;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import android.support.test.espresso.Espresso.*;
import android.support.test.espresso.matcher.ViewMatchers.*;
import android.support.test.espresso.action.ViewActions.*;

import javax.annotation.MatchesPattern;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class NoteActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mNoteActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void CreateNewNote(){
        ViewInteraction fab = onView(withId(R.id.fab));

        fab.perform(click());
        String test_note = "Test Note";
        onView(withId(R.id.edit_note_head)).perform(typeText(test_note));
        String stringToBeTyped = "This is the test body of the note";
        onView(withId(R.id.edit_note_content)).perform(typeText(stringToBeTyped),
                closeSoftKeyboard());
        onView(withId(R.id.edit_note_head)).check(matches(withText(test_note)));

        onView(withId(R.id.edit_note_content)).check(matches(withText(stringToBeTyped)));
    }

}