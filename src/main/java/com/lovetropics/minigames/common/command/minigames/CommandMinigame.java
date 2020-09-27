package com.lovetropics.minigames.common.command.minigames;

import java.util.function.Supplier;

import com.mojang.brigadier.Command;

import net.minecraft.command.CommandSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Helper class for minigame commands.
 */
public class CommandMinigame {
    /**
     * Default logic for executing a minigame action. Fetches the ActionResult back from
     * the minigame manager actions and dispatches them as messages to command source.
     * @param action The action executed (usually from MinigameManager)
     * @param source The source of the executing command.
     * @return The result of the execution (0 == fail, 1 == success)
     */
    public static int executeMinigameAction(Supplier<ActionResult<ITextComponent>> action, CommandSource source) {
    	ActionResult<ITextComponent> result;
    	try {
    		result = action.get();
    	} catch (Exception e) {
    		e.printStackTrace();
    		source.sendErrorMessage(new StringTextComponent(e.toString()));
    		return 0;
    	}

        if (result.getType() == ActionResultType.FAIL) {
            source.sendErrorMessage(result.getResult());

            return 0;
        } else {
            source.sendFeedback(result.getResult(), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
