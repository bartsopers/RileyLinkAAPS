package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public class NonceState {
    private long[] table;
    private int index;

    public NonceState(int lot, int tid) {
        initializeTable(lot, tid, (byte)0);
    }

    public NonceState(int lot, int tid, byte seed) {
        initializeTable(lot, tid, seed);
    }

    private void initializeTable(int lot, int tid, byte seed) {
        table = new long[21];
        table[0] = (long)(lot & 0xFFFF) + 0x55543DC3l + (((long)(lot) & 0xFFFFFFFFl) >> 16);
        table[0] = table[0] & 0xFFFFFFFFl;
        table[1] = (tid & 0xFFFF) + 0xAAAAE44El + (((long)(tid) & 0xFFFFFFFFl) >> 16);
        table[1] = table[1] & 0xFFFFFFFFl;
        index = 0;
        table[0] += seed;
        for(int i = 0; i < 16; i++) {
            table[2+i] = generateEntry();
        }
        index = (int) ((table[0] + table[1]) & 0X0F);
    }

    private int generateEntry() {
        table[0] = (((table[0] >> 16) + (table[0] & 0xFFFF) * 0x5D7Fl) & 0xFFFFFFFFl);
        table[1] = (((table[1] >> 16) + (table[1] & 0xFFFF) * 0x8CA0l) & 0xFFFFFFFFl);
        return (int)(((long)(table[1]) + ((long)(table[0]) << 16)) & 0xFFFFFFFFl);
    }

    public int getCurrentNonce() {
        return (int) table[(2 + index)];
    }

    public void advanceToNextNonce() {
        int nonce = getCurrentNonce();
        table[(2 + index)] = generateEntry();
        index = (nonce & 0x0F);
    }
}
