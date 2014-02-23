package de.fhbingen.mensa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomBar extends View {

	public CustomBar(Context context, AttributeSet atts) {
		super(context, atts);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(16f);
		paint.setLinearText(true);
	}
	
	public void setData(int colorIndex, int max, int value){
		this.colorIndex = colorIndex;
		this.max = max;
		this.value = value;
		invalidate();
		requestLayout();
	}
	
	@Override
	protected void onDraw(Canvas c) {

		int dens = c.getDensity();
		
		paint.setColor(colors[colorIndex]);
		if(max > 0){
			c.drawRect(
				0, 
				0, 
				(int)(getWidth() * (value/(float)max)),
				getHeight(), 
				paint
			);
		}
		
		if(value == 0){
			c.drawRect(
				0, 
				0, 
				25 / (dens / 160f), 
				getHeight(), 
				paint
			);
		}
		
		paint.setColor(Color.BLACK);
		c.drawText(
			Integer.toString(value),
			6 / (dens / 160f),
			getHeight() * 2/3f,
			paint
		);
		
		super.onDraw(c);
	}
	
	private int colorIndex = 0;
	private int max = 5;
	private int value = 5;
	private final Paint paint;
	private static final int[] colors = { 0xFFFF8B5A, 0xFFFFB234, 0xFFFFD834, 0xFFADD633, 0xFF9FC05A};

}
