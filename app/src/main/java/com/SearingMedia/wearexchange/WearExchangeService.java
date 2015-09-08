package com.SearingMedia.wearexchange;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public abstract class WearExchangeService extends WearableListenerService implements MessageApi.MessageListener {
    // Variables
    private List<String> knownMessagePathList;

    // ****************************
    // Constructor
    // ****************************
    public WearExchangeService() {
        this.knownMessagePathList = getKnownMessagePathList();
    }

    // ****************************
    // Abstract Methods
    // ****************************

    /**
     * A list of paths that should be filtered (received)
     * @return
     */
    protected abstract List<String> getKnownMessagePathList();

    /**
     * When a local broadcast cannot be sent, the implementer can send an intent
     * @param messageEvent
     */
    protected abstract void sendNewIntentMessage(MessageEvent messageEvent);

    // ****************************
    // Implementations
    // ****************************
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Guard Clause
        if(knownMessagePathList == null || knownMessagePathList.isEmpty()) {
            return;
        }

        for(String knownMessagePath : knownMessagePathList) {
            if (messageEvent.getPath().equals(knownMessagePath)) {
                // Try to send a local broadcast, if it fails, send an intent
                if(!WearExchangeBroadcaster.sendMessageBroadcast(messageEvent, this)) {
                    sendNewIntentMessage(messageEvent);
                }

                break;
            }
        }
    }
}