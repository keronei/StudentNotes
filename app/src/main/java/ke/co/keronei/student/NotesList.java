package ke.co.keronei.student;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

public class NotesList extends AppCompatActivity {
    private NoteRecylerAdapter mNoteRecycleAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(NotesList.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        initializeDisplayContent();

    }


    @Override
    protected void onPostResume() {
            super.onPostResume();
        mNoteRecycleAdapter.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {

    final RecyclerView recyclerNotes = (RecyclerView) findViewById(R.id.recycler_notes_list);
    final LinearLayoutManager notesLayout = new LinearLayoutManager(this);
     recyclerNotes.setLayoutManager(notesLayout);

     List<NoteInfo> mNotes = DataManager.getInstance().getNotes();

        mNoteRecycleAdapter = new NoteRecylerAdapter(this,mNotes);

     recyclerNotes.setAdapter(mNoteRecycleAdapter);

    }

}
