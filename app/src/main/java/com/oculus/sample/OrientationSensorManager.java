package com.oculus.sample;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensorManager {

    private static final float LOWPASS_ALPHA = 0.1f;

    private final Context mContext;
    private final SensorManager mSensorManager;
    private float[] mAccellValues = {0f, 0f, 0f};
    private float[] mMagnetValues = {0f, 0f, 0f};
    private float[] mOrientationValues = {0f, 0f, 0f};

    private final float[] mOriginalRotationMatrix = new float[9];
    private final float[] mRemappedRotationMatrix = new float[9];

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    lowPassFilter(mAccellValues, event.values);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    lowPassFilter(mMagnetValues, event.values);
                    break;
            }

            updateOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };

    private OnNewOrientationListener mListener;

    public OrientationSensorManager(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() {
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public float getAzimuth() {
        return mOrientationValues[0];
    }

    public float getPitch() {
        return mOrientationValues[1];
    }

    public float getRoll() {
        return mOrientationValues[2];
    }

    public void setListener(OnNewOrientationListener listener) {
        mListener = listener;
    }

    private void updateOrientation() {
        SensorManager.getRotationMatrix(mOriginalRotationMatrix, null, mAccellValues, mMagnetValues);
        SensorManager.remapCoordinateSystem(mOriginalRotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                mRemappedRotationMatrix);
        SensorManager.getOrientation(mRemappedRotationMatrix, mOrientationValues);

        if (mListener != null) {
            mListener.onNewOrientation(getAzimuth(), getPitch(), getRoll());
        }
    }

    private void lowPassFilter(float[] inOutValues, float[] newValues) {
        final int size = inOutValues.length;
        for (int i = 0; i < size; i++) {
            inOutValues[i] += LOWPASS_ALPHA * (newValues[i] - inOutValues[i]);
        }
    }

    public interface OnNewOrientationListener {
        void onNewOrientation(float azimuth, float pitch, float roll);
    }
}
