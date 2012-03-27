/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.controller.application;

import java.math.BigInteger;
import rainbow.scheduler.partition.AlphabetGenerator;
import rainbow.scheduler.partition.PlaintextSpace;
import rainbowpc.controller.messages.NewQuery;
import rainbowpc.controller.messages.WorkBlockSetup;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 *
 * @author WesleyLuk
 */
public class BruteForcer extends Thread {

	public static final int MD5LENGTH = 128 / 8;
	NewQuery query;
	WorkBlockSetup work;
	PlaintextSpace space;
	MessageDigest md;
	byte[] targetQuery;
	BruteForceEventListener listener;

	public BruteForcer(NewQuery query, WorkBlockSetup work, BruteForceEventListener listener) {
		this.query = query;
		this.work = work;
		this.listener = listener;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		convertQuery();
	}

	private void convertQuery() {
		//System.out.println("Converting " + query.getQuery());
		BigInteger bigint = new BigInteger(query.getQuery(), 16);
		byte[] value = bigint.toByteArray();
		if (value.length != MD5LENGTH) {
			byte[] temp = value;
			value = new byte[MD5LENGTH];
			for (int i = 1; i <= MD5LENGTH; i++) {
				if (i >= temp.length) {
					value[MD5LENGTH - i] = 0;
				} else {
					value[MD5LENGTH - i] = temp[temp.length - i];
				}
			}
		}
		//System.out.println("Done conversion value is");
		/*
		for (int i = 0; i < MD5LENGTH; i++) {
			System.out.print(Integer.toString(value[i] & 0xff, 16));
		}
		System.out.println();
		*/
		targetQuery = value;
	}

	public void run() {
		String alphabet = AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE);
		for (long blockNumber = work.getStartBlockNumber(); blockNumber < work.getEndBlockNumber(); blockNumber++) {
			space = new PlaintextSpace(alphabet, blockNumber, work.getStringLength());
			for (int blockIndex = 0; blockIndex < space.BLOCK_SIZE; blockIndex++) {
				String text = space.getText(blockIndex);
				// System.out.println(text);
				if (text == null) {
					break;
				}
				md.reset();
				md.update(text.getBytes());
				if (check(md.digest())) {
					System.out.println("Found match " + text);
					listener.matchFound(text);
				}
				if(interrupted()){
					System.out.println("Work thread interrupted");
					return;
				}
			}
			System.out.println("Completed block " + blockNumber + " String length " + work.getStringLength());
		}
		listener.workBlockComplete(work);
		System.out.println("Completed workblock");
	}

	public boolean check(byte[] hash) {
		for (int i = 0; i < MD5LENGTH; i++) {
			if (hash[i] != targetQuery[i]) {
				return false;
			}
		}
		return true;
	}
}
