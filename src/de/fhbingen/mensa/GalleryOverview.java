package de.fhbingen.mensa;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import de.fhbingen.mensa.data.orm.Photo;

public class GalleryOverview extends Activity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_overview);

        // Extract dishId from extra
        final int dishId = getIntent().getExtras().getInt(Photo.COL_DISHID);

        final GridView gv = (GridView) findViewById(R.id.gridView);
        final Cursor cursor = Photo.getCursorForDishId(dishId);
        final PhotoGridCursorAdapter cursorAdapter = new PhotoGridCursorAdapter(getApplicationContext(), cursor);
        gv.setAdapter(cursorAdapter);

        // OnClick
        gv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final SQLiteCursor cursor = (SQLiteCursor) parent.getAdapter().getItem(position);

        final long photoId = cursor.getInt(cursor.getColumnIndex(Photo.COL_PHOTOID));
        Log.d(TAG, "photoId: " + photoId);

        final Intent intent = new Intent(this, PhotoDetailActivity.class);
        intent.putExtra(Photo.COL_PHOTOID, photoId);

        startActivity(intent);
    }

    private static final String TAG = GalleryOverview.class.getSimpleName();
}
