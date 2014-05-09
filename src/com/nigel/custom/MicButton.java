
package com.nigel.custom;

import com.nigel.ecustlock.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MicButton extends View {

	Bitmap background = null;
	BitmapShader bitmapShader = null;
	Paint paint = null;
	Paint shadowPaint = null;
	ShapeDrawable shapeDrawable = null;
	
	Shader radialGradient = null;  
	
	int cx = 0;
	int cy = 0;
	static final int HALO_WIDTH = 32;
	
	public MicButton(Context context) {
		super(context);
		init();
	}
	
	public MicButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
        paint = new Paint();
        paint.setColor(0xcc0099cc);
        paint.setAntiAlias(true);
		
		shadowPaint = new Paint();
		shadowPaint.setColor(Color.WHITE);
		shadowPaint.setAntiAlias(true);
		
        background = BitmapFactory.decodeResource(getResources(), R.drawable.ic_lock_holo_dark);
		bitmapShader = new BitmapShader(background, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		cx = getWidth()/2;
		cy = getHeight()/2;
		int minLength = Math.min(getWidth(), getHeight());
		
		canvas.drawCircle(cx, cy, minLength/2-HALO_WIDTH, shadowPaint);
		canvas.drawCircle(cx, cy, minLength/2-HALO_WIDTH-3, paint);
        canvas.drawBitmap(background, cx - background.getWidth()/2, cy - background.getHeight()/2, paint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				shadowPaint.setShadowLayer(HALO_WIDTH, 0, 0, Color.WHITE);
				invalidate();
				return true;
			case MotionEvent.ACTION_UP:
				shadowPaint.setShadowLayer(0, 0, 0, Color.WHITE);
				invalidate();
				return true;
		
			default:
				break;
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Try for a width based on our minimum
		int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
		int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

		// Whatever the width ends up being, ask for a height that would let the pie
		// get as big as it can
		int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop(); 
		int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);
		setMeasuredDimension(w, h);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public void setBackground() {
		
	}
	
}
