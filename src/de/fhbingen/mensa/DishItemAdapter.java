package de.fhbingen.mensa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DishItemAdapter extends ArrayAdapter<Dish> {

	private final int dishItemLayoutResource;
	
	public DishItemAdapter(Context context, int dishItemLayoutResource) {
		super(context, 0);
		this.dishItemLayoutResource = dishItemLayoutResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final View view = getWorkingView(convertView);
		final ViewHolder viewHolder = getViewHolder(view);
		final Dish dish = getItem(position);
		
		viewHolder.titleView.setText(dish.getText());
		viewHolder.subTitleView.setText(Double.toString(dish.getPriceStudent()));
		
		return convertView;
	}
	
	private View getWorkingView(final View convertView) {
		// The workingView is basically just the convertView re-used if possible
		// or inflated new if not possible
		View workingView = null;
	 
		if(null == convertView) {
			final Context context = getContext();
			final LayoutInflater inflater = (LayoutInflater)context.getSystemService
		      (Context.LAYOUT_INFLATER_SERVICE);
	 
			workingView = inflater.inflate(dishItemLayoutResource, null);
		} else {
			workingView = convertView;
		}
	 
		return workingView;
	}
	
	private ViewHolder getViewHolder(final View workingView) {
		// The viewHolder allows us to avoid re-looking up view references
		// Since views are recycled, these references will never change
		final Object tag = workingView.getTag();
		ViewHolder viewHolder = null;
	 
		if(null == tag || !(tag instanceof ViewHolder)) {
			viewHolder = new ViewHolder();
	 
			viewHolder.titleView = (TextView) workingView.findViewById(R.id.textView_title);
			viewHolder.subTitleView = (TextView) workingView.findViewById(R.id.textView_price);
			//viewHolder.imageView = (ImageView) workingView.findViewById(R.id.imageView_dish);
	 
			workingView.setTag(viewHolder);
	 
		} else {
			viewHolder = (ViewHolder) tag;
		}
	 
		return viewHolder;
	}
	/**
	 * ViewHolder allows us to avoid re-looking up view references
	 * Since views are recycled, these references will never change
	 */
	private static class ViewHolder {
		public TextView titleView;
		public TextView subTitleView;
		public ImageView imageView;
	}
	
}
