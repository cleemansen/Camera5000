package de.starquay.android.camera5000;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.Toast;

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
			// http://stackoverflow.com/questions/2043019/image-processing-on-android
			if (imageData != null) {

				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.S");
				HelperMethods.StoreByteImage(imageData, 100, sdf.format(cal.getTime()) + ".jpg");

				c.startPreview();

			}
		}
	};

	/**
	 * From original Android Camera App
	 * See: http://android.git.kernel.org/?p=platform/packages/apps/Camera.git;a=blob;f=src/com/android/camera/ImageManager.java;h=76a6d1dffdcfb91f2c55032ce14f7cd9ecf7962c;hb=HEAD
	 * @param imageData
	 * @param quality
	 * @param filename
	 * @return
	 */
	public static boolean StoreByteImage(byte[] imageData, int quality, String filename) {
		String directory = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/FreqCap2";
		Bitmap source = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		
		OutputStream outputStream = null;
		String filePath = directory + "/" + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(directory, filename);
			outputStream = new FileOutputStream(file);
			if (source != null) {
				source.compress(CompressFormat.JPEG, 95, outputStream);
			} else {
				outputStream.write(imageData);
				// degree[0] = getExifOrientation(filePath);
			}
//			outputStream.flush();
//			outputStream.close();
			Log.e(TAG, file.getAbsolutePath());
		} catch (FileNotFoundException ex) {
			Log.w(TAG, ex);
			return false;
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return false;
		} finally {
			closeSilently(outputStream);
		}
		return true;

//		File sdImageMainDirectory = Environment.getExternalStorageDirectory().getAbsoluteFile();
//
//		FileOutputStream fileOutputStream = null;
//		try {
//
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inSampleSize = 4;
//
//			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);// ,
//																							// options);
//
//			File finalDir = new File(sdImageMainDirectory.getAbsoluteFile().getPath() + "/FreqCap");
//			finalDir.mkdir();
//			fileOutputStream = new FileOutputStream(finalDir + "/" + fileName + ".jpg");
//
//			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
//
//			myImage.compress(CompressFormat.JPEG, quality, bos);
//
//			bos.flush();
//			bos.close();
//
//		} catch (FileNotFoundException e) {
//			Log.e(TAG, "File not found!", e);
//			e.printStackTrace();
//		} catch (IOException e) {
//			Log.e(TAG, "IO!", e);
//			e.printStackTrace();
//		}
//
//		return true;
	}
	private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;

	public static Uri test(ContentResolver cr, String title, long dateTaken, Location location, String directory, String filename, Bitmap source, byte[] jpegData, int[] degree) {
		OutputStream outputStream = null;
		String filePath = directory + "/" + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(directory, filename);
			outputStream = new FileOutputStream(file);
			if (source != null) {
				source.compress(CompressFormat.JPEG, 75, outputStream);
				degree[0] = 0;
			} else {
				outputStream.write(jpegData);
				// degree[0] = getExifOrientation(filePath);
			}
		} catch (FileNotFoundException ex) {
			Log.w(TAG, ex);
			return null;
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return null;
		} finally {
			closeSilently(outputStream);
		}

		// Read back the compressed file size.
		long size = new File(directory, filename).length();

		ContentValues values = new ContentValues(9);
		values.put(Images.Media.TITLE, title);

		// That filename is what will be handed to Gmail when a user shares a
		// photo. Gmail gets the name of the picture attachment from the
		// "DISPLAY_NAME" field.
		values.put(Images.Media.DISPLAY_NAME, filename);
		values.put(Images.Media.DATE_TAKEN, dateTaken);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.ORIENTATION, degree[0]);
		values.put(Images.Media.DATA, filePath);
		values.put(Images.Media.SIZE, size);

		if (location != null) {
			values.put(Images.Media.LATITUDE, location.getLatitude());
			values.put(Images.Media.LONGITUDE, location.getLongitude());
		}
		
		return cr.insert(STORAGE_URI, values);

	}

	public static void closeSilently(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
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
