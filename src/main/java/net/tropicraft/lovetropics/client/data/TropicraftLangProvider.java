package net.tropicraft.lovetropics.client.data;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.Util;

public class TropicraftLangProvider extends LanguageProvider {

    private static class AccessibleLanguageProvider extends LanguageProvider {

        public AccessibleLanguageProvider(DataGenerator gen, String modid, String locale) {
            super(gen, modid, locale);
        }

        @Override
        public void add(String key, String value) {
            super.add(key, value);
        }

        @Override
        protected void addTranslations() {}
    }

    private final AccessibleLanguageProvider upsideDown;

    public TropicraftLangProvider(DataGenerator gen) {
        super(gen, Constants.MODID, "en_us");
        this.upsideDown = new AccessibleLanguageProvider(gen, Constants.MODID, "en_ud");
    }

    @Override
    protected void addTranslations() {
        add(LoveTropics.TROPICRAFT_ITEM_GROUP, "Tropicraft");
        add(LoveTropics.LOVE_TROPICS_ITEM_GROUP, "Love Tropics");

        add(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED, "You've already registered for the current minigame!");
        add(TropicraftLangKeys.COMMAND_MINIGAME_NOT_REGISTERED, "Minigame with that ID has not been registered: %s");
        add(TropicraftLangKeys.COMMAND_MINIGAME_ID_INVALID, "A minigame with that ID doesn't exist!");
        add(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED, "Another minigame is already in progress! Stop that one first before polling another.");
        add(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING, "Another minigame is already polling! Stop that one first before polling another.");
        add(TropicraftLangKeys.COMMAND_MINIGAME_POLLING, "Minigame %s is polling. Type %s to get a chance to play!");
        add(TropicraftLangKeys.COMMAND_SORRY_ALREADY_STARTED, "Sorry, the current minigame has already started!");
        add(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING, "There is no minigame currently polling.");
        add(TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME, "You have registered for Minigame %s. When the minigame starts, random registered players will be picked to play. Please wait for hosts to start the minigame. You can continue to do what you were doing until then.");
        add(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME, "You are not currently registered for any minigames.");
        add(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, "You have unregistered for Minigame %s.");
        add(TropicraftLangKeys.COMMAND_ENTITY_NOT_PLAYER, "Entity that attempted command is not player.");
        add(TropicraftLangKeys.COMMAND_MINIGAME_POLLED, "Minigame successfully polled!");
        add(TropicraftLangKeys.COMMAND_NOT_ENOUGH_PLAYERS, "There aren't enough players to start this minigame. It requires at least %s amount of players.");
        add(TropicraftLangKeys.COMMAND_MINIGAME_STARTED, "You have started the minigame.");
        add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE, "Survive The Tide");
        add(TropicraftLangKeys.MINIGAME_SIGNATURE_RUN, "Signature Run");
        add(TropicraftLangKeys.MINIGAME_UNDERWATER_TRASH_HUNT, "Underwater Trash Hunt");
        add(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS, "There are no longer enough players to start the minigame!");
        add(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS, "There are now enough players to start the minigame!");
        add(TropicraftLangKeys.COMMAND_NO_MINIGAME, "There is no currently running minigame to stop!");
        add(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, "You have stopped the %s minigame.");
        add(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME, "The minigame %s has finished. If you were inside the minigame, you have been teleported back to your original position.");
        add(TropicraftLangKeys.COMMAND_MINIGAME_STOPPED_POLLING, "An operator has stopped polling the minigame %s.");
        add(TropicraftLangKeys.COMMAND_STOP_POLL, "You have successfully stopped the poll.");
        
        add(TropicraftLangKeys.COMMAND_RESET_DONATION, "Resetting donation data.");
        add(TropicraftLangKeys.COMMAND_RESET_LAST_DONATION, "Reset last seen donation ID to %d.");
        add(TropicraftLangKeys.COMMAND_SIMULATE_DONATION, "Simulating donation for name %s and amount %s");
        
        add(TropicraftLangKeys.DONATION, "%s donated %s!");

        add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH1, "Through the rising sea levels, the volatile and chaotic weather, and the struggle to survive, one player remains: %s.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH2, "\nThose who have fallen have been swept away by the encroaching tides that engulf countless landmasses in this dire future.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH3, "\nThe lone survivor of this island, %s, has won - but at what cost? The world is not what it once was, and they must survive in this new apocalyptic land.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH4, "\nWhat would you do different next time? Together, we could stop this from becoming our future.");

