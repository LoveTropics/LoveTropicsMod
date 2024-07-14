package com.lovetropics.minigames.gametests.api;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.mixin.gametest.GameTestHelperAccess;
import com.lovetropics.minigames.mixin.gametest.GameTestInfoAccess;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LTGameTestHelper extends GameTestHelper {
    private final GameTestHelper delegate;
    final GameTestInfo info;
    final AtomicInteger playerCount = new AtomicInteger();

    public LTGameTestHelper(GameTestHelper helper) {
        super(null);
        this.delegate = helper;
        this.info = ((GameTestHelperAccess) helper).getTestInfo();
    }

    @Override
    public ServerLevel getLevel() {
        return delegate.getLevel();
    }

    @Override
    public BlockState getBlockState(BlockPos pPos) {
        return delegate.getBlockState(pPos);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos) {
        return delegate.getBlockEntity(pPos);
    }

    @Override
    public void killAllEntities() {
        delegate.killAllEntities();
    }

    @Override
    public void killAllEntitiesOfClass(Class pEntityClass) {
        delegate.killAllEntitiesOfClass(pEntityClass);
    }

    @Override
    public ItemEntity spawnItem(Item pItem, float pX, float pY, float pZ) {
        return delegate.spawnItem(pItem, pX, pY, pZ);
    }

    @Override
    public ItemEntity spawnItem(Item pItem, BlockPos pPos) {
        return delegate.spawnItem(pItem, pPos);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> pType, BlockPos pPos) {
        return delegate.spawn(pType, pPos);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> pType, Vec3 pPos) {
        return delegate.spawn(pType, pPos);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> pType, int pX, int pY, int pZ) {
        return delegate.spawn(pType, pX, pY, pZ);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> pType, float pX, float pY, float pZ) {
        return delegate.spawn(pType, pX, pY, pZ);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> pType, BlockPos pPos) {
        return delegate.spawnWithNoFreeWill(pType, pPos);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> pType, int pX, int pY, int pZ) {
        return delegate.spawnWithNoFreeWill(pType, pX, pY, pZ);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> pType, Vec3 pPos) {
        return delegate.spawnWithNoFreeWill(pType, pPos);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> pType, float pX, float pY, float pZ) {
        return delegate.spawnWithNoFreeWill(pType, pX, pY, pZ);
    }

    @Override
    public GameTestSequence walkTo(Mob pMob, BlockPos pPos, float pSpeed) {
        return delegate.walkTo(pMob, pPos, pSpeed);
    }

    @Override
    public void pressButton(int pX, int pY, int pZ) {
        delegate.pressButton(pX, pY, pZ);
    }

    @Override
    public void pressButton(BlockPos pPos) {
        delegate.pressButton(pPos);
    }

    @Override
    public void useBlock(BlockPos pPos) {
        delegate.useBlock(pPos);
    }

    @Override
    public void useBlock(BlockPos pPos, Player pPlayer) {
        delegate.useBlock(pPos, pPlayer);
    }

    @Override
    public void useBlock(BlockPos pPos, Player pPlayer, BlockHitResult pResult) {
        delegate.useBlock(pPos, pPlayer, pResult);
    }

    @Override
    public LivingEntity makeAboutToDrown(LivingEntity pEntity) {
        return delegate.makeAboutToDrown(pEntity);
    }

    @Override
    public Player makeMockPlayer(GameType gameType) {
        return delegate.makeMockPlayer(gameType);
    }

    @Override
    public LivingEntity withLowHealth(LivingEntity pEntity) {
        return delegate.withLowHealth(pEntity);
    }

    @Override
    public void pullLever(int pX, int pY, int pZ) {
        delegate.pullLever(pX, pY, pZ);
    }

    @Override
    public void pullLever(BlockPos pPos) {
        delegate.pullLever(pPos);
    }

    @Override
    public void pulseRedstone(BlockPos pPos, long pDelay) {
        delegate.pulseRedstone(pPos, pDelay);
    }

    @Override
    public void destroyBlock(BlockPos pPos) {
        delegate.destroyBlock(pPos);
    }

    @Override
    public void setBlock(int pX, int pY, int pZ, Block pBlock) {
        delegate.setBlock(pX, pY, pZ, pBlock);
    }

    @Override
    public void setBlock(int pX, int pY, int pZ, BlockState pState) {
        delegate.setBlock(pX, pY, pZ, pState);
    }

    @Override
    public void setBlock(BlockPos pPos, Block pBlock) {
        delegate.setBlock(pPos, pBlock);
    }

    @Override
    public void setBlock(BlockPos pPos, BlockState pState) {
        delegate.setBlock(pPos, pState);
    }

    @Override
    public void setNight() {
        delegate.setNight();
    }

    @Override
    public void setDayTime(int pTime) {
        delegate.setDayTime(pTime);
    }

    @Override
    public void assertBlockPresent(Block pBlock, int pX, int pY, int pZ) {
        delegate.assertBlockPresent(pBlock, pX, pY, pZ);
    }

    @Override
    public void assertBlockPresent(Block pBlock, BlockPos pPos) {
        delegate.assertBlockPresent(pBlock, pPos);
    }

    @Override
    public void assertBlockNotPresent(Block pBlock, int pX, int pY, int pZ) {
        delegate.assertBlockNotPresent(pBlock, pX, pY, pZ);
    }

    @Override
    public void assertBlockNotPresent(Block pBlock, BlockPos pPos) {
        delegate.assertBlockNotPresent(pBlock, pPos);
    }

    @Override
    public void succeedWhenBlockPresent(Block pBlock, int pX, int pY, int pZ) {
        delegate.succeedWhenBlockPresent(pBlock, pX, pY, pZ);
    }

    @Override
    public void succeedWhenBlockPresent(Block pBlock, BlockPos pPos) {
        delegate.succeedWhenBlockPresent(pBlock, pPos);
    }

    @Override
    public void assertBlock(BlockPos pPos, Predicate<Block> pPredicate, String pExceptionMessage) {
        delegate.assertBlock(pPos, pPredicate, pExceptionMessage);
    }

    @Override
    public void assertBlock(BlockPos pPos, Predicate<Block> pPredicate, Supplier<String> pExceptionMessage) {
        delegate.assertBlock(pPos, pPredicate, pExceptionMessage);
    }

    @Override
    public <T extends Comparable<T>> void assertBlockProperty(BlockPos pPos, Property<T> pProperty, T pValue) {
        delegate.assertBlockProperty(pPos, pProperty, pValue);
    }

    @Override
    public <T extends Comparable<T>> void assertBlockProperty(BlockPos pPos, Property<T> pProperty, Predicate<T> pPredicate, String pExceptionMessage) {
        delegate.assertBlockProperty(pPos, pProperty, pPredicate, pExceptionMessage);
    }

    @Override
    public void assertBlockState(BlockPos pPos, Predicate<BlockState> pPredicate, Supplier<String> pExceptionMessage) {
        delegate.assertBlockState(pPos, pPredicate, pExceptionMessage);
    }

    @Override
    public void assertRedstoneSignal(BlockPos pPos, Direction pDirection, IntPredicate pSignalStrengthPredicate, Supplier<String> pExceptionMessage) {
        delegate.assertRedstoneSignal(pPos, pDirection, pSignalStrengthPredicate, pExceptionMessage);
    }

    @Override
    public void assertEntityPresent(EntityType<?> pType) {
        delegate.assertEntityPresent(pType);
    }

    @Override
    public void assertEntityPresent(EntityType<?> pType, int pX, int pY, int pZ) {
        delegate.assertEntityPresent(pType, pX, pY, pZ);
    }

    @Override
    public void assertEntityPresent(EntityType<?> pType, BlockPos pPos) {
        delegate.assertEntityPresent(pType, pPos);
    }

    @Override
    public void assertEntityPresent(EntityType<?> pEntityType, Vec3 pStartPos, Vec3 pEndPos) {
        delegate.assertEntityPresent(pEntityType, pStartPos, pEndPos);
    }

    @Override
    public void assertEntitiesPresent(EntityType<?> pEntityType, BlockPos pPos, int pCount, double pRadius) {
        delegate.assertEntitiesPresent(pEntityType, pPos, pCount, pRadius);
    }

    @Override
    public void assertEntityPresent(EntityType<?> pType, BlockPos pPos, double pExpansionAmount) {
        delegate.assertEntityPresent(pType, pPos, pExpansionAmount);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityType<T> pEntityType, BlockPos pPos, double pRadius) {
        return delegate.getEntities(pEntityType, pPos, pRadius);
    }

    @Override
    public void assertEntityInstancePresent(Entity pEntity, int pX, int pY, int pZ) {
        delegate.assertEntityInstancePresent(pEntity, pX, pY, pZ);
    }

    @Override
    public void assertEntityInstancePresent(Entity pEntity, BlockPos pPos) {
        delegate.assertEntityInstancePresent(pEntity, pPos);
    }

    @Override
    public void assertItemEntityCountIs(Item pItem, BlockPos pPos, double pExpansionAmount, int pCount) {
        delegate.assertItemEntityCountIs(pItem, pPos, pExpansionAmount, pCount);
    }

    @Override
    public void assertItemEntityPresent(Item pItem, BlockPos pPos, double pExpansionAmount) {
        delegate.assertItemEntityPresent(pItem, pPos, pExpansionAmount);
    }

    @Override
    public void assertItemEntityNotPresent(Item pItem, BlockPos pPos, double pRadius) {
        delegate.assertItemEntityNotPresent(pItem, pPos, pRadius);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> pType) {
        delegate.assertEntityNotPresent(pType);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> pType, int pX, int pY, int pZ) {
        delegate.assertEntityNotPresent(pType, pX, pY, pZ);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> pType, BlockPos pPos) {
        delegate.assertEntityNotPresent(pType, pPos);
    }

    @Override
    public void assertEntityTouching(EntityType<?> pType, double pX, double pY, double pZ) {
        delegate.assertEntityTouching(pType, pX, pY, pZ);
    }

    @Override
    public void assertEntityNotTouching(EntityType<?> pType, double pX, double pY, double pZ) {
        delegate.assertEntityNotTouching(pType, pX, pY, pZ);
    }

    @Override
    public <E extends Entity, T> void assertEntityData(BlockPos pPos, EntityType<E> pType, Function<? super E, T> pEntityDataGetter, @org.jetbrains.annotations.Nullable T pTestEntityData) {
        delegate.assertEntityData(pPos, pType, pEntityDataGetter, pTestEntityData);
    }

    @Override
    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos pPos, EntityType<E> pEntityType, Item pItem) {
        delegate.assertEntityIsHolding(pPos, pEntityType, pItem);
    }

    @Override
    public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPos pPos, EntityType<E> pEntityType, Item pItem) {
        delegate.assertEntityInventoryContains(pPos, pEntityType, pItem);
    }

    @Override
    public void assertContainerEmpty(BlockPos pPos) {
        delegate.assertContainerEmpty(pPos);
    }

    @Override
    public void assertContainerContains(BlockPos pPos, Item pItem) {
        delegate.assertContainerContains(pPos, pItem);
    }

    @Override
    public void assertSameBlockStates(BoundingBox pBoundingBox, BlockPos pPos) {
        delegate.assertSameBlockStates(pBoundingBox, pPos);
    }

    @Override
    public void assertSameBlockState(BlockPos pTestPos, BlockPos pComparisonPos) {
        delegate.assertSameBlockState(pTestPos, pComparisonPos);
    }

    @Override
    public void assertAtTickTimeContainerContains(long pTickTime, BlockPos pPos, Item pItem) {
        delegate.assertAtTickTimeContainerContains(pTickTime, pPos, pItem);
    }

    @Override
    public void assertAtTickTimeContainerEmpty(long pTickTime, BlockPos pPos) {
        delegate.assertAtTickTimeContainerEmpty(pTickTime, pPos);
    }

    @Override
    public <E extends Entity, T> void succeedWhenEntityData(BlockPos pPos, EntityType<E> pType, Function<E, T> pEntityDataGetter, T pTestEntityData) {
        delegate.succeedWhenEntityData(pPos, pType, pEntityDataGetter, pTestEntityData);
    }

    @Override
    public <E extends Entity> void assertEntityProperty(E pEntity, Predicate<E> pPredicate, String pName) {
        delegate.assertEntityProperty(pEntity, pPredicate, pName);
    }

    @Override
    public <E extends Entity, T> void assertEntityProperty(E pEntity, Function<E, T> pEntityPropertyGetter, String pValueName, T pTestEntityProperty) {
        delegate.assertEntityProperty(pEntity, pEntityPropertyGetter, pValueName, pTestEntityProperty);
    }

    @Override
    public void succeedWhenEntityPresent(EntityType<?> pType, int pX, int pY, int pZ) {
        delegate.succeedWhenEntityPresent(pType, pX, pY, pZ);
    }

    @Override
    public void succeedWhenEntityPresent(EntityType<?> pType, BlockPos pPos) {
        delegate.succeedWhenEntityPresent(pType, pPos);
    }

    @Override
    public void succeedWhenEntityNotPresent(EntityType<?> pType, int pX, int pY, int pZ) {
        delegate.succeedWhenEntityNotPresent(pType, pX, pY, pZ);
    }

    @Override
    public void succeedWhenEntityNotPresent(EntityType<?> pType, BlockPos pPos) {
        delegate.succeedWhenEntityNotPresent(pType, pPos);
    }

    @Override
    public void succeed() {
        delegate.succeed();
    }

    @Override
    public void succeedIf(Runnable pCriterion) {
        delegate.succeedIf(pCriterion);
    }

    @Override
    public void succeedWhen(Runnable pCriterion) {
        delegate.succeedWhen(pCriterion);
    }

    @Override
    public void succeedOnTickWhen(int pTick, Runnable pCriterion) {
        delegate.succeedOnTickWhen(pTick, pCriterion);
    }

    @Override
    public void runAtTickTime(long pTickTime, Runnable pTask) {
        delegate.runAtTickTime(pTickTime, pTask);
    }

    @Override
    public void runAfterDelay(long pDelay, Runnable pTask) {
        delegate.runAfterDelay(pDelay, pTask);
    }

    @Override
    public void randomTick(BlockPos pPos) {
        delegate.randomTick(pPos);
    }

    @Override
    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ) {
        return delegate.getHeight(pHeightmapType, pX, pZ);
    }

    @Override
    public void fail(String pExceptionMessage, BlockPos pPos) {
        delegate.fail(pExceptionMessage, pPos);
    }

    @Override
    public void fail(String pExceptionMessage, Entity pEntity) {
        delegate.fail(pExceptionMessage, pEntity);
    }

    @Override
    public void fail(String pExceptionMessage) {
        delegate.fail(pExceptionMessage);
    }

    @Override
    public void failIf(Runnable pCriterion) {
        delegate.failIf(pCriterion);
    }

    @Override
    public void failIfEver(Runnable pCriterion) {
        delegate.failIfEver(pCriterion);
    }

    @Override
    public GameTestSequence startSequence() {
        return delegate.startSequence();
    }

    @Override
    public BlockPos absolutePos(BlockPos pPos) {
        return delegate.absolutePos(pPos);
    }

    @Override
    public BlockPos relativePos(BlockPos pPos) {
        return delegate.relativePos(pPos);
    }

    @Override
    public Vec3 absoluteVec(Vec3 pRelativeVec3) {
        return delegate.absoluteVec(pRelativeVec3);
    }

    @Override
    public Vec3 relativeVec(Vec3 pAbsoluteVec3) {
        return delegate.relativeVec(pAbsoluteVec3);
    }

    @Override
    public void assertTrue(boolean pCondition, String pFailureMessage) {
        delegate.assertTrue(pCondition, pFailureMessage);
    }

    public void assertTrue(boolean pCondition, Supplier<String> pFailureMessage) {
        if (!pCondition) {
            throw new GameTestAssertException(pFailureMessage.get());
        }
    }

    @Override
    public void assertFalse(boolean pCondition, String pFailureMessage) {
        delegate.assertFalse(pCondition, pFailureMessage);
    }

    @Override
    public long getTick() {
        return delegate.getTick();
    }

    @Override
    public void forEveryBlockInStructure(Consumer<BlockPos> pConsumer) {
        delegate.forEveryBlockInStructure(pConsumer);
    }

    @Override
    public void onEachTick(Runnable pTask) {
        delegate.onEachTick(pTask);
    }

    @Override
    public void placeAt(Player pPlayer, ItemStack pStack, BlockPos pPos, Direction pDirection) {
        delegate.placeAt(pPlayer, pStack, pPos, pDirection);
    }

    public TestGameLobby createGame(ServerPlayer player, PlayerRole initiatorRole) {
        String name = player.getScoreboardName() + "'s Lobby";

        GameResult<IGameLobby> result = IGameManager.get().createGameLobby(name, player);
        assertTrue(result.isOk(), () -> "Game could not be created: " + result.getError().getString());

        IGameLobby lobby = result.getOk();
        lobby.getPlayers().join(player, initiatorRole);

        ((List<GameTestListener>) ((GameTestInfoAccess) info).getListeners()).add(0, new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo pTestInfo) {

            }

            @Override
            public void testPassed(GameTestInfo pTest, GameTestRunner pRunner) {
                if (lobby.getCurrentPhase() != null)
                    lobby.getCurrentPhase().requestStop(GameStopReason.finished());
                lobby.getManagement().close();
            }

            @Override
            public void testFailed(GameTestInfo pTest, GameTestRunner pRunner) {
                if (lobby.getCurrentPhase() != null)
                    lobby.getCurrentPhase().requestStop(GameStopReason.canceled());
                lobby.getManagement().close();
            }

            @Override
            public void testAddedForRerun(GameTestInfo pOldTest, GameTestInfo pNewTest, GameTestRunner pRunner) {

            }
        });

        return new TestGameLobby(lobby);
    }

    public Runnable startGame(IGameLobby game) {
        return () -> {
            final var result = game.getControls().get(LobbyControls.Type.PLAY).run();
            assertTrue(result.isOk(), () -> "Game could not start: " + result.getError().getString());
        };
    }

    public FakePlayerBuilder playerBuilder() {
        return new FakePlayerBuilder(this);
    }

    public LTFakePlayer createFakePlayer() {
        return playerBuilder().build();
    }

    public <T> void assertReceivedPacket(LTFakePlayer player, int index, Class<T> type, Predicate<T> test) {
        assertTrue(index < player.receivedPackets.size(), "Not enough packets received");
        final var pkt = player.receivedPackets.get(index);
        assertTrue(type.isInstance(pkt), "Received packet was of wrong type. Was: " + pkt.getClass() + ", expected: " + type);
        assertTrue(test.test(type.cast(pkt)), "Packet did not match!");
    }

    public void assertNoPacketsReceived(LTFakePlayer player) {
        assertTrue(player.receivedPackets.isEmpty(), "Player received at least a packet!");
    }

    public void assertPlayerInventoryContainsAt(Player player, int index, ItemStack stack) {
        final ItemStack toCompare = player.getInventory().getItem(index);
        assertTrue(ItemStack.isSameItemSameComponents(stack, toCompare), () -> "Items did not match: expected " + stack + ", but was " + toCompare);
        assertTrue(stack.getCount() == toCompare.getCount(), () -> "Stack count did not match: expected " + stack.getCount() + ", but was " + toCompare.getCount());
    }

    public void assertEntityHealth(LivingEntity entity, float health) {
        assertTrue(entity.getHealth() == health, () -> "Entity health did not match! Expected " + health + " but was " + entity.getHealth());
    }

    public void assertEntityMaxHealth(LivingEntity entity, float health) {
        assertTrue(entity.getMaxHealth() == health, () -> "Entity max health did not match! Expected " + health + " but was " + entity.getMaxHealth());
    }

    public TestPermissionAPI.Roles getRoles(Entity entity) {
        return LTMinigamesGameTests.PERMISSIONS.byEntity(entity);
    }
}
