package com.prappz.glare.common;

import com.squareup.otto.Bus;

/**
 * Created by root on 2/11/16.
 */

public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}