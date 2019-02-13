package ke.co.keronei.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import static ke.co.keronei.student.StudentProviderContract.Notes;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ID = 21;
    private NoteRecylerAdapter mNoteRecycleAdapter;
    private RecyclerView recyclerDisplayContent;
    private LinearLayoutManager mNotesLayout;
    private CourseRecylerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private SharedPreferences sharedPreferences;

    private StudentNoteOpenHelper mDbOpenHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDbOpenHelper = new StudentNoteOpenHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        PreferenceManager.setDefaultValues(this,R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_notification, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(LOADER_ID,null, this);
        //loadNoteData();
        updateUserDetail();
        //mNoteRecycleAdapter.notifyDataSetChanged();
    }

   /* private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] NotesColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.getQualifiedName(NoteInfoEntry._ID)};

        String joinedTables  = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                " ON " + NoteInfoEntry.getQualifiedName(NoteInfoEntry.COLUMN_COURSE_ID) + " = "+
                CourseInfoEntry.getQualifiedName(NoteInfoEntry.COLUMN_COURSE_ID);

        String OrderClient = CourseInfoEntry.COLUMN_COURSE_TITLE +" , "+ NoteInfoEntry.COLUMN_NOTE_TITLE;

        Cursor NotesCursor = db.query(joinedTables, NotesColumns, null,
                null, null, null, OrderClient);

        mNoteRecycleAdapter.changeCursor(NotesCursor);
    }*/

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();

    }

    private void updateUserDetail() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View viewHeader = navigationView.getHeaderView(0);
        TextView userDisplayName = (TextView) viewHeader.findViewById(R.id.user_display_name);
        TextView userDisplayEmail = (TextView)viewHeader.findViewById(R.id.user_display_mail);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String name = sharedPreferences.getString("user_name_display", "");
        String mail = sharedPreferences.getString("user_mail", "");

        userDisplayName.setText(name);
        userDisplayEmail.setText(mail);
    }

    private void initializeDisplayContent() {

        DataManager.loadFromDatabase(mDbOpenHelper);
        recyclerDisplayContent = (RecyclerView) findViewById(R.id.recycler_with_drawer);
        mNotesLayout = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_span_count));

        mNoteRecycleAdapter = new NoteRecylerAdapter(this, null);

        List<CourseInfo> mCourses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecylerAdapter(this,mCourses);

        displayNotes();


    }

    private void displayCourses() {
        recyclerDisplayContent.setLayoutManager(mCoursesLayoutManager);
        recyclerDisplayContent.setAdapter(mCourseRecyclerAdapter);
        selectMenuItem(R.id.nav_coursers);
    }

    private void displayNotes() {
        recyclerDisplayContent.setLayoutManager(mNotesLayout);
        recyclerDisplayContent.setAdapter(mNoteRecycleAdapter);

        selectMenuItem(R.id.nav_notes);
    }

    private void selectMenuItem(int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        }

        return super.onOptionsItemSelected(item);
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

    private void handleShare() {
        View view = (View)findViewById(R.id.recycler_with_drawer);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Snackbar.make(view, "Share via --"+
                sharedPreferences.getString("favourite_places",""), Snackbar.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = null;
        if(i == LOADER_ID) {
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
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if(loader.getId() == LOADER_ID)  {
            mNoteRecycleAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        if(loader.getId() == LOADER_ID)  {
            mNoteRecycleAdapter.changeCursor(null);
        }
    }
}
