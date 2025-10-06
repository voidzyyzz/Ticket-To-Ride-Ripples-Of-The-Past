package com.voidzyy.rotp_ttr.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.voidzyy.rotp_ttr.entity.TicketToRideStandEntity;

public class TtrStandModel extends HumanoidStandModel<TicketToRideStandEntity> {
    /* THIS SHOULD BE EMPTY*/

    public TtrStandModel() {
        super();

        addHumanoidBaseBoxes(null);
        texWidth = 1;
        texHeight = 1;



    }


    @Override
    protected ModelPose initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm, 0, 0, -0),

        });
    }


}