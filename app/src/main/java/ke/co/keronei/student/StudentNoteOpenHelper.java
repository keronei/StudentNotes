package ke.co.keronei.student;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ke.co.keronei.student.StudentNoteDataBaseContract.CourseInfoEntry;
import static ke.co.keronei.student.StudentNoteDataBaseContract.NoteInfoEntry;

public class StudentNoteOpenHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "Studentdb.db";
    public static final int DB_VERSION = 2;
    public StudentNoteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(CourseInfoEntry.CREATE_INDEX1);
        db.execSQL(NoteInfoEntry.CREATE_INDEX1);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        //worker.insertCourses();
        //worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < DB_VERSION) {
            db.execSQL(CourseInfoEntry.CREATE_INDEX1);
            db.execSQL(NoteInfoEntry.CREATE_INDEX1);

        }
    }
}
