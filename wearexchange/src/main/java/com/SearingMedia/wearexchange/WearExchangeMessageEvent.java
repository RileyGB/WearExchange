package com.SearingMedia.wearexchange;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import java.io.Serializable;

public class WearExchangeMessageEvent implements Serializable, MessageEvent {

    // Variables
    private int requestId;
    private String path;
    private byte[] data;
    private String sourceNodeId;

    // ******************************
    // Constructor
    // ******************************
    public WearExchangeMessageEvent(MessageEvent messageEvent) {
        if(messageEvent == null) {
            Log.e(getClass().getSimpleName(), "MessageEvent is null in constructor");
            return;
        }

        requestId = messageEvent.getRequestId();
        path = messageEvent.getPath();
        data = messageEvent.getData();
        sourceNodeId = messageEvent.getSourceNodeId();
    }

    // ******************************
    // Implementations
    // ******************************
    @Override
    public int getRequestId() {
        return requestId;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String getSourceNodeId() {
        return sourceNodeId;
    }
}
