package ke.co.keronei.student;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StudentNoteReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTE_TITLE = "ke.co.keronei.student.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "ke.co.keronei.student.extra.NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "ke.co.keronei.student.extra.NOTE_ID";

    @Override
    public void onReceive(Context context, Intent intent) {

        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);

        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);

        RemindNotesReview.notify(context, noteTitle, noteText, noteId );

    }
}
