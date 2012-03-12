package rainbow.controller.workBlock;

// This is trivial at this point, but we may want to extend this in the
// future.
public class WorkBlock implements Comparable<WorkBlock> {
	private int id;
	private WorkBlockPartition partition;
	private long startIndex;
	private long endBlock;

	public WorkBlock(int id, long startIndex, long endBlock, WorkBlockPartition partition) {
		this.id = id;
		this.partition = partition;
		this.startIndex = startIndex;
		this.endBlock = endBlock;
	}
	
	public long getStartBlockNumber() {
		return startIndex;
	}

	public long getEndBlockNumber() {
		return endBlock;
	}

	public String toString() {
		return Integer.toString(id) + ": " + getStartBlockNumber();
	}

	public int getId() {
		return id;
	}

	public WorkBlockPartition getPartition() {
		return partition;
	}

	public int getPartitionId() {
		return partition.getPartitionId();
	}

	public int getStringLength() {
		return partition.getStringLength();
	}

	public int compareTo(WorkBlock block) {
		return Integer.compare(id, block.id);
	}

	public boolean equals(WorkBlock block) {
		return id == block.id && 
			startIndex == block.startIndex && 
			endBlock == block.endBlock;
	}
}
