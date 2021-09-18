package carpet.mixins;

import carpet.fakes.ChunkHolderInterface;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolder_scarpetChunkCreationMixin implements ChunkHolderInterface
{
    @Shadow protected abstract void combineSavingFuture(CompletableFuture<? extends Either<? extends Chunk, ChunkHolder.Unloaded>> newChunkFuture, String type);

    @Shadow @Final private AtomicReferenceArray<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> futuresByStatus;

    @Override
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> setDefaultProtoChunk(ChunkPos chpos, ThreadExecutor<Runnable> executor, ServerWorld world)
    {
        int i = ChunkStatus.EMPTY.getIndex();
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture2 = CompletableFuture.supplyAsync(
                () -> Either.left(new ProtoChunk(chpos, UpgradeData.NO_UPGRADE_DATA, world,  world.getRegistryManager().get(Registry.BIOME_KEY))),
                executor
        );
        combineSavingFuture(completableFuture2, "unfull"); // possible debug data
        futuresByStatus.set(i, completableFuture2);
        return completableFuture2;
    }
}
