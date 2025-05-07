import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mcseed.rand.JRand;
import kludwisz.mineshafts.MineshaftGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AirBoxTests {
    @Test
    public void testStaircaseAirBoxes() {
        BlockBox box = MineshaftGenerator.MineshaftStairs.getBoundingBox(new ArrayList<>(), -730, 32, -1190, BlockDirection.SOUTH);
        MineshaftGenerator.MineshaftStairs stairs = new MineshaftGenerator.MineshaftStairs(0, box, BlockDirection.SOUTH);
        stairs.calculateAirBoxes();
        List<BlockBox> expectedBoxes = getListFromString(
                """
                BlockBox{minX=-730, minY=32, minZ=-1190, maxX=-728, maxY=34, maxZ=-1189}
                BlockBox{minX=-730, minY=27, minZ=-1183, maxX=-728, maxY=29, maxZ=-1182}
                BlockBox{minX=-730, minY=31, minZ=-1188, maxX=-728, maxY=34, maxZ=-1188}
                BlockBox{minX=-730, minY=30, minZ=-1187, maxX=-728, maxY=33, maxZ=-1187}
                BlockBox{minX=-730, minY=29, minZ=-1186, maxX=-728, maxY=32, maxZ=-1186}
                BlockBox{minX=-730, minY=28, minZ=-1185, maxX=-728, maxY=31, maxZ=-1185}
                BlockBox{minX=-730, minY=28, minZ=-1184, maxX=-728, maxY=30, maxZ=-1184}
                """
        );
        assertEquals(
                new HashSet<>(expectedBoxes),
                new HashSet<>(stairs.airBoxes)
        );

        box = MineshaftGenerator.MineshaftStairs.getBoundingBox(new ArrayList<>(), -734, 29, -1231, BlockDirection.EAST);
        stairs = new MineshaftGenerator.MineshaftStairs(0, box, BlockDirection.EAST);
        stairs.calculateAirBoxes();
        expectedBoxes = getListFromString(
                """
                BlockBox{minX=-727, minY=24, minZ=-1231, maxX=-726, maxY=26, maxZ=-1229}
                BlockBox{minX=-732, minY=28, minZ=-1231, maxX=-732, maxY=31, maxZ=-1229}
                BlockBox{minX=-731, minY=27, minZ=-1231, maxX=-731, maxY=30, maxZ=-1229}
                BlockBox{minX=-730, minY=26, minZ=-1231, maxX=-730, maxY=29, maxZ=-1229}
                BlockBox{minX=-734, minY=29, minZ=-1231, maxX=-733, maxY=31, maxZ=-1229}
                BlockBox{minX=-729, minY=25, minZ=-1231, maxX=-729, maxY=28, maxZ=-1229}
                BlockBox{minX=-728, minY=25, minZ=-1231, maxX=-728, maxY=27, maxZ=-1229}
                """
        );
        assertEquals(
                new HashSet<>(expectedBoxes),
                new HashSet<>(stairs.airBoxes)
        );

        System.out.println("Mineshaft Staircase Airboxes OK");
    }

    @Test
    public void testCrossingAirBoxes() {
        FakeJRand rand = new FakeJRand();
        rand.nextIntValue = 0;
        BlockBox box = MineshaftGenerator.MineshaftCrossing.getBoundingBox(new ArrayList<>(), rand, -133, -20, -2448, BlockDirection.EAST);
        assertNotNull(box);
        MineshaftGenerator.MineshaftCrossing crossing = new MineshaftGenerator.MineshaftCrossing(0, box, BlockDirection.EAST);
        crossing.calculateAirBoxes();
        List<BlockBox> expectedBoxes = getListFromString(
                """
                BlockBox{minX=-132, minY=-20, minZ=-2447, maxX=-130, maxY=-14, maxZ=-2447}
                BlockBox{minX=-131, minY=-20, minZ=-2448, maxX=-131, maxY=-14, maxZ=-2448}
                BlockBox{minX=-131, minY=-20, minZ=-2446, maxX=-131, maxY=-14, maxZ=-2446}
                BlockBox{minX=-133, minY=-20, minZ=-2448, maxX=-133, maxY=-18, maxZ=-2446}
                BlockBox{minX=-129, minY=-20, minZ=-2448, maxX=-129, maxY=-18, maxZ=-2446}
                BlockBox{minX=-132, minY=-20, minZ=-2449, maxX=-130, maxY=-18, maxZ=-2449}
                BlockBox{minX=-132, minY=-20, minZ=-2445, maxX=-130, maxY=-18, maxZ=-2445}
                BlockBox{minX=-133, minY=-16, minZ=-2448, maxX=-133, maxY=-14, maxZ=-2446}
                BlockBox{minX=-129, minY=-16, minZ=-2448, maxX=-129, maxY=-14, maxZ=-2446}
                BlockBox{minX=-132, minY=-16, minZ=-2449, maxX=-130, maxY=-14, maxZ=-2449}
                BlockBox{minX=-132, minY=-16, minZ=-2445, maxX=-130, maxY=-14, maxZ=-2445}
                """
        );
        assertEquals(
                new HashSet<>(expectedBoxes),
                new HashSet<>(crossing.airBoxes)
        );

        rand.nextIntValue = 1;
        box = MineshaftGenerator.MineshaftCrossing.getBoundingBox(new ArrayList<>(), rand, -524, -21, -2639, BlockDirection.WEST);
        assertNotNull(box);
        crossing = new MineshaftGenerator.MineshaftCrossing(0, box, BlockDirection.WEST);
        crossing.calculateAirBoxes();
        expectedBoxes = getListFromString(
                """
                BlockBox{minX=-527, minY=-21, minZ=-2638, maxX=-525, maxY=-19, maxZ=-2638}
                BlockBox{minX=-526, minY=-21, minZ=-2639, maxX=-526, maxY=-19, maxZ=-2639}
                BlockBox{minX=-526, minY=-21, minZ=-2637, maxX=-526, maxY=-19, maxZ=-2637}
                BlockBox{minX=-528, minY=-21, minZ=-2639, maxX=-528, maxY=-19, maxZ=-2637}
                BlockBox{minX=-524, minY=-21, minZ=-2639, maxX=-524, maxY=-19, maxZ=-2637}
                BlockBox{minX=-527, minY=-21, minZ=-2640, maxX=-525, maxY=-19, maxZ=-2640}
                BlockBox{minX=-527, minY=-21, minZ=-2636, maxX=-525, maxY=-19, maxZ=-2636}
                """
        );
        assertEquals(
                new HashSet<>(expectedBoxes),
                new HashSet<>(crossing.airBoxes)
        );

        System.out.println("Mineshaft Crossing Airboxes OK");
    }


    private static List<BlockBox> getListFromString(String output) {
        List<BlockBox> list = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                String[] parts = line.replace("BlockBox{", "").replace("}", "").split(", ");
                int minX = Integer.parseInt(parts[0].split("=")[1]);
                int minY = Integer.parseInt(parts[1].split("=")[1]);
                int minZ = Integer.parseInt(parts[2].split("=")[1]);
                int maxX = Integer.parseInt(parts[3].split("=")[1]);
                int maxY = Integer.parseInt(parts[4].split("=")[1]);
                int maxZ = Integer.parseInt(parts[5].split("=")[1]);
                list.add(new BlockBox(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }
        return list;
    }

    private static class FakeJRand extends JRand {
        public int nextIntValue = 0;

        public FakeJRand() {
            this(0L);
        }

        public FakeJRand(long seed) {
            super(seed);
        }

        @Override
        public int nextInt(int bound) {
            return nextIntValue;
        }
    }
}
