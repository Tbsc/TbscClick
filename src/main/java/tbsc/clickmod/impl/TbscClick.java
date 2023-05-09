package tbsc.clickmod.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;
import tbsc.clickmod.Compat;
import tbsc.clickmod.IClick;
import tbsc.clickmod.IKeyBind;
import tbsc.clickmod.IRayTrace;

@Mod(TbscClick.MODID)
public class TbscClick implements IClick {

    public static final String MODID = "tbscclick";

    public static KeyMapping keyToggleRight;
    public static KeyMapping keyToggleLeft;
    public static KeyMapping keyToggleSmartAttack;
    public static KeyMapping keyToggleHoldRight;
    public static KeyMapping keySpeed;
    private IKeyBind myKeyUse;
    private IKeyBind myKeyToggleRight;
    private IKeyBind myKeyToggleLeft;
    private IKeyBind myKeyToggleSmartAttack;
    private IKeyBind myKeyToggleHoldRight;
    private IKeyBind myKeySpeed;

    private int ticksStepBetweenClicks = Config.DEF_TICKS_STEP;
    private int maxTicksBetweenClicks = Config.DEF_MAX_TICKS;
    private int minTicksBetweenClicks = Config.DEF_MIN_TICKS;

    private Minecraft minecraft = null;

    private Compat compat;

    public TbscClick() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetupEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

    @SubscribeEvent
    public void onClientSetupEvent(RegisterKeyMappingsEvent event) {
        compat = new Compat(this);

        MinecraftForge.EVENT_BUS.register(this);

        keyToggleRight = new KeyMapping("key.tbscclick.toggleright", GLFW.GLFW_KEY_G, "key.categories.tbscclick");
        keyToggleLeft = new KeyMapping("key.tbscclick.toggleleft", GLFW.GLFW_KEY_H, "key.categories.tbscclick");
        keyToggleSmartAttack = new KeyMapping("key.tbscclick.togglesmartattack", GLFW.GLFW_KEY_V, "key.categories.tbscclick");
        keyToggleHoldRight = new KeyMapping("key.tbscclick.toggleholdright", GLFW.GLFW_KEY_B, "key.categories.tbscclick");
        keySpeed = new KeyMapping("key.tbscclick.speed", GLFW.GLFW_KEY_N, "key.categories.tbscclick");

        event.register(keyToggleRight);
        event.register(keyToggleLeft);
        event.register(keyToggleSmartAttack);
        event.register(keyToggleHoldRight);
        event.register(keySpeed);

        minecraft = Minecraft.getInstance();

        myKeyUse = new KeyBind(minecraft.options.keyUse);
        myKeyToggleRight = new KeyBind(keyToggleRight);
        myKeyToggleLeft = new KeyBind(keyToggleLeft);
        myKeyToggleSmartAttack = new KeyBind(keyToggleSmartAttack);
        myKeyToggleHoldRight = new KeyBind(keyToggleHoldRight);
        myKeySpeed = new KeyBind(keySpeed);

        Config.loadConfig(Config.CONFIG_SPEC, FMLPaths.CONFIGDIR.get().resolve("TbscClick.toml"));
        processConfig();
    }

    private void processConfig() {
        ticksStepBetweenClicks = Config.ticksStep.get();
        maxTicksBetweenClicks = Config.maxTicks.get();
        minTicksBetweenClicks = Config.minTicks.get();
    }

    public void onConfigReloaded(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Reloading) {
            processConfig();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        compat.onTick();
    }

    @SubscribeEvent
    public void onInitGuiPre(ScreenEvent.Init.Pre event) {
        compat.onInitGuiPre();
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        compat.onKeyPressed();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGuiOverlayEvent event) {
        if (event.getOverlay() == VanillaGuiOverlay.DEBUG_TEXT.type()) {
            compat.onRenderGameOverlay();
        }
    }

    @Override
    public Minecraft getMinecraft() {
        return minecraft;
    }

    @Override
    public boolean isGamePaused() {
        return minecraft.isPaused();
    }

    @Override
    public boolean isInGame() {
        return minecraft.screen == null;
    }

    @Override
    public boolean isInPauseMenu() {
        return minecraft.screen instanceof PauseScreen;
    }

    @Override
    public IRayTrace getRayTrace() {
        return new RayTrace(minecraft.hitResult);
    }

    @Override
    public float getSmartAttackCooldown() {
        return minecraft.player != null ? minecraft.player.getAttackStrengthScale(0) : 0.0F;
    }

    InputEvent.InteractionKeyMappingTriggered clickInputEvent;

    @Override
    public void postClickInputEvent() {
        clickInputEvent = ForgeHooksClient.onClickInput(0, minecraft.options.keyAttack, InteractionHand.MAIN_HAND);
    }

    @Override
    public void swingHandIfShould() {
        if (clickInputEvent.shouldSwingHand() && minecraft.player != null) {
            minecraft.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void setHoldButton(IKeyBind key, boolean held) {
        key.setHeld(held);
    }

    @Override
    public IKeyBind getUseKey() {
        return myKeyUse;
    }

    @Override
    public IKeyBind getToggleRightKey() {
        return myKeyToggleRight;
    }

    @Override
    public IKeyBind getToggleLeftKey() {
        return myKeyToggleLeft;
    }

    @Override
    public IKeyBind getToggleSmartAttackKey() {
        return myKeyToggleSmartAttack;
    }

    @Override
    public IKeyBind getToggleHoldRightKey() {
        return myKeyToggleHoldRight;
    }

    @Override
    public IKeyBind getSpeedKey() {
        return myKeySpeed;
    }

    @Override
    public int getTicksStepBetweenClicks() {
        return ticksStepBetweenClicks;
    }

    @Override
    public int getMaxTicksBetweenClicks() {
        return maxTicksBetweenClicks;
    }

    @Override
    public int getMinTicksBetweenClicks() {
        return minTicksBetweenClicks;
    }

    @Override
    public String getRightClickMouseMethodMapping() {
        return "m_91277_";
    }

    @Override
    public String getLeftClickCounterFieldMapping() {
        return "f_91078_";
    }

    @Override
    public String getLeftClickMouseMethodMapping() {
        return "m_202354_";
    }

    @Override
    public String getRightClickDelayTimerFieldMapping() {
        return "f_91011_";
    }

    @Override
    public void renderTextOnScreen(String text, float x, float y, int color) {
        minecraft.font.draw(new PoseStack(), ChatFormatting.BOLD + text, x, y, color);
    }

    @Override
    public boolean isPlayerHandBusy() {
        return minecraft.player != null && minecraft.player.isHandsBusy();
    }

    @Override
    public void sendMessage(String message) {
        minecraft.gui.getChat().addMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public void sendMessageWithId(String message, int id) {
        compat.reflInvokeMethod(ChatComponent.class, minecraft.gui.getChat(), "m_240964_",
                new Class[]{Component.class, MessageSignature.class, GuiMessageTag.class},
                new Object[]{Component.literal(message), new MessageSignature(new byte[256]), GuiMessageTag.system()});
    }

}
