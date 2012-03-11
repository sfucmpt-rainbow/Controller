package rainbow.controller.workBlock;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Collection;

public class WorkBlockPartition {
	private static final int ALL_BLOCKS = -1;

	private int jobId;
	private String startString;
	private String target;
	private LinkedList<WorkBlock> blocks;
	private TreeMap<Integer, WorkBlock> working;

	public WorkBlockPartition(
		int jobId, 
		String startString, 
		String alphabet, 
		String target, 
		int blockLength
	) {
		this(jobId, startString, alphabet, target, blockLength, ALL_BLOCKS);
	}

	public WorkBlockPartition(
		int jobId, 
		String startString, 
		String alphabet, 
		String target, 
		int blockLength, 
		int blockCount
	) {
		this.jobId = jobId;
		this.startString = startString;
		this.target = target;
		this.blocks = blockCount == ALL_BLOCKS? 
			generateAllWorkBlocks(startString, alphabet, blockLength) :
			generateWorkBlocks(startString, alphabet, blockLength, blockCount);
		this.working = new TreeMap<Integer, WorkBlock>(); 
	}

	///////////////////////////////////////////////////////////////////////////////////
	// Constructors assisting generating helper methods
	//
	private LinkedList<WorkBlock> generateWorkBlocks(
		String startString, 
		String alphabet,
		int blockLength, 
		int blockCount
	) {
		int blockId = 0;
		LinkedList<WorkBlock> workBlocks = new LinkedList<WorkBlock>();
		workBlocks.add(new WorkBlock(blockId++, startString));
		long stringIndex = convertStringToIndex(startString, alphabet);
		// skips the first one which we already have
		for (int i = 1; i < blockCount; i++) {
			stringIndex += blockLength;
			String blockString = indexToString(stringIndex, startString.length(), alphabet);
			workBlocks.add(new WorkBlock(blockId++, blockString));
		}
		return workBlocks;
	}

	private LinkedList<WorkBlock> generateAllWorkBlocks(
		String startString, 
		String alphabet,
		int blockLength
	) {
		long startIndex = convertStringToIndex(startString, alphabet);
		long endIndex = (long)Math.pow(alphabet.length(), startString.length());
		int blockCount = (int)(endIndex - startIndex) / blockLength;
		return generateWorkBlocks(startString, alphabet, blockLength, blockCount);
	}

	////////////////////////////////////////////////////////////////////////////////////
	// String to index and vice versa arithmetic methods
	//
	private long convertStringToIndex(String str, String alphabet) {
		int radix = alphabet.length();
		long index = 0;
		for (int i = 0; i < str.length(); i++) {
			index *= radix;
			index += alphabet.indexOf(str.charAt(i));
		}
		return index;
	}

	private String indexToString(long stringIndex, int stringLength, String alphabet) {
		StringBuilder builder = new StringBuilder(stringLength);
		long radix = alphabet.length();
		for (int i = 0; i < stringLength; i++) {
			int charIndex = (int)(stringIndex % radix);
			stringIndex /= radix;
			builder.append(alphabet.charAt(charIndex));
		}
		builder.reverse();
		return builder.toString();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// WorkBlockParition interaction methods
	//
	public LinkedList<WorkBlock> getBlocks() {
		return blocks;
	}

	public Collection<WorkBlock> getWorkingBlocks() {
		return working.values();
	}

	public boolean isComplete() {
		return blocks.isEmpty() && working.isEmpty();
	}

	public WorkBlock getNextBlock() {
		if (!blocks.isEmpty()) {
			WorkBlock block = blocks.removeFirst();
			working.put(block.getId(), block);
			return block;
		}
		return null;
	}

	public boolean repushBlock(int jobId) {
		WorkBlock block = working.remove(jobId);
		if (block != null) {
			blocks.add(block);
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		WorkBlockPartition test = new WorkBlockPartition(0, "c", "abcde", "aa", 1);
		// expected output is c, d, e
		for (WorkBlock block : test.getBlocks()) {
			System.out.println(block);
		}

		test = new WorkBlockPartition(1, "aaa", "ab", "hash", 1);
		for (WorkBlock block : test.getBlocks()) {
			System.out.println(block);
		}
		System.out.println("Taking blocks 0, 1, 2");
		test.getNextBlock();
		test.getNextBlock();
		test.getNextBlock();
		System.out.println("Readding block 0");
		test.repushBlock(0);
		System.out.println("====== Available Blocks =======");
		for (WorkBlock block : test.getBlocks()) {
			System.out.println(block);
		}
		System.out.println("====== Working Blocks ======");
		for (WorkBlock block : test.getWorkingBlocks()) {
			System.out.println(block);
		}
	}
}







