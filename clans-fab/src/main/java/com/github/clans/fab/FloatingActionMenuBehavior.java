package com.github.clans.fab;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import static android.view.View.VISIBLE;


public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {
    private static final boolean AUTO_HIDE_DEFAULT = false;

    private Rect mTmpRect;
    private boolean mAutoHideEnabled;
    private int topInset;

    public void setTopInset(int insetsCompat){
        topInset=insetsCompat;
    }

    public FloatingActionMenuBehavior() {
        super();
        mAutoHideEnabled = AUTO_HIDE_DEFAULT;
    }

    public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu child,
                                          View dependency) {
        if (dependency instanceof AppBarLayout) {
            // If we're depending on an AppBarLayout we will show/hide it automatically
            // if the FAB is anchored to the AppBarLayout
            updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
        }
        return false;
    }

    private boolean shouldUpdateVisibility(View dependency, FloatingActionMenu child) {
        /*final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (!mAutoHideEnabled) {
            return false;
        }

        if (lp.getAnchorId() != dependency.getId()) {
            // The anchor ID doesn't match the dependency, so we won't automatically
            // show/hide the FAB
            return false;
        }

        //noinspection RedundantIfStatement
        if (child.getVisibility() != VISIBLE) {
            // The view isn't set to be visible so skip changing its visibility
            return false;
        }*/

        return true;
    }



    int getTopInsets(){

        return topInset;

    }

    int appBar_getMinimumHeightForVisibleOverlappingContent(AppBarLayout appBarLayout){

        final int topInset = getTopInsets();
        final int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = appBarLayout.getChildCount();
        final int lastChildMinHeight = childCount >= 1
                ? ViewCompat.getMinimumHeight(appBarLayout.getChildAt(childCount - 1)) : 0;
        if (lastChildMinHeight != 0) {
            return (lastChildMinHeight * 2) + topInset;
        }

        // If we reach here then we don't have a min height explicitly set. Instead we'll take a
        // guess at 1/3 of our height being visible
        return appBarLayout.getHeight() / 3;
    }

    private boolean updateFabVisibilityForAppBarLayout(CoordinatorLayout parent,
                                                       AppBarLayout appBarLayout, FloatingActionMenu child) {

        if (!shouldUpdateVisibility(appBarLayout, child)) {
            return false;
        }

        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }

        // First, let's get the visible rect of the dependency
        final Rect rect = mTmpRect;
        ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);
        if (rect.bottom <=  appBar_getMinimumHeightForVisibleOverlappingContent(appBarLayout)  ) { // AZAZAZAZAAZAZZAAAZAZAZAAZAZAZ
            // If the anchor's bottom is below the seam, we'll animate our FAB out
            //child.setVisibility(View.INVISIBLE);
            child.hideMenuButton(true);
            //child.hideMenuButton(true);
        } else {
            //child.setVisibility(View.VISIBLE);
            // Else, we'll animate our FAB back in
            child.showMenuButton(true);
            //child.show(mInternalAutoHideListener, false);
        }
        return true;
    }


    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionMenu child,
                                 int layoutDirection) {
        // First, let's make sure that the visibility of the FAB is consistent
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, count = dependencies.size(); i < count; i++) {
            final View dependency = dependencies.get(i);
            if (dependency instanceof AppBarLayout) {
                if (updateFabVisibilityForAppBarLayout(
                        parent, (AppBarLayout) dependency, child)) {
                    break;
                }
            }
        }
        // Now let the CoordinatorLayout lay out the FAB
        parent.onLayoutChild(child, layoutDirection);
        // Now offset it if needed
        //offsetIfNeeded(parent, child);
        return true;
    }

    @Override
    public boolean getInsetDodgeRect(@NonNull CoordinatorLayout parent,
                                     @NonNull FloatingActionMenu child, @NonNull Rect rect) {
        // Since we offset so that any internal shadow padding isn't shown, we need to make
        // sure that the shadow isn't used for any dodge inset calculations

        /*final Rect shadowPadding = child.mShadowPadding;
        rect.set(child.getLeft() + shadowPadding.left,
                child.getTop() + shadowPadding.top,
                child.getRight() - shadowPadding.right,
                child.getBottom() - shadowPadding.bottom);*/

        rect.set(child.getLeft() ,
                child.getTop(),
                child.getRight(),
                child.getBottom());
        return true;
    }

}