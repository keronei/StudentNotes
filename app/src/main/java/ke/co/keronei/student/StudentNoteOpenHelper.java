package ke.co.keronei.student;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentNoteOpenHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "Studentdb.db";
    public static final int DB_VERSION = 1;
    public StudentNoteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StudentNoteDataBaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(StudentNoteDataBaseContract.NoteInfoEntry.SQL_CREATE_TABLE);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
