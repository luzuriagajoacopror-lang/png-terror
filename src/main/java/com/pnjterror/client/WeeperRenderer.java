package com.pnjterror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pnjterror.PnjTerror;
import com.pnjterror.entity.WeeperEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WeeperRenderer extends HumanoidMobRenderer<WeeperEntity, HumanoidModel<WeeperEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PnjTerror.MODID, "textures/entity/weeper.png");

    public WeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WeeperEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(WeeperEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.95F, 1.15F, 0.95F);
    }
}
