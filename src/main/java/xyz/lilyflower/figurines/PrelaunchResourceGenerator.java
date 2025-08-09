package xyz.lilyflower.figurines;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import org.apache.commons.io.FileUtils;
import xyz.lilyflower.figurines.item.FigurineItem;

public class PrelaunchResourceGenerator implements PreLaunchEntrypoint {
    static final LinkedHashMap<String, FigurineItem.FigurineData> UNREGISTERED = new LinkedHashMap<>();

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

                System.out.println("Adding "  + figurine.getName());
                UNREGISTERED.put("figurine_" + identifier, new FigurineItem.FigurineData(
                        parsed.get("name").getAsString(),
                        parsed.get("description").getAsString(),
                        FigurineItem.Category.valueOf(parsed.get("category").getAsString().toUpperCase(Locale.ROOT)),
                        0,
                        Rarity.valueOf(parsed.get("rarity").getAsString().toUpperCase(Locale.ROOT)),
                        text
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
