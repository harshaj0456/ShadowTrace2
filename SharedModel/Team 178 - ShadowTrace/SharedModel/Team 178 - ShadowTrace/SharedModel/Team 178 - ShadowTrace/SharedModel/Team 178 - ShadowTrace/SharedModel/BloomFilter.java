package SharedModel;

/*
 * BloomFilter provides a fast probabilistic way
 * to check whether a node may already have been scheduled.
 *
 * Used by RemovalEngine to avoid duplicate cleanup scheduling.
 */
public class BloomFilter {

    private boolean[] bitArray;
    private int filterSize;

    public BloomFilter(int filterSize) {
        if (filterSize <= 0) {
            filterSize = 1000;
        }

        this.filterSize = filterSize;
        this.bitArray = new boolean[filterSize];
    }

    /*
     * First hash function.
     */
    private int hash1(int nodeId) {
        return (int) Math.abs((nodeId * 2654435761L) % filterSize);
    }

    /*
     * Second hash function.
     */
    private int hash2(int nodeId) {
        long hashValue = (nodeId * 2246822519L) % filterSize;
        return (int) Math.abs(hashValue != 0 ? hashValue : nodeId % filterSize);
    }

    /*
     * Marks a node as added in the Bloom filter.
     */
    public void add(int nodeId) {
        bitArray[hash1(nodeId)] = true;
        bitArray[hash2(nodeId)] = true;
    }

    /*
     * Returns true if the node might already be present.
     * Returns false if it is definitely not present.
     */
    public boolean mightBeFlagged(int nodeId) {
        return bitArray[hash1(nodeId)] && bitArray[hash2(nodeId)];
    }
}