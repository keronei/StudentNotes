package ke.co.keronei.student;

import android.content.Context;
import android.content.Intent;

public class EventBroadcastHelper {
    public static final String ACTION_COURSE_EVENT = "ke.co.keronei.student.action.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID = "ke.co.keronei.student.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "ke.co.keronei.student.extra.COURSE_MESSAGE";

    public static void sendEventBroadcast(Context context, String courseId, String message){
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE, message);

        context.sendBroadcast(intent);
    }
}
