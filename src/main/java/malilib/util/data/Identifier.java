package malilib.util.data;

/**
 * This is a simple dummy "wrapper" class that just extends the vanilla ResourceLocation/Identifier class.
 * The sole purpose of this class is to hide that mapping difference from lots of the places in
 * the mods' code, where an identifier is needed.
 */
public class Identifier extends net.minecraft.util.Identifier
{
    public Identifier(String resourceName)
    {
        super(resourceName);
    }

    public Identifier(String namespaceIn, String pathIn)
    {
        super(namespaceIn, pathIn);
    }
}
