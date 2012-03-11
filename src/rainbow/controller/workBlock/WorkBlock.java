package rainbow.controller.workBlock;

// This is trivial at this point, but we may want to extend this in the
// future.
public class WorkBlock implements Comparable<WorkBlock> {
	private String startString;
	private int id;

	public WorkBlock(int id, String startString) {
		this.startString = startString;
		this.id = id;
	}
	
	public String getStartString() {
		return startString;
	}

	public String toString() {
		return Integer.toString(id) + ": " + getStartString();
	}

	public int getId() {
		return id;
	}

	public int compareTo(WorkBlock block) {
		return Integer.compare(id, block.id);
	}

	public boolean equals(WorkBlock block) {
		return id == block.id && startString == block.startString;
	}
}
