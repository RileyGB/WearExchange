package com.SearingMedia.wearexchange;

import android.content.Context;

import com.google.android.gms.wearable.MessageEvent;

import de.greenrobot.event.EventBus;

public abstract class WearExchangeBroadcaster {
    public final static String WEAR_EXCHANGE_BROADCAST = "WearExchangeBroadcast";
    public final static String BROADCAST_MESSAGE_KEY = "WearExchangeMessage";

    public static boolean sendMessageBroadcast(MessageEvent messageEvent, Context context) {
        if (EventBus.getDefault().hasSubscriberForEvent(WearExchangeMessageEvent.class)) {
            EventBus.getDefault().post(new WearExchangeMessageEvent(messageEvent));
            return true;
        } else {
            return false;
        }
    }
}
