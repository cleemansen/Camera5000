package de.starquay.android.camera5000;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Screen extends Activity implements SurfaceHolder.Callback, OnClickListener, OnSharedPreferenceChangeListener {

	private Camera mCamera;
	private Size mPreviewSize;

	/** TextView of the remaining numbers of pictures in burst mode */
	private TextView burstCntView;
	/** TextView of the count down until next picture in burst mode */
	private TextView burstNextView;
	/** The Count Down Object */
	private CountDownAction countDown;

	/** A list of preferences, which have been changed by the user */
	private List<String> newPrefs4CamBuffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** Window features */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		/** Layout */
		setContentView(R.layout.camera_surface);
		SurfaceView surface = (SurfaceView) findViewById(R.id.surface_camera);
		SurfaceHolder holder = surface.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		/** click listener on the surface */
		surface.setOnClickListener(this);

		/** text fields */
		burstCntView = (TextView) findViewById(R.id.burstCnt);
		burstNextView = (TextView) findViewById(R.id.burstNext);

		// DOES NOT WORK WELL
		// SharedPreferences settings =
		// PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// settings.registerOnSharedPreferenceChangeListener(this);
		// newPrefs4CamBuffer = new LinkedList<String>();

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		setCamera(mCamera);
		applySettings();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mCamera.release();
			setCamera(null);
		}
	}

	/**
	 * App will be stopped. Check if a count down is running
	 */
	@Override
	protected void onStop() {
		if (countDown != null)
			countDown.cancel();
		super.onStop();
	}

	/**
	 * Sets the camera and his parameters (i.e. optimal preview size)
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			List<Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

			if (mSupportedPreviewSizes != null) {
				// get the resolution of the display
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				mPreviewSize = HelperMethods.getOptimalPreviewSize(mSupportedPreviewSizes, metrics.widthPixels, metrics.heightPixels);
			}
			applySettings();
		}
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs:
			startActivityForResult(readCameraParametersAndPrepareAppSettings(), RESULT_OK);
			break;
		case R.id.test_button:
			SurfaceView view = new SurfaceView(getBaseContext());
			try {
				mCamera.setPreviewDisplay(view.getHolder());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// mCamera.startPreview();
			// startBurstSession(5, 5);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 0 && requestCode == RESULT_OK) {
			applySettings();
		}
	};

	/**
	 * Reads the supported Parameters from the camera and prepares an Intent for
	 * the Preferences Screen
	 * 
	 * @return
	 */
	private Intent readCameraParametersAndPrepareAppSettings() {
		Intent showPrefs = new Intent(getBaseContext(), Preferences.class);
		Parameters para = mCamera.getParameters();

		/** Scene Modes */
		String key = this.getString(R.string.key_sceneMode);
		List<String> listOfSupportedModes = para.getSupportedSceneModes();
		showPrefs.putExtra(key, listOfSupportedModes.toArray(new String[listOfSupportedModes.size()]));
		/** Color Effects */
		key = this.getString(R.string.key_colorEffect);
		listOfSupportedModes = para.getSupportedColorEffects();
		showPrefs.putExtra(key, listOfSupportedModes.toArray(new String[listOfSupportedModes.size()]));
		/** White Balance */
		key = this.getString(R.string.key_whiteBalance);
		listOfSupportedModes = para.getSupportedWhiteBalance();
		showPrefs.putExtra(key, listOfSupportedModes.toArray(new String[listOfSupportedModes.size()]));
		/** Focus Mode */
		key = this.getString(R.string.key_focusMode);
		listOfSupportedModes = para.getSupportedFocusModes();
		showPrefs.putExtra(key, listOfSupportedModes.toArray(new String[listOfSupportedModes.size()]));

		/** Picture Size */
		key = this.getString(R.string.key_picQuali);
		List<Size> picSize = para.getSupportedPictureSizes();
		String[] picSizeArray = new String[picSize.size()];
		for (int i = 0; i < picSize.size(); i++) {
			Size size = picSize.get(i);
			picSizeArray[i] = size.width + "x" + size.height;
		}
		showPrefs.putExtra(key, picSizeArray);

		return showPrefs;

	}

	/**
	 * Reacts on changed shared preferences
	 * 
	 * THIS DOES NOT WORK WELL - CHANGED. It is not possible to change only one
	 * camera parameter, you have to set all parameters at one time.
	 * 
	 * The LinkedList applyTheseSettings is a Work-Around: I couldn't apply the
	 * changes directly to the camera, because the camera was released by
	 * onPause().
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		/** Color Effects */
		String look4key = this.getString(R.string.key_colorEffect);
		if (key.equals(look4key)) {
			newPrefs4CamBuffer.add(key);
		}
		/** Scene Modes */
		look4key = this.getString(R.string.key_sceneMode);
		if (key.equals(look4key)) {
			newPrefs4CamBuffer.add(key);
		}
		/** White Balance */
		look4key = this.getString(R.string.key_whiteBalance);
		if (key.equals(look4key)) {
			newPrefs4CamBuffer.add(key);
		}
		/** Focus Mode */
		look4key = this.getString(R.string.key_focusMode);
		if (key.equals(look4key)) {
			newPrefs4CamBuffer.add(key);
		}
		/** Picture Quality */
		look4key = this.getString(R.string.key_picQuali);
		if (key.equals(look4key)) {
			newPrefs4CamBuffer.add(key);
		}

	}

	/**
	 * Applies the settings done by the user with the preference activity
	 */
	private void applySettings() {
		Parameters para = mCamera.getParameters();
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String infoMsg = "";

		/** Color Effects */
		String storedKey = this.getString(R.string.key_colorEffect);
		para.setColorEffect(shared.getString(storedKey, Parameters.EFFECT_NONE));
		infoMsg += "ColorFx: " + shared.getString(storedKey, Parameters.EFFECT_NONE) + "\n";
		/** Scene Mode */
		storedKey = this.getString(R.string.key_sceneMode);
		para.setSceneMode(shared.getString(storedKey, Parameters.SCENE_MODE_AUTO));
		infoMsg += "Scene: " + shared.getString(storedKey, Parameters.EFFECT_NONE) + "\n";
		/** White Balance */
		storedKey = this.getString(R.string.key_whiteBalance);
		para.setWhiteBalance(shared.getString(storedKey, Parameters.SCENE_MODE_AUTO));
		infoMsg += "White Balance: " + shared.getString(storedKey, Parameters.EFFECT_NONE) + "\n";
		/** Focus Mode */
		storedKey = this.getString(R.string.key_focusMode);
		para.setFocusMode(shared.getString(storedKey, Parameters.FOCUS_MODE_AUTO));
		infoMsg += "FocusMode: " + shared.getString(storedKey, Parameters.EFFECT_NONE) + "\n";
		/** Picture Quality */
		storedKey = this.getString(R.string.key_picQuali);
		int posInQualiArray = Integer.valueOf(shared.getString(storedKey, "0"));
		int width = para.getSupportedPictureSizes().get(posInQualiArray).width;
		int height = para.getSupportedPictureSizes().get(posInQualiArray).height;
		para.setPictureSize(width, height);
		//*********
		List<Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

		if (mSupportedPreviewSizes != null) {
			// get the resolution of the display
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			mPreviewSize = HelperMethods.getOptimalPreviewSize(mSupportedPreviewSizes, metrics.widthPixels, metrics.heightPixels);
		}
		//************
		para.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		DecimalFormat df = new DecimalFormat("0.0");
		infoMsg += "Pic.Quali: " + df.format((float)((float)width*(float)height)/1000000f) + "MP\n";
		
		TextView info = (TextView)findViewById(R.id.infoView);
		info.setText(infoMsg);

		mCamera.setParameters(para);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

		mCamera.setParameters(parameters);
		mCamera.startPreview();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e("surfaceCreated", "IOException caused by setPreviewDisplay()", exception);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	@Override
	public void onClick(View v) {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (shared.getBoolean(this.getString(R.string.key_burstMode), false)) {
			int shots = Integer.valueOf(shared.getString(this.getString(R.string.key_burstNumberValue), "1"));
			int frequency = Integer.valueOf(shared.getString(this.getString(R.string.key_burstIntervalValue), "5"));
			startBurstSession(shots, frequency);
		} else {
			HelperMethods.takePicture(mCamera);
		}
	}

	/**
	 * starts taking pictures in an interval of @link{frequency} seconds and
	 * 
	 * @link{shots pictures
	 * 
	 * @param shots
	 *            : number of pictures
	 * @param frequency
	 *            : pause between two shoots in seconds!
	 */
	private void startBurstSession(int shots, int frequency) {
		/** init a countdown for the user */
		countDown = new CountDownAction(frequency * 1000, 1000, shots);
		/** this will take the pictures in the given interval */
		// mBurstHandler = new BurstHandler(this, shots, frequency);

	}

	/**
	 * Inner Class for CountDown in Burst mode
	 * 
	 * @author Clemens
	 * 
	 */
	public class CountDownAction extends CountDownTimer {

		private int burstCnt;

		public CountDownAction(long millisInFuture, long countDownInterval, int burstCnt) {
			super(millisInFuture, countDownInterval);
			this.burstCnt = burstCnt;
			// start immediately with the first picture
			HelperMethods.takePicture(mCamera);
			burstCntView.setText("Img Cnt: " + burstCnt);
			this.start();
		}

		@Override
		public void onFinish() {
			burstNextView.setText("finish");
			HelperMethods.takePicture(mCamera);
			burstCnt--;
			burstCntView.setText("Img Cnt: " + burstCnt);
			if (burstCnt > 0) {
				this.start();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			burstNextView.setText("Timer: " + millisUntilFinished / 1000);
		}
	}

}
