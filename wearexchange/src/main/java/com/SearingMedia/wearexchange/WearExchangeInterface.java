package com.SearingMedia.wearexchange;

import android.content.Context;

import com.google.android.gms.wearable.MessageEvent;

public interface WearExchangeInterface {
    void messageReceived(MessageEvent messageEvent);

    void wearConnectionMade(String nodeId);

    void wearConnectionLost(int cause);

    Context getWearContext();
}
