package xyz.lilyflower.figurines.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

public class FigurineItem extends Item {
    public final String name;
    public final String description;
    public final Category category;
    public final int index;
    public final String[] voicelines;

    public FigurineItem(String identifier, FigurineData data) {
        super(new Item.Settings().maxCount(1).registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("figurines", identifier))).rarity(data.rarity));

        this.name = data.name;
        this.description = data.description;
        this.category = data.category;
        this.index = data.index;
        this.voicelines = data.voicelines;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.of(WordUtils.capitalizeFully(this.name).replace("Of ", "of "));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return ActionResult.PASS;
        try {
            String line = voicelines[world.random.nextInt(voicelines.length)];
            user.sendMessage(Text.of(line), true);
            user.getItemCooldownManager().set(user.getStackInHand(hand), 35);
            return ActionResult.CONSUME;
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return ActionResult.FAIL;
    }

    public enum Category {
        SPECIAL(Formatting.BLUE),
        SERVICE(Formatting.DARK_GREEN),
        SYNDICATE(Formatting.DARK_RED),
        ANTAGONISTS(Formatting.DARK_GRAY),
        SECURITY(Formatting.RED),
        MEDICAL(Formatting.AQUA),
        SCIENCE(Formatting.DARK_PURPLE),
        ENGINEERING(Formatting.YELLOW),
        CARGO(Formatting.GOLD),
        ANIMALS(Formatting.GREEN)
        ;

        public final Formatting colour;

        Category(Formatting colour) {
            this.colour = colour;
        }
    }

    public record FigurineData(String name, String description, Category category, int index, Rarity rarity, String... voicelines) {}
}
