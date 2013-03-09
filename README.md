# Java Structured Memory Access

JSMA allows to create arrays of C-like *struct* object, and access them in a straightforward manner.
Both preprocessor code generation and runtime code generation are supported.

## Simple code sample
##### Data structure declaration
    @Struct
    public interface Test {
        @Field
        public int getIntValue();
        public void setIntValue(int value);

        @Field(length=16)
        public double getDoubleValue(int index);
        public void setDoubleValue(int index, double value);
    }
##### Usage (with ASM runtime generation)

    // Creates an ASM-based allocator, based on direct `ByteBuffer`s, with native byte ordering
    IStructArrayFactory<?> allocator = AsmStructArrayFactory.newInstance(ByteBufferStructData.Direct.Native);
    StructPointer<Test> p = allocator.newStructArray(Test.class, 16);
    [...]
    p.at(4);                         // Moves the pointer to position 4
    p.get().getIntValue();           // Retrieves IntValue
    p.get().setDoubleValue(12, 5.0); // Stores 5.0 in array item 12 of DoubleValue

## Why?
This library has been written to address two problems which arise when writing low latency Java code.

*   Memory access patterns are unpredictable. An array (or collection) of non-primitive types gives no guarantees on the memory layout of its contents; every object could be placed in memory very far from its neighbours, spoiling cache access.
*   Creating many short- or medium-lived objects imposes a burden on the garbage collector; this can introduce random delays, which can be unacceptable in presence of low latency requirements.

Java offers a solution: `java.nio.ByteBuffer` and its evil twin, `sun.misc.Unsafe`. Both allow to allocate an area of memory, and access it by reading primitive types at offsets. Manually writing such a code is however tedious and error-prone. JSMA aims at providing a convenient and easy to use abstraction to these classes.

## How?

To use JSMA the first step is to declare one or more abstract data structures. JSMA will provide the implementation, either at compile time with annotation processing, or at runtime with bytecode generation.

### Declaration

A JSMA data structure is a plain Java `interface`, annotated with `@Struct`, and declaring methods following JSMA conventions, which are largely borrowed by the Javabeans convention.

To declare a field of a primitive type (everything is supported except for `boolean`), the value getter and setter methods must be declared. For instance, to declare a field called `Speed` with `float` type, the following two methods must be added to the interface:

    @Field // The annotation is optional in this case
    public float getSpeed();
    public void setSpeed(float value);

Fixed-length arrays of primitive types are supported. In this case however the `@Field` annotation is mandatory on at least one of the two methods, and must specify the length of the array.

    @Field(length=16)
    public int getAmmo(int index);
    public void setAmmo(int index, int value);

A structure can also contain another structure. In this case the return type is constrained to a `StructPointer` narrowed on the internal structure class, and there must not be a setter method:

    public StructPointer<Engine> getEngine();

Arrays of structures are supported as well:

    @Field(length=64)
    public StructPointer<Bullet> getBullet(int index);

Fields will be put in memory in unspecified order, unless the `position` attribute of `@Field` is used. Every field will be aligned at a 4-bytes boundary; arrays are aligned as a whole (thus a single short value will occupy 4 bytes, but an array of 32 shorts will occupy 64 bytes).

### Runtime generation

Runtime implementation generation can either be obtained through `java.lang.reflect.Proxy` or by direct bytecode generation through the ASM library. The former option produces slower but possibily safer implementations; the latter generates as fast as possible code.

Another option to decide is wheter accessing the data through `java.nio.ByteBuffer` or `sun.misc.Unsafe`. The former is safer albeit slower; the latter can make data access as fast as plain field access, but can corrupt the memory in case it is misused. ByteBuffers can either be [plain][1] or [direct][2], and can have either big endian, low endian or native memory layout; Unsafe access is only off-heap, and with native ordering. The seven options can be accessed with the following constants:

    ByteBufferStorage.Plain.Native
    ByteBufferStorage.Plain.BigEndian
    ByteBufferStorage.Plain.LittleEndian 
    ByteBufferStorage.Direct.Native
    ByteBufferStorage.Direct.BigEndian
    ByteBufferStorage.Direct.LittleEndian
    UnsafeStorage.Factory

One of these constants must be passed to either the Asm or the Proxy allocators:

    Allocator<?> allocator1 = ProxyAllocator.newInstance(ByteBufferStorage.Plain.BigEndian);
    Allocator<?> allocator2 = AsmAllocator.newInstance(UnsafeStorage.Instance);

Once an `Allocator` has been obtained, new arrays can be instantiated through it:

    MasterStructPointer<Spaceship> ships = allocator.newStructArray(Spaceship.class, 16);



[1]: http://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html#allocate%28int%29
[2]: http://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html#allocateDirect%28int%29
