package kludwisz.mineshafts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcseed.rand.JRand;
import kludwisz.util.StructurePiece;

public class MineshaftGenerator {
    public static void generate(JRand rand, int chunkX, int chunkZ, boolean mesa, MCVersion version, ArrayList<StructurePiece> pieces) {
        MineshaftGenerator.MineshaftRoom mineshaftRoom = new MineshaftGenerator.MineshaftRoom(0, rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
        pieces.add(mineshaftRoom);
        mineshaftRoom.placeJigsaw(mineshaftRoom, pieces, rand);

        BlockBox boundingBox = BlockBox.empty();
        for (StructurePiece structurePiece : pieces) {
            boundingBox.encompass(structurePiece.boundingBox);
        }

        int m = getYOffset(boundingBox, mesa, version, rand);

        boundingBox.offset(0, m, 0);

        for (StructurePiece structurePiece : pieces) {
            structurePiece.translate(0, m, 0);
        }
    }

    private static int getYOffset(BlockBox boundingBox, boolean mesa, MCVersion version, JRand rand) {
        int m;

        if (version.isOlderThan(MCVersion.v1_18)) {
            if (mesa) {
                m = 63 - boundingBox.maxY + boundingBox.getYSpan() / 2 + 5;
            }
            else {
                int l = boundingBox.getYSpan() + 1;
                if (l < 53) l += rand.nextInt(53 - l);
                m = l - boundingBox.maxY;
            }
        }
        else {
            if (mesa) {
                return 63; // in reality, this is heightmap-based, this value is just a placeholder
            }
            else {
                int l = boundingBox.getYSpan() + (-64) + 1;
                if (l < 53) l += rand.nextInt(53 - l);
                m = l - boundingBox.maxY;
            }
        }

        return m;
    }

    public static StructurePiece getRandomJigsaw(ArrayList<StructurePiece> pieceList, JRand rand, int i, int j, int k, BlockDirection direction, int l) {
        int m = rand.nextInt(100);
        BlockBox blockBox2;
        if (m >= 80) {
            blockBox2 = MineshaftGenerator.MineshaftCrossing.getBoundingBox(pieceList, rand, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftCrossing(l, blockBox2, direction);
            }
        } else if (m >= 70) {
            blockBox2 = MineshaftGenerator.MineshaftStairs.getBoundingBox(pieceList, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftStairs(l, blockBox2, direction);
            }
        } else {
            blockBox2 = MineshaftGenerator.MineshaftCorridor.getBoundingBox(pieceList, rand, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftCorridor(l, rand, blockBox2, direction);
            }
        }

        return null;
    }

    private static void tryGenerateJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> pieceList, JRand rand, int i, int j, int k, BlockDirection direction, int l) {
        if (l <= 8 && Math.abs(i - structurePiece.boundingBox.minX) <= 80 && Math.abs(k - structurePiece.boundingBox.minZ) <= 80) {
            StructurePiece mineshaftPart = getRandomJigsaw(pieceList, rand, i, j, k, direction, l + 1);
            if (mineshaftPart != null) {
                pieceList.add(mineshaftPart);
                mineshaftPart.placeJigsaw(structurePiece, pieceList, rand);
            }
        }
    }

