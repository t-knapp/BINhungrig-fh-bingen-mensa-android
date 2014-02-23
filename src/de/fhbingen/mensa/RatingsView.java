package de.fhbingen.mensa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RatingsView extends View {

	public RatingsView(Context context, AttributeSet attibutes) {
		super(context, attibutes);
		
		paint = new Paint();
		paint.setAntiAlias(true);
	}
	
	public void setData(int[] data){
		this.data = data;
		invalidate();
		requestLayout();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		//LEFTOFFSET = canvas.getWidth()/2;
		
		Log.d("RatingsView", canvas.getWidth() + "x" + canvas.getHeight());
		
		int dens = canvas.getDensity();
		
		double max = 0;
		for (int val : data) {
			max = val > max ? val : max;
		}
		
		for(int i = 0; i < data.length; i++){
			paint.setColor(colors[i]);
			
			if(data[i]==0){
				canvas.drawRect(
					LEFTOFFSET, 
					(i*(BARHEIGHT + BARSPACING))/ (dens / 160f), 
					LEFTOFFSET + 25 / (dens / 160f) , 
					((i*(BARHEIGHT+BARSPACING))+BARHEIGHT) / (dens / 160f), 
					paint);
			} else {
				canvas.drawRect(
					LEFTOFFSET, 
					(i*(BARHEIGHT + BARSPACING))/ (dens / 160f), 
					LEFTOFFSET + (int)(canvas.getWidth() * (data[i]/max)), 
					((i*(BARHEIGHT+BARSPACING))+BARHEIGHT) / (dens / 160f), 
					paint);
			}
			
			paint.setColor(Color.BLACK);
			paint.setTextSize(16f);
			canvas.drawText(
				Integer.toString(data[i]),
				LEFTOFFSET + 5 / (dens / 160f),
				(i*(BARHEIGHT + BARSPACING) + BARHEIGHT/2 + BARHEIGHT/5)/ (dens / 160f), 
				paint);
		}
		super.onDraw(canvas);
	}

	private final int BARHEIGHT = 50;
	private final int BARSPACING = 5;
	private final int LEFTOFFSET = 0;
	
	private int[] data = new int[5];
	private final Paint paint;
	private final int[] colors = { 0xFF9FC05A, 0xFFADD633, 0xFFFFD834, 0xFFFFB234, 0xFFFF8B5A };
}
