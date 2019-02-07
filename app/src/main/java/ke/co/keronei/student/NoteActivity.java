package ke.co.keronei.student;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    public  static final String NOTE_POSITION = "ke.co.keronei.student.NOTE_POSITION";

    public  static final String ORIGINAL_NOTE_ID = "ke.co.keronei.student.ORIGINAL_NOTE_ID";
    public  static final String ORIGINAL_NOTE_HEAD = "ke.co.keronei.student.ORIGINAL_NOTE_HEAD";

    public  static final String ORIGINAL_NOTE_CONTENT = "ke.co.keronei.student.ORIGINAL_NOTE_CONTENT";

    public static final int POSITION_DEFAULT = -1;
    private NoteInfo note;
    private boolean isNewNoteState;
    private Spinner notesSpinner;
    private EditText noteHead;
    private EditText notecontent;
    private int notePosition;
    private boolean isCancellingNote;
    private String originalNoteId;
    private String originalNoteHead;
    private String originalNoteContent;
    private int lastNoteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notesSpinner = (Spinner) findViewById(R.id.spinner_note_selector);

        List<CourseInfo> coursesList = DataManager.getInstance().getCourses();

        ArrayAdapter<CourseInfo> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, coursesList);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        notesSpinner.setAdapter(arrayAdapter);

        readDisplayStatevalues();
        if(savedInstanceState == null){
            saveOriginals();
        }else {
            restoreInstanceState(savedInstanceState);
        }



        noteHead = (EditText) findViewById(R.id.edit_note_head);

        notecontent = (EditText) findViewById(R.id.edit_note_content);

        if(!isNewNoteState)

            displayNote(notesSpinner, noteHead, notecontent);

    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        originalNoteId = savedInstanceState.getString(ORIGINAL_NOTE_ID);
        originalNoteHead = savedInstanceState.getString(ORIGINAL_NOTE_HEAD);
        originalNoteContent = savedInstanceState.getString(ORIGINAL_NOTE_CONTENT);
    }

    private void saveOriginals() {
        if(isNewNoteState)
            return;
        originalNoteId = note.getCourse().getCourseId();
        originalNoteHead = note.getTitle();
        originalNoteContent = note.getText();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ORIGINAL_NOTE_ID, originalNoteId);
        outState.putString(ORIGINAL_NOTE_HEAD, originalNoteHead);
        outState.putString(ORIGINAL_NOTE_CONTENT, originalNoteContent);
    }


    private void displayNote(Spinner notesSpinner, EditText noteHead, EditText notecontent) {
        List<CourseInfo> list_of_notes = DataManager.getInstance().getCourses();

        int noteIndex = list_of_notes.indexOf(note.getCourse());

        notesSpinner.setSelection(noteIndex);

        noteHead.setText(note.getTitle());

        notecontent.setText(note.getText());
    }

    private void readDisplayStatevalues() {
       Intent intent = getIntent();

        int Position  = intent.getIntExtra(NOTE_POSITION, POSITION_DEFAULT);

        isNewNoteState = Position == POSITION_DEFAULT;

        if(isNewNoteState){
            createNewNote();
        }else {
            note = DataManager.getInstance().getNotes().get(Position);
        }
    }

    private void createNewNote() {

        DataManager datamng = DataManager.getInstance();

        notePosition = datamng.createNewNote();

        note = datamng.getNotes().get(notePosition);
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
                DataManager.getInstance().removeNote(notePosition);
            }else {
                storePreviousCourseValues();
            }
        }else {
            SaveNote();

        }

    }

    private void storePreviousCourseValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteId);
        note.setCourse(course);
        note.setTitle(originalNoteHead);
        note.setText(originalNoteContent );
    }

    private void SaveNote() {
        note.setCourse((CourseInfo)notesSpinner.getSelectedItem());
        note.setTitle(noteHead.getText().toString());
        note.setText(notecontent.getText().toString());

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
        item.setEnabled(notePosition < lastNoteIndex);
            return super.onPrepareOptionsMenu(menu);

    }

    private void moveToNextNote() {
        SaveNote();
        ++notePosition;

        note = DataManager.getInstance().getNotes().get(notePosition);
        saveOriginals();
        displayNote(notesSpinner, noteHead, notecontent);
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
