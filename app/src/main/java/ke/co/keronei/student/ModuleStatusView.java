package ke.co.keronei.student;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    private static final int EDIT_MODE_COUNT = 6;
    private static final int INVALID_INDEX = -1;
    private static final int CIRCLE_VALUE = 0;
    private static final float ACTUAL_PIXEL_TO_RENDER = 2f;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    private float mOutLineField;
    private float shapeSize;
    private float spacing;
    private Rect[] mModuleRectangle;
    private int mOutlineColor;
    private int fillColor;
    private Paint mPaintFill;
    private Paint mOutlinePaint;
    private float radius;
    private int maxHorizontalModules;
    private int mShape;
    private ModuleAccessabliltyHelper moduleAccessabliltyHelper;


    public boolean[] getmModuleStatus() {
        return mModuleStatus;
    }

    public void setmModuleStatus(boolean[] mModuleStatus) {
        this.mModuleStatus = mModuleStatus;
    }

    public boolean[] mModuleStatus;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        if(isInEditMode())
            setUpEditValues();

        setFocusable(true);

        moduleAccessabliltyHelper = new ModuleAccessabliltyHelper(this);
        ViewCompat.setAccessibilityDelegate(this, moduleAccessabliltyHelper);
        // Load attributes

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float displayDensity = dm.density;

        float defaultOutlineWidthPixel = displayDensity * ACTUAL_PIXEL_TO_RENDER;
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);


        mOutlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        mShape = a.getInt( R.styleable.ModuleStatusView_shape, CIRCLE_VALUE);

        mOutLineField = a.getDimension(R.styleable.ModuleStatusView_outlineWidth,defaultOutlineWidthPixel);

        a.recycle();

        shapeSize = 100f;
        spacing = 20f;
        radius = (shapeSize - mOutLineField)/2;



        mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(mOutLineField);
        mOutlinePaint.setColor(mOutlineColor);
        fillColor = getContext().getResources().getColor(R.color.my_prefered_color);

        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(fillColor);

    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        moduleAccessabliltyHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return moduleAccessabliltyHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);

    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return moduleAccessabliltyHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setupModuleRectangles(w);
    }

    private void setUpEditValues() {
        boolean[] editModeValue = new boolean[EDIT_MODE_COUNT];

        int midleCount = EDIT_MODE_COUNT/2;

        for(int i = 0; i < midleCount; i++){
            editModeValue[i] = true;
        }

        setmModuleStatus(editModeValue );
    }

    private void setupModuleRectangles(int width) {

        int availablewidth = width-getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatcanFit = (int)(availablewidth / (shapeSize + spacing));
        int XmaxHorizontalModules = Math.min(horizontalModulesThatcanFit, mModuleStatus.length);



        mModuleRectangle = new Rect[mModuleStatus.length];
        for(int moduleIndex = 0; moduleIndex < mModuleRectangle.length; moduleIndex ++){
            int column = moduleIndex % XmaxHorizontalModules;
            int row = moduleIndex / XmaxHorizontalModules;

            int x = getPaddingLeft() + (int)(column *( shapeSize + spacing));
            int y = getPaddingTop() + (int)(row * (shapeSize + spacing));
            mModuleRectangle[moduleIndex] = new Rect(x, y, x +(int)shapeSize, y+(int)shapeSize);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 0;
        int desireHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availavleWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModuleThatCanFit = (int)(availavleWidth / (shapeSize + spacing));
        maxHorizontalModules = Math.min(horizontalModuleThatCanFit, mModuleStatus.length);

        desiredWidth = (int)((maxHorizontalModules *(spacing + shapeSize)) - spacing);

        desiredWidth += getPaddingLeft() + getPaddingRight();

        int rows = ((mModuleStatus.length - 1)/maxHorizontalModules) + 1;

        desireHeight = (int)((rows * (shapeSize + spacing))- spacing);
        desireHeight += getPaddingBottom() + getPaddingTop();

        int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(desireHeight, heightMeasureSpec, 0);

        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        for(int moduleIndex = 0; moduleIndex < mModuleRectangle.length; moduleIndex ++){
            if(mShape == CIRCLE_VALUE){
            float x = mModuleRectangle[moduleIndex].centerX();
            float y = mModuleRectangle[moduleIndex].centerY();


            if(mModuleStatus[moduleIndex])
                 canvas.drawCircle(x, y, radius, mPaintFill);

            canvas.drawCircle(x,y, radius, mOutlinePaint);

        }else{
                drawSquare(canvas, moduleIndex);
            }
    }}
    private void drawSquare(Canvas canvas, int moduleIndex) {
        Rect moduleRectangle = mModuleRectangle[moduleIndex];

        if(mModuleStatus[moduleIndex])
            canvas.drawRect(moduleRectangle, mPaintFill);

        canvas.drawRect(moduleRectangle.left + (mOutLineField/2),
                moduleRectangle.top + (mOutLineField/2),
                moduleRectangle.right - (mOutLineField/2),
                moduleRectangle.bottom - (mOutLineField/2),
                mOutlinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return  true;
            case MotionEvent.ACTION_UP:
                int touchedModuleIndex = findItemAtPosition(event.getX(), event.getY());
                onModuleSelected(touchedModuleIndex);
                return true;



        }
        return super.onTouchEvent(event);
    }

    private void onModuleSelected(int touchedModuleIndex) {
        //check that the passed index is a valid one
        if(touchedModuleIndex == INVALID_INDEX)
            return;
        mModuleStatus[touchedModuleIndex] = !mModuleStatus[touchedModuleIndex];
        invalidate();

        moduleAccessabliltyHelper.invalidateVirtualView(touchedModuleIndex);

        moduleAccessabliltyHelper.sendEventForVirtualView(touchedModuleIndex, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private int findItemAtPosition(float x, float y) {
        int moduleIndex = INVALID_INDEX;
        for(int index = 0; index < mModuleRectangle.length; index ++){
            if(mModuleRectangle[index].contains((int)x, (int)y)){
                moduleIndex  = index;
                break;
            }
        }
        return moduleIndex;
    }

    private class ModuleAccessabliltyHelper extends ExploreByTouchHelper{
        public ModuleAccessabliltyHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float v, float v1) {
            int moduleIndex = findItemAtPosition(v, v1);
            return moduleIndex == INVALID_INDEX ? ExploreByTouchHelper.INVALID_ID : moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> list) {
            if (mModuleRectangle == null)
                    return;

            for(int moduleIndex = 0; moduleIndex < mModuleRectangle.length; moduleIndex ++){
                list.add(moduleIndex);
            }

        }

        @Override
        protected void onPopulateNodeForVirtualView(int i, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {

            accessibilityNodeInfoCompat.setFocusable(true);
            accessibilityNodeInfoCompat.setBoundsInParent(mModuleRectangle[i]);
            accessibilityNodeInfoCompat.setContentDescription("Module" + i);
            accessibilityNodeInfoCompat.setCheckable(true);
            accessibilityNodeInfoCompat.setChecked(mModuleStatus[i]);

            accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);

        }

        @Override
        protected boolean onPerformActionForVirtualView(int i, int i1, @Nullable Bundle bundle) {
            switch (i1){
                case AccessibilityNodeInfoCompat.ACTION_CLICK:
                    onModuleSelected(i);
                    return true;
            }
            return false;
        }
    }
}
