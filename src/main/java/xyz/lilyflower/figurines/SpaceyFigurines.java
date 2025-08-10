package xyz.lilyflower.figurines;

import java.util.LinkedHashSet;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.lilyflower.figurines.item.FigurineItem;

@SuppressWarnings({"unused", "deprecation"})
public class SpaceyFigurines implements ModInitializer, ClientModInitializer {
	private static final LinkedHashSet<FigurineItem> FIGURINES = new LinkedHashSet<>();
	public static final ItemGroup FIGURINE_GROUP = Registry.register(Registries.ITEM_GROUP, RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("figurines", "figurines")), FabricItemGroup.builder()
			.displayName(Text.of("Figurines"))
			.entries((context, entries) -> {
				for (FigurineItem figurine : FIGURINES) {
					entries.add(figurine.getDefaultStack());
				}
			})
			.icon(() -> {
				Item[] arr = new Item[FIGURINES.size()];
				return FIGURINES.toArray(arr)[0].getDefaultStack();
			})
			.build());

	public static final Logger LOGGER = LogManager.getLogger("Spacey Figurines");

	@Override
	public void onInitialize() {
		PrelaunchResourceGenerator.UNREGISTERED.forEach((name, data) -> {
			FigurineItem figurine = new FigurineItem(name, data);
			Registry.register(Registries.ITEM, Identifier.of("figurines", name), figurine);
			FIGURINES.add(figurine);
		});
	}

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (stack.getItem() instanceof FigurineItem figurine) {
				lines.add(Text.literal("Category: ").append(Text.literal(WordUtils.capitalize(figurine.category.name().toLowerCase(Locale.ROOT))).formatted(figurine.category.colour)));
				lines.add(Text.literal("Number: ").append(Text.literal(Integer.toString(figurine.index)).formatted(Formatting.GRAY)));
				lines.add(Text.literal("Description: ").append(Text.literal(figurine.description).formatted(Formatting.GRAY)));
			}
		});
	}
}