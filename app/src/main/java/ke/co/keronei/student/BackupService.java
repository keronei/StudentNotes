package ke.co.keronei.student;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 *
 */
public class BackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "ke.co.keronei.student.extra.COURSE_ID";

    public BackupService() {
        super("BackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //this service simulates a backup

        if (intent != null){
            String courseId = intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this, courseId);
        }
    }


}
