import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.mineshafts.MineshaftLoot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExamplesToTest {
    /**
     * Example of use: getting all the positions and loot seeds of minecart chests
     * in a specific chunk, generating as part of a specific non-mesa mineshaft structure.
     */
    @Test
    public void getLootInChunk() {
        final MCVersion version = MCVersion.v1_17_1;
        final long worldSeed = 1234321L;
        final CPos mineshaftStartChunk = new CPos(3, -20);
        final CPos chestChunk = new CPos(3,-23);
        MineshaftLoot ml = new MineshaftLoot(version);

        assertTrue(ml.generateMineshaft(worldSeed, mineshaftStartChunk, false));

        ml.getAllChestsInChunk(chestChunk, worldSeed).forEach(pair -> {
            System.out.println("Chest at " + tpCommand(pair.getFirst()) + ", loot seed " + pair.getSecond());
        });

        // output:
        // Chest at Pos{x=54, y=27, z=-355}, loot seed 7460246627384350961
        // Chest at Pos{x=61, y=32, z=-366}, loot seed -7222143982153524434L
    }

    /**
     * Example of use: getting all the positions and loot seeds of minecart chests
     * that are part of a given mesa mineshaft.
     */
    @Test
    public void getAllLoot() {
        final MCVersion version = MCVersion.v1_17_1;
        final long worldSeed = 1234321L;
        final CPos mineshaftStartChunk = new CPos(385, -23);
        MineshaftLoot ml = new MineshaftLoot(version);

        assertTrue(ml.generateMineshaft(worldSeed, mineshaftStartChunk, true));

        ml.getAllChests(worldSeed)
                .forEach(pair -> {
                    System.out.println("Chest at " + tpCommand(pair.getFirst()) + ", loot seed " + pair.getSecond());
                });

        // output:
        // Chest at /tp @p 6161 68 -415, loot seed 2790521319154443197
        // Chest at /tp @p 6116 69 -419, loot seed -3692010751452976821
    }

    /**
     * Example of use: getting all the positions of spider corridors
     * in a given mesa mineshaft.
     */
    @Test
    public void getSpiderCorridors() {
        final long worldSeed = 1234321L;
        final CPos mineshaftStartChunk = new CPos(385, -23);
        MineshaftLoot ml = new MineshaftLoot(MCVersion.v1_17_1);

        assertTrue(ml.generateMineshaft(worldSeed, mineshaftStartChunk, true));

        ml.getCorridors().forEach(corridor -> {
            if (corridor.hasCobwebs) {
                System.out.println("Spider corridor at " + tpCommand(corridor.boundingBox.getCenter()));
            }
        });

        // output:
        // Spider corridor at /tp @p 6139 71 -384
        // Spider corridor at /tp @p 6113 72 -377
        // Spider corridor at /tp @p 6146 65 -371
    }

    /**
     * Example of use: getting the (x,z) positions of spider corridors (MC 1.21.4)
     */
    @Test
    public void getSpiderCorridorsRecentVersion() {
        final MCVersion version = MCVersion.v1_21;
        final long worldSeed = 123456789L;
        final CPos mineshaftStartChunk = new CPos(62, 83);
        MineshaftLoot ml = new MineshaftLoot(version);

        assertTrue(ml.generateMineshaft(worldSeed, mineshaftStartChunk, false));

        ml.getCorridors().forEach(corridor -> {
            if (corridor.hasCobwebs) {
                System.out.println("Spider corridor at " + tpCommand(corridor.boundingBox.getCenter()));
            }
        });

        // output:
        // Spider corridor at /tp @p 1003 28 1383
        // Spider corridor at /tp @p 1015 24 1347
        // Spider corridor at /tp @p 1009 23 1330
    }

    /**
     * Example of use: getting the (x,z) positions of minecart chests (MC 1.21.4)
     */
    @Test
    public void getLootRecentVersion() {
        final MCVersion version = MCVersion.v1_21;
        final long worldSeed = 123456789L;
        final CPos mineshaftStartChunk = new CPos(7, 18);
        final CPos chestChunk = new CPos(7, 17);
        MineshaftLoot ml = new MineshaftLoot(version);

        assertTrue(ml.generateMineshaft(worldSeed, mineshaftStartChunk, false));

        ml.getAllChestsInChunk(chestChunk, worldSeed).forEach(pair -> {
            System.out.println("Chest at " + tpCommand(pair.getFirst()) + ", loot seed " + pair.getSecond());
        });

        // output (height is incorrect):
    }

    private String tpCommand(Vec3i pos) {
        return "/tp @p " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
