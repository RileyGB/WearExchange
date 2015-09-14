package com.SearingMedia.wearexchange;

import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import de.greenrobot.event.EventBus;

public class WearExchangeController implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {
    // Variables
    private WearExchangeInterface wearExchangeInterface;
    private GoogleApiClient googleApiClient;

    // **********************************
    // Constructors
    // **********************************
    public WearExchangeController(WearExchangeInterface wearExchangeInterface) {
        this.wearExchangeInterface = wearExchangeInterface;
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

        EventBus.getDefault().register(this);
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

        EventBus.getDefault().unregister(this);
    }

    // **********************************
    // Helpers
    // **********************************
    public void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            googleApiClient, node.getId(), path, text.getBytes()).await();
                }
            }
        }).start();
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
    }

    @Override
    public void onConnectionSuspended(int cause) {
        switch (cause) {
            case CAUSE_NETWORK_LOST:
            case CAUSE_SERVICE_DISCONNECTED:
                wearExchangeInterface.wearConnectionLost(cause);
                break;
        }
    }

    public void onEvent(WearExchangeMessageEvent wearExchangeMessageEvent) {
        wearExchangeInterface.messageReceived(wearExchangeMessageEvent);
    }
}
