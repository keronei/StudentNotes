package ke.co.keronei.student;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class StudentNoteUploaderService extends JobService {
    public static final String EXTRA_DATA_URI = "ke.co.keronei.student.extras.DATA_URI";
    private NoteUploader noteUploader;

    public StudentNoteUploaderService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters, Void, Void> uploader = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... jobParametersGenius) {
                JobParameters jobParameters1 = jobParametersGenius[0];
                String StringdataUri = jobParameters1.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(StringdataUri);
                noteUploader.doUpload(dataUri);
                if(!noteUploader.isCanceled())
                    jobFinished(jobParameters1, false);
                return null;
            }
        };
        noteUploader = new NoteUploader(this);
        uploader.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        noteUploader.cancel();
        return true;
    }

}
