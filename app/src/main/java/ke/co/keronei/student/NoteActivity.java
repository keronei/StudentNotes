package ke.co.keronei.student;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import static ke.co.keronei.student.StudentNoteDataBaseContract.CourseInfoEntry;
import static ke.co.keronei.student.StudentNoteDataBaseContract.NoteInfoEntry;

public class NoteActivity extends  AppCompatActivity {

    public  static final String NOTE_ID = "ke.co.keronei.student.NOTE_ID";

    public  static final String ORIGINAL_NOTE_ID = "ke.co.keronei.student.ORIGINAL_NOTE_ID";
    public  static final String ORIGINAL_NOTE_HEAD = "ke.co.keronei.student.ORIGINAL_NOTE_HEAD";

    public  static final String ORIGINAL_NOTE_CONTENT = "ke.co.keronei.student.ORIGINAL_NOTE_CONTENT";

    public static final int ID_DEFAULT = -1;
    public static final int LOADER_ID = 0;
    private NoteInfo note;
    private boolean isNewNoteState;
    private Spinner notesSpinner;
    private EditText noteHead;
    private EditText notecontent;
    private int noteid;
    private boolean isCancellingNote;
    private String originalNoteId;
    private String originalNoteHead;
    private String originalNoteContent;
    private int lastNoteIndex;
    public StudentNoteOpenHelper studentNoteOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseId;

    private int mNoteTitlePos;
    private int mActualNoteText;
    public int databaseId;
    private SimpleCursorAdapter simpleCursorAdapter;

    @Override
    protected void onDestroy() {
        studentNoteOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        studentNoteOpenHelper = new StudentNoteOpenHelper(this);

        notesSpinner = (Spinner) findViewById(R.id.spinner_note_selector);


        simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},new int[]{android.R.id.text1},0);

        simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        notesSpinner.setAdapter(simpleCursorAdapter);

        loadSomeCourseData();

        readDisplayStatevalues();
        if(savedInstanceState == null){
            saveOriginals();
        }else {
            restoreInstanceState(savedInstanceState);
        }


        noteHead = (EditText) findViewById(R.id.edit_note_head);

        notecontent = (EditText) findViewById(R.id.edit_note_content);

        if(!isNewNoteState)

          loadSomeNotesData();

    }

    private void loadSomeNotesData() {


        SQLiteDatabase db = studentNoteOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + " =?";

        String[] selectionArgs ={Integer.toString(databaseId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT

        };

       mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                null, null, null);

        mCourseId = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mActualNoteText = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();

    }

    public void updateNoteToDatabase(String courseId, String noteTitle, String noteText){
        String selection = NoteInfoEntry._ID + " = ? ";
        String[] selectionArgs = {Integer.toString(databaseId)};
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase sqLiteDatabase = studentNoteOpenHelper.getWritableDatabase();
        sqLiteDatabase.update(NoteInfoEntry.TABLE_NAME,contentValues, selection, selectionArgs);
    }


    private void loadSomeCourseData() {
        SQLiteDatabase db = studentNoteOpenHelper.getReadableDatabase();
        String[] columns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, columns, null, null,
                null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        simpleCursorAdapter.changeCursor(cursor);
    }


    private void restoreInstanceState(Bundle savedInstanceState) {
        originalNoteId = savedInstanceState.getString(ORIGINAL_NOTE_ID);
        originalNoteHead = savedInstanceState.getString(ORIGINAL_NOTE_HEAD);
        originalNoteContent = savedInstanceState.getString(ORIGINAL_NOTE_CONTENT);
    }

    private void saveOriginals() {
        if(isNewNoteState)
            return;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ORIGINAL_NOTE_ID, originalNoteId);
        outState.putString(ORIGINAL_NOTE_HEAD, originalNoteHead);
        outState.putString(ORIGINAL_NOTE_CONTENT, originalNoteContent);
    }


    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseId);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteActualText = mNoteCursor.getString(mActualNoteText);


        int noteIndex = getIndexOf(courseId);

        notesSpinner.setSelection(noteIndex);

        noteHead.setText(noteTitle);

        notecontent.setText(noteActualText);
    }

    private int getIndexOf(String courseId) {
        Cursor cursor = simpleCursorAdapter.getCursor();

        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        int currentPosOfCourse = 0;

        Boolean isMoreValues = cursor.moveToFirst();

        while (isMoreValues){
            String courseID = cursor.getString(courseIdPos);

            if(courseID.equals(courseId))
                break;
            ++currentPosOfCourse;
            isMoreValues = cursor.moveToNext();

        }
        return  currentPosOfCourse;
    }

    private void readDisplayStatevalues() {
       Intent intent = getIntent();

        databaseId = intent.getIntExtra(NOTE_ID, ID_DEFAULT);

        isNewNoteState = databaseId == ID_DEFAULT;


        if(isNewNoteState){
            createNewNote();
        }
            //note = DataManager.getInstance().getNotes().get(databaseId);

    }

    private void createNewNote() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase db = studentNoteOpenHelper.getWritableDatabase();
        databaseId = (int)db.insert(NoteInfoEntry.TABLE_NAME, null, contentValues);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isCancellingNote){
            if(isNewNoteState) {
                removePlaceHolderFromDatabase();
            }else {
                storePreviousCourseValues();
            }
        }else {
            SaveNote();

        }

    }

    private void removePlaceHolderFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ? ";
        final String[] selectionArgs = {Integer.toString(databaseId)};

        AsyncTask delete = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase sqLiteDatabase = studentNoteOpenHelper.getWritableDatabase();
                sqLiteDatabase.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };

        delete.execute();

    }

    private void storePreviousCourseValues() {
       // CourseInfo course = DataManager.getInstance().getCourse(originalNoteId);
       /* note.setCourse(course);
        note.setTitle(originalNoteHead);
        note.setText(originalNoteContent );
    */}

    private void SaveNote() {
        String courseID = getTheCourseId();

        String noteHeadString = noteHead.getText().toString();
        String actualNoteText = notecontent.getText().toString();

        updateNoteToDatabase(courseID, noteHeadString, actualNoteText);

    }

    private String getTheCourseId() {
        int selectedItemPos = notesSpinner.getSelectedItemPosition();
        Cursor cursor = simpleCursorAdapter.getCursor();
        cursor.moveToPosition(selectedItemPos);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        return cursor.getString(courseIdPos);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.send_mail) {
            
            sendMail();
            return true;
        }
        else if (id == R.id.discard_note){
            isCancellingNote = true;
            finish();

        }else if(id == R.id.move_to_next){
            moveToNextNote();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.move_to_next);
        lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(noteid < lastNoteIndex);
            return super.onPrepareOptionsMenu(menu);

    }

    private void moveToNextNote() {
        SaveNote();
        ++noteid;
        saveOriginals();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendMail() {
        CourseInfo course = (CourseInfo) notesSpinner.getSelectedItem();

        String subject = noteHead.getText().toString();

        String content ="Hey! \n Doing some interesting Android course, Check " +course.getTitle()+"\n Here are my notes; " +
        notecontent.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        startActivity(intent);



    }


}
