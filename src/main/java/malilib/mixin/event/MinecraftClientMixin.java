package malilib.mixin.event;

import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import malilib.event.dispatch.ClientWorldChangeEventDispatcherImpl;
import malilib.event.dispatch.InitializationDispatcherImpl;
import malilib.registry.Registry;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
    @Shadow public ClientWorld world;

    private ClientWorld worldBefore;

    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("RETURN"))
    private void onInitComplete(RunArgs args, CallbackInfo ci)
    {
        // Register all mod handlers
        ((InitializationDispatcherImpl) Registry.INITIALIZATION_DISPATCHER).onGameInitDone();
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void onLoadWorldPre(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        // Only handle dimension changes/respawns here.
        // The initial join is handled in MixinClientPlayNetworkHandler onGameJoin 
        if (this.world != null)
        {
            this.worldBefore = this.world;
            ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPre(this.world, worldClientIn);
        }
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("RETURN"))
    private void onLoadWorldPost(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        if (this.worldBefore != null)
        {
            ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPost(this.worldBefore, worldClientIn);
            this.worldBefore = null;
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnectPre(Screen screen, CallbackInfo ci)
    {
        this.worldBefore = this.world;
        ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPre(this.worldBefore, null);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onDisconnectPost(Screen screen, CallbackInfo ci)
    {
        ((ClientWorldChangeEventDispatcherImpl) Registry.CLIENT_WORLD_CHANGE_EVENT_DISPATCHER).onWorldLoadPost(this.worldBefore, null);
        this.worldBefore = null;
    }
}
