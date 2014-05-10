package com.nigel.ecustlock;

import com.nigel.custom.MicButton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class AuthActivity extends Activity 
		implements OnClickListener, OnTouchListener {

	MicButton btnAuth;
	TextView tvInfo;
	ImageView imageSmall;
	
	Animator mCurrentAnimator;
	int mShortAnimationDuration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		btnAuth = new MicButton(getApplicationContext());
		setContentView(R.layout.activity_auth);
		
		btnAuth = (MicButton) super.findViewById(R.id.micButton1);
		tvInfo = (TextView) super.findViewById(R.id.tv_test_info);
		imageSmall = (ImageView) super.findViewById(R.id.auth_avatar_small);
		btnAuth.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		btnAuth.setOnTouchListener(this);
		
		// Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_test_info:
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch (v.getId()) {
			case R.id.micButton1:
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					tvInfo.setText("按下");
					zoomImage(imageSmall, R.drawable.anonymous_selected);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					tvInfo.setText("收回");
				}
					
				return false;
		}
		
		return false;
	}
	
	private void zoomImage(final View thumbView, int imageResId) {
		// If there's an animation in progress, cancel it
	    // immediately and proceed with this one.
	    if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }
	    
	    final ImageView expandedImageView = (ImageView) findViewById(R.id.auth_avater_big);
	    expandedImageView.setImageResource(imageResId);

	    // Calculate the starting and ending bounds for the zoomed-in image.
	    // This step involves lots of math. Yay, math.
	    final Rect startBounds = new Rect();
	    final Rect finalBounds = new Rect();
	    final Point globalOffset = new Point();
	    
	    // The start bounds are the global visible rectangle of the thumbnail,
	    // and the final bounds are the global visible rectangle of the container
	    // view. Also set the container view's offset as the origin for the
	    // bounds, since that's the origin for the positioning animation
	    // properties (X, Y).
	    thumbView.getGlobalVisibleRect(startBounds);
	    findViewById(R.id.relativeLayout2)
	            .getGlobalVisibleRect(finalBounds, globalOffset);
	    Log.d("globalOffset", "x="+globalOffset.x + ", y="+globalOffset.y);
	    Log.d("startBounds0", "left="+startBounds.left + ", right="+startBounds.right);
	    Log.d("startBounds0", "top="+startBounds.top + ", bottom="+startBounds.bottom);
	    startBounds.offset(-globalOffset.x, -globalOffset.y);
	    finalBounds.offset(-globalOffset.x, -globalOffset.y);
	    
	    // Adjust the start bounds to be the same aspect ratio as the final
	    // bounds using the "center crop" technique. This prevents undesirable
	    // stretching during the animation. Also calculate the start scaling
	    // factor (the end scaling factor is always 1.0).
	    float startScale;
	    if ((float) finalBounds.width() / finalBounds.height()
	            > (float) startBounds.width() / startBounds.height()) {
	        // Extend start bounds horizontally
	        startScale = (float) startBounds.height() / finalBounds.height();
	        float startWidth = startScale * finalBounds.width();
	        float deltaWidth = (startWidth - startBounds.width()) / 2;
//	        startBounds.left -= deltaWidth;
//	        startBounds.right += deltaWidth;
	    } else {
	        // Extend start bounds vertically
	        startScale = (float) startBounds.width() / finalBounds.width();
	        float startHeight = startScale * finalBounds.height();
	        float deltaHeight = (startHeight - startBounds.height()) / 2;
//	        startBounds.top -= deltaHeight;
//	        startBounds.bottom += deltaHeight;
	    }
	    Log.d("scale", ""+startScale);
	    Log.d("startBounds1", "left="+startBounds.left + ", right="+startBounds.right);
	    Log.d("startBounds1", "top="+startBounds.top + ", bottom="+startBounds.bottom);
	    Log.d("finalBounds1", "left="+finalBounds.left + ", right="+finalBounds.right);
	    Log.d("finalBounds1", "top="+finalBounds.top + ", bottom="+finalBounds.bottom);
	    // Hide the thumbnail and show the zoomed-in view. When the animation
	    // begins, it will position the zoomed-in view in the place of the
	    // thumbnail.
	    thumbView.setAlpha(0f);
	    expandedImageView.setVisibility(View.VISIBLE);

	    // Set the pivot point for SCALE_X and SCALE_Y transformations
	    // to the top-left corner of the zoomed-in view (the default
	    // is the center of the view).
	    expandedImageView.setPivotX(0f);
	    expandedImageView.setPivotY(0f);

	    // Construct and run the parallel animation of the four translation and
	    // scale properties (X, Y, SCALE_X, and SCALE_Y).
	    int exwidth = expandedImageView.getWidth();
	    int exheight = expandedImageView.getHeight();
	    AnimatorSet set = new AnimatorSet();
	    set
	            .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
	                    startBounds.left, finalBounds.left + finalBounds.width()/2 - exwidth/2))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
	                    startBounds.top, finalBounds.top + finalBounds.height()/2 - exheight/2))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
	            startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
	                    View.SCALE_Y, startScale, 1f));
	    set.setDuration(mShortAnimationDuration);
	    set.setInterpolator(new DecelerateInterpolator());
	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	            mCurrentAnimator = null;
	        }

	        @Override
	        public void onAnimationCancel(Animator animation) {
	            mCurrentAnimator = null;
	        }
	    });
	    set.start();
	    mCurrentAnimator = set;
	    
	    Log.d("finalBounds2", "left="+finalBounds.left + ", right="+finalBounds.right);
	    Log.d("finalBounds2", "top="+finalBounds.top + ", bottom="+finalBounds.bottom);
	    
	    // Upon clicking the zoomed-in image, it should zoom back down to the original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });

	}
}
