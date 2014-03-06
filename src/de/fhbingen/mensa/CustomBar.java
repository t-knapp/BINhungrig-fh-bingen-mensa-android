package de.fhbingen.mensa;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class CustomBar extends View {

	public CustomBar(Context context, AttributeSet atts) {
		super(context, atts);
		
		//Set colors at construction time. Creation order in XML important.
		colorIndex = cI--;
		if(cI == -1){
			cI = 4;
		}
		
		Resources resources = this.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    dens = metrics.densityDpi;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.ratingBarCountFont));
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
				11 * (dens / 160), /* calculate px equivalent of 12 dp */
				getHeight(),
				paint
			);
		}
		
		paint.setColor(Color.BLACK);
		c.drawText(
			Integer.toString(value),
			2 * (dens / 160),
			getHeight() * 3/4f,
			paint
		);
		
		super.onDraw(c);
	}
	
	private int colorIndex = 0;
	private int max = 0;
	private int value = 0;
	private final Paint paint;
	private static final int[] colors = { 0xFFFF8B5A, 0xFFFFB234, 0xFFFFD834, 0xFFADD633, 0xFF9FC05A};
	private static int cI = 4;
	private float dens;

}
