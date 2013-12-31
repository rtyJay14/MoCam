package self.project.mocam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import self.project.mocam.R;
import self.project.mocam.R.id;
import self.project.mocam.R.layout;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

public class GridFullScreenActivity extends Activity {
	ImageView imageView;
	private ShareActionProvider mShareActionProvider;
	private Intent mShareIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen);

		/*
		 * Action Bars
		 */
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);

		/*
		 * Intent Image Data
		 */
		imageView = (ImageView) findViewById(R.id.imageView1);

		getData();
	}

	private void getData() {
		String ps = getIntent().getStringExtra("picture");

		imageView.setImageBitmap(BitmapFactory.decodeFile(ps));

		/*
		 * Bundle extras = getIntent().getExtras(); byte[] byteArray =
		 * extras.getByteArray("picture");
		 * 
		 * Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 1,
		 * byteArray.length);
		 * 
		 * image.setImageBitmap(bmp);
		 */

		/*
		 * String uri = i.getStringExtra('uri'); ImageView imageView = new
		 * ImageView(this); LayoutParams lp = new
		 * LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 * imageView.setLayoutParams(lp); Uri imageUri = Uri.parse(imagePath);
		 * imageView.setImageUri(imageUri);
		 */
	}

	// Activity request codes
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "MoCam";

	private Uri fileUri; // file url to store image/video

	/**
	 * ------------ Helper Methods ----------------------
	 * */

	/*
	 * Creating file uri to store image/video
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/*
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	//Creating ShareActionProvider
	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share_action_provider, menu);

		// Find the MenuItem that we know has the ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_share);

		// Get its ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();

		// Connect the dots: give the ShareActionProvider its Share Intent
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(mShareIntent);
		}

		// Return true so Android will know we want to display the menu
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.menu_share:

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

