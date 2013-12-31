package self.project.mocam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class CameraActivity extends Activity {

	private ShareActionProvider mShareActionProvider;
	private Intent mShareIntent;

	// Activity request codes
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "MoCam";

	private Uri fileUri; // file url to store image/video

	private ImageView imgPreview;
	private VideoView videoPreview;

	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;

	ImageAdapter myImageAdapter;
	AsyncTaskLoadFiles myAsyncTaskLoadFiles;
	GridView gridview;

	// private ImageAdapter myImageAdapter;

	// Cursor used to access the results from querying for images on the SD
	// card.

	private Cursor cursor;

	// Column index for the Thumbnails Image IDs.

	private int columnIndex;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		 * imgPreview = (ImageView) findViewById(R.id.imgPreview); videoPreview
		 * = (VideoView) findViewById(R.id.videoPreview); btnCapturePicture =
		 * (Button) findViewById(R.id.btnCapturePicture); btnRecordVideo =
		 * (Button) findViewById(R.id.btnRecordVideo);
		 */

		/*
		 * Action Bars
		 */
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		// actionBar.setDisplayHomeAsUpEnabled(true);

		/*
		 * Action Provider
		 */
		mShareIntent = new Intent();
		mShareIntent.setAction(Intent.ACTION_SEND);
		mShareIntent.setType("text/plain");
		mShareIntent.putExtra(Intent.EXTRA_TEXT,
				"From me to you, this text is new.");

		/*
		 * Images in Gridview
		 */
		gridview = (GridView) findViewById(R.id.sdcard);
		myImageAdapter = new ImageAdapter(this);
		gridview.setAdapter(myImageAdapter);

		/*
		 * gridview.setOnItemClickListener(myOnItemClickListener);
		 * 
		 * Button buttonReload = (Button)findViewById(R.id.reload);
		 * buttonReload.setOnClickListener(new OnClickListener(){
		 * 
		 * @Override public void onClick(View arg0) {
		 * 
		 * //Cancel the previous running task, if exist.
		 * myAsyncTaskLoadFiles.cancel(true);
		 * 
		 * //new another ImageAdapter, to prevent the adapter have //mixed files
		 * myImageAdapter = new ImageAdapter(CameraActivity.this);
		 * gridview.setAdapter(myImageAdapter); myAsyncTaskLoadFiles = new
		 * AsyncTaskLoadFiles(myImageAdapter); myAsyncTaskLoadFiles.execute();
		 * 
		 * }});
		 */
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent intent = new Intent(getApplicationContext(),
						GridFullScreenActivity.class);
				intent.putExtra("picture",
						myImageAdapter.itemList.get(position));
				startActivity(intent);
				/*
				 * Intent i = new Intent(getApplicationContext(),
				 * GridFullScreen.class); // passing array index
				 * i.putExtra("picture", myImageAdapter.itemList.get(position));
				 * startActivity(i);
				 */
			}

		});

		/*
		 * Move to asyncTaskLoadFiles String ExternalStorageDirectoryPath =
		 * Environment .getExternalStorageDirectory() .getAbsolutePath();
		 * 
		 * String targetPath = ExternalStorageDirectoryPath + "/test/";
		 * 
		 * Toast.makeText(getApplicationContext(), targetPath,
		 * Toast.LENGTH_LONG).show(); File targetDirector = new
		 * File(targetPath);
		 * 
		 * File[] files = targetDirector.listFiles(); for (File file : files){
		 * myImageAdapter.add(file.getAbsolutePath()); }
		 */
		myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(myImageAdapter);
		myAsyncTaskLoadFiles.execute();

	}

	OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String prompt = "remove "
					+ (String) parent.getItemAtPosition(position);
			Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_SHORT)
					.show();

			myImageAdapter.remove(position);
			myImageAdapter.notifyDataSetChanged();

		}
	};

	@SuppressLint("NewApi")
	class MyFaceDetectionListener implements Camera.FaceDetectionListener {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			if (faces.length > 0) {
				Log.d("FaceDetection", "face detected: " + faces.length
						+ " Face 1 Location X: " + faces[0].rect.centerX()
						+ "Y: " + faces[0].rect.centerY());
			}
		}
	}

	public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void> {

		File targetDirector;
		ImageAdapter myTaskAdapter;

		public AsyncTaskLoadFiles(ImageAdapter adapter) {
			myTaskAdapter = adapter;
		}

		@Override
		protected void onPreExecute() {
			String ExternalStorageDirectoryPath = Environment
					.getExternalStorageDirectory().getAbsolutePath();

			String targetPath = ExternalStorageDirectoryPath
					+ "/Pictures/MoCam/";
			targetDirector = new File(targetPath);
			myTaskAdapter.clear();

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			File[] files = targetDirector.listFiles();
			for (File file : files) {
				publishProgress(file.getAbsolutePath());
				if (isCancelled())
					break;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			myTaskAdapter.add(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			myTaskAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
	}

	/*
	 * Store the file URL as it will be null after returning from camera app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on screen orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Image captured and saved to fileUri specified in the Intent
				/*
				 * Toast.makeText(this, "Image saved!" + intent.getData(),
				 * Toast.LENGTH_LONG).show();
				 */
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
				Toast.makeText(getApplicationContext(),
						"Capturing Image Cancelled!", Toast.LENGTH_LONG).show();
			} else {
				// Image capture failed, advise user
				Toast.makeText(getApplicationContext(),
						"Capturing Image Failed!", Toast.LENGTH_LONG).show();
			}
		}

		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Video captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Video saved to:\n" + intent.getData(),
						Toast.LENGTH_LONG).show();
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
				Toast.makeText(getApplicationContext(),
						"Capturing Vide Cancelled!", Toast.LENGTH_LONG).show();
			} else {
				// Video capture failed, advise user
				Toast.makeText(getApplicationContext(),
						"Capturing Video Failed!", Toast.LENGTH_LONG).show();
			}
		}
	}

	/*
	 * Recording video
	 */
	private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

		// set video quality
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
															// name

		// start the video capture Intent
		startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	/*
	 * Previewing recorded video
	 */
	private void previewVideo() {
		try {
			// hide image preview
			imgPreview.setVisibility(View.GONE);

			videoPreview.setVisibility(View.VISIBLE);
			videoPreview.setVideoPath(fileUri.getPath());
			// start playing
			videoPreview.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Checking device has camera hardware or not
	 */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// this device has no camera
			return false;
		}
	}

	/*
	 * Capturing Camera Image will launch camera app. request image capture
	 */
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	/*
	 * Display image from a path to ImageView
	 */
	private void previewCapturedImage() {
		try {
			// hide video preview
			videoPreview.setVisibility(View.GONE);

			imgPreview.setVisibility(View.VISIBLE);

			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
					options);

			imgPreview.setImageBitmap(bitmap);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

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

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);

		// Find the MenuItem that we know has the ShareActionProvider
		MenuItem item = menu.findItem(R.id.camera);

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

		case R.id.camera:
			captureImage();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
