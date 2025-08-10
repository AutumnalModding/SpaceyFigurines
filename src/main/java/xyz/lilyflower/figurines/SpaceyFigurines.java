package xyz.lilyflower.figurines;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"unused", "deprecation"})
public class SpaceyFigurines implements ModInitializer, ClientModInitializer, PreLaunchEntrypoint {
	private static final LinkedHashSet<FigurineItem> FIGURINES = new LinkedHashSet<>();
	static final LinkedHashMap<String, FigurineItem.FigurineData> UNREGISTERED = new LinkedHashMap<>();
	public static ItemGroup FIGURINE_GROUP;

	public static final Logger LOGGER = LogManager.getLogger("Spacey Figurines");

	@Override
	public void onInitialize() {
		UNREGISTERED.forEach((name, data) -> {
			FigurineItem figurine = new FigurineItem(name, data);
			Registry.register(Registries.ITEM, Identifier.of("figurines", name), figurine);
			FIGURINES.add(figurine);
		});

		FIGURINE_GROUP = Registry.register(Registries.ITEM_GROUP, RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("figurines", "figurines")), FabricItemGroup.builder()
				.displayName(Text.of("Figurines"))
				.entries((context, entries) -> {
					for (FigurineItem figurine : FIGURINES) {
						entries.add(figurine.getDefaultStack());
					}
				})
				.icon(() -> {
					try {
						Item[] arr = new Item[FIGURINES.size()];
						return FIGURINES.toArray(arr)[0].getDefaultStack();
					} catch (ArrayIndexOutOfBoundsException exception) {
						return Blocks.AIR.asItem().getDefaultStack();
					}
				})
				.build());
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

	@Override
	@SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
	public void onPreLaunch() {
		try {
			FileUtils.deleteDirectory(Path.of("resourcepacks/figurines").toFile());
			Path.of("resourcepacks/figurines/assets/figurines/items").toFile().mkdirs();
			Path.of("resourcepacks/figurines/assets/figurines/textures/item").toFile().mkdirs();
			Path.of("resourcepacks/figurines/assets/figurines/models/item").toFile().mkdirs();
			String PACK_METADATA = """
                    {
                      "pack": {
                        "description": "Spacey Figurines dynamic resource pack",
                        "pack_format": 81
                      }
                    }
                    """;
			Files.writeString(Path.of("resourcepacks/figurines/pack.mcmeta"), PACK_METADATA);
			File figurines = Path.of("config/figurines/").toFile();
			figurines.mkdirs();

			File[] possible = figurines.listFiles();
			Arrays.sort(possible);
			for (File figurine : possible) {
				JsonObject parsed = JsonParser.parseString(Files.readString(figurine.toPath())).getAsJsonObject();
				String identifier = parsed.get("identifier").getAsString();

				JsonArray voicelines = parsed.getAsJsonArray("voicelines");
				String[] text = new String[voicelines.size()];
				for (int line = 0; line < voicelines.size(); line++) {
					text[line] = voicelines.get(line).getAsString();
				}

				String ITEM_MODEL = """
                        {
                          "parent": "minecraft:item/generated",
                          "textures": {
                            "layer0": "figurines:item/figurine_""" + identifier + "\"" + """
                          }
                        }
                        """;
				String SECOND_ITEM_MODEL_FOR_SOME_REASON = """
                        {
                          "model": {
                            "type": "minecraft:model",
                            "model": "figurines:item/figurine_""" + identifier + "\"" + """
                          }
                        }
                        """;

				Files.writeString(Path.of("resourcepacks/figurines/assets/figurines/models/item/" + "figurine_" + identifier + ".json"), ITEM_MODEL);
				Files.writeString(Path.of("resourcepacks/figurines/assets/figurines/items/" + "figurine_" + identifier + ".json"), SECOND_ITEM_MODEL_FOR_SOME_REASON);

				Base64.Decoder decoder = Base64.getDecoder();
				byte[] raw = decoder.decode(parsed.get("texture").getAsString());
				Files.write(Path.of("resourcepacks/figurines/assets/figurines/textures/item/figurine_" + identifier + ".png"), raw, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

				UNREGISTERED.put("figurine_" + identifier, new FigurineItem.FigurineData(
						parsed.get("name").getAsString(),
						parsed.get("description").getAsString(),
						FigurineItem.Category.valueOf(parsed.get("category").getAsString().toUpperCase(Locale.ROOT)),
						parsed.get("number").getAsInt(),
						Rarity.valueOf(parsed.get("rarity").getAsString().toUpperCase(Locale.ROOT)),
						text
				));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FigurineItem extends Item {
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
}