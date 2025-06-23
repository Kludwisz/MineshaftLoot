package kludwisz.mineshafts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;

import com.seedfinding.mccore.version.MCVersion;
import kludwisz.util.StructurePiece;
import kludwisz.rng.WorldgenRandom;

import static kludwisz.mineshafts.MineshaftGenerator.MineshaftCorridor;

public class MineshaftLoot 
{
	public static final int DECORATION_SALT_1_16 = 30000;
	public static final int DECORATION_SALT_1_21 = 30001;
	private static final int[] cobwebPlacement = {-1, -1, 1, 1, -2, -2, 2, 2};

	private final MCVersion version;
	private final int decorationSalt;
    private final WorldgenRandom rand;
	private final ChunkRand carverRand = new ChunkRand();
    private final ArrayList<StructurePiece> mineshaftPieces = new ArrayList<>();
    private final ArrayList<MineshaftCorridor> corridors = new ArrayList<>();

	public MineshaftLoot(MCVersion version) {
		this.version = version;

		if (version.isNewerOrEqualTo(MCVersion.v1_18)) {
			rand = new WorldgenRandom(WorldgenRandom.Type.XOROSHIRO);
			decorationSalt = DECORATION_SALT_1_21;
		}
		else {
			rand = new WorldgenRandom(WorldgenRandom.Type.JAVA);
			decorationSalt = DECORATION_SALT_1_16;
		}
	}

	// ------------------------------------------------------------------------------

    // needs to be done in the same order as piece generation !
    // returns chest positions and loot seeds (for use with the LootContext object)
    public ArrayList<Pair<BPos, Long>> getChestsInPieceInChunk(MineshaftCorridor c, BlockBox chunk) {
    	if (c.hasCobwebs) {
    		// getting chest loot from spider corridors would require
    		// additional cobweb placement simulations, so here it's skipped
    		skipCallsInPieceInChunk(c, chunk);
    		return new ArrayList<>();
    	}
    	
    	int m = c.numSegments * 5;
		rand.skip(m * 3); // skip ceiling air blocks

        ArrayList<Pair<BPos, Long>> chests = new ArrayList<>();
        BPos chest;

        for (int j = 0; j < c.numSegments; j++) {
        	int center = j*5 + 2;
        	skipInitialDecorators(c, chunk, center);
            
            //   get first chest
            if(rand.nextInt(100) == 0) {
            	chest = c.getWorldPos(2, 0, center-1);
            	if ( chunk.contains(chest) ) {
            		rand.nextSeed();
            		chests.add(new Pair<>(chest, rand.nextLong()));
            	}
            }
            
            //   get second chest
            if(rand.nextInt(100) == 0) {
            	chest = c.getWorldPos(0, 0, center+1);
            	if ( chunk.contains(chest) ) {
            		rand.nextSeed();
            		chests.add(new Pair<>(chest, rand.nextLong()));
            	}
            }
        }

		skipRails(c, chunk);
        
        return chests;
    }
    
    // same as getChestsInPieceInChunk, but slightly faster (and also "handles" spider corridors)
    // practically the same as getAllChestsInPieceInChunk, but here we just skip everything, so it's faster
    public void skipCallsInPieceInChunk(MineshaftCorridor c, BlockBox chunk) {
        int m = c.numSegments * 5;
        rand.skip(m * 3); // ceiling air
        
        // if spawner corridor, skip cobwebs as well
        if (c.hasCobwebs)
        	rand.skip(m * 6);

        BPos chest;
        int center;

        for (int j = 0; j < c.numSegments; j++) {
        	center = j*5 + 2;
			skipInitialDecorators(c, chunk, center);

            if(rand.nextInt(100) == 0) {
            	chest = c.getWorldPos(2, 0, center-1);
            	if (chunk.contains(chest))
            		rand.skip(3);
            }
            
            if(rand.nextInt(100) == 0) {
            	chest = c.getWorldPos(0, 0, center+1);
            	if (chunk.contains(chest))
            		rand.skip(3);
            }

			if (c.hasCobwebs)
				rand.nextSeed();
        }

		skipRails(c, chunk);
    }

	// skips support placement and cobweb placement calls
	private void skipInitialDecorators(MineshaftCorridor c, BlockBox chunk, int center) {
		//   supports, cobwebs and chests check for proper chunk placement first and only then make rand calls
		//   that's why we need to skip only the calls that would occur inside the chunk's BlockBox
		//   for supports, we also need an extra isSupportingBox method that checks if the support's BlockBox lies in one chunk

		// supports
		if (c.isSupportingBox(center, chunk)) {
			if(rand.nextInt(4) != 0) {
				rand.skip(2);
			}
		}

		// cobwebs
		for (int i=0; i<8; i++) {
			if (chunk.contains(c.getWorldPos((i%2)*2, 2, center+cobwebPlacement[i])))
				rand.nextSeed();
		}
	}

	// skips rail placement calls
	private void skipRails(MineshaftCorridor c, BlockBox chunk) {
		if (!c.hasRails) return;

		// rails, like cobwebs, check for proper chunk placement before calling rand
		int m = c.depth * 5;
		for (int j=0; j<m; j++)
			if (chunk.contains(c.getWorldPos(1, 0, j)))
				rand.nextSeed();
	}

