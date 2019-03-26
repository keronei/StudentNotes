package ke.co.keronei.student;

import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static ke.co.keronei.student.StudentProviderContract.Notes;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ID = 21;
    private NoteRecyclerAdapter mNoteRecycleAdapter;
    private RecyclerView recyclerDisplayContent;
    private LinearLayoutManager mNotesLayout;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private SharedPreferences sharedPreferences;

    private StudentNoteOpenHelper mDbOpenHelper;
    private static final int UPLOADER_JOB_ID = 1;
    private List<CourseInfo> mCourses;

    private int mActualId;
    private isDisplaying mIsDisplayingCourses;
    private isDisplaying mIsDisplayingNotes;
    private isDisplaying mCheckWhatsDisplayed;
    private int mMCourseId;

    private enum isDisplaying {
        IS_DISPLAYING_NOTES,
        IS_DISPLAYING_COURSES
    }

    String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDbOpenHelper = new StudentNoteOpenHelper(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIsDisplayingCourses = isDisplaying.IS_DISPLAYING_COURSES;

        mIsDisplayingNotes = isDisplaying.IS_DISPLAYING_NOTES;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

            switch (mCheckWhatsDisplayed) {
                case IS_DISPLAYING_NOTES:
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    startActivity(intent);
                    break;
                case IS_DISPLAYING_COURSES:

                    CreateNewCourse();
                    break;
            }


        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initializeDisplayContent();

        //enableStrictMode();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder,
                                 final int swipeDirection) {
                final View view = (View) findViewById(R.id.recycler_with_drawer);
                final int actualIdOnAdapter = viewHolder.getAdapterPosition();

                if (mCheckWhatsDisplayed.equals(isDisplaying.IS_DISPLAYING_NOTES)) {

                    //Confirm action:

                    SweetAlertDialog warning = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                    warning
                            .setTitleText("Delete")
                            .setContentText("This note will be permanently removed!")
                            .setConfirmText("Yes, remove it!")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                        //sure to remove-- proceed
                                        sweetAlertDialog.dismissWithAnimation();
                                        //deleteItemOnSwipe(actualIdOnAdapter);
                                        mActualId = mNoteRecycleAdapter.CheckNoteIdToRemove(actualIdOnAdapter);

                                        //then
                                        int number = deleteItemOnSwipe(0, mActualId);
                                        loadNoteData();

                                        Snackbar sbar = Snackbar.make(view, "Removed  " + String.valueOf(number),
                                                Snackbar.LENGTH_LONG);
                                        sbar.setAction("Undo", view1 -> putNoteBack());

                                        sbar.show();

                                    }
                            ).show();
                    warning.setOnDismissListener(dialog -> loadNoteData());
                } else {
                    SweetAlertDialog warning = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                    warning
                            .setTitleText("Delete")
                            .setContentText("This will remove this course!")
                            .setConfirmText("Proceed")
                            .setConfirmClickListener(sweetAlertDialog -> {
                                        //sure to remove-- proceed
                                        sweetAlertDialog.dismissWithAnimation();

                                        //keep in mind that the course having notes under it will not be removed
                                        //foreign key constraint

                                        mMCourseId = mCourseRecyclerAdapter.CheckCourseIdToRemove(actualIdOnAdapter);


                                        //then

                                        int number = deleteItemOnSwipe(1, mMCourseId);
                                        checkCoursesAgain();


                                        Snackbar sbar = Snackbar.make(view, "Removed  " + number,
                                                Snackbar.LENGTH_LONG);
                                        sbar.setAction("Undo", view1 -> putNoteBack());

                                        sbar.show();

                                    }
                            ).show();
                    warning.setOnDismissListener(dialog -> checkCoursesAgain()


                    );
                }

            }
        }).attachToRecyclerView(recyclerDisplayContent);

    }

    public void CreateNewCourse() {
        final Dialog addNewUnit = new Dialog(MainActivity.this);
        addNewUnit.setContentView(R.layout.add_course);
        addNewUnit.setTitle("Create Course");
        Objects.requireNonNull(addNewUnit.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageButton addAndDismiss = addNewUnit.findViewById(R.id.create_and_dismiss);
        EditText unit_name = addNewUnit.findViewById(R.id.new_unit);


        addAndDismiss.setOnClickListener((v) -> {
            if (unit_name.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Dismissing, Provide a name to create", Toast.LENGTH_LONG).show();
            } else {


                AsyncTask<ContentValues, Void, Uri> addCourse = new AsyncTask<ContentValues, Void, Uri>() {
                    @Override
                    protected Uri doInBackground(ContentValues... contentValues) {

                        ContentValues cv = contentValues[0];
                        Uri rowUri = getContentResolver().insert(StudentProviderContract.Courses.CONTENT_URI, cv);

                        return rowUri;
                    }

                    @Override
                    protected void onPostExecute(Uri row_uri) {

                        Log.d(TAG, row_uri.toString());

                    }
                };
                ContentValues contentValues = new ContentValues();
                contentValues.put(StudentProviderContract.Courses.COLUMN_COURSE_TITLE, unit_name.getText().toString());


                addCourse.execute(contentValues);
            }


            addNewUnit.dismiss();
        });

        addNewUnit.show();
    }


    private void putNoteBack() {

    }

    public Integer deleteItemOnSwipe(int CourseOrNote, int Noteid_CourseID) {
        Uri uri = null;

        switch (CourseOrNote) {
            case 0:
                uri = ContentUris.withAppendedId(Notes.CONTENT_URI, Noteid_CourseID);
                break;
            case 1:
                uri = ContentUris.withAppendedId(StudentProviderContract.Courses.CONTENT_URI, Noteid_CourseID);
                break;

        }
        final int[] rowsAffected = new int[1];


        Uri finalUri = uri;

        if (finalUri != null) {

            AsyncTask<Integer, Void, Integer> remove = new AsyncTask<Integer, Void, Integer>() {
                @Override
                protected Integer doInBackground(Integer... voids) {


                    int numberAffected = getContentResolver().delete(finalUri, null, null);
                    return numberAffected;
                }

                @Override
                protected void onPostExecute(Integer rows) {


                    rowsAffected[0] = rows;
                }
            };
            remove.execute();
        }

        return rowsAffected[0];


    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy new_policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(new_policy);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //getSupportLoaderManager().initLoader(LOADER_ID,null, this);
        loadNoteData();
        updateUserDetail();
        //mNoteRecycleAdapter.notifyDataSetChanged();
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] NotesColumns = {
                StudentNoteDataBaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE,
                StudentNoteDataBaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE,
                StudentNoteDataBaseContract.NoteInfoEntry.getQualifiedName(
                        StudentNoteDataBaseContract.NoteInfoEntry._ID)};

        String joinedTables = StudentNoteDataBaseContract.NoteInfoEntry.TABLE_NAME + " JOIN "
                + StudentNoteDataBaseContract.CourseInfoEntry.TABLE_NAME +
                " ON " + StudentNoteDataBaseContract.NoteInfoEntry.getQualifiedName(
                StudentNoteDataBaseContract.NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                StudentNoteDataBaseContract.CourseInfoEntry.getQualifiedName(
                        StudentNoteDataBaseContract.NoteInfoEntry.COLUMN_COURSE_ID);

        String OrderClient = StudentNoteDataBaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE + " , "
                + StudentNoteDataBaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE;

        AsyncTask<Void, Void, Cursor> fetchFromDb = new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                Cursor NotesCursor = db.query(joinedTables, NotesColumns, null,
                        null, null, null, OrderClient);
                return NotesCursor;
            }

            @Override
            protected void onPostExecute(Cursor notes) {

                mNoteRecycleAdapter.changeCursor(notes);
            }
        };

        fetchFromDb.execute();


    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();

    }

    private void updateUserDetail() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View viewHeader = navigationView.getHeaderView(0);
        TextView userDisplayName = viewHeader.findViewById(R.id.user_display_name);
        TextView userDisplayEmail = viewHeader.findViewById(R.id.user_display_mail);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String name = sharedPreferences.getString("user_name_display", "");
        String mail = sharedPreferences.getString("user_mail", "");

        userDisplayName.setText(name);
        userDisplayEmail.setText(mail);
    }

    private void initializeDisplayContent() {

        DataManager.loadFromDatabase(mDbOpenHelper);
        recyclerDisplayContent = findViewById(R.id.recycler_with_drawer);
        mNotesLayout = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_span_count));

        mNoteRecycleAdapter = new NoteRecyclerAdapter(this, null);


        mCourses = DataManager.getInstance().getCourses();

        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, mCourses);


        displayNotes();


    }


    private void checkCoursesAgain() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        List<CourseInfo> newContent = DataManager.getInstance().getCourses();

        mCourseRecyclerAdapter.RefreshContent(newContent);
        displayCourses();
        loadNoteData();
    }

    private void displayCourses() {
        recyclerDisplayContent.setLayoutManager(mCoursesLayoutManager);
        recyclerDisplayContent.setAdapter(mCourseRecyclerAdapter);
        selectMenuItem(R.id.nav_coursers);
        mCheckWhatsDisplayed = mIsDisplayingCourses;
    }

    private void displayNotes() {
        recyclerDisplayContent.setLayoutManager(mNotesLayout);
        recyclerDisplayContent.setAdapter(mNoteRecycleAdapter);

        selectMenuItem(R.id.nav_notes);
        mCheckWhatsDisplayed = mIsDisplayingNotes;
    }

    private void selectMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.backup_note) {
            startBackupService();
        } else if (id == R.id.upload_note) {
            startUploadJob();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startUploadJob() {
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putString(StudentNoteUploaderService.EXTRA_DATA_URI,
                Notes.CONTENT_URI.toString());

        ComponentName componentName = new ComponentName(this, StudentNoteUploaderService.class);

        JobInfo jobInfo = new JobInfo.Builder(UPLOADER_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(persistableBundle)
                .build();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_coursers) {
            displayCourses();

        } else if (id == R.id.nav_share) {
            handleShare();

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startBackupService() {
        Intent intent = new Intent(this, BackupService.class);
        intent.putExtra(BackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    private void handleShare() {
        View view = (View) findViewById(R.id.recycler_with_drawer);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Snackbar.make(view, "Share via --" +
                        sharedPreferences.getString("favourite_places", ""),
                Snackbar.LENGTH_LONG).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = null;
        if (i == LOADER_ID) {
            final String[] noteColumns = {
                    Notes._ID,
                    Notes.COLUMN_NOTE_TITLE,
                    Notes.COLUMN_COURSE_TITLE
            };
            final String noteOrderBy = Notes.COLUMN_COURSE_TITLE +
                    "," + Notes.COLUMN_NOTE_TITLE;

            loader = new CursorLoader(this, Notes.EXPANDED_CONTENT_URI, noteColumns,
                    null, null, noteOrderBy);

        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == LOADER_ID) {
            mNoteRecycleAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == LOADER_ID) {
            mNoteRecycleAdapter.changeCursor(null);
        }
    }


}
