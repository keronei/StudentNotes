package ke.co.keronei.student;

import android.net.Uri;
import android.provider.BaseColumns;

public final class StudentProviderContract {
    private StudentProviderContract(){}

    public static final String AUTHORITY = "ke.co.keronei.student.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://"+AUTHORITY);

    protected interface CoursesIdColumns{
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    protected  interface CoursesColumns{
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }
    protected interface NotesColumns {
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";

    }
    public static final class Courses implements CoursesColumns, BaseColumns, CoursesIdColumns {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);


    }

    public abstract  @interface ForeignKey{

    }

    public static final class Notes implements NotesColumns, BaseColumns, CoursesIdColumns, CoursesColumns {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

        //for joined tables
        public static final String PATH_EXPANDED = "notes_expanded";
        public static final Uri EXPANDED_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);



    }
}
