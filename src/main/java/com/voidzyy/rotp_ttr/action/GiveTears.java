package com.voidzyy.rotp_ttr.action;

import com.github.standobyte.jojo.action.stand.StandEntityAction;

public class GiveTears extends StandEntityAction {

    public GiveTears(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public boolean enabledInHudDefault() {
        return false;
    }

}
