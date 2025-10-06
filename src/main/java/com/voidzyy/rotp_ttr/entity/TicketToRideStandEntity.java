package com.voidzyy.rotp_ttr.entity;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class TicketToRideStandEntity extends StandEntity {
    // 唯一需要同步的数据：开关状态
    private static final DataParameter<Boolean> IS_SENSES_ACTIVE =
            EntityDataManager.defineId(TicketToRideStandEntity.class, DataSerializers.BOOLEAN);

    public TicketToRideStandEntity(StandEntityType<TicketToRideStandEntity> type, World world) {
        super(type, world);
    }

}