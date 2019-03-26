package ke.co.keronei.student;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static ke.co.keronei.student.StudentNoteDataBaseContract.CourseInfoEntry;
import static ke.co.keronei.student.StudentNoteDataBaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {
    private final Context theContext;
    private final LayoutInflater layoutInflater;
    private Cursor mCursor;
    private int coursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    public NoteRecyclerAdapter(Context mcontext, Cursor cursor) {
        theContext = mcontext;
        mCursor = cursor;
        layoutInflater = LayoutInflater.from(theContext);


        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (mCursor == null)
            return;

        coursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);


    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();

        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    public int CheckNoteIdToRemove(int noteId){
        mCursor.moveToPosition(noteId);

        return mCursor.getInt(mIdPos);


    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = layoutInflater.inflate(R.layout.item_in_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        mCursor.moveToPosition(i);
        String courseTitle = mCursor.getString(coursePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int id = mCursor.getInt(mIdPos);

        viewHolder.mCourseName.setText(courseTitle);
        viewHolder.mNoteHead.setText(noteTitle);
        viewHolder.mNoteIdInDatabase = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mCourseName;
        public final TextView mNoteHead;
        public int mNoteIdInDatabase;


        public ViewHolder(final View itemView) {

            super(itemView);

            mCourseName = (TextView) itemView.findViewById(R.id.textview_head);
            mNoteHead = (TextView) itemView.findViewById(R.id.textview_note_content);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Snackbar.make(itemView,String.valueOf(mNoteIdInDatabase), Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(theContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mNoteIdInDatabase);
                    theContext.startActivity(intent);
                }
            });

        }


    }
}
