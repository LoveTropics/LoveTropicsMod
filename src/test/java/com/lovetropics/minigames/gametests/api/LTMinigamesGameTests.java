package com.lovetropics.minigames.gametests.api;

import com.lovetropics.minigames.common.core.game.datagen.BehaviorFactory;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorProvider;
import com.lovetropics.minigames.common.core.game.datagen.GameProvider;
import com.lovetropics.minigames.gametests.ActionTests;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = "ltminigames", bus = Mod.EventBusSubscriber.Bus.MOD)
public class LTMinigamesGameTests {
    public static final Map<ResourceLocation, MinigameTest> TESTS = Stream.of(new ActionTests())
            .collect(Collectors.toMap(MinigameTest::id, Function.identity()));

    @SubscribeEvent
    static void register(final RegisterGameTestsEvent event) throws NoSuchMethodException {
        event.register(LTMinigamesGameTests.class.getMethod("generate"));
    }

    @SubscribeEvent
    static void gather(final GatherDataEvent event) {
        final PackOutput out = event.getGenerator().getPackOutput("testing");

        final BehaviorFactory behaviors = new BehaviorFactory();
        event.getGenerator()
                .addProvider(event.includeServer(), new GameProvider(out, behaviors, event.getLookupProvider()) {
                    @Override
                    protected void generate(GameGenerator generator, HolderLookup.Provider holderProvider) {
                        TESTS.forEach((key, test) -> test.generateGame(generator, behaviors, holderProvider));
                    }
                });

        event.getGenerator()
                .addProvider(event.includeServer(), new BehaviorProvider(out, behaviors, event.getLookupProvider()));

        event.getGenerator().addProvider(true, new PackMetadataGenerator(out)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("LTMinigames testing"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), Map.of(
                        PackType.SERVER_DATA, SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA),
                        PackType.CLIENT_RESOURCES, SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)
                ))));
    }

    @SubscribeEvent
    static void addFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            final var resources = new PathPackResources("testing", ModList.get()
                    .getModContainerById("ltminigames").orElseThrow()
                    .getModInfo().getOwningFile()
                    .getFile().findResource("testing"), true);
            event.addRepositorySource(onLoad -> onLoad.accept(Pack.readMetaAndCreate(
                    "testing", Component.literal("testing"), true,
                    pId -> resources, PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN
            )));
        }
    }

    @GameTestGenerator
    public static List<TestFunction> generate() {
        final List<TestFunction> tests = new ArrayList<>();

        for (var entry : TESTS.entrySet()) {
            var test = entry.getValue();
            var id = entry.getKey();
            for (Method testMethod : test.getClass().getDeclaredMethods()) {
                GameTest gametest = testMethod.getAnnotation(GameTest.class);
                if (gametest == null) continue;
                String testName =  id.getPath() + "." + testMethod.getName();
                String template = gametest.template().isBlank() ? "ltminigames:empty_3x3" : net.minecraftforge.gametest.ForgeGameTestHooks.getTemplateNamespace(testMethod) + ":" + (gametest.template().isEmpty() ? testName : gametest.template());
                String batch = gametest.batch().isBlank() ? id.getPath() : gametest.batch();
                Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());
                final var consumer = (Consumer<LTGameTestHelper>)turnMethodIntoConsumer(testMethod, test);
                tests.add(new TestFunction(batch, testName, template, rotation, gametest.timeoutTicks(), gametest.setupTicks(), gametest.required(), gametest.requiredSuccesses(), gametest.attempts(), helper -> consumer.accept(new LTGameTestHelper(helper))));
            }
        }

        return tests;
    }

    private static Consumer<?> turnMethodIntoConsumer(Method pTestMethod, Object instance) {
        return (arg) -> {
            try {
                pTestMethod.invoke(instance, arg);
            } catch (InvocationTargetException invocationtargetexception) {
                if (invocationtargetexception.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)invocationtargetexception.getCause();
                } else {
                    throw new RuntimeException(invocationtargetexception.getCause());
                }
            } catch (ReflectiveOperationException reflectiveoperationexception) {
                throw new RuntimeException(reflectiveoperationexception);
            }
        };
    }
}