        add(TropicraftLangKeys.MINIGAME_FINISH, "The minigame will end in 10 seconds...");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO1, "The year...2050. Human-caused climate change has gone unmitigated and the human population has been forced to flee to higher ground.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO2, "\nYour task, should you choose to accept it, which you have to because of climate change, is to survive the rising tides, unpredictable weather, and other players.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO3, "\nBrave the conditions and defeat the others who are just trying to survive, like you. And remember...your resources are as limited as your time.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO4, "\nSomeone else may have the tool or food you need to survive. What kind of person will you be when the world is falling apart?");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO5, "\nLet's see!");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_DISABLED, "NOTE: PvP is disabled for %s minutes! Go fetch resources before time runs out.");
        add(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_ENABLED, "WARNING: PVP HAS BEEN ENABLED! Beware of other players...");

        add(TropicraftLangKeys.SURVIVE_THE_TIDE_DOWN_TO_TWO, "IT'S DOWN TO TWO PLAYERS! %s and %s are now head to head - who will triumph above these rising tides?");
    }
    
    private String getAutomaticName(Supplier<? extends IForgeRegistryEntry<?>> sup) {
        return Util.toEnglishName(sup.get().getRegistryName().getPath());
    }
    
    private void addBlock(Supplier<? extends Block> block) {
        addBlock(block, getAutomaticName(block));
    }
    
    private void addBlockWithTooltip(Supplier<? extends Block> block, String tooltip) {
        addBlock(block);
        addTooltip(block, tooltip);
    }
    
    private void addBlockWithTooltip(Supplier<? extends Block> block, String name, String tooltip) {
        addBlock(block, name);
        addTooltip(block, tooltip);
    }
    
    private void addItem(Supplier<? extends Item> item) {
        addItem(item, getAutomaticName(item));
    }
    
    private void addItemWithTooltip(Supplier<? extends Item> block, String name, List<String> tooltip) {
        addItem(block, name);
        addTooltip(block, tooltip);
    }
    
    private void addTooltip(Supplier<? extends IItemProvider> item, String tooltip) {
        add(item.get().asItem().getTranslationKey() + ".desc", tooltip);
    }
    
    private void addTooltip(Supplier<? extends IItemProvider> item, List<String> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            add(item.get().asItem().getTranslationKey() + ".desc." + i, tooltip.get(i));
        }
    }
    
    private void add(ItemGroup group, String name) {
        add(group.getTranslationKey(), name);
    }
    
    private void addEntityType(Supplier<? extends EntityType<?>> entity) {
        addEntityType(entity, getAutomaticName(entity));
    }
    
    private void addBiome(Supplier<? extends Biome> biome) {
        addBiome(biome, getAutomaticName(biome));
    }
    
    // Automatic en_ud generation

    private static final String NORMAL_CHARS = 
            /* lowercase */ "abcdefghijklmn\u00F1opqrstuvwxyz" +
            /* uppercase */ "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            /*  numbers  */ "0123456789" +
            /*  special  */ "_,;.?!/\\'";
    private static final String UPSIDE_DOWN_CHARS = 
            /* lowercase */ "\u0250q\u0254p\u01DD\u025Fb\u0265\u0131\u0638\u029E\u05DF\u026Fuuodb\u0279s\u0287n\u028C\u028Dx\u028Ez" +
            /* uppercase */ "\u2C6F\u15FA\u0186\u15E1\u018E\u2132\u2141HI\u017F\u029E\uA780WNO\u0500\u1F49\u1D1AS\u27D8\u2229\u039BMX\u028EZ" +
            /*  numbers  */ "0\u0196\u1105\u0190\u3123\u03DB9\u312586" +
            /*  special  */ "\u203E'\u061B\u02D9\u00BF\u00A1/\\,";
    
    static {
        if (NORMAL_CHARS.length() != UPSIDE_DOWN_CHARS.length()) {
            throw new AssertionError("Char maps do not match in length!");
        }
    }

    private String toUpsideDown(String normal) {
        char[] ud = new char[normal.length()];
        for (int i = 0; i < normal.length(); i++) {
            char c = normal.charAt(i);
            if (c == '%') {
                String fmtArg = "";
                while (Character.isDigit(c) || c == '%' || c == '$' || c == 's' || c == 'd') { // TODO this is a bit lazy
                    fmtArg += c;
                    i++;
                    c = i == normal.length() ? 0 : normal.charAt(i);
                }
                i--;
                for (int j = 0; j < fmtArg.length(); j++) {
                    ud[normal.length() - 1 - i + j] = fmtArg.charAt(j);
                }
                continue;
            }
            int lookup = NORMAL_CHARS.indexOf(c);
            if (lookup >= 0) {
                c = UPSIDE_DOWN_CHARS.charAt(lookup);
            }
            ud[normal.length() - 1 - i] = c;
        }
        return new String(ud);
    }

    @Override
    protected void add(String key, String value) {
        super.add(key, value);
        upsideDown.add(key, toUpsideDown(value));
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        super.act(cache);
        upsideDown.act(cache);
    }
}
