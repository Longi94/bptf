// automatically generated by the FlatBuffers compiler, do not modify

package com.tlongdev.bktf.flatbuffers.itemschema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class ItemSchema extends Table {
  public static ItemSchema getRootAsItemSchema(ByteBuffer _bb) { return getRootAsItemSchema(_bb, new ItemSchema()); }
  public static ItemSchema getRootAsItemSchema(ByteBuffer _bb, ItemSchema obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public ItemSchema __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public Origin origins(int j) { return origins(new Origin(), j); }
  public Origin origins(Origin obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int originsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public Item items(int j) { return items(new Item(), j); }
  public Item items(Item obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int itemsLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public Particle particle(int j) { return particle(new Particle(), j); }
  public Particle particle(Particle obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int particleLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public DecoratedWeapon decoratedWeapon(int j) { return decoratedWeapon(new DecoratedWeapon(), j); }
  public DecoratedWeapon decoratedWeapon(DecoratedWeapon obj, int j) { int o = __offset(10); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int decoratedWeaponLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }

  public static int createItemSchema(FlatBufferBuilder builder,
      int originsOffset,
      int itemsOffset,
      int particleOffset,
      int decoratedWeaponOffset) {
    builder.startObject(4);
    ItemSchema.addDecoratedWeapon(builder, decoratedWeaponOffset);
    ItemSchema.addParticle(builder, particleOffset);
    ItemSchema.addItems(builder, itemsOffset);
    ItemSchema.addOrigins(builder, originsOffset);
    return ItemSchema.endItemSchema(builder);
  }

  public static void startItemSchema(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addOrigins(FlatBufferBuilder builder, int originsOffset) { builder.addOffset(0, originsOffset, 0); }
  public static int createOriginsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startOriginsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addItems(FlatBufferBuilder builder, int itemsOffset) { builder.addOffset(1, itemsOffset, 0); }
  public static int createItemsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startItemsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addParticle(FlatBufferBuilder builder, int particleOffset) { builder.addOffset(2, particleOffset, 0); }
  public static int createParticleVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startParticleVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addDecoratedWeapon(FlatBufferBuilder builder, int decoratedWeaponOffset) { builder.addOffset(3, decoratedWeaponOffset, 0); }
  public static int createDecoratedWeaponVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startDecoratedWeaponVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endItemSchema(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishItemSchemaBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

