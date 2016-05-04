package com.SearingMedia.wearexchange;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.greenrobot.event.EventBus;

public class WearExchangeController implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {
    // Constants
    public static final String NO_NODE_ID = "none";

    // Variables
    private WearExchangeInterface wearExchangeInterface;
    private GoogleApiClient googleApiClient;
    private ScheduledExecutorService scheduledExecutorService;

    // **********************************
    // Constructors
    // **********************************
    public WearExchangeController(WearExchangeInterface wearExchangeInterface) {
        this.wearExchangeInterface = wearExchangeInterface;

        scheduledExecutorService = Executors.newScheduledThreadPool(4);
    }

    // **********************************
    // Lifecycle
    // **********************************
    public void create() {
        googleApiClient = new GoogleApiClient.Builder(wearExchangeInterface.getWearContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        connect();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void connect() {
        if (!isConnected() && !isConnecting()) {
            googleApiClient.connect();
        }
    }

    public void disconnect() {
        if (googleApiClient != null) {
            Wearable.MessageApi.removeListener(googleApiClient, this);

            if (isConnected() || isConnecting()) {
                googleApiClient.disconnect();
            }
        }
    }

    public void destroy() {
        disconnect();

        if (googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(this);
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    // **********************************
    // Helpers
    // **********************************
    public void sendMessage(final String path, final String data) {
        if (data == null) {
            Log.e(getClass().getSimpleName(), "Null text, could not send message");
            return;
        }

        final byte[] messageBytes = data.getBytes();

        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, messageBytes).await();
                    }
                } catch (OutOfMemoryError e) {
                    // Necessary for some Samsung devices
                    Log.e(getClass().getSimpleName(), "Out of memory error while sending message");
                }
            }
        });
    }

    public boolean isConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    public boolean isConnecting() {
        return googleApiClient != null && googleApiClient.isConnecting();
    }

    // **********************************
    // Implementations
    // **********************************
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        // Unhandled
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(googleApiClient, this);
        new VerifyWearableConnectedTask().execute();
    }

    @Override
    public void onConnectionSuspended(int cause) {
       // Do nothing
    }

    // **********************************
    // EventBus Events
    // **********************************
    public void onEvent(WearExchangeMessageEvent wearExchangeMessageEvent) {
        // Guard Clause
        if (wearExchangeInterface == null) {
            return;
        }

        wearExchangeInterface.messageReceived(wearExchangeMessageEvent);
    }

    // **********************************
    // Async Tasks
    // **********************************
    private class VerifyWearableConnectedTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

            if (wearExchangeInterface == null || googleApiClient == null) {
                return null;
            }

            if (nodes != null && !nodes.getNodes().isEmpty()) {
                wearExchangeInterface.wearConnectionMade(nodes.getNodes().get(0).getId());
            }
            else {
                wearExchangeInterface.wearConnectionLost("");
            }

            return null;
        }
    }
}
