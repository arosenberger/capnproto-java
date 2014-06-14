package org.capnproto;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Serialize {
    public static void writeMessage(GatheringByteChannel outputChannel,
                                    MessageBuilder message) throws IOException {
        ByteBuffer[] segments = message.getSegmentsForOutput();
        int tableSize = (segments.length + 2) & (~1);

        ByteBuffer table = ByteBuffer.allocate(4 * tableSize);
        table.order(ByteOrder.LITTLE_ENDIAN);

        table.putInt(0, segments.length - 1);

        for (int i = 0; i < segments.length; ++i) {
            table.putInt(4 * (i + 1), segments[i].limit() / 8);
        }

        // Any padding is already zeroed.

        outputChannel.write(table);

        outputChannel.write(segments);
    }
}
