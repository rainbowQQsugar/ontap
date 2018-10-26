package com.abinbev.dsa.ui.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.SimpleAnimatorListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 11/12/15.
 */
public class SlidingPane extends RelativeLayout implements NestedScrollingParent {

    private static final long ANIMATION_DURATION = 300;
    @Bind(R.id.content_frame)
    FrameLayout contentFrame;

    @Bind(R.id.content_collapsed)
    FrameLayout collapsedFrame;

    @Nullable
    @Bind(R.id.handle)
    FrameLayout handleContainer;

    @Nullable
    @Bind(R.id.handle_img)
    ImageView handleImage;

    boolean isOpen = true;
    boolean isAnimating;
    boolean contentClosed;
    View collapsedView;
    DrawerCallback drawerCallback;

    DragStrategy dragStrategy;

    public interface DrawerCallback {
        void onDrawerUpdate(int offset);
    }

    public SlidingPane(Context context) {
        this(context, null);
    }

    public SlidingPane(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingPane(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.sliding_pane, this);
        ButterKnife.bind(this);
        ViewCompat.setElevation(this, 12);
        contentFrame.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                contentFrame.getViewTreeObserver().removeOnPreDrawListener(this);
                if (drawerCallback != null) {
                    drawerCallback.onDrawerUpdate(contentClosed ? 0 : contentFrame.getWidth());
                }
                return false;
            }
        });

        if (getResources().getBoolean(R.bool.isTablet)) {
            dragStrategy = new TabletDragStrategy();
        }
        else {
            dragStrategy = new PhoneDragStrategy();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        dragStrategy.onAttachedToWindow(this);
    }

    public void setDrawerCallback(DrawerCallback drawerCallback) {
        this.drawerCallback = drawerCallback;
    }

    public void setContent(View view) {
        contentFrame.addView(view);
    }

    public int getContentId() {
        return contentFrame.getId();
    }

    public void setCollapsedView(View view) {
        this.collapsedView = view;
    }

    @Nullable
    @OnClick(R.id.handle)
    public void toggle() {
        if (isAnimating) {
            return;
        }
        if (isOpen) {
            if (collapsedView != null) {
                if (contentClosed) {
                    closeDrawer();
                } else {
                    collapsedFrame.removeAllViews();
                    collapsedFrame.addView(collapsedView);
                    closeContent();
                }
            } else {
                closeDrawer();
            }
        } else {
            openDrawer();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragStrategy.onInterceptTouchEvent(this, ev);
    }

    protected boolean defaultOnInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return dragStrategy.onTouchEvent(this, event);
    }

    protected boolean defaultOnTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return dragStrategy.onStartNestedScroll(this, child, target, nestedScrollAxes);
    }

    protected boolean defaultOnStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    public void expandVertically() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        final int initialMargin = layoutParams.topMargin;

        animate().setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                        layoutParams.topMargin = (int) (initialMargin - (valueAnimator.getAnimatedFraction() * initialMargin));
                        setLayoutParams(layoutParams);
                    }
                })
                .start();
    }

    private void closeDrawer() {
        int targetX = -(getWidth() - handleContainer.getWidth());
        handleImage.animate().setDuration(ANIMATION_DURATION).rotation(180).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                handleImage.setImageResource(R.drawable.ic_arrow);
            }
        });

        animate().setDuration(ANIMATION_DURATION).translationX(targetX).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                isOpen = false;
                isAnimating = false;
                contentClosed = false;
            }
        }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (drawerCallback != null && !contentClosed) {
                    drawerCallback.onDrawerUpdate((int) ((1f - valueAnimator.getAnimatedFraction()) * contentFrame.getWidth()));
                }
            }
        }).start();
    }

    private void openDrawer() {
        contentFrame.setTranslationX(0);

        handleImage.animate().setDuration(ANIMATION_DURATION).rotation(0).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(null);

        animate().setDuration(ANIMATION_DURATION).translationX(0).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                isOpen = true;
                isAnimating = false;
            }
        }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (drawerCallback != null) {
                    drawerCallback.onDrawerUpdate((int) (valueAnimator.getAnimatedFraction() * contentFrame.getWidth()));
                }
            }
        }).start();
    }

    private void closeContent() {
        contentFrame.animate().setDuration(ANIMATION_DURATION).translationX(-contentFrame.getWidth()).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                isAnimating = true;
                handleImage.setImageResource(R.drawable.ic_close);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                isAnimating = false;
                contentClosed = true;
            }
        }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (drawerCallback != null) {
                    drawerCallback.onDrawerUpdate((int) ((1f - valueAnimator.getAnimatedFraction()) * contentFrame.getWidth()));
                }
            }
        }).start();
    }

    private interface DragStrategy {
        boolean onStartNestedScroll(SlidingPane slidingPane, View child, View target, int nestedScrollAxes);
        boolean onTouchEvent(SlidingPane slidingPane, MotionEvent event);
        boolean onInterceptTouchEvent(SlidingPane slidingPane, MotionEvent ev);
        void onAttachedToWindow(SlidingPane slidingPane);
    }

    private static class PhoneDragStrategy implements DragStrategy {

        int touchSlop;                                          // Used to determine if move is big enough to drag this pane.
        int originalTopMargin = 0;                              // Original top margin for this view.
        int startTopMargin = 0;                                 // Top margin saved during initial touch.
        float startTouchY;                                      // Position of initial touch on y axis.
        boolean isScrolling = false;                            // Tells if this pane is being scrolled.
        boolean canChildScrollDown = false;                     // Tells if child view can be scrolled down.

        @Override
        public boolean onStartNestedScroll(SlidingPane slidingPane, View child, View target, int nestedScrollAxes) {
            // Check if child view can be scrolled down.
            canChildScrollDown = target.canScrollVertically(-1);
            return slidingPane.defaultOnStartNestedScroll(child, target, nestedScrollAxes);
        }

        @Override
        public boolean onTouchEvent(SlidingPane slidingPane, MotionEvent event) {
            if (isScrolling) {
                int translationY = (int) (event.getRawY() - startTouchY);

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) slidingPane.getLayoutParams();
                layoutParams.topMargin = startTopMargin + translationY;

                if (layoutParams.topMargin < 0) {
                    layoutParams.topMargin = 0;
                }
                else if (layoutParams.topMargin > originalTopMargin) {
                    layoutParams.topMargin = originalTopMargin;
                }

                slidingPane.setLayoutParams(layoutParams);

                return true;
            }
            else {
                return slidingPane.defaultOnTouchEvent(event);
            }
        }

        @Override
        public boolean onInterceptTouchEvent(SlidingPane slidingPane, MotionEvent ev) {
            final int action = MotionEventCompat.getActionMasked(ev);

            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                isScrolling = false;
                return false;
            }
            else if (action == MotionEvent.ACTION_DOWN) {
                isScrolling = false;
                startTouchY = ev.getRawY();
                RelativeLayout.LayoutParams layoutParams = (LayoutParams) slidingPane.getLayoutParams();
                startTopMargin = layoutParams.topMargin;
                canChildScrollDown = false;
                return false;
            }
            else if (action == MotionEvent.ACTION_MOVE) {
                if (isScrolling) {
                    return true;
                }

                final float moveDistance = Math.abs(ev.getRawY() - startTouchY);

                if (moveDistance > touchSlop) {
                    boolean isDown = ev.getRawY() > startTouchY;
                    boolean isUp = ev.getRawY() < startTouchY;
                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) slidingPane.getLayoutParams();

                    if ((isUp && layoutParams.topMargin > 0) ||
                            (isDown && layoutParams.topMargin < originalTopMargin && !canChildScrollDown)) {
                        isScrolling = true;
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public void onAttachedToWindow(SlidingPane slidingPane) {

            ViewConfiguration viewConfig = ViewConfiguration.get(slidingPane.getContext());
            touchSlop = viewConfig.getScaledTouchSlop();

            // Save original top margin
            RelativeLayout.LayoutParams layoutParams = (LayoutParams) slidingPane.getLayoutParams();
            originalTopMargin = layoutParams.topMargin;

        }
    }

    private static class TabletDragStrategy implements DragStrategy {

        @Override
        public boolean onStartNestedScroll(SlidingPane slidingPane, View child, View target, int nestedScrollAxes) {
            return slidingPane.defaultOnStartNestedScroll(child, target, nestedScrollAxes);
        }

        @Override
        public boolean onTouchEvent(SlidingPane slidingPane, MotionEvent event) {
            return slidingPane.defaultOnTouchEvent(event);
        }

        @Override
        public boolean onInterceptTouchEvent(SlidingPane slidingPane, MotionEvent ev) {
            return slidingPane.defaultOnInterceptTouchEvent(ev);
        }

        @Override
        public void onAttachedToWindow(SlidingPane slidingPane) {

        }
    }
}
