// automatically generated by the FlatBuffers compiler, do not modify

package com.tlongdev.bktf.flatbuffers.itemschema;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class DecoratedWeapon extends Table {
  public static DecoratedWeapon getRootAsDecoratedWeapon(ByteBuffer _bb) { return getRootAsDecoratedWeapon(_bb, new DecoratedWeapon()); }
  public static DecoratedWeapon getRootAsDecoratedWeapon(ByteBuffer _bb, DecoratedWeapon obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public DecoratedWeapon __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int defindex() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int grade() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createDecoratedWeapon(FlatBufferBuilder builder,
      int defindex,
      int grade) {
    builder.startObject(2);
    DecoratedWeapon.addGrade(builder, grade);
    DecoratedWeapon.addDefindex(builder, defindex);
    return DecoratedWeapon.endDecoratedWeapon(builder);
  }

  public static void startDecoratedWeapon(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addDefindex(FlatBufferBuilder builder, int defindex) { builder.addInt(0, defindex, 0); }
  public static void addGrade(FlatBufferBuilder builder, int grade) { builder.addInt(1, grade, 0); }
  public static int endDecoratedWeapon(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}
