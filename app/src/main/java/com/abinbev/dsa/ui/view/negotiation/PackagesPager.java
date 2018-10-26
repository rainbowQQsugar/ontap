package com.abinbev.dsa.ui.view.negotiation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.abinbev.dsa.model.Paquetes_por_segmento__c;
import com.abinbev.dsa.utils.SimpleAnimatorListener;

/**
 * Created by wandersonblough on 12/9/15.
 */
public class PackagesPager extends ViewPager {

    private static final String TAG = "PackagesPager";

    ChildHelper childHelper;
    boolean isAnimating;
    ValueAnimator.AnimatorUpdateListener updateListener;
    SimpleAnimatorListener animatorListener;

    public interface ChildHelper {
        PackagePageView getCurrentChild();

        Paquetes_por_segmento__c getChildAt(int postition);
    }

    public PackagesPager(Context context) {
        this(context, null);
    }

    public PackagesPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                requestLayout();
            }
        };
        animatorListener = new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                isAnimating = false;
            }
        };
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        try {
            childHelper = (ChildHelper) getAdapter();
        } catch (ClassCastException e) {
            Log.e(TAG, "PackagesPager: Adapter must implement " + ChildHelper.class.getSimpleName(), e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isAnimating) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        } else {
            int targetHeight = getMeasuredHeight();
            if (getChildCount() > 0) {
                View child = childHelper.getCurrentChild();
                child.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
                targetHeight = child.getMeasuredHeight();
            }
            if (getMeasuredHeight() != targetHeight) {
                ValueAnimator animator = ValueAnimator.ofInt(getHeight(), targetHeight);
                animator.setDuration(200);
                animator.addUpdateListener(updateListener);
                animator.addListener(animatorListener);
                animator.start();
            } else {
                setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    public Paquetes_por_segmento__c getPackage(int position) {
        return childHelper.getChildAt(position);
    }
}
