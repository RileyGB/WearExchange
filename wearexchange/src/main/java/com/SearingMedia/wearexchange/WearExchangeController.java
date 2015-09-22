package com.SearingMedia.wearexchange;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import de.greenrobot.event.EventBus;

public class WearExchangeController implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener, NodeApi.NodeListener {
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

        if(!EventBus.getDefault().isRegistered(this)) {
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
            Wearable.NodeApi.removeListener(googleApiClient, this);

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

        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    // **********************************
    // Helpers
    // **********************************
    public void sendMessage(final String path, final String text) {
        if(text == null) {
            Log.e(getClass().getSimpleName(), "Null text, could not send message");
            return;
        }

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
        Wearable.NodeApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onPeerConnected(Node node) {
        // Guard Clause
        if(wearExchangeInterface == null || googleApiClient == null || node == null) {
            return;
        }

        if(isConnected()) {
            wearExchangeInterface.wearConnectionMade(googleApiClient.getSessionId(), node.getId());
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        // Guard Clause
        if(wearExchangeInterface == null || googleApiClient == null || node == null) {
            return;
        }

        wearExchangeInterface.wearConnectionLost(googleApiClient.getSessionId(), node.getId());
    }

    // **********************************
    // EventBus Events
    // **********************************
    public void onEvent(WearExchangeMessageEvent wearExchangeMessageEvent) {
        // Guard Clause
        if(wearExchangeInterface == null) {
            return;
        }

        wearExchangeInterface.messageReceived(wearExchangeMessageEvent);
    }
}
