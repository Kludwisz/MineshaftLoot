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
import com.seedfinding.mcseed.lcg.LCG;

import kludwisz.mineshafts.util.CoordinateTransformer;
import static kludwisz.mineshafts.MineshaftGenerator.MineshaftCorridor;

public class MineshaftLoot 
{
	private static final int[] cobwebPlacement = {-1, -1, 1, 1, -2, -2, 2, 2};
	private static final LCG skipChest = LCG.JAVA.combine(3);
	private static final LCG skipTorches = LCG.JAVA.combine(2);
    private static final LCG skipSingleCall = LCG.JAVA.combine(1);
    private static final ChunkRand rand = new ChunkRand();

    private static final ArrayList<StructurePiece> mineshaftPieces = new ArrayList<>();
    private static final ArrayList<MineshaftCorridor> corridors = new ArrayList<>();

    // needs to be done in the same order as piece generation !
    // returns chest positions and loot seeds (for use with the LootContext object)
    public static ArrayList<Pair<BPos, Long>> getChestsInPieceInChunk(MineshaftCorridor c, BlockBox chunk)
    {
    	if (c.hasCobwebs) {
    		// getting chest loot from spider corridors would require
    		// additional cobweb placement simulations, so here it's skipped
    		skipCallsInPieceInChunk(c, chunk);
    		return new ArrayList<Pair<BPos, Long>>();
    	}
    	
    	int m = c.length * 5;
        LCG skipCeiling = LCG.JAVA.combine(m * 3);
        ArrayList<Pair<BPos, Long>> chests = new ArrayList<>();

        //   skip ceiling air blocks
        rand.advance(skipCeiling);
        
        CoordinateTransformer.setParams(c.facing, c.boundingBox);
        BPos chest;
        int center;
        
        // length = numsections from the mc source code
        for (int j=0; j<c.length; j++) {
        	center = j*5 + 2;
        	
        	//   supports, cobwebs and chests check for proper chunk placement first and only then make rand calls
            //   that's why we need to skip only the calls that would occur inside the chunk's BlockBox
        	//   for supports, we also need an extra isSupportingBox method (defined in CoordinateTransformer)
        	if (CoordinateTransformer.isSupportingBox(center, chunk)) {
        		if(rand.nextInt(4) != 0) {
                    rand.advance(skipTorches);
                }
        	}
            
        	//   cobwebs
            for (int i=0; i<8; i++) {
            	if ( chunk.contains( CoordinateTransformer.getWorldPos((i%2)*2, 2, center+cobwebPlacement[i]))) 
            		rand.advance(skipSingleCall);
            }
            
            //   get first chest
            if(rand.nextInt(100) == 0) {
            	chest = CoordinateTransformer.getWorldPos(2, 0, center-1);
            	if ( chunk.contains(chest) ) {
            		rand.advance(skipSingleCall);
            		chests.add(new Pair<BPos, Long>(chest, rand.nextLong()));
            	}
            }
            
            //   get second chest
            if(rand.nextInt(100) == 0) {
            	chest = CoordinateTransformer.getWorldPos(0, 0, center+1);
            	if ( chunk.contains(chest) ) {
            		rand.advance(skipSingleCall);
            		chests.add(new Pair<BPos, Long>(chest, rand.nextLong()));
            	}
            }
        }
        
        //   unfortunately, rails (yet again) check for proper chunk placement before calling rand
        if (c.hasRails) {
        	for (int j=0; j<m; j++)
        		if ( chunk.contains(CoordinateTransformer.getWorldPos(1, 0, j)) )
        			rand.advance(skipSingleCall);
        }
        
        return chests;
    }
    
    // same as getChestsInPieceInChunk, but slightly faster (and also handles spider corridors
    // practically the same as getAllChestsInPieceInChunk, but here we just skip everything, so it's faster
    public static void skipCallsInPieceInChunk(MineshaftCorridor c, BlockBox chunk)
    {
        int m = c.length * 5;
        LCG skipCobwebs = LCG.JAVA.combine(m * 3 * 2);
        LCG skipCeiling = LCG.JAVA.combine(m * 3);

        rand.advance(skipCeiling);
        
        //   if spawner corridor, skip cobwebs as well
        if (c.hasCobwebs)
        	rand.advance(skipCobwebs);
        
        CoordinateTransformer.setParams(c.facing, c.boundingBox);
        BPos chest;
        int center;
        boolean spiderSpawnerPlaced = false;
        
        for (int j=0; j<c.length; j++) {
        	center = j*5 + 2;
        	
        	if (CoordinateTransformer.isSupportingBox(center, chunk)) {
	            if(rand.nextInt(4) != 0) {
	                rand.advance(skipTorches);
	            }
        	}

            for (int i=0; i<8; i++) {
            	if ( chunk.contains( CoordinateTransformer.getWorldPos((i%2)*2, 2, center+cobwebPlacement[i]))) 
            		rand.advance(skipSingleCall);
            }
            
            if(rand.nextInt(100) == 0) {
            	chest = CoordinateTransformer.getWorldPos(2, 0, center-1);
            	if ( chunk.contains(chest) )
            		rand.advance(skipChest);
            }
            
            if(rand.nextInt(100) == 0) {
            	chest = CoordinateTransformer.getWorldPos(0, 0, center+1);
            	if ( chunk.contains(chest) )
            		rand.advance(skipChest);
            }
            
            if (c.hasCobwebs && !spiderSpawnerPlaced)
            	rand.advance(skipSingleCall);
        }
        
        if (c.hasRails) {
        	for (int j=0; j<m; j++)
        		if ( chunk.contains(CoordinateTransformer.getWorldPos(1, 0, j)) )
        			rand.advance(skipSingleCall);
        }
    }
    
