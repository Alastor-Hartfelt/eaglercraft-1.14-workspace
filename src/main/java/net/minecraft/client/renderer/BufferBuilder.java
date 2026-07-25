package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import net.lax1dude.eaglercraft.internal.buffer.ByteBuffer;
import net.lax1dude.eaglercraft.internal.buffer.FloatBuffer;
import net.lax1dude.eaglercraft.internal.buffer.IntBuffer;
import net.lax1dude.eaglercraft.internal.buffer.ShortBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer byteBuffer;
    private IntBuffer rawIntBuffer;
    private ShortBuffer rawShortBuffer;
    private FloatBuffer rawFloatBuffer;
    private int vertexCount;
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;
    private boolean noColor;
    private int drawMode;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;

    public BufferBuilder(int bufferSizeIn) {
        this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        this.rawIntBuffer = this.byteBuffer.asIntBuffer();
        this.rawShortBuffer = this.byteBuffer.asShortBuffer();
        this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
    }

    private void growBuffer(int increaseAmount) {
        if (this.vertexCount * this.vertexFormat.getSize() + increaseAmount > this.byteBuffer.capacity()) {
            int i = this.byteBuffer.capacity();
            int j = i + func_216566_c(increaseAmount);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
            int k = this.rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(j);
            this.byteBuffer.position(0);
            bytebuffer.put(this.byteBuffer);
            bytebuffer.rewind();
            this.byteBuffer = bytebuffer;
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawIntBuffer.position(k);
            this.rawShortBuffer = this.byteBuffer.asShortBuffer();
            this.rawShortBuffer.position(k << 1);
        }
    }

    private static int func_216566_c(int p_216566_0_) {
        int i = 131072;
        if (p_216566_0_ == 0) {
            return i;
        } else {
            if (p_216566_0_ < 0) {
                i *= -1;
            }

            int j = p_216566_0_ % i;
            return j == 0 ? p_216566_0_ : p_216566_0_ + i - j;
        }
    }

    public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
        int i = this.vertexCount / 4;
        float[] afloat = new float[i];

        for (int j = 0; j < i; ++j) {
            afloat[j] = getDistanceSq(this.rawFloatBuffer, (float) ((double) cameraX + this.xOffset), (float) ((double) cameraY + this.yOffset), (float) ((double) cameraZ + this.zOffset), this.vertexFormat.getIntegerSize(), j * this.vertexFormat.getSize());
        }

        Integer[] ainteger = new Integer[i];

        for (int k = 0; k < ainteger.length; ++k) {
            ainteger[k] = k;
        }

        Arrays.sort(ainteger, (p_210255_1_, p_210255_2_) -> {
            return Floats.compare(afloat[p_210255_2_], afloat[p_210255_1_]);
        });
        BitSet bitset = new BitSet();
        int l = this.vertexFormat.getSize();
        int[] aint = new int[l];
        int[] tempQuad = new int[l];

        for (int i1 = bitset.nextClearBit(0); i1 < ainteger.length; i1 = bitset.nextClearBit(i1 + 1)) {
            int j1 = ainteger[i1];
            if (j1 != i1) {
                this.rawIntBuffer.limit(j1 * l + l);
                this.rawIntBuffer.position(j1 * l);
                this.rawIntBuffer.get(aint);
                int k1 = j1;

                for (int l1 = ainteger[j1]; k1 != i1; l1 = ainteger[l1]) {
                    this.rawIntBuffer.limit(l1 * l + l);
                    this.rawIntBuffer.position(l1 * l);
                    this.rawIntBuffer.get(tempQuad);
                    this.rawIntBuffer.limit(k1 * l + l);
                    this.rawIntBuffer.position(k1 * l);
                    this.rawIntBuffer.put(tempQuad);
                    bitset.set(k1);
                    k1 = l1;
                }


                this.rawIntBuffer.limit(i1 * l + l);
                this.rawIntBuffer.position(i1 * l);
                this.rawIntBuffer.put(aint);
            }

            bitset.set(i1);
        }

    }

    public BufferBuilder.State getVertexState() {
        this.rawIntBuffer.rewind();
        int i = this.getBufferSize();
        this.rawIntBuffer.limit(i);
        int[] aint = new int[i];
        this.rawIntBuffer.get(aint);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(i);
        return new BufferBuilder.State(aint, new VertexFormat(this.vertexFormat));
    }

    private int getBufferSize() {
        return this.vertexCount * (this.vertexFormat.getSize() >> 2);
    }

    private static float getDistanceSq(FloatBuffer floatBufferIn, float x, float y, float z, int integerSize, int offset) {
        float f = floatBufferIn.get(offset + integerSize * 0 + 0);
        float f1 = floatBufferIn.get(offset + integerSize * 0 + 1);
        float f2 = floatBufferIn.get(offset + integerSize * 0 + 2);
        float f3 = floatBufferIn.get(offset + integerSize * 1 + 0);
        float f4 = floatBufferIn.get(offset + integerSize * 1 + 1);
        float f5 = floatBufferIn.get(offset + integerSize * 1 + 2);
        float f6 = floatBufferIn.get(offset + integerSize * 2 + 0);
        float f7 = floatBufferIn.get(offset + integerSize * 2 + 1);
        float f8 = floatBufferIn.get(offset + integerSize * 2 + 2);
        float f9 = floatBufferIn.get(offset + integerSize * 3 + 0);
        float f10 = floatBufferIn.get(offset + integerSize * 3 + 1);
        float f11 = floatBufferIn.get(offset + integerSize * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - x;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - y;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - z;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    public void setVertexState(BufferBuilder.State state) {
        this.rawIntBuffer.clear();
        this.growBuffer(state.getRawBuffer().length * 4);
        this.rawIntBuffer.put(state.getRawBuffer());
        this.vertexCount = state.getVertexCount();
        this.vertexFormat = new VertexFormat(state.getVertexFormat());
    }

    public void reset() {
        this.vertexCount = 0;
        this.vertexFormatElement = null;
        this.vertexFormatIndex = 0;
    }

    public void begin(int glMode, VertexFormat format) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            this.isDrawing = true;
            this.reset();
            this.drawMode = glMode;
            this.vertexFormat = format;
            this.vertexFormatElement = format.getElement(this.vertexFormatIndex);
            this.noColor = false;
            this.byteBuffer.limit(this.byteBuffer.capacity());
        }
    }

    public BufferBuilder tex(double u, double v) {
        int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) u);
                this.byteBuffer.putFloat(i + 4, (float) v);
                break;
            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int) u);
                this.byteBuffer.putInt(i + 4, (int) v);
                break;
            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) v));
                this.byteBuffer.putShort(i + 2, (short) ((int) u));
                break;
            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) v));
                this.byteBuffer.put(i + 1, (byte) ((int) u));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public BufferBuilder lightmap(int skyLight, int blockLight) {
        int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) skyLight);
                this.byteBuffer.putFloat(i + 4, (float) blockLight);
                break;
            case UINT:
            case INT:
                this.byteBuffer.putInt(i, skyLight);
                this.byteBuffer.putInt(i + 4, blockLight);
                break;
            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) blockLight);
                this.byteBuffer.putShort(i + 2, (short) skyLight);
                break;
            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) blockLight);
                this.byteBuffer.put(i + 1, (byte) skyLight);
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putBrightness4(int vertex0, int vertex1, int vertex2, int vertex3) {
        int i = this.vertexFormat.getSize();
        int j = i >> 2;
        int k = (this.vertexCount - 4) * j + this.vertexFormat.getUvOffsetById(1) / 4;
        this.rawIntBuffer.put(k, vertex0);
        this.rawIntBuffer.put(k + j, vertex1);
        this.rawIntBuffer.put(k + j * 2, vertex2);
        this.rawIntBuffer.put(k + j * 3, vertex3);
    }

    public void putPosition(double x, double y, double z) {
        int i = this.vertexFormat.getIntegerSize();
        int j = (this.vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            this.rawIntBuffer.put(l, Float.floatToRawIntBits((float) (x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
            this.rawIntBuffer.put(i1, Float.floatToRawIntBits((float) (y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1))));
            this.rawIntBuffer.put(j1, Float.floatToRawIntBits((float) (z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1))));
        }

    }

    private int getColorIndex(int vertexIndex) {
        return ((this.vertexCount - vertexIndex) * this.vertexFormat.getSize() + this.vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int vertexIndex) {
        int i = this.getColorIndex(vertexIndex);
        int j = -1;
        if (!this.noColor) {
            j = this.rawIntBuffer.get(i);
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        this.rawIntBuffer.put(i, j);
    }

    private void putColor(int argb, int vertexIndex) {
        int i = this.getColorIndex(vertexIndex);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        this.putColorRGBA(i, j, k, l);
    }

    public void putColorRGB_F(float red, float green, float blue, int vertexIndex) {
        int i = this.getColorIndex(vertexIndex);
        int j = func_216567_a((int) (red * 255.0F), 0, 255);
        int k = func_216567_a((int) (green * 255.0F), 0, 255);
        int l = func_216567_a((int) (blue * 255.0F), 0, 255);
        this.putColorRGBA(i, j, k, l);
    }

    private static int func_216567_a(int p_216567_0_, int p_216567_1_, int p_216567_2_) {
        if (p_216567_0_ < p_216567_1_) {
            return p_216567_1_;
        } else {
            return p_216567_0_ > p_216567_2_ ? p_216567_2_ : p_216567_0_;
        }
    }

    private void putColorRGBA(int index, int red, int green, int blue) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.rawIntBuffer.put(index, -16777216 | blue << 16 | green << 8 | red);
        } else {
            this.rawIntBuffer.put(index, red << 24 | green << 16 | blue << 8 | 255);
        }

    }

    public void noColor() {
        this.noColor = true;
    }

    public BufferBuilder color(float red, float green, float blue, float alpha) {
        return this.color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F));
    }

    public BufferBuilder color(int red, int green, int blue, int alpha) {
        if (this.noColor) {
            return this;
        } else {
            int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
            switch (this.vertexFormatElement.getType()) {
                case FLOAT:
                    this.byteBuffer.putFloat(i, (float) red / 255.0F);
                    this.byteBuffer.putFloat(i + 4, (float) green / 255.0F);
                    this.byteBuffer.putFloat(i + 8, (float) blue / 255.0F);
                    this.byteBuffer.putFloat(i + 12, (float) alpha / 255.0F);
                    break;
                case UINT:
                case INT:
                    this.byteBuffer.putFloat(i, (float) red);
                    this.byteBuffer.putFloat(i + 4, (float) green);
                    this.byteBuffer.putFloat(i + 8, (float) blue);
                    this.byteBuffer.putFloat(i + 12, (float) alpha);
                    break;
                case USHORT:
                case SHORT:
                    this.byteBuffer.putShort(i, (short) red);
                    this.byteBuffer.putShort(i + 2, (short) green);
                    this.byteBuffer.putShort(i + 4, (short) blue);
                    this.byteBuffer.putShort(i + 6, (short) alpha);
                    break;
                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        this.byteBuffer.put(i, (byte) red);
                        this.byteBuffer.put(i + 1, (byte) green);
                        this.byteBuffer.put(i + 2, (byte) blue);
                        this.byteBuffer.put(i + 3, (byte) alpha);
                    } else {
                        this.byteBuffer.put(i, (byte) alpha);
                        this.byteBuffer.put(i + 1, (byte) blue);
                        this.byteBuffer.put(i + 2, (byte) green);
                        this.byteBuffer.put(i + 3, (byte) red);
                    }
            }

            this.nextVertexFormatIndex();
            return this;
        }
    }

    public void addVertexData(int[] vertexData) {
        int i = this.vertexFormat.getSize();
        this.growBuffer(vertexData.length * 4 + i);
        this.rawIntBuffer.position(this.getBufferSize());
        this.rawIntBuffer.put(vertexData);
        this.vertexCount += vertexData.length / (i >> 2);
    }

    public void addQuadOptimized(int[] vertexData, float xOffset, float yOffset, float zOffset, int[] vertexBrightness, float[] colorMultR, float[] colorMultG, float[] colorMultB) {
        int vertexSizeInts = this.vertexFormat.getIntegerSize();
        int quadInts = vertexSizeInts * 4;
        this.growBuffer(quadInts * 4 + this.vertexFormat.getSize());
        int offset = this.getBufferSize();
        boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

        float finalXOffset = xOffset + (float) this.xOffset;
        float finalYOffset = yOffset + (float) this.yOffset;
        float finalZOffset = zOffset + (float) this.zOffset;

        for (int i = 0; i < 4; ++i) {
            int vIdx = i * vertexSizeInts;
            int destIdx = offset + vIdx;

            this.rawIntBuffer.put(destIdx, Float.floatToRawIntBits(Float.intBitsToFloat(vertexData[vIdx]) + finalXOffset));
            this.rawIntBuffer.put(destIdx + 1, Float.floatToRawIntBits(Float.intBitsToFloat(vertexData[vIdx + 1]) + finalYOffset));
            this.rawIntBuffer.put(destIdx + 2, Float.floatToRawIntBits(Float.intBitsToFloat(vertexData[vIdx + 2]) + finalZOffset));

            int origColor = vertexData[vIdx + 3];
            float cr = colorMultR[i];
            float cg = colorMultG[i];
            float cb = colorMultB[i];

            if (littleEndian) {
                int r = (int) ((float) (origColor & 255) * cr);
                int g = (int) ((float) (origColor >> 8 & 255) * cg);
                int b = (int) ((float) (origColor >> 16 & 255) * cb);
                this.rawIntBuffer.put(destIdx + 3, origColor & -16777216 | b << 16 | g << 8 | r);
            } else {
                int r = (int) ((float) (origColor >> 24 & 255) * cr);
                int g = (int) ((float) (origColor >> 16 & 255) * cg);
                int b = (int) ((float) (origColor >> 8 & 255) * cb);
                this.rawIntBuffer.put(destIdx + 3, origColor & 255 | r << 24 | g << 16 | b << 8);
            }

            this.rawIntBuffer.put(destIdx + 4, vertexData[vIdx + 4]);
            this.rawIntBuffer.put(destIdx + 5, vertexData[vIdx + 5]);

            this.rawIntBuffer.put(destIdx + 6, vertexBrightness[i]);

            for (int j = 7; j < vertexSizeInts; j++) {
                this.rawIntBuffer.put(destIdx + j, vertexData[vIdx + j]);
            }
        }

        this.vertexCount += 4;
    }

    public void addFluidQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float r, float g, float b, int lightmap) {
        int vertexSizeInts = this.vertexFormat.getIntegerSize();
        this.growBuffer(vertexSizeInts * 16 + this.vertexFormat.getSize());
        int k = this.vertexCount * vertexSizeInts;

        float cx = (float) this.xOffset;
        float cy = (float) this.yOffset;
        float cz = (float) this.zOffset;

        int j1 = (int) (r * 255.0F);
        int k1 = (int) (g * 255.0F);
        int l1 = (int) (b * 255.0F);
        boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
        int colorInt;
        if (littleEndian) {
            colorInt = j1 | k1 << 8 | l1 << 16 | 255 << 24;
        } else {
            colorInt = 255 | j1 << 24 | k1 << 16 | l1 << 8;
        }

        // Vert 0
        this.rawFloatBuffer.put(k, x0 + cx);
        this.rawFloatBuffer.put(k + 1, y0 + cy);
        this.rawFloatBuffer.put(k + 2, z0 + cz);
        this.rawIntBuffer.put(k + 3, colorInt);
        this.rawFloatBuffer.put(k + 4, u0);
        this.rawFloatBuffer.put(k + 5, v0);
        this.rawIntBuffer.put(k + 6, lightmap);

        // Vert 1
        this.rawFloatBuffer.put(k + 7, x1 + cx);
        this.rawFloatBuffer.put(k + 8, y1 + cy);
        this.rawFloatBuffer.put(k + 9, z1 + cz);
        this.rawIntBuffer.put(k + 10, colorInt);
        this.rawFloatBuffer.put(k + 11, u1);
        this.rawFloatBuffer.put(k + 12, v1);
        this.rawIntBuffer.put(k + 13, lightmap);

        // Vert 2
        this.rawFloatBuffer.put(k + 14, x2 + cx);
        this.rawFloatBuffer.put(k + 15, y2 + cy);
        this.rawFloatBuffer.put(k + 16, z2 + cz);
        this.rawIntBuffer.put(k + 17, colorInt);
        this.rawFloatBuffer.put(k + 18, u2);
        this.rawFloatBuffer.put(k + 19, v2);
        this.rawIntBuffer.put(k + 20, lightmap);

        // Vert 3
        this.rawFloatBuffer.put(k + 21, x3 + cx);
        this.rawFloatBuffer.put(k + 22, y3 + cy);
        this.rawFloatBuffer.put(k + 23, z3 + cz);
        this.rawIntBuffer.put(k + 24, colorInt);
        this.rawFloatBuffer.put(k + 25, u3);
        this.rawFloatBuffer.put(k + 26, v3);
        this.rawIntBuffer.put(k + 27, lightmap);

        this.vertexCount += 4;
    }

    public void endVertex() {
        ++this.vertexCount;
        this.growBuffer(this.vertexFormat.getSize());
    }

    public BufferBuilder pos(double x, double y, double z) {
        int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float) (x + this.xOffset));
                this.byteBuffer.putFloat(i + 4, (float) (y + this.yOffset));
                this.byteBuffer.putFloat(i + 8, (float) (z + this.zOffset));
                break;
            case UINT:
            case INT:
                this.byteBuffer.putInt(i, Float.floatToRawIntBits((float) (x + this.xOffset)));
                this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float) (y + this.yOffset)));
                this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float) (z + this.zOffset)));
                break;
            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) (x + this.xOffset)));
                this.byteBuffer.putShort(i + 2, (short) ((int) (y + this.yOffset)));
                this.byteBuffer.putShort(i + 4, (short) ((int) (z + this.zOffset)));
                break;
            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) (x + this.xOffset)));
                this.byteBuffer.put(i + 1, (byte) ((int) (y + this.yOffset)));
                this.byteBuffer.put(i + 2, (byte) ((int) (z + this.zOffset)));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putNormal(float x, float y, float z) {
        int i = (byte) ((int) (x * 127.0F)) & 255;
        int j = (byte) ((int) (y * 127.0F)) & 255;
        int k = (byte) ((int) (z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = this.vertexFormat.getSize() >> 2;
        int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
        this.rawIntBuffer.put(j1, l);
        this.rawIntBuffer.put(j1 + i1, l);
        this.rawIntBuffer.put(j1 + i1 * 2, l);
        this.rawIntBuffer.put(j1 + i1 * 3, l);
    }

    private void nextVertexFormatIndex() {
        ++this.vertexFormatIndex;
        this.vertexFormatIndex %= this.vertexFormat.getElementCount();
        this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);
        if (this.vertexFormatElement.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextVertexFormatIndex();
        }

    }

    public BufferBuilder normal(float x, float y, float z) {
        int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                this.byteBuffer.putFloat(i, x);
                this.byteBuffer.putFloat(i + 4, y);
                this.byteBuffer.putFloat(i + 8, z);
                break;
            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int) x);
                this.byteBuffer.putInt(i + 4, (int) y);
                this.byteBuffer.putInt(i + 8, (int) z);
                break;
            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) x * 32767 & '\uffff'));
                this.byteBuffer.putShort(i + 2, (short) ((int) y * 32767 & '\uffff'));
                this.byteBuffer.putShort(i + 4, (short) ((int) z * 32767 & '\uffff'));
                break;
            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) x * 127 & 255));
                this.byteBuffer.put(i + 1, (byte) ((int) y * 127 & 255));
                this.byteBuffer.put(i + 2, (byte) ((int) z * 127 & 255));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void setTranslation(double x, double y, double z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public void finishDrawing() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            this.isDrawing = false;
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.getBufferSize() * 4);
        }
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getDrawMode() {
        return this.drawMode;
    }

    public void putColor4(int argb) {
        for (int i = 0; i < 4; ++i) {
            this.putColor(argb, i + 1);
        }

    }

    public void putColorRGB_F4(float red, float green, float blue) {
        for (int i = 0; i < 4; ++i) {
            this.putColorRGB_F(red, green, blue, i + 1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public class State {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;

        public State(int[] buffer, VertexFormat format) {
            this.stateRawBuffer = buffer;
            this.stateVertexFormat = format;
        }

        public int[] getRawBuffer() {
            return this.stateRawBuffer;
        }

        public int getVertexCount() {
            return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
        }

        public VertexFormat getVertexFormat() {
            return this.stateVertexFormat;
        }
    }
}
