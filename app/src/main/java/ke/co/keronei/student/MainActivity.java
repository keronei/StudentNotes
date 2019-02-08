package ke.co.keronei.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

import static ke.co.keronei.student.StudentNoteDataBaseContract.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NoteRecylerAdapter mNoteRecycleAdapter;
    private RecyclerView recyclerDisplayContent;
    private LinearLayoutManager mNotesLayout;
    private CourseRecylerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private SharedPreferences sharedPreferences;

    private StudentNoteOpenHelper mDbOpenHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        mDbOpenHelper = new StudentNoteOpenHelper(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadNoteData();
        updateUserDetail();
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] NotesColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,

                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry._ID};

        String OrderClient = NoteInfoEntry.COLUMN_COURSE_ID +" , "+ NoteInfoEntry.COLUMN_NOTE_TITLE;
        Cursor NotesCursor = db.query(NoteInfoEntry.TABLE_NAME, NotesColumns, null,
                null, null, null, OrderClient);

        mNoteRecycleAdapter.changeCursor(NotesCursor);
    }

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
        MenuItem menuItem = menu.findItem(id).setChecked(true);
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

    private void handleAction(String message) {

    }
}
