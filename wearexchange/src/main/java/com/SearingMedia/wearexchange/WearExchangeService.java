package com.SearingMedia.wearexchange;

import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.greenrobot.event.EventBus;

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
     *
     * @return
     */
    protected abstract List<String> getKnownMessagePathList();

    /**
     * When a local broadcast cannot be sent, the implementer can send an intent
     *
     * @param messageEvent
     */
    protected abstract void sendIntentMessage(MessageEvent messageEvent);

    // ****************************
    // Implementations
    // ****************************
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Guard Clauses
        if (knownMessagePathList == null || knownMessagePathList.isEmpty()) {
            Log.e(getClass().getSimpleName(), "KnownMessagePathList is empty/null");
            return;
        }
        else if (messageEvent == null || messageEvent.getPath() == null) {
            Log.e(getClass().getSimpleName(), "MessageEvent or its path is null");
            return;
        }

        for (String knownMessagePath : knownMessagePathList) {
            if (messageEvent.getPath().equals(knownMessagePath)) {
                // Try to send a local broadcast, if it fails, send an intent
                if (canSendEventBusMessage()) {
                    EventBus.getDefault().post(new WearExchangeMessageEvent(messageEvent));
                } else {
                    sendIntentMessage(messageEvent);
                }

                break;
            }
        }
    }

    private boolean canSendEventBusMessage() {
        return EventBus.getDefault().hasSubscriberForEvent(WearExchangeMessageEvent.class);
    }
}