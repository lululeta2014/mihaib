// Package kvstore provides a layer on top of a storage layer,
// decoupling programs from its details.
//
// It defines key-value store interfaces with minimal characteristics so that
// any storage layer should be able to implement them.
// To use any storage layer (e.g. a remote system, a database, a file,
// an in-memory data structure) an adapter must be created which implements
// the basic interfaces defined by this package and is backed by the storage
// layer.
// This package also builds storage types with additional properties, on top
// of the basic interfaces.
//
// In general, programs running on several machines use the same data store
// simultaneously, each from multiple goroutines. The data store is responsible
// for handling concurrent access (ideally providing ACID properties).
package kvstore