    public static class MineshaftStairs extends StructurePiece {
        public MineshaftStairs(int i, BlockBox blockBox, BlockDirection direction) {
            super(i);
            facing = direction;
            boundingBox = blockBox;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> pieceList, int i, int j, int k, BlockDirection direction) {
            BlockBox blockBox = new BlockBox(i, j - 5, k, i, j + 3 - 1, k);
            switch(direction) {
                case NORTH:
                default:
                    blockBox.maxX = i + 3 - 1;
                    blockBox.minZ = k - 8;
                    break;
                case SOUTH:
                    blockBox.maxX = i + 3 - 1;
                    blockBox.maxZ = k + 8;
                    break;
                case WEST:
                    blockBox.minX = i - 8;
                    blockBox.maxZ = k + 3 - 1;
                    break;
                case EAST:
                    blockBox.maxX = i + 8;
                    blockBox.maxZ = k + 3 - 1;
            }

            return getOverlappingPiece(pieceList, blockBox) != null ? null : blockBox;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> pieceList, JRand rand) {
            if (facing != null) {
                switch(facing) {
                    case NORTH:
                    default:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY, boundingBox.minZ - 1, BlockDirection.NORTH, length);
                        break;
                    case SOUTH:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                        break;
                    case WEST:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ, BlockDirection.WEST, length);
                        break;
                    case EAST:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ, BlockDirection.EAST, length);
                }
            }

        }

        public void calculateAirBoxes() {
            this.airBoxes = new ArrayList<>(7);
            BlockRotation rot = this.facing.getRotation();

            BPos startPos = this.boundingBox.getInside(BPos.ORIGIN, rot);
            int ox = startPos.getX();
            int oy = startPos.getY();
            int oz = startPos.getZ();

            this.airBoxes.add(BlockBox.rotated(ox, oy, oz, 0, 5, 0, 3, 3, 2, rot));
            this.airBoxes.add(BlockBox.rotated(ox, oy, oz, 0, 0, 7, 3, 3, 2, rot));

            for (int i = 0; i < 5; i++) {
                final int y = 5 - i - (i < 4 ? 1 : 0);
                final int sizeY = 7 - i - y + 1;
                this.airBoxes.add(BlockBox.rotated(ox, oy, oz, 0, y, 2 + i, 3, sizeY, 1, rot));
            }
        }
    }

    public static class MineshaftCrossing extends StructurePiece {
        private final BlockDirection direction;
        private final boolean twoFloors;

        public MineshaftCrossing(int i, BlockBox blockBox, BlockDirection direction) {
            super(i);
            this.direction = direction;
            boundingBox = blockBox;
            twoFloors = blockBox.getYSpan() > 3;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> pieceList, JRand rand, int i, int j, int k, BlockDirection facing) {
            BlockBox blockBox = new BlockBox(i, j, k, i, j + 3 - 1, k);
            if (rand.nextInt(4) == 0) {
                blockBox.maxY += 4;
            }

            switch(facing) {
                case NORTH:
                default:
                    blockBox.minX = i - 1;
                    blockBox.maxX = i + 3;
                    blockBox.minZ = k - 4;
                    break;
                case SOUTH:
                    blockBox.minX = i - 1;
                    blockBox.maxX = i + 3;
                    blockBox.maxZ = k + 3 + 1;
                    break;
                case WEST:
                    blockBox.minX = i - 4;
                    blockBox.minZ = k - 1;
                    blockBox.maxZ = k + 3;
                    break;
                case EAST:
                    blockBox.maxX = i + 3 + 1;
                    blockBox.minZ = k - 1;
                    blockBox.maxZ = k + 3;
            }

            return getOverlappingPiece(pieceList, blockBox) != null ? null : blockBox;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> pieceList, JRand rand) {
            switch(this.direction) {
                case NORTH:
                default:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, BlockDirection.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.WEST, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.EAST, length);
                    break;
                case SOUTH:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.WEST, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.EAST, length);
                    break;
                case WEST:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, BlockDirection.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.WEST, length);
                    break;
                case EAST:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, BlockDirection.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, BlockDirection.EAST, length);
            }

            if (twoFloors) {
                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY + 3 + 1, boundingBox.minZ - 1, BlockDirection.NORTH, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY + 3 + 1, boundingBox.minZ + 1, BlockDirection.WEST, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY + 3 + 1, boundingBox.minZ + 1, BlockDirection.EAST, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + 1, boundingBox.minY + 3 + 1, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                }
            }
        }

        public void calculateAirBoxes() {
            this.airBoxes = new ArrayList<>(this.twoFloors ? 3+8 : 3+4);

            Vec3i center = this.boundingBox.getCenter();
            int cx = center.getX();
            int cz = center.getZ();

            // central boxes
            this.airBoxes.add(new BlockBox(
                    cx-1, boundingBox.minY, cz,
                    cx+1, boundingBox.maxY, cz
            ));
            this.airBoxes.add(new BlockBox(
                    cx, boundingBox.minY, cz-1,
                    cx, boundingBox.maxY, cz-1
            ));
            this.airBoxes.add(new BlockBox(
                    cx, boundingBox.minY, cz+1,
                    cx, boundingBox.maxY, cz+1
            ));

            // entrance boxes
            int floors = this.twoFloors ? 2 : 1;
            for (int i = 0; i < floors; i++) {
                final int minY = boundingBox.minY + i * 4;
                final int maxY = minY + 2;

                this.airBoxes.add(new BlockBox(boundingBox.minX, minY, cz-1, boundingBox.minX, maxY, cz+1));
                this.airBoxes.add(new BlockBox(boundingBox.maxX, minY, cz-1, boundingBox.maxX, maxY, cz+1));
                this.airBoxes.add(new BlockBox(cx-1, minY, boundingBox.minZ, cx+1, maxY, boundingBox.minZ));
                this.airBoxes.add(new BlockBox(cx-1, minY, boundingBox.maxZ, cx+1, maxY, boundingBox.maxZ));
            }
        }
    }

    public static class MineshaftCorridor extends StructurePiece {
        public boolean hasCobwebs;
        public boolean hasRails;
        public int numSegments;

        public MineshaftCorridor(int i, JRand rand, BlockBox blockBox, BlockDirection direction) {
            super(i);
            numSegments = blockBox.getXSpan() > blockBox.getZSpan() ? blockBox.getXSpan() / 5 : blockBox.getZSpan() / 5;
            facing = direction;
            boundingBox = blockBox;
            hasRails = rand.nextInt(3) == 0;
            hasCobwebs = !hasRails && rand.nextInt(23) == 0;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> pieceList, JRand rand, int i, int j, int k, BlockDirection direction) {
            BlockBox blockBox = new BlockBox(i, j, k, i, j + 3 - 1, k);
            int l;
            for(l = rand.nextInt(3) + 2; l > 0; --l) {
                int m = l * 5;
                switch(direction) {
                    case NORTH:
                    default:
                        blockBox.maxX = i + 3 - 1;
                        blockBox.minZ = k - (m - 1);
                        break;
                    case SOUTH:
                        blockBox.maxX = i + 3 - 1;
                        blockBox.maxZ = k + m - 1;
                        break;
                    case WEST:
                        blockBox.minX = i - (m - 1);
                        blockBox.maxZ = k + 3 - 1;
                        break;
                    case EAST:
                        blockBox.maxX = i + m - 1;
                        blockBox.maxZ = k + 3 - 1;
                }

                if (getOverlappingPiece(pieceList, blockBox) == null) {
                    break;
                }
            }
            return l > 0 ? blockBox : null;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> pieceList, JRand rand) {
            int j = rand.nextInt(4);
            if (facing != null) {
                switch(facing) {
                    case NORTH:
                    default:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, BlockDirection.WEST, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, BlockDirection.EAST, length);
                        }
                        break;
                    case SOUTH:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ - 3, BlockDirection.WEST, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ - 3, BlockDirection.EAST, length);
                        }
                        break;
                    case WEST:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, BlockDirection.NORTH, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                        }
                        break;
                    case EAST:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX - 3, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, BlockDirection.NORTH, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX - 3, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
                        }
                }
            }

            if (length < 8) {
                int k;
                int l;
                if (facing != BlockDirection.NORTH && facing != BlockDirection.SOUTH) {
                    for(k = boundingBox.minX + 3; k + 3 <= boundingBox.maxX; k += 5) {
                        l = rand.nextInt(5);
                        if (l == 0) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, k, boundingBox.minY, boundingBox.minZ - 1, BlockDirection.NORTH, length + 1);
                        } else if (l == 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, k, boundingBox.minY, boundingBox.maxZ + 1, BlockDirection.SOUTH, length + 1);
                        }
                    }
                } else {
                    for(k = boundingBox.minZ + 3; k + 3 <= boundingBox.maxZ; k += 5) {
                        l = rand.nextInt(5);
                        if (l == 0) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY, k, BlockDirection.WEST, length + 1);
                        } else if (l == 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY, k, BlockDirection.EAST, length + 1);
                        }
                    }
                }
            }
        }

        public void calculateAirBoxes() {
            // FIXME inaccurate, need to account for supports
            this.airBoxes = Collections.singletonList(this.boundingBox);
        }

        // ----------------------------------
        // post-generation property access

        public boolean isSupportingBox(int relativeZ, BlockBox chunk) {
//            for (int relativeX=0; relativeX<=2; relativeX++) {
//                if (!chunk.contains(this.getWorldPos(relativeX, 3, relativeZ)))
//                    return false;
//            }
//            return true;

            // it's not possible for the center block to be in a different chunk than both side blocks
            return chunk.contains(this.getWorldPos(0, 3, relativeZ))
                    && chunk.contains(this.getWorldPos(2, 3, relativeZ));
        }

        public List<BPos> getPossibleChestPositions() {
            ArrayList<BPos> poses = new ArrayList<>();
            addPossibleChestPositions(poses);
            return poses;
        }

        public void addPossibleChestPositions(List<BPos> list) {
            for (int seg = 0; seg < numSegments; seg++) {
                int z = 5 * seg + 2; // position of the support
                list.add(this.getWorldPos(0, 0, z+1));
                list.add(this.getWorldPos(2, 0, z-1));
            }
        }
    }

    public static class MineshaftRoom extends StructurePiece {
        public MineshaftRoom(int i, JRand rand, int j, int k) {
            super(i);
            boundingBox = new BlockBox(j, 50, k, j + 7 + rand.nextInt(6), 54 + rand.nextInt(6), k + 7 + rand.nextInt(6));
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> pieceList, JRand rand) {
            int j = boundingBox.getYSpan() - 3 - 1;
            if (j <= 0) {
                j = 1;
            }

            int k;
            for(k = 0; k < boundingBox.getXSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getXSpan());
                if (k + 3 > boundingBox.getXSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + k, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ - 1, BlockDirection.NORTH, length);
            }

            for(k = 0; k < boundingBox.getXSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getXSpan());
                if (k + 3 > boundingBox.getXSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX + k, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.maxZ + 1, BlockDirection.SOUTH, length);
            }

            for(k = 0; k < boundingBox.getZSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getZSpan());
                if (k + 3 > boundingBox.getZSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.minX - 1, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ + k, BlockDirection.WEST, length);
            }

            for(k = 0; k < boundingBox.getZSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getZSpan());
                if (k + 3 > boundingBox.getZSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, pieceList, rand, boundingBox.maxX + 1, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ + k, BlockDirection.EAST, length);
            }
        }

        public void calculateAirBoxes() {
            // FIXME inaccurate
            this.airBoxes = Collections.singletonList(this.boundingBox);
        }
    }
}
