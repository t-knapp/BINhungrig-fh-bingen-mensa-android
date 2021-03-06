package de.fhbingen.mensa;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import de.fhbingen.mensa.data.orm.Photo;

/**
 * Adapter for GalleryOverview List showing Photos of a dish in grid.
 *
 * Created by tknapp on 10.12.15.
 */
public class PhotoGridCursorAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    PhotoGridCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.gallery_fragment, parent, false);
        return  v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final SquaredImageView iv = (SquaredImageView) view.findViewById(R.id.picture);
        final byte[] bytes = cursor.getBlob(cursor.getColumnIndex(Photo.COL_THUMB));
        iv.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }

    private final static String TAG = PhotoGridCursorAdapter.class.getSimpleName();
}
