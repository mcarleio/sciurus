package io.mcarle.sciurus.cache;

class CacheException extends RuntimeException {

    CacheException(Throwable cause) {
        super(cause);
    }

    CacheException() {
    }

    static class UnknownCache extends CacheException {

    }

    static class CacheSupplierReturnedNull extends CacheException {

    }

    static class CacheSupplierThrewException extends CacheException {

        CacheSupplierThrewException(Throwable cause) {
            super(cause);
        }

    }

}
