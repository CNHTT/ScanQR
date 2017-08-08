package szfp.scan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;


public class FinderView extends View {
//	private static final long ANIMATION_DELAY = 10;
	private Paint finderMaskPaint;
	private int measureedWidth;
	private int measureedHeight;
	
	public FinderView(Context context) {
		super(context);
		init(context);
	}

	public FinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(leftRect, finderMaskPaint);
		canvas.drawRect(topRect, finderMaskPaint);
		canvas.drawRect(rightRect, finderMaskPaint);
		canvas.drawRect(bottomRect, finderMaskPaint);
		
		//画框
		roi_box.setBounds(middleRect);
		roi_box.draw(canvas);	
		
	/*	if (lineRect.bottom < middleRect.bottom) {
			zx_code_line.setBounds(lineRect);
			lineRect.top = lineRect.top + lineHeight / 2;
			lineRect.bottom = lineRect.bottom + lineHeight / 2;
		} else {
			lineRect.set(middleRect);
			lineRect.bottom = lineRect.top + lineHeight;
			zx_code_line.setBounds(lineRect);
		}*/
	//	zx_code_line.draw(canvas);
	//	postInvalidateDelayed(ANIMATION_DELAY, middleRect.left, middleRect.top, middleRect.right, middleRect.bottom);
	}

	private Rect topRect = new Rect();
	private Rect bottomRect = new Rect();
	private Rect rightRect = new Rect();
	private Rect leftRect = new Rect();
	private Rect middleRect = new Rect();
	private Drawable roi_box;

	private void init(Context context) {
		int finder_mask = context.getResources().getColor(R.color.finder_mask);
		finderMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		finderMaskPaint.setColor(finder_mask);
		roi_box = context.getResources().getDrawable(R.drawable.roi_box);
		//scan_line = context.getResources().getDrawable(R.drawable.scan_line);
	}

	//////////////新增该方法//////////////////////
	/**
	 * 根据图片size求出矩形框在图片所在位置，tip：相机旋转90度以后，拍摄的图片是横着的，所有传递参数时，做了交换
	 * @param w
	 * @param h
	 * @return
	 */
	public Rect getScanImageRect(int w, int h) {
		//先求出实际矩形
		Rect rect = new Rect();
		float tempw = w / (float) measureedWidth;
		float temph = h / (float) measureedHeight;
		rect.left = (int) (middleRect.left*tempw);
		rect.right =(int) ( middleRect.right*tempw);	
		rect.top = (int) (middleRect.top * temph);
		rect.bottom = (int) (middleRect.bottom * temph);
		return rect;
	}

	////////////////////////////////////
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureedWidth = MeasureSpec.getSize(widthMeasureSpec);
		measureedHeight = MeasureSpec.getSize(heightMeasureSpec);
		int borderWidth = measureedWidth / 2 + 160;	
		middleRect.set((measureedWidth - borderWidth) / 2, (measureedHeight - borderWidth) / 2,
				(measureedWidth - borderWidth) / 2 + borderWidth, (measureedHeight - borderWidth) / 2 + borderWidth);	
	//	lineRect.set(middleRect);
	//	lineRect.bottom = lineRect.top + lineHeight;
		leftRect.set(0, middleRect.top, middleRect.left, middleRect.bottom);
		topRect.set(0, 0, measureedWidth, middleRect.top);
		rightRect.set(middleRect.right, middleRect.top, measureedWidth, middleRect.bottom);
		bottomRect.set(0, middleRect.bottom, measureedWidth, measureedHeight);
	}
}
