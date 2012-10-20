package com.jsillanpaa.drawinput;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.HwrStroke;

/**
 * This class represents a canvas area where user can draw characters.
 * 
 * Class listens to onTouch events and notifies DrawInputMethodService when a
 * new stroke has been drawn on canvas.
 * 
 * @author joonas
 */
public class DrawInputCanvas extends SurfaceView {
	private static final String TAG = "DrawInputCanvas";
	private static final float LINE_WIDTH = 3.0f;
	private static final float TEXT_FONTSIZE = 24;
	private static final Typeface TEXT_TYPEFACE = Typeface.create("Serif", Typeface.ITALIC);


	public interface DrawInputCanvasListener {
		public void onNewStroke(DrawInputCanvas canvas);
		public void onSwipeLeft(DrawInputCanvas canvas);
		public void onSwipeRight(DrawInputCanvas canvas);
	}

	private float previous_x;
	private float previous_y;

	private HwrStroke mStrokeBeingDrawn;
	private HwrCharacter mCharBeingDrawn;
	private ArrayList<DrawInputCanvasListener> mListeners;
	private Paint mLinePaint;
	private Paint mDotPaint;
	private String mDisplayText;
	private Paint mTextPaint;

	public DrawInputCanvas(Context context) {
		super(context);
		init();
	}

	public DrawInputCanvas(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DrawInputCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		Log.i(TAG, "init()");
		mListeners = new ArrayList<DrawInputCanvas.DrawInputCanvasListener>();
		mCharBeingDrawn = new HwrCharacter('-');

		mLinePaint = new Paint();
		mLinePaint.setColor(Color.GREEN);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(LINE_WIDTH);

		mDotPaint = new Paint(mLinePaint);
		mDotPaint.setStyle(Style.FILL);
		
		mTextPaint = new Paint();
		mTextPaint.setColor(Color.GREEN);
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setStrokeWidth(1.0f);
		mTextPaint.setTextSize(TEXT_FONTSIZE);
		mTextPaint.setTypeface(TEXT_TYPEFACE);
		setWillNotDraw(false);
	}

	@Override
	public void draw(Canvas canvas) {
		Log.i(TAG, "onDraw()");
		if (canvas != null) {
			if(mDisplayText!=null){
				drawText(canvas);
			}
			else{
				drawChar(canvas);
			}

		}

	}

	private void drawChar(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		for (HwrStroke stroke : mCharBeingDrawn.strokes) {
			for (int i = 1; i < stroke.points.size(); i++) {
				PointF previous = stroke.points.get(i - 1);
				PointF current = stroke.points.get(i);
				canvas.drawLine(previous.x, previous.y, current.x,
						current.y, mLinePaint);

			}
		}
	}

	private void drawText(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		float h = canvas.getHeight()/2 - mTextPaint.getFontSpacing()/2;
		canvas.drawText(mDisplayText, 10, h, mTextPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {

		float event_x = motionEvent.getX();
		float event_y = motionEvent.getY();

		switch (motionEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			previous_x = event_x;
			previous_y = event_y;

			mStrokeBeingDrawn = new HwrStroke();
			mStrokeBeingDrawn.points.add(new PointF(previous_x, previous_y));
			mCharBeingDrawn.strokes.add(mStrokeBeingDrawn);

			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:

			/* Don't know if this check is necessary but lets do it anyways ? */
			if (previous_x != event_x && previous_y != event_y) {
				previous_x = event_x;
				previous_y = event_y;

				mStrokeBeingDrawn.points.add(new PointF(event_x, event_y));
				invalidate();
			}
			break;

		case MotionEvent.ACTION_UP:
			if (previous_x != event_x && previous_y != event_y) {
				mStrokeBeingDrawn.points.add(new PointF(event_x, event_y));
			}
			invalidate(); // invalidate before try recognition
			notifyNewStroke();
			break;
		}

		return true;

	}

	private void notifyNewStroke() {
		for (DrawInputCanvasListener l : mListeners) {
			l.onNewStroke(this);
		}
	}

	public void clear() {
		Log.i(TAG, "clear()");
		mCharBeingDrawn.strokes.clear();
		invalidate();
	}

	public void addCanvasListener(DrawInputCanvasListener l) {
		mListeners.add(l);
	}

	public HwrCharacter getCharBeingDrawn() {
		return mCharBeingDrawn;
	}

	public void showText(String string) {
		mDisplayText = string;
		invalidate();
	}
	public void removeText() {
		mDisplayText = null;
		invalidate();
	}

}