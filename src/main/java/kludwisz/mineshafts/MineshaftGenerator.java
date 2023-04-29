package kludwisz.mineshafts;

import java.util.ArrayList;

import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.seedutils.rand.JRand;
import kaptainwutax.mcutils.util.block.BlockBox;
import kaptainwutax.mcutils.version.MCVersion;


public class MineshaftGenerator {
   
	public static ArrayList<StructurePiece> generateForChunk(long worldSeed, int chunkX, int chunkZ, boolean mesa, ArrayList<Corridor> corridors) {
        ChunkRand rand = new ChunkRand();
        long s = rand.setCarverSeed(worldSeed, chunkX, chunkZ, MCVersion.v1_16_1);

        if(rand.nextDouble() < 0.004D) {
            rand.setSeed(s);
            return generate((JRand)rand, chunkX, chunkZ, mesa, corridors);
        }

        return new ArrayList<>();
    }

    public static ArrayList<StructurePiece> generate(JRand rand, int chunkX, int chunkZ, boolean mesa, ArrayList<Corridor> corridors) {
        ArrayList<StructurePiece> children = new ArrayList<>();
        
        MineshaftGenerator.MineshaftRoom mineshaftRoom = new MineshaftGenerator.MineshaftRoom(0, rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
        children.add(mineshaftRoom);
        mineshaftRoom.placeJigsaw(mineshaftRoom, children, rand);

        BlockBox boundingBox = BlockBox.empty();
        for(StructurePiece structurePiece : children){
            boundingBox.encompass(structurePiece.boundingBox);
        }

        int m;
        
        if(mesa) {
            m = 63 - boundingBox.maxY + boundingBox.getYSpan() / 2 + 5;
        } else {
            int l = boundingBox.getYSpan() + 1;
            if (l < 53) l += rand.nextInt(53 - l);
            m = l - boundingBox.maxY;
        }

        boundingBox.offset(0, m, 0);

        for(StructurePiece structurePiece : children){
            structurePiece.translate(0, m, 0);
            if(structurePiece instanceof MineshaftCorridor){
    //            int y = structurePiece.applyYTransform(0);
    //            int x = structurePiece.applyXTransform(1, 2);
    //            int z = structurePiece.applyZTransform(1, 2);
                BlockBox bb = structurePiece.boundingBox;
                int length;
                if (structurePiece.facing.axis == Direction.Axis.Z) {
                    length = structurePiece.boundingBox.getZSpan()/5;
                } else {
                    length = structurePiece.boundingBox.getXSpan()/5;
                }
                corridors.add(new Corridor(length, structurePiece.facing, bb, ((MineshaftCorridor)structurePiece).hasCobwebs, ((MineshaftCorridor)structurePiece).hasRails));
            }
        }

        return children;
    }

    public static StructurePiece getJRandJigsaw(ArrayList<StructurePiece> ArrayList, JRand rand, int i, int j, int k, Direction direction, int l) {
        int m = rand.nextInt(100);
        BlockBox blockBox2;
        if (m >= 80) {
            blockBox2 = MineshaftGenerator.MineshaftCrossing.getBoundingBox(ArrayList, rand, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftCrossing(l, blockBox2, direction);
            }
        } else if (m >= 70) {
            blockBox2 = MineshaftGenerator.MineshaftStairs.getBoundingBox(ArrayList, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftStairs(l, blockBox2, direction);
            }
        } else {
            blockBox2 = MineshaftGenerator.MineshaftCorridor.getBoundingBox(ArrayList, rand, i, j, k, direction);
            if (blockBox2 != null) {
                return new MineshaftGenerator.MineshaftCorridor(l, rand, blockBox2, direction);
            }
        }

        return null;
    }

    private static void tryGenerateJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> ArrayList, JRand rand, int i, int j, int k, Direction direction, int l) {
        if (l <= 8 && Math.abs(i - structurePiece.boundingBox.minX) <= 80 && Math.abs(k - structurePiece.boundingBox.minZ) <= 80) {
            StructurePiece mineshaftPart = getJRandJigsaw(ArrayList, rand, i, j, k, direction, l + 1);
            if (mineshaftPart != null) {
                ArrayList.add(mineshaftPart);
                mineshaftPart.placeJigsaw(structurePiece, ArrayList, rand);
            }
        }
    }

