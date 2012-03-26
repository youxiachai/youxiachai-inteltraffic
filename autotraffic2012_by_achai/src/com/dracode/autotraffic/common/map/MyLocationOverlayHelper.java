package com.dracode.autotraffic.common.map;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

import com.dracode.andrdce.ct.UserAppSession;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.LocationHelper.FixedLocationInfo;
import com.mapabc.mapapi.GeoPoint;
import com.mapabc.mapapi.MapView;
import com.mapabc.mapapi.MyLocationOverlay;
import com.mapabc.mapapi.Projection;

/**
 * 自定义MyLocationOverlay。更换MyLocation图标。
 */
public class MyLocationOverlayHelper extends MyLocationOverlay {

	private Location mLocation;
	protected final Paint mPaint = new Paint();
	protected final Paint mCirclePaint = new Paint();
	private Bitmap gps_marker = null;
	private Point mMapCoords = new Point();
	private final float gps_marker_CENTER_X;
	private final float gps_marker_CENTER_Y;
	private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	public Context mContext;
	private OnLocationFoundEvent theEvt;

	public static final int FIRST_LOCATION = 1001;

	public MyLocationOverlayHelper(Context context, MapView mapView,
			OnLocationFoundEvent evt) {
		super(context, mapView);
		mContext = context;
		theEvt = evt;
		gps_marker = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.marker_gpsvalid)).getBitmap();
		gps_marker_CENTER_X = gps_marker.getWidth() / 2 - 0.5f;
		gps_marker_CENTER_Y = gps_marker.getHeight() / 2 - 0.5f;
	}

	public static interface OnLocationFoundEvent {
		public void onLocationFound(int centerX, int centerY);
	}

	@Override
	public boolean runOnFirstFix(final Runnable runnable) {
		if (mLocation != null) {
			new Thread(runnable).start();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
	}

	@Override
	public void onLocationChanged(Location location) {

		double x, y;
		x=location.getLongitude();
		y=location.getLatitude();
		FixedLocationInfo fli = LocationHelper.getRelateFixedLocaton(x, y);
		if(fli!=null){
			location.setLongitude(fli.lon);
			location.setLatitude(fli.lat);
		}
		
		mLocation = location;
		for (final Runnable runnable : mRunOnFirstFix) {
			new Thread(runnable).start();
		}
		mRunOnFirstFix.clear();
		super.onLocationChanged(location);
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView,
			final Location mLocation, GeoPoint point, long time) {
		Projection pj = mapView.getProjection();
		if (mLocation != null) {
			mMapCoords = pj.toPixels(point, null);
			final float radius = pj.metersToEquatorPixels(mLocation
					.getAccuracy());
			this.mCirclePaint.setAntiAlias(true);
			this.mCirclePaint.setARGB(35, 131, 182, 222);
			this.mCirclePaint.setAlpha(50);
			this.mCirclePaint.setStyle(Style.FILL);
			canvas.drawCircle(mMapCoords.x, mMapCoords.y, radius,
					this.mCirclePaint);
			this.mCirclePaint.setARGB(225, 131, 182, 222);
			this.mCirclePaint.setAlpha(150);
			this.mCirclePaint.setStyle(Style.STROKE);
			canvas.drawCircle(mMapCoords.x, mMapCoords.y, radius,
					this.mCirclePaint);
			canvas.drawBitmap(gps_marker, mMapCoords.x - gps_marker_CENTER_X,
					mMapCoords.y - gps_marker_CENTER_Y, this.mPaint);
		}
	}

	protected Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			GeoPoint myPoint;
			switch (msg.what) {
			case FIRST_LOCATION:
				myPoint = getMyLocation();
				if (myPoint != null) {
					if (theEvt != null)
						theEvt.onLocationFound(myPoint.getLongitudeE6(),
								myPoint.getLatitudeE6());
				}
				break;
			}
		}
	};

	boolean firstLocateAnimateAdded = false;

	public void animateToLocation() {
		if (!firstLocateAnimateAdded) {
			runOnFirstFix(new Runnable() {
				public void run() {
					handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
				}
			});
			firstLocateAnimateAdded = true;
		}
		UserAppSession.showToast(mContext, "正在定位...");
		handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
	}

}
