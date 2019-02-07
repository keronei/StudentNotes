package ke.co.keronei.student;
import org.junit.Rule;
import org.junit.Test;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.Matchers.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;


public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule(MainActivity.class);

    @Test
    public void testThroughnext() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.recycler_with_drawer)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        final List<NoteInfo> notes = DataManager.getInstance().getNotes();

        for (int noteSelectedIndex = 0; noteSelectedIndex < notes.size(); noteSelectedIndex++ ) {
        //int noteSelectedIndex = 1;



                    NoteInfo selectedNote = notes.get(noteSelectedIndex);

                    onView(withId(R.id.spinner_note_selector)).check(
                            matches(withSpinnerText(selectedNote.getCourse().getTitle()))
                    );
                    onView(withId(R.id.edit_note_head)).check(
                            matches(withText(selectedNote.getTitle()))
                    );
                    onView(withId(R.id.edit_note_content)).check(
                            matches(withText(selectedNote.getText()))
                    );

                    if(noteSelectedIndex < notes.size() - 1)
                    onView(allOf(withId(R.id.move_to_next), isEnabled())).perform(click());

        }

        onView(withId(R.id.move_to_next)).check(matches(not(isEnabled())));
    }

}

