package xyz.lilyflower.figurines;

import java.util.LinkedHashSet;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.lilyflower.figurines.item.FigurineItem;

public class SpaceyFigurines implements ModInitializer {
	private static final LinkedHashSet<FigurineItem> FIGURINES = new LinkedHashSet<>();
	public static final ItemGroup FIGURINE_GROUP = Registry.register(Registries.ITEM_GROUP, RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("figurines", "figurines")), FabricItemGroup.builder()
			.displayName(Text.of("Figurines"))
			.entries((context, entries) -> {
				for (FigurineItem figurine : FIGURINES) {
					entries.add(figurine.getDefaultStack());
				}
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
}