    public static class MineshaftStairs extends StructurePiece {
        public MineshaftStairs(int i, BlockBox blockBox, Direction direction) {
            super(i);
            facing = direction;
            boundingBox = blockBox;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> ArrayList, int i, int j, int k, Direction direction) {
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

            return getOverlappingPiece(ArrayList, blockBox) != null ? null : blockBox;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> ArrayList, JRand rand) {
            if (facing != null) {
                switch(facing) {
                    case NORTH:
                    default:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY, boundingBox.minZ - 1, Direction.NORTH, length);
                        break;
                    case SOUTH:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY, boundingBox.maxZ + 1, Direction.SOUTH, length);
                        break;
                    case WEST:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ, Direction.WEST, length);
                        break;
                    case EAST:
                        MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ, Direction.EAST, length);
                }
            }

        }
    }

    public static class MineshaftCrossing extends StructurePiece {
        private final Direction direction;
        private final boolean twoFloors;

        public MineshaftCrossing(int i, BlockBox blockBox, Direction direction) {
            super(i);
            this.direction = direction;
            boundingBox = blockBox;
            twoFloors = blockBox.getYSpan() > 3;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> ArrayList, JRand rand, int i, int j, int k, Direction facing) {
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

            return getOverlappingPiece(ArrayList, blockBox) != null ? null : blockBox;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> ArrayList, JRand rand) {
            switch(this.direction) {
                case NORTH:
                default:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, Direction.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, Direction.WEST, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, Direction.EAST, length);
                    break;
                case SOUTH:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, Direction.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, Direction.WEST, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, Direction.EAST, length);
                    break;
                case WEST:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, Direction.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, Direction.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY, boundingBox.minZ + 1, Direction.WEST, length);
                    break;
                case EAST:
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.minZ - 1, Direction.NORTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY, boundingBox.maxZ + 1, Direction.SOUTH, length);
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY, boundingBox.minZ + 1, Direction.EAST, length);
            }

            if (twoFloors) {
                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY + 3 + 1, boundingBox.minZ - 1, Direction.NORTH, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY + 3 + 1, boundingBox.minZ + 1, Direction.WEST, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY + 3 + 1, boundingBox.minZ + 1, Direction.EAST, length);
                }

                if (rand.nextBoolean()) {
                    MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + 1, boundingBox.minY + 3 + 1, boundingBox.maxZ + 1, Direction.SOUTH, length);
                }
            }

        }
    }

    public static class MineshaftCorridor extends StructurePiece {
        public boolean hasCobwebs;
        public boolean hasRails;

        public MineshaftCorridor(int i, JRand rand, BlockBox blockBox, Direction direction) {
            super(i);
            facing = direction;
            boundingBox = blockBox;
            hasRails = rand.nextInt(3) == 0;
            hasCobwebs = !hasRails && rand.nextInt(23) == 0;
        }

        public static BlockBox getBoundingBox(ArrayList<StructurePiece> ArrayList, JRand rand, int i, int j, int k, Direction direction) {
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

                if (getOverlappingPiece(ArrayList, blockBox) == null) {
                    break;
                }
            }
            return l > 0 ? blockBox : null;
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> ArrayList, JRand rand) {
            int j = rand.nextInt(4);
            if (facing != null) {
                switch(facing) {
                    case NORTH:
                    default:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, Direction.WEST, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, Direction.EAST, length);
                        }
                        break;
                    case SOUTH:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ - 3, Direction.WEST, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ - 3, Direction.EAST, length);
                        }
                        break;
                    case WEST:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, Direction.NORTH, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, Direction.SOUTH, length);
                        }
                        break;
                    case EAST:
                        if (j <= 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ, facing, length);
                        } else if (j == 2) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX - 3, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.minZ - 1, Direction.NORTH, length);
                        } else {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX - 3, boundingBox.minY - 1 + rand.nextInt(3), boundingBox.maxZ + 1, Direction.SOUTH, length);
                        }
                }
            }

            if (length < 8) {
                int k;
                int l;
                if (facing != Direction.NORTH && facing != Direction.SOUTH) {
                    for(k = boundingBox.minX + 3; k + 3 <= boundingBox.maxX; k += 5) {
                        l = rand.nextInt(5);
                        if (l == 0) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, k, boundingBox.minY, boundingBox.minZ - 1, Direction.NORTH, length + 1);
                        } else if (l == 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, k, boundingBox.minY, boundingBox.maxZ + 1, Direction.SOUTH, length + 1);
                        }
                    }
                } else {
                    for(k = boundingBox.minZ + 3; k + 3 <= boundingBox.maxZ; k += 5) {
                        l = rand.nextInt(5);
                        if (l == 0) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY, k, Direction.WEST, length + 1);
                        } else if (l == 1) {
                            MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY, k, Direction.EAST, length + 1);
                        }
                    }
                }
            }
        }
    }

    public static class MineshaftRoom extends StructurePiece {
        public MineshaftRoom(int i, JRand rand, int j, int k) {
            super(i);
            boundingBox = new BlockBox(j, 50, k, j + 7 + rand.nextInt(6), 54 + rand.nextInt(6), k + 7 + rand.nextInt(6));
        }

        public void placeJigsaw(StructurePiece structurePiece, ArrayList<StructurePiece> ArrayList, JRand rand) {
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

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + k, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ - 1, Direction.NORTH, length);
            }

            for(k = 0; k < boundingBox.getXSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getXSpan());
                if (k + 3 > boundingBox.getXSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX + k, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.maxZ + 1, Direction.SOUTH, length);
            }

            for(k = 0; k < boundingBox.getZSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getZSpan());
                if (k + 3 > boundingBox.getZSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.minX - 1, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ + k, Direction.WEST, length);
            }

            for(k = 0; k < boundingBox.getZSpan(); k += 4) {
                k += rand.nextInt(boundingBox.getZSpan());
                if (k + 3 > boundingBox.getZSpan()) {
                    break;
                }

                MineshaftGenerator.tryGenerateJigsaw(structurePiece, ArrayList, rand, boundingBox.maxX + 1, boundingBox.minY + rand.nextInt(j) + 1, boundingBox.minZ + k, Direction.EAST, length);
            }
        }
    }
    
}
