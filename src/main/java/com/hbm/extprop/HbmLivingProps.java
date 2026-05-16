package com.hbm.extprop;

import net.minecraft.entity.EntityLivingBase;

public class HbmLivingProps {

    public static void addCont(EntityLivingBase entity, ContaminationEffect effect) {
        // TODO: реальная контаминация на radiation-этапе
    }

    public static class ContaminationEffect {
        public final float dose;
        public final int duration;
        public final boolean acidic;

        public ContaminationEffect(float dose, int duration, boolean acidic) {
            this.dose = dose;
            this.duration = duration;
            this.acidic = acidic;
        }
    }
}
