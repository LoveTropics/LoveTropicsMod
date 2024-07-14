package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class PlayerDisguise {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MODID);

    public static final Supplier<AttachmentType<PlayerDisguise>> ATTACHMENT = ATTACHMENT_TYPES.register(
            "player_disguise", () -> AttachmentType.builder(holder -> new PlayerDisguise((LivingEntity) holder))
                    .serialize(new IAttachmentSerializer<>() {
                        @Override
                        public PlayerDisguise read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider registries) {
                            PlayerDisguise disguise = new PlayerDisguise((LivingEntity) holder);
                            RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
                            DisguiseType.CODEC.parse(registryOps, tag).resultOrPartial(LOGGER::error).ifPresent(disguise::set);
                            return disguise;
                        }

                        @Override
                        public Tag write(PlayerDisguise disguise, HolderLookup.Provider registries) {
                            RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);
                            return DisguiseType.CODEC.encodeStart(registryOps, disguise.disguise).getOrThrow();
                        }
                    }).build()
    );

    private final LivingEntity entity;

    private DisguiseType disguise = DisguiseType.DEFAULT;
    @Nullable
    private Entity disguisedEntity;

    private PlayerDisguise(LivingEntity entity) {
        this.entity = entity;
    }

    @Nullable
    public static PlayerDisguise getOrNull(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.getData(ATTACHMENT);
        }
        return null;
    }

    public boolean isDisguised() {
        return !type().isDefault();
    }

    public void clear() {
        set(DisguiseType.DEFAULT);
    }

    public void clear(DisguiseType disguise) {
        set(this.disguise.clear(disguise));
    }

    public void set(DisguiseType disguise) {
        if (this.disguise.equals(disguise)) {
            return;
        }
        this.disguise = disguise;
        disguisedEntity = disguise.createEntityFor(entity);
        entity.refreshDimensions();
    }

    public DisguiseType type() {
        return disguise;
    }

    @Nullable
    public Entity entity() {
        return disguisedEntity;
    }

    public void copyFrom(PlayerDisguise from) {
        set(from.type());
    }

    public float getEffectiveScale() {
        if (disguisedEntity != null) {
            float entityScale = disguisedEntity.getBbHeight() / EntityType.PLAYER.getHeight();
            return disguise.scale() * entityScale;
        } else {
            return disguise.scale();
        }
    }
}
