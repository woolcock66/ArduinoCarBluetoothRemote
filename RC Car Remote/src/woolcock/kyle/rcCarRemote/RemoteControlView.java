package woolcock.kyle.rcCarRemote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class RemoteControlView extends View {

	private SparseArray<PointF> mActivePointers;
	private Paint mTextPaint;

	public RemoteControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		// Generate Paints
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Update our size variables here
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();

		// get pointer ID
		int pointerId = event.getPointerId(pointerIndex);

		// get masked (not specific to a pointer) action
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {
		case MotionEvent.ACTION_DOWN:
			// Let Waterfall
		case MotionEvent.ACTION_POINTER_DOWN:
			// We have a new pointer. Lets add it to the list of pointers
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			mActivePointers.put(pointerId, f);
			break;
		case MotionEvent.ACTION_MOVE:
			// Update pointer location
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				PointF point = mActivePointers.get(event.getPointerId(i));
				if (point != null) {
					point.x = event.getX(i);
					point.y = event.getY(i);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			// Let Waterfall
		case MotionEvent.ACTION_POINTER_UP:
			// Let Waterfall
		case MotionEvent.ACTION_CANCEL:
			// Case when touch is released or canceled
			mActivePointers.remove(pointerId);
			break;
		}
		// calls onDraw
		invalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// TODO look at drawBitmap()
	}

}
