package com.wytings.special.util;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rex on 2018/03/14.
 * https://github.com/wytings
 */


public class Broadcaster {

    private final Handler handler;
    private static final Broadcaster broadcaster = new Broadcaster();
    private final ArrayMap<String, List<Runnable>> eventReceiverMap = new ArrayMap<>(10);

    public synchronized static Broadcaster getInstance() {
        return broadcaster;
    }

    private Broadcaster() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void listenEvent(String event, Runnable receiver) {
        if (receiver == null) {
            G.w("throw away null receiver");
            return;
        }
        synchronized (eventReceiverMap) {
            if (eventReceiverMap.get(event) == null) {
                eventReceiverMap.put(event, new LinkedList<>());
            }
            if (!eventReceiverMap.get(event).contains(receiver)) {
                eventReceiverMap.get(event).add(receiver);
            }
        }
    }


    public void removeRunnable(Runnable receiver) {
        synchronized (eventReceiverMap) {
            if (receiver == null) {
                return;
            }
            for (List<Runnable> receiverList : eventReceiverMap.values()) {
                if (receiverList.contains(receiver)) {
                    receiverList.remove(receiver);
                }
            }
        }
    }

    public void notifyEvent(final String event) {

        List<Runnable> receivers = eventReceiverMap.get(event);
        if (receivers == null || receivers.isEmpty()) {
            return;
        }
        List<Runnable> receiverList = new ArrayList<>(receivers);
        for (final Runnable receiver : receiverList) {
            handler.post(receiver);
        }
    }

}
