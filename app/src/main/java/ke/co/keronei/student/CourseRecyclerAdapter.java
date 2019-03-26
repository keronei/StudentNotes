package ke.co.keronei.student;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CourseRecylerAdapter extends  RecyclerView.Adapter<CourseRecylerAdapter.ViewHolder>  {
    private final Context theContext;
    private final LayoutInflater layoutInflater;
    private  final List<CourseInfo> mCoursesList;
    private CourseInfo mCourse;

    public CourseRecylerAdapter(Context context, List<CourseInfo> mNotesList) {
        theContext = context;
        layoutInflater = LayoutInflater.from(theContext);
        mCoursesList = mNotesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = layoutInflater.inflate(R.layout.courses_item_in_list, viewGroup, false);
        return new ViewHolder(itemView );
    }

    public int CheckCourseIdToRemove(int noteId){

         return mCoursesList.get(noteId).getCourseID();


    }

    public void RefreshContent(List<CourseInfo> courseInfo){
        mCoursesList.clear();

        mCoursesList.addAll(courseInfo);

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        mCourse = mCoursesList.get(i);

        viewHolder.mCourseName.setText(mCourse.getTitle());

        viewHolder.mCurrentposition = i;
    }

    @Override
    public int getItemCount() {
        return mCoursesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mCourseName;
        public int mCurrentposition;


        public ViewHolder(View itemView) {

            super(itemView);

            mCourseName = (TextView) itemView.findViewById(R.id.textview_head);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v,mCoursesList.get(mCurrentposition).getTitle(),Snackbar.LENGTH_LONG).show();
                }
            });

        }
    }
}
