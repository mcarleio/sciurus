package io.mcarle.sciurus;

import java.util.Arrays;

public class ExecutionIdentifier {

    private final String signature;
    private final Object[] args;

    public ExecutionIdentifier(String signature, Object[] args) {
        this.signature = signature;
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionIdentifier source = (ExecutionIdentifier) o;

        return signature.equals(source.signature) && Arrays.deepEquals(args, source.args);
    }

    @Override
    public int hashCode() {
        return 29 * signature.hashCode() + Arrays.deepHashCode(args);
    }

    @Override
    public String toString() {
        return signature;
    }
}