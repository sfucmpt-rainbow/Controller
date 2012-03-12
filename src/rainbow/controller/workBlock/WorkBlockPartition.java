package rainbow.controller.workBlock;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Collection;

public class WorkBlockPartition {
	private int jobId;
	private int partitionId;
	private int size;
	private int stringLength;
	private long startBlockNumber;
	private long endBlockNumber;
	private String target;
	private LinkedList<WorkBlock> blocks;
	private TreeMap<Integer, WorkBlock> working;

	public WorkBlockPartition(
		int jobId, 
		int partitionId,
		long startBlockNumber,
		long endBlockNumber,
		String alphabet, 
		String target, 
		int stringLength,
		int blockLength
	) {
		this.jobId = jobId;
		this.partitionId = partitionId;
		this.startBlockNumber = startBlockNumber;
		this.endBlockNumber = endBlockNumber;
		this.target = target;
		this.working = new TreeMap<Integer, WorkBlock>(); 
		this.blocks = generateWorkBlocks(startBlockNumber, endBlockNumber, blockLength);
		this.size = (int)(endBlockNumber - startBlockNumber);
		this.stringLength = stringLength;
	}

	///////////////////////////////////////////////////////////////////////////////////
	// Constructors assisting generating helper methods
	//
	private LinkedList<WorkBlock> generateWorkBlocks(long startBlockNumber, long endBlockNumber, int blockLength) {
		int blockId = 0;
		LinkedList<WorkBlock> workBlocks = new LinkedList<WorkBlock>();
		for (long i = startBlockNumber; i < endBlockNumber; i++) {
			workBlocks.add(new WorkBlock(blockId++, i, i + blockLength, this));
		}
		return workBlocks;
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
			System.out.println(jobId + " requeue!");
			return true;
		}
		return false;
	}
	
	public int getPartitionId() {
		return partitionId;
	}

	public int getJobId() {
		return jobId;
	}

	public int getStringLength() {
		return stringLength;
	}

	public long getStartBlockNumber() {
		return startBlockNumber;
	}

	public long getEndBlockNumber() {
		return endBlockNumber;
	}
	
	public int getSize() {
		return size;
	}

	public int getCurrentSize() {
		return getWaitingSize() + getWorkingSize();
	}

	public int getWaitingSize() {
		return blocks.size();
	}

	public int getWorkingSize() {
		return working.size();
	}

	public boolean isDone() {
		return getCurrentSize() == 0;
	}

	public boolean hasUnassignedWork() {
		return getWaitingSize() > 0;
	}

	public WorkBlock markBlockComplete(int blockId) {
		return working.remove(blockId);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// Main test method
	//
	public static void main(String[] args) {
		WorkBlockPartition test = new WorkBlockPartition(0, 0, 0, 1, "abcde", "aa", 2, 1);
		// expected output is c, d, e
		for (WorkBlock block : test.getBlocks()) {
			System.out.println(block);
		}

		test = new WorkBlockPartition(1, 1, 0, 999, "abcde", "hash", 10, 1);
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







