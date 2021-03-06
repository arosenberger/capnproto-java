package org.capnproto;

import java.nio.ByteBuffer;

public final class MessageReader {
    final ByteBuffer[] segmentSlices;

    public MessageReader(ByteBuffer[] segmentSlices) {
        this.segmentSlices = segmentSlices;
    }

    public <T> T getRoot(FromStructReader<T> factory) {
        SegmentReader segment = new SegmentReader(this.segmentSlices[0]);
        PointerReader pointerReader = PointerReader.getRoot(segment, 0,
                                                            0x7fffffff /* XXX */);
        AnyPointer.Reader any = new AnyPointer.Reader(pointerReader);
        return any.getAsStruct(factory);
    }
}
