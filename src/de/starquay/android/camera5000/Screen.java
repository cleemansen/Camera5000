package de.starquay.android.camera5000;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Screen extends Activity implements SurfaceHolder.Callback, OnClickListener {

	private Camera mCamera;
	private Parameters para;
	private Size mPreviewSize;

	private TextView burstCntView;
	private TextView burstNextView;
	private CountDownAction countDown;

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
			//mCamera.startPreview();
			//startBurstSession(5, 5);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Applies the stored settings
	 */
	private void applySettings() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		String s = settings.getString("colorEffectsPrefList", Parameters.EFFECT_NONE);
		para.setColorEffect(s);
		s = settings.getString("sceneModesPrefList", Parameters.SCENE_MODE_AUTO);
		para.setSceneMode(s);
		mCamera.setParameters(para);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 0 && requestCode == RESULT_OK) {
			
		}
	};

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
			readCameraParametersAndPrepareAppSettings();
		}
	}
	
	/**
	 * Reads the supported Parameters from the camera
	 * and prepares an Intent for the Preferences Screen
	 * @return
	 */
	private Intent readCameraParametersAndPrepareAppSettings() {
		Intent showPrefs = new Intent(getBaseContext(), Preferences.class);
		para = mCamera.getParameters();
		
		/** Scene Modes */
		List<String> sceneModes = para.getSupportedSceneModes();
		String[] t = sceneModes.toArray(new String[sceneModes.size()]);
		showPrefs.putExtra("Scene Modes", para.getSupportedSceneModes().toArray(new String[sceneModes.size()]));
		/** Color Effects */
		List<String> colorEffects = para.getSupportedColorEffects();
		showPrefs.putExtra("Color Effects", colorEffects.toArray(new String[colorEffects.size()]));
		/** Picture Size */
		List<Size> picSize = para.getSupportedPictureSizes();
		String[] picSizeArray = new String[picSize.size()];
		for(int i = 0; i < picSize.size(); i++) {
			Size size = picSize.get(i);
			picSizeArray[i] = size.width + "x" + size.height;
		}
		showPrefs.putExtra("Picture Size", picSizeArray);
		/** Focus Mode */
		List<String> focusMode = para.getSupportedFocusModes();
		showPrefs.putExtra("Focus Mode", focusMode.toArray(new String[focusMode.size()]));
		/** White Balance */
		List<String> whiteBalance = para.getSupportedWhiteBalance();
		showPrefs.putExtra("Focus Mode", whiteBalance.toArray(new String[whiteBalance.size()]));
		
		return showPrefs;

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
		int shots = 4;
		int frequency = 8;
		startBurstSession(shots, frequency);
	}

	/**
	 * starts taking pictures in an interval of @link{frequency} seconds and
	 * @link{shots} pictures
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
	 * Inner Class for CountDown of the TextView TIMER
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
