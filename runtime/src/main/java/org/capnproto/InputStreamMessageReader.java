package org.capnproto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import static org.capnproto.Constants.WORD_SIZE;

public final class InputStreamMessageReader {
    private static final int SEGMENT_LIMIT = 512;

    static byte[] readExact(InputStream is, int length) throws IOException {
        byte[] bytes = new byte[length];

        int bytesRead = 0;
        while (bytesRead < length) {
            int r = is.read(bytes, bytesRead, length - bytesRead);
            if (r < 0) {
                throw new IOException("premature EOF");
            }
            bytesRead += r;
        }

        return bytes;
    }

    static ByteBuffer makeByteBuffer(byte[] bytes) {
        ByteBuffer result = ByteBuffer.wrap(bytes);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.mark();
        return result;
    }

    public static MessageReader create(InputStream is) throws IOException {
        ByteBuffer headerWord = makeByteBuffer(readExact(is, WORD_SIZE));

        int segmentCount = readTotalSegments(headerWord);

        int firstSegmentSize = 0;
        if (segmentCount > 0) {
            firstSegmentSize = headerWord.getInt();
        }

        int totalWords = firstSegmentSize;

        // in words
        Vector<Integer> moreSizes = new Vector<Integer>();

        if (segmentCount > 1) {
            ByteBuffer moreSizesRaw = makeByteBuffer(readExact(is, 4 * (segmentCount & ~1)));
            for (int ii = 0; ii < segmentCount - 1; ++ii) {
                int size = moreSizesRaw.getInt(ii * 4);
                moreSizes.add(size);
                totalWords += size;
            }
        }

        // TODO check that totalWords is reasonable

        byte[] allSegments = readExact(is, totalWords * WORD_SIZE);

        ByteBuffer[] segmentSlices = new ByteBuffer[segmentCount];

        segmentSlices[0] = ByteBuffer.wrap(allSegments, 0, firstSegmentSize * WORD_SIZE);
        segmentSlices[0].order(ByteOrder.LITTLE_ENDIAN);
        segmentSlices[0].mark();

        int offset = firstSegmentSize;

        for (int ii = 1; ii < segmentCount; ++ii) {
            segmentSlices[ii] = ByteBuffer.wrap(allSegments, offset * 8, moreSizes.get(ii - 1) * 8);
            segmentSlices[ii].order(ByteOrder.LITTLE_ENDIAN);
            segmentSlices[ii].mark();
            offset += moreSizes.get(ii - 1);
        }

        return new MessageReader(segmentSlices);
    }

    private static int readTotalSegments(ByteBuffer headerWord) {
        int segmentCount = 1 + headerWord.getInt();

        if (segmentCount > SEGMENT_LIMIT) {
            throw new RuntimeException(
                String.format("%s segments is greater than segment limit: %s",
                    segmentCount,
                    SEGMENT_LIMIT)
            );
        }

        return segmentCount;
    }

    private static int readFirstSegmentLength(ByteBuffer headerWord) {
        return headerWord.getInt();
    }
}
