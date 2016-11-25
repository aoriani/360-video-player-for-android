package com.oculus.sample;

import android.util.Log;

import java.util.Arrays;

public class SensorSmoother {
    private static final int READINGS_SIZE = 10;
    private static final double EIGHTY_FIVE_DEGREE = 85 * Math.PI / 180;
    private static final double LOW_PASS_FILTER_FACTOR = 0.05;

    private double[] azimuthReadings = new double[READINGS_SIZE];
    private double[] pitchReadings = new double[READINGS_SIZE];
    private int dataIndex = 0;
    private double azimuthSum = 0.0;
    private double pitchSum = 0.0;
    private double phi = Math.PI / 2;
    private double theta = Math.PI / 2;

    public SensorSmoother() {
        Arrays.fill(azimuthReadings, 0.0);
        Arrays.fill(pitchReadings, 0.0);
    }

    public void addReadings(float[] orientationValues) {
        final double newAzimuth = orientationValues[0];
        final double newPitch = orientationValues[1];

        azimuthSum -= azimuthReadings[dataIndex];
        pitchSum -= pitchReadings[dataIndex];

        azimuthSum += newAzimuth;
        pitchSum += newPitch;
        azimuthReadings[dataIndex] = newAzimuth;
        pitchReadings[dataIndex] = newPitch;
        dataIndex = (dataIndex + 1) % READINGS_SIZE;

        double meanAzimuth = azimuthSum / READINGS_SIZE;
        double meanPitch = pitchSum / READINGS_SIZE;

        double newPhi = Math.max(-EIGHTY_FIVE_DEGREE, Math.min(meanPitch, EIGHTY_FIVE_DEGREE)) +  Math.PI/2.0;
        double newTheta = meanAzimuth;

        phi += LOW_PASS_FILTER_FACTOR * (newPhi - phi);
        theta += LOW_PASS_FILTER_FACTOR * (newTheta - theta);
        Log.d("ORIANI", "AZ " + String.format("%.2f", Math.toDegrees(theta)));
    }


    public Orientation getOrientation() {
        return new Orientation(phi, theta);
    }


    public static class Orientation {
        public final double phi;
        public final double theta;

        Orientation(double phi, double theta) {
            this.phi = phi;
            this.theta = theta;
        }
    }
}
