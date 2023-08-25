package kludwisz.mineshafts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.MCLootTables;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.mcseed.lcg.LCG;

public class MineshaftLoot 
{
	private static final int[] cobwebPlacement = {-1, -1, 1, 1, -2, -2, 2, 2};
	private static final LCG skipChest = LCG.JAVA.combine(3);
	private static final LCG skipTorches = LCG.JAVA.combine(2);
    private static final LCG skipSingleCall = LCG.JAVA.combine(1);
    private static final ChunkRand rand = new ChunkRand();
    private static ArrayList<StructurePiece> mineshaftPieces = new ArrayList<>();
    private static ArrayList<Corridor> corridors = new ArrayList<>();

    // needs to be done in the same order as piece generation !
    // returns chest positions and loot seeds (for use with the LootContext object)
    public static ArrayList<Pair<BPos, Long>> getChestsInPieceInChunk(Corridor c, BlockBox chunk)
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
        
        CoordinateTransformer.setParams(c.direction, c.bb);
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
    public static void skipCallsInPieceInChunk(Corridor c, BlockBox chunk) 
    {
        int m = c.length * 5;
        LCG skipCobwebs = LCG.JAVA.combine(m * 3 * 2);
        LCG skipCeiling = LCG.JAVA.combine(m * 3);

        rand.advance(skipCeiling);
        
        //   if spawner corridor, skip cobwebs as well
        if (c.hasCobwebs)
        	rand.advance(skipCobwebs);
        
        CoordinateTransformer.setParams(c.direction, c.bb);
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
            		rand.advance(skipSingleCall);;
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
    public static ArrayList<Pair<BPos, Long>> getAllChestsInCorridor(Corridor c, long worldSeed, MCVersion version) 
    {
    	Set<CPos> pieceChunks = new HashSet<>();
    	ArrayList <Pair<BPos, Long>> chests = new ArrayList<>();
    	
    	// getting all chunks that contain parts of the piece:
    	// getting corners
    	pieceChunks.add( new CPos(c.bb.minX >> 4, c.bb.minZ >> 4) );
    	pieceChunks.add( new CPos(c.bb.minX >> 4, c.bb.maxZ >> 4) );
    	pieceChunks.add( new CPos(c.bb.maxX >> 4, c.bb.minZ >> 4) );
    	pieceChunks.add( new CPos(c.bb.maxX >> 4, c.bb.maxZ >> 4) );
    	
    	// getting edge midpoints
    	pieceChunks.add( new CPos(c.bb.minX >> 4, (c.bb.minZ+c.bb.maxZ) >> 5) );
    	pieceChunks.add( new CPos(c.bb.maxX >> 4, (c.bb.minZ+c.bb.maxZ) >> 5) );
    	pieceChunks.add( new CPos((c.bb.minX+c.bb.maxX) >> 5, c.bb.minZ >> 4) );
    	pieceChunks.add( new CPos((c.bb.minX+c.bb.maxX) >> 5, c.bb.maxZ >> 4) );
    	
    	// we need to process every chunk that contains our piece, in any order
    	for (CPos chunkPos : pieceChunks) {
    		rand.setDecoratorSeed(worldSeed, chunkPos.getX() << 4, chunkPos.getZ() << 4, 0, 3, version);
	    	BlockBox chunk = new BlockBox(chunkPos.getX() << 4, chunkPos.getZ() << 4, (chunkPos.getX() << 4)+15, (chunkPos.getZ() << 4)+15);
    		
	    	for (Corridor piece : corridors) {
	    		if (piece.bb.contains(c.bb.getCenter()) && piece.bb.intersects(chunk)) {
	    			chests.addAll( getChestsInPieceInChunk(piece, chunk) );
	    			break; // after proccessing the target piece part, we can move on
	    		}
        		if (piece.bb.intersects(chunk))
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
        
    	for (Corridor piece : corridors) {
    		if (piece.bb.intersects(chunk)) {
    	//		System.out.println("Piece processed: " + " rails: " + piece.hasRails + " cobwebs: " + piece.hasCobwebs + "   " + piece.bb);
    			chests.addAll( getChestsInPieceInChunk(piece, chunk) );
    		}
    	}
    	
    	return chests;
    };
    
    // wrapper for MineshaftGenerator.generateForChunk
    public static boolean generateMineshaft(CPos chunkPos, long structureSeed, boolean mesa) {
    	mineshaftPieces.clear();
    	corridors.clear();
    	
    	mineshaftPieces = MineshaftGenerator.generateForChunk(structureSeed, chunkPos.getX(), chunkPos.getZ(), mesa, corridors);
    	if (mineshaftPieces == null)
    		return false;
    	return true;
    }
    
    // getters for the corridor and piece lists
    public static List<Corridor> getCorridors() {
    	return corridors;
    }
    
    public static List<StructurePiece> getPieces() {
    	return mineshaftPieces;
    }
    
    
    // simple example of use
    public static void main() {
    	ArrayList<Pair<BPos, Long>> loot = new ArrayList<>();
        for (long seed=(1L<<48); seed<(1L<<62); seed++)
        {
        	if (!generateMineshaft(new CPos(0,0), seed & Mth.MASK_48, false)) continue;
        	
        	loot.clear();
        	if (corridors.size()<4)
        		continue;
        	
        //	if (corridors.size() <5 )
        //		System.out.println(seed + " has tiny mineshaft");
        	loot = getAllChestsInCorridor(corridors.get(3), seed, MCVersion.v1_16_1);
        //	loot = getAllChestsInChunk(new CPos(0,-1), seed);
        	if (loot.size() <1) continue;
        	
        	LootContext ctx;
        	List<ItemStack> items;
        	for (Pair<BPos, Long> p : loot) {
        		ctx = new LootContext(p.getSecond(), MCVersion.v1_16_1);
        		items = MCLootTables.ABANDONED_MINESHAFT_CHEST.get().generate(ctx);
        		System.out.println(seed + " /tp " + p.getFirst().getX() + " "+ p.getFirst().getY() + " "+ p.getFirst().getZ() + "     LOOT: " + items);
        	}	
        }
    }
    
}