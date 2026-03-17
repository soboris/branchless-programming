import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class Main {
    private static final long DELIMITER = 0x3B3B3B3B3B3B3B3BL;

    private static Unsafe initUnsafe() throws IllegalAccessException, NoSuchFieldException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    private static int positionOfDelimiter(long entry, long delimiter) {
        int index;

        final long match = entry ^ delimiter;
        long mask = match - 0x0101010101010101L;
        mask &= ~match;
        mask &= 0x8080808080808080L;
        index = Long.numberOfTrailingZeros(mask) >>> 3;
        index = index == 8 ? -1 : index;

        return index;
    }

    private static int parseSignedInteger(long value, int length) {
        int result = 0;

        final int negative;
        final int[] digits = new int[8];
        final int[] final_digits = { 0, 0, 0, 0, 0, 0, 0, 0 };

        digits[0] = (int) (value & 0xFFL);
        digits[1] = (int) ((value >>> 8) & 0xFFL);
        digits[2] = (int) ((value >>> 16) & 0xFFL);
        digits[3] = (int) ((value >>> 24) & 0xFFL);
        digits[4] = (int) ((value >>> 32) & 0xFFL);
        digits[5] = (int) ((value >>> 40) & 0xFFL);
        digits[6] = (int) ((value >>> 48) & 0xFFL);
        digits[7] = (int) ((value >>> 56) & 0xFFL);

        negative = ~(digits[0] >>> 4) & 1;
        digits[0] &= negative - 1;

        final_digits[length - (~(length - 1) >>> 31) * 1] = digits[0] - (digits[0] >>> 5) * 0x30;
        final_digits[length - (~(length - 2) >>> 31) * 2] = digits[1] - (digits[1] >>> 5) * 0x30;
        final_digits[length - (~(length - 3) >>> 31) * 3] = digits[2] - (digits[2] >>> 5) * 0x30;
        final_digits[length - (~(length - 4) >>> 31) * 4] = digits[3] - (digits[3] >>> 5) * 0x30;
        final_digits[length - (~(length - 5) >>> 31) * 5] = digits[4] - (digits[4] >>> 5) * 0x30;
        final_digits[length - (~(length - 6) >>> 31) * 6] = digits[5] - (digits[5] >>> 5) * 0x30;
        final_digits[length - (~(length - 7) >>> 31) * 7] = digits[6] - (digits[6] >>> 5) * 0x30;
        final_digits[length - (~(length - 8) >>> 31) * 8] = digits[7] - (digits[7] >>> 5) * 0x30;

        result = -negative ^ ((final_digits[7] * 10000000 + final_digits[6] * 1000000 + final_digits[5] * 100000
                + final_digits[4] * 10000 + final_digits[3] * 1000 + final_digits[2] * 100 + final_digits[1] * 10
                + final_digits[0]) - negative);

        return result;
    }

    private static double parseSignedDecimal(long value, int length) {
        double result = 0;

        final int dot_pos_from_left;
        final int dot_pos_from_right;
        final int negative;
        final int[] digits = new int[8];
        final int[] final_digits = { 0, 0, 0, 0, 0, 0, 0, 0 };
        final int[] POWERS_OF_10 = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000 };

        dot_pos_from_left = positionOfDelimiter(value, 0x2E2E2E2E2E2E2E2EL);
        dot_pos_from_right = length - dot_pos_from_left - 1;

        digits[0] = (int) (value & 0xFFL);
        digits[1] = (int) ((value >>> 8) & 0xFFL);
        digits[2] = (int) ((value >>> 16) & 0xFFL);
        digits[3] = (int) ((value >>> 24) & 0xFFL);
        digits[4] = (int) ((value >>> 32) & 0xFFL);
        digits[5] = (int) ((value >>> 40) & 0xFFL);
        digits[6] = (int) ((value >>> 48) & 0xFFL);
        digits[7] = (int) ((value >>> 56) & 0xFFL);

        negative = ~(digits[0] >>> 4) & 1;
        digits[0] &= negative - 1;

        final_digits[length - (~(length - 1) >>> 31) * 1] = digits[0] - (digits[0] >>> 5) * 0x30;
        final_digits[length - (~(length - 2) >>> 31) * 2] = digits[1] - (digits[1] >>> 5) * 0x30;
        final_digits[length - (~(length - 3) >>> 31) * 3] = digits[2] - (digits[2] >>> 5) * 0x30;
        final_digits[length - (~(length - 4) >>> 31) * 4] = digits[3] - (digits[3] >>> 5) * 0x30;
        final_digits[length - (~(length - 5) >>> 31) * 5] = digits[4] - (digits[4] >>> 5) * 0x30;
        final_digits[length - (~(length - 6) >>> 31) * 6] = digits[5] - (digits[5] >>> 5) * 0x30;
        final_digits[length - (~(length - 7) >>> 31) * 7] = digits[6] - (digits[6] >>> 5) * 0x30;
        final_digits[length - (~(length - 8) >>> 31) * 8] = digits[7] - (digits[7] >>> 5) * 0x30;

        final_digits[0] = final_digits[0 + ((dot_pos_from_right - 1 - 0) >>> 31) - ((length - 1 - 0) >>> 31)];
        final_digits[1] = final_digits[1 + ((dot_pos_from_right - 1 - 1) >>> 31) - ((length - 1 - 1) >>> 31)];
        final_digits[2] = final_digits[2 + ((dot_pos_from_right - 1 - 2) >>> 31) - ((length - 1 - 2) >>> 31)];
        final_digits[3] = final_digits[3 + ((dot_pos_from_right - 1 - 3) >>> 31) - ((length - 1 - 3) >>> 31)];
        final_digits[4] = final_digits[4 + ((dot_pos_from_right - 1 - 4) >>> 31) - ((length - 1 - 4) >>> 31)];
        final_digits[5] = final_digits[5 + ((dot_pos_from_right - 1 - 5) >>> 31) - ((length - 1 - 5) >>> 31)];
        final_digits[6] = final_digits[6 + ((dot_pos_from_right - 1 - 6) >>> 31) - ((length - 1 - 6) >>> 31)];
        final_digits[7] = final_digits[7 + ((dot_pos_from_right - 1 - 7) >>> 31) - ((length - 1 - 7) >>> 31)];

        result = -negative ^ ((final_digits[7] * 10000000 + final_digits[6] * 1000000 + final_digits[5] * 100000
                + final_digits[4] * 10000 + final_digits[3] * 1000 + final_digits[2] * 100 + final_digits[1] * 10
                + final_digits[0]) - negative);

        result /= POWERS_OF_10[dot_pos_from_right * (~dot_pos_from_left >>> 31)];

        return result;
    }

    private static byte[] longToBytes(long x) {
        byte[] result = new byte[8];

        result[0] = (byte) (x & 0xFF);
        result[1] = (byte) ((x >> 8) & 0xFF);
        result[2] = (byte) ((x >> 16) & 0xFF);
        result[3] = (byte) ((x >> 24) & 0xFF);
        result[4] = (byte) ((x >> 32) & 0xFF);
        result[5] = (byte) ((x >> 40) & 0xFF);
        result[6] = (byte) ((x >> 48) & 0xFF);
        result[7] = (byte) ((x >> 56) & 0xFF);

        return result;
    }

    public static void testInteger() {
        final String rec = "ABC;-123"; // Process 8 bytes at a time

        try {
            final Unsafe unsafe = initUnsafe();
            final byte[] raw = rec.getBytes();
            final long BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
            final long entry = unsafe.getLong(raw, BYTE_ARRAY_BASE_OFFSET);

            final int index = positionOfDelimiter(entry, DELIMITER);
            final long mask = index == -1 ? 0xFFFFFFFFFFFFFFFFL : (1L << (index * 8)) - 1;
            final long part1 = entry & mask;
            final long part2 = ((entry & ~mask) >>> (index * 8)) >>> 8;
            final byte[] part1_raw = part1 != 0 ? longToBytes(part1) : longToBytes(0x5FL);

            System.out.println("Parsing integer record");
            System.out.println("Source string: " + rec);
            System.out.printf("Delimiter at position: %d\n", index);
            System.out.printf("Part1: %x\n", part1);
            System.out.printf("Part2: %x\n", part2);
            System.out.printf("The number for %s is: %d\n", new String(part1_raw),
                    parseSignedInteger(part2, 8 - (Long.numberOfLeadingZeros(part2) >>> 3)));
            System.out.printf("The number in decimal for %s is: %f\n", new String(part1_raw),
                    parseSignedDecimal(part2, 8 - (Long.numberOfLeadingZeros(part2) >>> 3)));
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Do not do this in production code
        } catch (NoSuchFieldException e) {
            e.printStackTrace(); // Do not do this in production code
        }
    }

    public static void testDecimal() {
        final String rec = "A;-1.234"; // Process 8 bytes at a time

        try {
            final Unsafe unsafe = initUnsafe();
            final byte[] raw = rec.getBytes();
            final long BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
            final long entry = unsafe.getLong(raw, BYTE_ARRAY_BASE_OFFSET);

            final int index = positionOfDelimiter(entry, DELIMITER);
            final long mask = index == -1 ? 0xFFFFFFFFFFFFFFFFL : (1L << (index * 8)) - 1;
            final long part1 = entry & mask;
            final long part2 = ((entry & ~mask) >>> (index * 8)) >>> 8;
            final byte[] part1_raw = part1 != 0 ? longToBytes(part1) : longToBytes(0x5FL);

            System.out.println("Parsing decimal record");
            System.out.println("Source string: " + rec);
            System.out.printf("Delimiter at position: %d\n", index);
            System.out.printf("Part1: %x\n", part1);
            System.out.printf("Part2: %x\n", part2);
            System.out.printf("The number in decimal for %s is: %f\n", new String(part1_raw),
                    parseSignedDecimal(part2, 8 - (Long.numberOfLeadingZeros(part2) >>> 3)));
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Do not do this in production code
        } catch (NoSuchFieldException e) {
            e.printStackTrace(); // Do not do this in production code
        }
    }

    public static void main(String[] args) {
        testInteger();
        System.out.println("\n");
        testDecimal();
    }
}
