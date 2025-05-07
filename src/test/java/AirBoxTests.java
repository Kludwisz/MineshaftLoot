import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import kludwisz.mineshafts.MineshaftGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AirBoxTests {
    // FIXME make this test independent from BlockBox's toString() method
    @Test
    public void testStaircaseAirBoxes() {
        BlockBox box = MineshaftGenerator.MineshaftStairs.getBoundingBox(new ArrayList<>(), -730, 32, -1190, BlockDirection.SOUTH);
        MineshaftGenerator.MineshaftStairs stairs = new MineshaftGenerator.MineshaftStairs(0, box, BlockDirection.SOUTH);
        stairs.calculateAirBoxes();

        String result = "";
        for (BlockBox airBox : stairs.airBoxes) {
            result += "Air Box: " + airBox + "\n";
        }

        assertEquals(
        """
                Air Box: BlockBox{minX=-730, minY=32, minZ=-1190, maxX=-728, maxY=34, maxZ=-1189}
                Air Box: BlockBox{minX=-730, minY=27, minZ=-1183, maxX=-728, maxY=29, maxZ=-1182}
                Air Box: BlockBox{minX=-730, minY=31, minZ=-1188, maxX=-728, maxY=34, maxZ=-1188}
                Air Box: BlockBox{minX=-730, minY=30, minZ=-1187, maxX=-728, maxY=33, maxZ=-1187}
                Air Box: BlockBox{minX=-730, minY=29, minZ=-1186, maxX=-728, maxY=32, maxZ=-1186}
                Air Box: BlockBox{minX=-730, minY=28, minZ=-1185, maxX=-728, maxY=31, maxZ=-1185}
                Air Box: BlockBox{minX=-730, minY=28, minZ=-1184, maxX=-728, maxY=30, maxZ=-1184}
                """,
                result
        );

        box = MineshaftGenerator.MineshaftStairs.getBoundingBox(new ArrayList<>(), -734, 29, -1231, BlockDirection.EAST);
        stairs = new MineshaftGenerator.MineshaftStairs(0, box, BlockDirection.EAST);
        stairs.calculateAirBoxes();

        result = "";
        for (BlockBox airBox : stairs.airBoxes) {
            result += "Air Box: " + airBox + "\n";
        }
        assertEquals(
                """
                        Air Box: BlockBox{minX=-734, minY=29, minZ=-1231, maxX=-733, maxY=31, maxZ=-1229}
                        Air Box: BlockBox{minX=-727, minY=24, minZ=-1231, maxX=-726, maxY=26, maxZ=-1229}
                        Air Box: BlockBox{minX=-732, minY=28, minZ=-1231, maxX=-732, maxY=31, maxZ=-1229}
                        Air Box: BlockBox{minX=-731, minY=27, minZ=-1231, maxX=-731, maxY=30, maxZ=-1229}
                        Air Box: BlockBox{minX=-730, minY=26, minZ=-1231, maxX=-730, maxY=29, maxZ=-1229}
                        Air Box: BlockBox{minX=-729, minY=25, minZ=-1231, maxX=-729, maxY=28, maxZ=-1229}
                        Air Box: BlockBox{minX=-728, minY=25, minZ=-1231, maxX=-728, maxY=27, maxZ=-1229}
                        """,
                result
        );

        System.out.println("Mineshaft Staircase Airboxes OK");
    }
}
