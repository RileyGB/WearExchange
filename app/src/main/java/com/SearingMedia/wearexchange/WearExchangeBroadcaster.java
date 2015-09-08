package com.SearingMedia.wearexchange;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;

public abstract class WearExchangeBroadcaster {
    public final static String WEAR_EXCHANGE_BROADCAST = "WearExchangeBroadcast";
    public final static String BROADCAST_MESSAGE_KEY = "WearExchangeMessage";

    public static boolean sendMessageBroadcast(MessageEvent messageEvent, Context context) {
        Intent intent = new Intent(WEAR_EXCHANGE_BROADCAST);

        intent.putExtra(BROADCAST_MESSAGE_KEY, new WearExchangeMessageEvent(messageEvent));

        return LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
