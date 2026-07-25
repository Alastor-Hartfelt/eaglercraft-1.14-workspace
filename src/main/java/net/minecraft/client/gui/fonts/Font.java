package net.minecraft.client.gui.fonts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.lax1dude.eaglercraft.Random;
import net.minecraft.client.gui.fonts.providers.IGlyphProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class Font implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EmptyGlyph field_212460_b = new EmptyGlyph();
    private static final IGlyph field_212461_c = () -> {
        return 4.0F;
    };
    private static final Random RANDOM = new Random();
    private final TextureManager textureManager;
    private final ResourceLocation id;
    private TexturedGlyph fallbackGlyph;
    private final List<IGlyphProvider> glyphProviders = Lists.newArrayList();
    private final HashMap<Integer, TexturedGlyph> field_212463_j = new HashMap<>();
    private final HashMap<Integer, ArrayList<Character>> glyphsByWidth = new HashMap<>();
    private final HashMap<Integer, IGlyph> glyphs = new HashMap<>();
    private final List<FontTexture> textures = Lists.newArrayList();

    public Font(TextureManager textureManagerIn, ResourceLocation resourceLocationIn) {
        this.textureManager = textureManagerIn;
        this.id = resourceLocationIn;
    }

    public void setGlyphProviders(List<IGlyphProvider> glyphProvidersIn) {
        for (IGlyphProvider iglyphprovider : this.glyphProviders) {
            if (!glyphProvidersIn.contains(iglyphprovider)) {
                iglyphprovider.close();
            }
        }

        this.glyphProviders.clear();
        this.deleteTextures();
        this.textures.clear();
        this.field_212463_j.clear();
        this.glyphs.clear();
        this.glyphsByWidth.clear();
        this.fallbackGlyph = this.createTexturedGlyph(DefaultGlyph.INSTANCE);
        Set<IGlyphProvider> set = Sets.newHashSet();

        for (char c0 = 0; c0 < '\uffff'; ++c0) {
            for (IGlyphProvider iglyphprovider1 : glyphProvidersIn) {
                IGlyph iglyph = (IGlyph) (c0 == ' ' ? field_212461_c : iglyphprovider1.func_212248_a(c0));
                if (iglyph != null) {
                    set.add(iglyphprovider1);
                    if (iglyph != DefaultGlyph.INSTANCE) {
                        this.glyphsByWidth.computeIfAbsent(MathHelper.ceil(iglyph.getAdvance(false)), (p_212456_0_) -> {
                            return new ArrayList<>();
                        }).add(c0);
                    }
                    break;
                }
            }
        }

        glyphProvidersIn.stream().filter(set::contains).forEach(this.glyphProviders::add);
    }

    public void close() {
        this.deleteTextures();
    }

    public void deleteTextures() {
        for (FontTexture fonttexture : this.textures) {
            fonttexture.close();
        }

    }

    public IGlyph findGlyph(char charIn) {
        return this.glyphs.computeIfAbsent((int) charIn, (p_212457_1_) -> {
            return (IGlyph) (p_212457_1_ == 32 ? field_212461_c : this.func_212455_c((char) (int) p_212457_1_));
        });
    }

    private IGlyphInfo func_212455_c(char p_212455_1_) {
        for (IGlyphProvider iglyphprovider : this.glyphProviders) {
            IGlyphInfo iglyphinfo = iglyphprovider.func_212248_a(p_212455_1_);
            if (iglyphinfo != null) {
                return iglyphinfo;
            }
        }

        return DefaultGlyph.INSTANCE;
    }

    public TexturedGlyph getGlyph(char character) {
        return this.field_212463_j.computeIfAbsent((int) character, (p_212458_1_) -> {
            return (TexturedGlyph) (p_212458_1_ == 32 ? field_212460_b : this.createTexturedGlyph(this.func_212455_c((char) (int) p_212458_1_)));
        });
    }

    private TexturedGlyph createTexturedGlyph(IGlyphInfo glyphInfoIn) {
        for (FontTexture fonttexture : this.textures) {
            TexturedGlyph texturedglyph = fonttexture.createTexturedGlyph(glyphInfoIn);
            if (texturedglyph != null) {
                return texturedglyph;
            }
        }

        FontTexture fonttexture1 = new FontTexture(new ResourceLocation(this.id.getNamespace(), this.id.getPath() + "/" + this.textures.size()), glyphInfoIn.isColored());
        this.textures.add(fonttexture1);
        this.textureManager.loadTexture(fonttexture1.getTextureLocation(), fonttexture1);
        TexturedGlyph texturedglyph1 = fonttexture1.createTexturedGlyph(glyphInfoIn);
        return texturedglyph1 == null ? this.fallbackGlyph : texturedglyph1;
    }

    public TexturedGlyph obfuscate(IGlyph glyph) {
        ArrayList<Character> charlist = this.glyphsByWidth.get(MathHelper.ceil(glyph.getAdvance(false)));
        return charlist != null && !charlist.isEmpty() ? this.getGlyph(charlist.get(RANDOM.nextInt(charlist.size()))) : this.fallbackGlyph;
    }
}