    // WARNING! this is extremely inaccurate in oceans, would recommend to ignore the output if biome is ocean
    // returns chest positions and loot seeds within the desired corridor piece (for use with the LootContext object)
    public static ArrayList<Pair<BPos, Long>> getAllChestsInCorridor(MineshaftCorridor c, long worldSeed, MCVersion version)
    {
    	HashSet<CPos> pieceChunks = new HashSet<>();
    	ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();
    	
    	// getting all chunks that contain parts of the piece:
    	// getting corners
    	pieceChunks.add( new CPos(c.boundingBox.minX >> 4, c.boundingBox.minZ >> 4) );
    	pieceChunks.add( new CPos(c.boundingBox.minX >> 4, c.boundingBox.maxZ >> 4) );
    	pieceChunks.add( new CPos(c.boundingBox.maxX >> 4, c.boundingBox.minZ >> 4) );
    	pieceChunks.add( new CPos(c.boundingBox.maxX >> 4, c.boundingBox.maxZ >> 4) );
    	
    	// getting edge midpoints
    	pieceChunks.add( new CPos(c.boundingBox.minX >> 4, (c.boundingBox.minZ + c.boundingBox.maxZ) >> 5) );
    	pieceChunks.add( new CPos(c.boundingBox.maxX >> 4, (c.boundingBox.minZ + c.boundingBox.maxZ) >> 5) );
    	pieceChunks.add( new CPos((c.boundingBox.minX + c.boundingBox.maxX) >> 5, c.boundingBox.minZ >> 4) );
    	pieceChunks.add( new CPos((c.boundingBox.minX + c.boundingBox.maxX) >> 5, c.boundingBox.maxZ >> 4) );
    	
    	// we need to process every chunk that contains our piece, in any order
    	for (CPos chunkPos : pieceChunks) {
    		rand.setDecoratorSeed(worldSeed, chunkPos.getX() << 4, chunkPos.getZ() << 4, 0, 3, version);
	    	BlockBox chunk = new BlockBox(chunkPos.getX() << 4, chunkPos.getZ() << 4, (chunkPos.getX() << 4)+15, (chunkPos.getZ() << 4)+15);
    		
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
    public static ArrayList<Pair<BPos, Long>> getAllChestsInChunk(CPos chunkPos, long worldSeed, MCVersion version) 
    {
    	rand.setDecoratorSeed(worldSeed, chunkPos.getX() << 4, chunkPos.getZ() << 4, 0, 3, version);
    	BlockBox chunk = new BlockBox(chunkPos.getX() << 4, chunkPos.getZ() << 4, (chunkPos.getX() << 4)+15, (chunkPos.getZ() << 4)+15);
    	ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();
        
    	for (MineshaftCorridor piece : corridors) {
    		if (piece.boundingBox.intersects(chunk)) {
    	//		System.out.println("Piece processed: " + " rails: " + piece.hasRails + " cobwebs: " + piece.hasCobwebs + "   " + piece.bb);
    			chests.addAll( getChestsInPieceInChunk(piece, chunk) );
    		}
    	}
    	
    	return chests;
    };
    
    // wrapper for MineshaftGenerator.generateForChunk
    public static boolean generateMineshaft(CPos chunkPos, long structureSeed, boolean mesa) {
    	mineshaftPieces.clear();
    	return MineshaftGenerator.generateForChunk(structureSeed, chunkPos.getX(), chunkPos.getZ(), mesa, mineshaftPieces);
    }
    
    // getters for the corridor and piece lists
    public static List<MineshaftCorridor> getCorridors() {
    	return corridors;
    }
    
    public static List<StructurePiece> getPieces() {
    	return mineshaftPieces;
    }
}