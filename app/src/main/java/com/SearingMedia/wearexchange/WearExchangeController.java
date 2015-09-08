package com.SearingMedia.wearexchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

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
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        connect();

        LocalBroadcastManager.getInstance(wearExchangeInterface.getWearContext()).registerReceiver(broadcastReceiver, new IntentFilter(WearExchangeBroadcaster.WEAR_EXCHANGE_BROADCAST));
    }

    public void connect() {
        if (!isConnected()) {
            googleApiClient.connect();
        }
    }

    public void disconnect() {
        if (googleApiClient != null) {
            Wearable.MessageApi.removeListener(googleApiClient, this);

            if (isConnected()) {
                googleApiClient.disconnect();
            }
        }

        LocalBroadcastManager.getInstance(wearExchangeInterface.getWearContext()).unregisterReceiver(broadcastReceiver);
    }

    public void destroy() {
        disconnect();

        if(googleApiClient != null) {
            googleApiClient.unregisterConnectionCallbacks(this);
        }
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
        return googleApiClient != null &&
                (googleApiClient.isConnected() || googleApiClient.isConnecting());
    }

    // **********************************
    // Implementations
    // **********************************
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        wearExchangeInterface.messageReceived(messageEvent);
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

    // **********************************
    // Inner Declarations
    // **********************************
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WearExchangeBroadcaster.WEAR_EXCHANGE_BROADCAST)) {
                onMessageReceived((MessageEvent) intent.getSerializableExtra(WearExchangeBroadcaster.BROADCAST_MESSAGE_KEY));
            }
        }
    };
}
