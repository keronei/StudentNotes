package ke.co.keronei.student;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NoteRecylerAdapter extends  RecyclerView.Adapter<NoteRecylerAdapter.ViewHolder>  {
    private final Context theContext;
    private final LayoutInflater layoutInflater;
    private  final List<NoteInfo> mNotesList;

    public NoteRecylerAdapter(Context theContext, List<NoteInfo> mNotesList) {
        this.theContext = theContext;
        layoutInflater = LayoutInflater.from(theContext);
        this.mNotesList = mNotesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = layoutInflater.inflate(R.layout.item_in_list, viewGroup, false);
        return new ViewHolder(itemView );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        NoteInfo mNotes = mNotesList.get(i);

        viewHolder.mCourseName.setText(mNotes.getCourse().getTitle());
        viewHolder.mNoteHead.setText(mNotes.getTitle()) ;
        viewHolder.mCurrentposition = i;
    }

    @Override
    public int getItemCount() {
        return mNotesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mCourseName;
        public final TextView mNoteHead;
        public int mCurrentposition;


        public ViewHolder(View itemView) {

            super(itemView);

            mCourseName = (TextView) itemView.findViewById(R.id.textview_head);
            mNoteHead = (TextView) itemView.findViewById(R.id.textview_note_content);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(theContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION, mCurrentposition);
                    theContext.startActivity(intent);
                }
            });

        }
    }
}
