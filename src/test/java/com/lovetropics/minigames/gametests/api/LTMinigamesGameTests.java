package com.lovetropics.minigames.gametests.api;

import com.google.common.base.Suppliers;
import com.lovetropics.lib.permission.PermissionsApi;
import com.lovetropics.lib.permission.role.RoleLookup;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorFactory;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorProvider;
import com.lovetropics.minigames.common.core.game.datagen.GameProvider;
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
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber(modid = "ltminigames", bus = EventBusSubscriber.Bus.MOD)
public class LTMinigamesGameTests {
    public static final TestPermissionAPI PERMISSIONS = new TestPermissionAPI();

    private static final Supplier<Map<ResourceLocation, MinigameTest>> TESTS = Suppliers.memoize(() -> {
        final var classes = ModList.get().getAllScanData().stream()
                .flatMap(sc -> sc.getAnnotations().stream())
                .filter(an -> an.annotationType().equals(RegisterMinigameTest.TYPE))
                .map(an -> an.clazz().getInternalName())
                .toList();

        final var testMap = new HashMap<ResourceLocation, MinigameTest>();
        try {
            for (String cls : classes) {
                final Class<?> clazz = Class.forName(cls.replace('/', '.'));
                final MinigameTest test = (MinigameTest) clazz.getDeclaredConstructor().newInstance();
                testMap.put(test.id(), test);
            }
        } catch (Exception ex) {
            LoveTropics.LOGGER.error("Could not create minigame test: ", ex);
        }

        return testMap;
    });
    public static final String TESTING_PACK = "testing";

    @SubscribeEvent
    static void register(final RegisterGameTestsEvent event) throws NoSuchMethodException {
        event.register(LTMinigamesGameTests.class.getMethod("generate"));
    }

    @SubscribeEvent
    static void gather(final GatherDataEvent event) {
        final PackOutput out = event.getGenerator().getPackOutput(TESTING_PACK);

        final BehaviorFactory behaviors = new BehaviorFactory();
        event.getGenerator()
                .addProvider(event.includeServer(), new GameProvider(out, behaviors, event.getLookupProvider()) {
                    @Override
                    protected void generate(GameGenerator generator, HolderLookup.Provider holderProvider) {
                        TESTS.get().forEach((key, test) -> test.generateGame(generator, behaviors, holderProvider));
                    }
                });

        event.getGenerator()
                .addProvider(event.includeServer(), new BehaviorProvider(out, behaviors, event.getLookupProvider()));

        event.getGenerator().addProvider(true, new PackMetadataGenerator(out)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("LTMinigames testing"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA))));
    }

    @SubscribeEvent
    static void addFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            PackLocationInfo info = new PackLocationInfo(TESTING_PACK, Component.literal("testing"), PackSource.BUILT_IN, Optional.empty());
            final var resources = new PathPackResources(info, ModList.get()
                    .getModContainerById(LoveTropics.ID).orElseThrow()
                    .getModInfo().getOwningFile()
                    .getFile().findResource(TESTING_PACK));
            event.addRepositorySource(onLoad -> onLoad.accept(Pack.readMetaAndCreate(
                    info, new Pack.ResourcesSupplier() {
                        @Override
                        public PackResources openPrimary(PackLocationInfo pLocation) {
                            return resources;
                        }

                        @Override
                        public PackResources openFull(PackLocationInfo pLocation, Pack.Metadata pMetadata) {
                            return resources;
                        }
                    }, PackType.SERVER_DATA, new PackSelectionConfig(true, Pack.Position.TOP, false)
            )));
        }
    }

    @GameTestGenerator
    public static List<TestFunction> generate() {
        final List<TestFunction> tests = new ArrayList<>();

        for (var entry : TESTS.get().entrySet()) {
            var test = entry.getValue();
            var id = entry.getKey();
            for (Method testMethod : test.getClass().getDeclaredMethods()) {
                GameTest gametest = testMethod.getAnnotation(GameTest.class);
                if (gametest == null) continue;

                String testName =  id.getPath() + "." + testMethod.getName();
                String template = gametest.template().isBlank() ? "ltminigames:empty_3x3" : GameTestHooks.getTemplateNamespace(testMethod) + ":" + (gametest.template().isEmpty() ? testName : gametest.template());
                String batch = gametest.batch().equals("defaultBatch") ? id.getPath() : gametest.batch();
                Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());
                final var consumer = (Consumer<LTGameTestHelper>)turnMethodIntoConsumer(testMethod, test);
                tests.add(new TestFunction(batch, testName, template, rotation, gametest.timeoutTicks(), gametest.setupTicks(), gametest.required(), gametest.manualOnly(), gametest.attempts(), gametest.requiredSuccesses(), gametest.skyAccess(), helper -> {
                    RoleLookup lookup = PermissionsApi.lookup();
                    try {
                        PermissionsApi.setRoleLookup(PERMISSIONS);
                        consumer.accept(new LTGameTestHelper(helper));
                    } finally {
                        PermissionsApi.setRoleLookup(lookup);
                    }
                }));
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
