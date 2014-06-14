package org.capnproto;

public final class AnyPointer {

    public static final class Builder {
        public final PointerBuilder builder;

        public Builder(PointerBuilder builder) {
            this.builder = builder;
        }

        public final <T> T initAsStruct(FromStructBuilder<T> factory) {
            return factory.fromStructBuilder(this.builder.initStruct(factory.structSize()));
        }
    }
}
