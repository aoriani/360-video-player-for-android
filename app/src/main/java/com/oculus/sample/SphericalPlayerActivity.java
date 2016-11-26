/**
 * Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * Licensed under the Creative Commons CC BY-NC 4.0 Attribution-NonCommercial
 * License (the "License"). You may obtain a copy of the License at
 * https://creativecommons.org/licenses/by-nc/4.0/.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.oculus.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.oculus.sample.player.SphericalVideoPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SphericalPlayerActivity extends AppCompatActivity
        implements OrientationSensorManager.OnNewOrientationListener {

    private final String SAMPLE_VIDEO_PATH =
            "android.resource://com.oculus.sample/raw/" + R.raw.sample360;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0x1;
    private static final double EIGHTY_FIVE_DEGREE = 85 * Math.PI / 180;

    private SphericalVideoPlayer videoPlayer;
    private OrientationSensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        videoPlayer = (SphericalVideoPlayer) findViewById(R.id.spherical_video_player);
        videoPlayer.setVideoURIPath(SAMPLE_VIDEO_PATH);
        videoPlayer.playWhenReady();

        mSensorManager = new OrientationSensorManager(this);
        mSensorManager.setListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestExternalStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.stop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            init();
        }
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(
                context,
                msg,
                Toast.LENGTH_SHORT).show();
    }

    private void init() {
        videoPlayer.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                videoPlayer.initRenderThread(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                videoPlayer.releaseResources();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        videoPlayer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            return;
        }

        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            toast(this, "Access not granted for reading video file :(");
            return;
        }

        init();
    }

    public static String readRawTextFile(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader buf = new BufferedReader(reader);
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public void onNewOrientation(float azimuth, float pitch, float roll) {
        final double phi = Math.max(-EIGHTY_FIVE_DEGREE, Math.min(pitch, EIGHTY_FIVE_DEGREE)) + Math.PI/2.0;
        final double theta = azimuth;

        videoPlayer.setOrientation(new SphericalVideoPlayer.Orientation(phi, theta));
    }
}
