package ke.co.keronei.student;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import static ke.co.keronei.student.StudentNoteDataBaseContract.CourseInfoEntry;
import static ke.co.keronei.student.StudentNoteDataBaseContract.NoteInfoEntry;
import static ke.co.keronei.student.StudentProviderContract.AUTHORITY;
import static ke.co.keronei.student.StudentProviderContract.Courses;
import static ke.co.keronei.student.StudentProviderContract.CoursesIdColumns;
import static ke.co.keronei.student.StudentProviderContract.Notes;

public class studentProviderCorrected extends ContentProvider {
    private static final String MIME_VENDOR_TYPE = "vnd."
            + StudentProviderContract.AUTHORITY + ".";
    private StudentNoteOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 4;

    private static final int NOTES_ROW = 3;

    private static final int COURSE_ROW = 5;

    private static final int NOTE_PATH_EXPANDED_ROW = 6;

    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, Courses.PATH + "/#", COURSE_ROW);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED + "/#", NOTE_PATH_EXPANDED_ROW);
    }

    public studentProviderCorrected() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);

                break;
            case COURSE_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + "= ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + "= ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);

                break;
            case NOTE_PATH_EXPANDED_ROW:
                break;
            case NOTES_EXPANDED:
                break;
        }
        return nRows;
    }

    @Override
    public String getType(Uri uri) {

        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase slq = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;

        int UriMatch = sUriMatcher.match(uri);

        switch (UriMatch) {
            case NOTES:
                rowId = slq.insert(NoteInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = slq.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:

                break;

        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new StudentNoteOpenHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        long rowId = -1;
        Cursor cursor = null;
        String rowSelection = null;
        String[] rowSelectionArgs = null;

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int UriMatch = sUriMatcher.match(uri);

        switch (UriMatch) {
            case COURSES:

                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs
                        , null, null, sortOrder);
                break;
            case NOTES:

                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs
                        , null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = fetchExpanded(db, projection, selection, selectionArgs, sortOrder);

                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                String requestedRow = NoteInfoEntry._ID + "= ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, requestedRow, rowSelectionArgs,
                        null, null, null);
                break;
            case COURSE_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + "= ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
            case NOTE_PATH_EXPANDED_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry.getQualifiedName(NoteInfoEntry._ID) + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = fetchExpanded(db, projection, rowSelection, rowSelectionArgs, null);
                break;

        }


        return cursor;
    }


    private Cursor fetchExpanded(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String[] Columns = new String[projection.length];

        for (int x = 0; x < projection.length; x++) {
            Columns[x] = projection[x].equals(BaseColumns._ID) ||
                    projection[x].equals(CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQualifiedName(projection[x]) : projection[x];
        }

        String joinedTables = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                " ON " + NoteInfoEntry.getQualifiedName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQualifiedName(CourseInfoEntry._ID);

        return db.query(joinedTables, Columns, selection, selectionArgs, null,
                null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        int uriMatcher = sUriMatcher.match(uri);

        switch (uriMatcher) {
            case NOTES:
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case COURSES:
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case COURSE_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
            case NOTE_PATH_EXPANDED_ROW:
                break;
            case NOTES_EXPANDED:
                break;


        }
        return nRows;
    }
}
