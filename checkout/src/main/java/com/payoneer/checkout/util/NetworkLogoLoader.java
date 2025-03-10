/*
 *  Copyright (c) 2020 Payoneer Germany GmbH
 * https://www.payoneer.com
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package com.payoneer.checkout.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.payoneer.checkout.R;
import com.payoneer.checkout.core.PaymentException;
import com.payoneer.checkout.core.WorkerSubscriber;
import com.payoneer.checkout.core.WorkerTask;
import com.payoneer.checkout.core.Workers;
import com.payoneer.checkout.network.ImageConnection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

/**
 * Class for loading network logo images into an ImageView.
 * This loader will first check if a locally stored logo image is available, if not, the loader will
 * download the logo using the provided URL.
 */
public final class NetworkLogoLoader {

    private final static String NETWORKLOGO_FOLDER = "networklogos/";
    private final Map<String, String> localNetworkLogos = new HashMap<>();
    private final ImageConnection imageConnection = new ImageConnection();

    /*
     * This is private because this class should never have an object created. The loading functionality
     * will be accessed statically
     */
    private NetworkLogoLoader() {
    }

    /**
     * Get the instance of this LogoLoader
     *
     * @return the instance of this LogoLoader
     */
    public static NetworkLogoLoader getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Load the network logo given the networkCode and URL and store it into the ImageView.
     *
     * @param view ImageView in which to place the Bitmap
     * @param networkCode code of the payment network
     * @param networkLogoUrl pointing to the remote image
     */
    public static void loadNetworkLogo(ImageView view, String networkCode, URL networkLogoUrl) {
        getInstance().loadImageIntoView(view, networkCode, networkLogoUrl);
    }

    private void loadImageIntoView(ImageView view, String networkCode, URL networkLogoUrl) {
        final Context context = view.getContext();
        if (localNetworkLogos.size() == 0) {
            loadLocalNetworkLogos(view.getContext());
        }

        WorkerTask<Bitmap> task = WorkerTask.fromCallable(() -> loadLogo(context, networkCode, networkLogoUrl));
        task.subscribe(new WorkerSubscriber<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                onLoadBitmapSuccess(view, bitmap);
            }

            @Override
            public void onError(Throwable cause) {
                Log.w("sdk_ImageHelper", cause);
                // we ignore image loading failures
            }
        });
        Workers.getInstance().forImageTasks().execute(task);
    }

    private void onLoadBitmapSuccess(ImageView view, Bitmap bitmap) {
        try {
            view.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.w("checkout-sdk", e);
            // we ignore image loading failures which may occur if the device is out of memory
        }
    }

    private Bitmap loadLogo(Context context, String networkCode, URL networkLogoUrl) throws PaymentException {
        if (localNetworkLogos.containsKey(networkCode)) {
            String fileName = localNetworkLogos.get(networkCode);
            return loadBitmapFromFile(context, fileName);
        } else {
            return imageConnection.loadBitmap(networkLogoUrl);
        }
    }

    private Bitmap loadBitmapFromFile(Context context, String fileName) throws PaymentException {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            throw new PaymentException(e);
        }
    }

    private void loadLocalNetworkLogos(Context context) {
        synchronized (localNetworkLogos) {
            if (localNetworkLogos.size() != 0) {
                return;
            }
            Resources res = context.getResources();
            String[] ts;
            String[] ar = res.getStringArray(R.array.networklogos);
            for (String icon : ar) {
                ts = icon.split(",");
                localNetworkLogos.put(ts[0], NETWORKLOGO_FOLDER + ts[1]);
            }
        }
    }

    private static class InstanceHolder {
        static final NetworkLogoLoader INSTANCE = new NetworkLogoLoader();
    }
}
