package com.easemob.easeui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2015/5/26.
 */

public class OverScrollView extends ScrollView {

    private static final String TAG = "ElasticScrollView";

    private static final float MOVE_FACTOR = 0.5f;

    private static final int ANIM_TIME = 300;

    private View contentView;

    private float startY;

    private Rect originalRect = new Rect();

    private boolean canPullDown = false;

    private boolean canPullUp = false;

    private boolean isMoved = false;

    public OverScrollView(Context context) {
        super(context);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (contentView == null)
            return;

        originalRect.set(contentView.getLeft(), contentView.getTop(), contentView.getRight(), contentView.getBottom());
    }

    /**
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (contentView == null) {
            return super.dispatchTouchEvent(ev);
        }

        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                canPullDown = isCanPullDown();
                canPullUp = isCanPullUp();

                startY = ev.getY();
                break;

            case MotionEvent.ACTION_UP:

                if (!isMoved)
                    break;

                TranslateAnimation anim = new TranslateAnimation(0, 0, contentView.getTop(), originalRect.top);
                anim.setDuration(ANIM_TIME);

                contentView.startAnimation(anim);

                contentView.layout(originalRect.left, originalRect.top, originalRect.right, originalRect.bottom);

                canPullDown = false;
                canPullUp = false;
                isMoved = false;

                break;
            case MotionEvent.ACTION_MOVE:

                if (!canPullDown && !canPullUp) {
                    startY = ev.getY();
                    canPullDown = isCanPullDown();
                    canPullUp = isCanPullUp();

                    break;
                }

                float nowY = ev.getY();
                int deltaY = (int) (nowY - startY);

                boolean shouldMove = (canPullDown && deltaY > 0)
                        || (canPullUp && deltaY < 0)
                        || (canPullUp && canPullDown);
                if (shouldMove) {
                    int offset = (int) (deltaY * MOVE_FACTOR);

                    contentView.layout(originalRect.left, originalRect.top + offset, originalRect.right, originalRect.bottom + offset);

                    isMoved = true;
                }

                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }


    /**
     */
    private boolean isCanPullDown() {
        return getScrollY() == 0 || contentView.getHeight() < getHeight() + getScrollY();
    }

    /**
     */
    private boolean isCanPullUp() {
        return contentView.getHeight() <= getHeight() + getScrollY();
    }

}