	public ArrayList<CPos> getPieceChunks(StructurePiece piece) {
		ArrayList<CPos> pieceChunks = new ArrayList<>();

		// the piece spans all chunk positions from its min corner to its max corner
		CPos minCorner = new CPos(piece.boundingBox.minX >> 4, piece.boundingBox.minZ >> 4);
		CPos maxCorner = new CPos(piece.boundingBox.maxX >> 4, piece.boundingBox.maxZ >> 4);

		for (int cx = minCorner.getX(); cx <= maxCorner.getX(); cx++) {
			for (int cz = minCorner.getZ(); cz <= maxCorner.getZ(); cz++) {
				pieceChunks.add(new CPos(cx, cz));
			}
		}

		return pieceChunks;
	}

	// -----------------------------------------------------------------------------------------------------------------

    // WARNING! this is extremely inaccurate in oceans, would recommend to ignore the output if biome is ocean
    // returns chest positions and loot seeds within the desired corridor piece (for use with the LootContext object)
    public ArrayList<Pair<BPos, Long>> getAllChestsInCorridor(MineshaftCorridor c, long worldSeed) {
    	ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();
    	
    	// we need to process every chunk that contains our piece, in any order
    	for (CPos chunkPos : getPieceChunks(c)) {
    		rand.setDecoratorSeed(worldSeed, chunkPos.getX(), chunkPos.getZ(), decorationSalt);
	    	BlockBox chunk = new BlockBox(chunkPos.getX() << 4, -64, chunkPos.getZ() << 4, (chunkPos.getX() << 4)+15, 512, (chunkPos.getZ() << 4)+15);
    		
	    	for (MineshaftCorridor piece : corridors) {
	    		if (piece.boundingBox.contains(c.boundingBox.getCenter()) && piece.boundingBox.intersects(chunk)) {
	    			chests.addAll( getChestsInPieceInChunk(piece, chunk) );
	    			break; // after proccessing the target piece part, we can move on
	    		}
        		if (piece.boundingBox.intersects(chunk))
        			skipCallsInPieceInChunk(piece, chunk);
        	}
    	}
    	
    	return chests;
    }
    
    // WARNING! this is extremely inaccurate in oceans, would recommend to ignore the output if biome is ocean
    // returns chest positions and loot seeds within the desired chunk (for use with the LootContext object)
    public ArrayList<Pair<BPos, Long>> getAllChestsInChunk(CPos chunkPos, long worldSeed) {
    	rand.setDecoratorSeed(worldSeed, chunkPos.getX(), chunkPos.getZ(), decorationSalt);
    	BlockBox chunk = new BlockBox(chunkPos.getX() << 4, -64, chunkPos.getZ() << 4, (chunkPos.getX() << 4)+15, 512, (chunkPos.getZ() << 4)+15);
    	ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();
        
    	for (MineshaftCorridor piece : corridors) {
    		if (piece.boundingBox.intersects(chunk)) {
    			chests.addAll( getChestsInPieceInChunk(piece, chunk) );
    		}
    	}
    	
    	return chests;
    }

	public ArrayList<Pair<BPos, Long>> getAllChests(long worldSeed) {
		ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();

		// doing this chunk-wise should be faster than piece-wise
		HashSet<CPos> checkedChunks = new HashSet<>();
		for (MineshaftCorridor corridor : corridors) {
			ArrayList<CPos> pieceChunks = getPieceChunks(corridor);
			for (CPos chunkPos : pieceChunks) {
				if (checkedChunks.add(chunkPos)) {
					chests.addAll(getAllChestsInChunk(chunkPos, worldSeed));
				}
			}
		}

		return chests;
	}

	public boolean generateMineshaft(long structureSeed, CPos chunkPos, boolean mesa) {
		return generateMineshaft(structureSeed, chunkPos, mesa, false);
	}

	public boolean generateMineshaft(long structureSeed, CPos chunkPos, boolean mesa, boolean skipCorridors) {
		mineshaftPieces.clear();

		int cx = chunkPos.getX();
		int cz = chunkPos.getZ();
		long s = carverRand.setCarverSeed(structureSeed, cx, cz, MCVersion.v1_16_1);

		if (carverRand.nextDouble() < 0.004D) {
			if (version.isOlderThan(MCVersion.v1_18)) // TODO verify
				carverRand.setSeed(s);
			MineshaftGenerator.generate(carverRand, cx, cz, mesa, version, mineshaftPieces);
		}
		else {
			return false;
		}

		if (!skipCorridors) {
			corridors.clear();
			for (StructurePiece piece : mineshaftPieces) {
				if (piece instanceof MineshaftCorridor) {
					corridors.add((MineshaftCorridor) piece);
				}
			}
		}

		return true;
	}

    // getters for the corridor and piece lists

    public List<MineshaftCorridor> getCorridors() {
    	return corridors;
    }
    
    public List<StructurePiece> getPieces() {
    	return mineshaftPieces;
    }
}