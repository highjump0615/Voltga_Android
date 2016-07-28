package com.diyin.Voltga.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by aaa on 14-10-2.
 */
public class PeopleItemLayout extends RelativeLayout {

    private static final String TAG = PeopleItemLayout.class.getSimpleName();

//    private GestureDetector mGestureDetector;

    public PeopleItemLayout(Context context) {
        super(context);
    }

    public PeopleItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PeopleItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    private void initGestureDetector(Context context) {
//        mGestureDetector = new GestureDetector(context, new GestureListener());
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        return mGestureDetector.onTouchEvent(e);
////        return super.onTouchEvent(e);
//    }
//
//    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//
//        @Override
//        public boolean onSingleTapUp(MotionEvent e) {
//            float x = e.getX();
//            float y = e.getY();
//
//            Log.d(TAG, "Tapped at: (" + x + ", " + y + ")");
//
////            return super.onSingleTapConfirmed(e);
//            return true;
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//
//            float x = e.getX();
//            float y = e.getY();
//
//            Log.d(TAG, "Double Tapped at: (" + x + ", " + y + ")");
//
//            return true;
//        }
//    }
}
