## MiMa

Minimal Machine Emulator. Commands are based on MiMa lecture
at KIT (WS 16/17).

### Instructions

Download java file, compile, execute via `java MiMa [filename]`.

### Commands

    // load and store
    LDC [constant] = Load constant into accumulator
    STV [address] = Store value of accumulator in memory at address
    LDV [address] = Load value of memory at address into accumulator
    STIV [address] = Store value of accumulator in memory at address returned by the memory at [address] (store indirect)
    LDIV [address] = Load value of memory at address returned by the memory at [address] into accumulator (load indirect)

    // operators
    ADD [address] = Add value of memory at address to accumulator
    AND [address] = Do bitwise AND operation of accumulator and value of memory at address
    OR [address] = Do bitwise OR operation of accumulator and value of memory at address
    XOR [address] = Do bitwise XOR operation of accumulator and value of memory at address
    NOT = Negate bits of accumulator
    RAR = Rotate bits of accumulator right by one position
    EQL [address] = Compare accumulator with value of memory at address. Set accumulator to -1 if same, else 0.

    // jumps
    JMN [label] = Jump to label if accumulator is negative
    JMP [label] = Jump to label
    :[name] = Label
    HALT = Halt program


### Examples

[Click here](https://github.com/DeerMichel/mima/tree/master/examples)

### Known issues (TODO)

* Indirect address has to be numbers only
* No syntax verification at all ^^
