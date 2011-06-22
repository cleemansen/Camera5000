package de.starquay.android.camera5000;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;

public class HelperMethods {
	
	private static final String TAG = "HelperMethods";
	
	private static Camera mCamera;
	
	/**
	 * Capture the picture from the camera
	 */
	public static void takePicture(Camera mCamera) {
		mCamera.takePicture(null, null, mPictureCallback);
		
	}

	static Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.S");
				HelperMethods.StoreByteImage(imageData, 50, sdf.format(cal.getTime()));

				c.startPreview();
			}
		}
	};
	
	public static boolean StoreByteImage(byte[] imageData, int quality, String fileName) {

		File sdImageMainDirectory = Environment.getExternalStorageDirectory().getAbsoluteFile();

		FileOutputStream fileOutputStream = null;
		try {

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;

			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

			File finalDir = new File(sdImageMainDirectory.getAbsoluteFile().getPath() + "/FreqCap");
			finalDir.mkdir();
			fileOutputStream = new FileOutputStream(finalDir + "/" + fileName + ".jpg");

			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found!", e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IO!", e);
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * calcs the optimal dimension for the camera preview on the display
	 * 
	 * @param sizes
	 * @param w
	 * @param h
	 * @return
	 */
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
}
