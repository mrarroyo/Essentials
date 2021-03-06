package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;


public class Commandpowertool extends EssentialsCommand
{
	public Commandpowertool()
	{
		super("powertool");
	}

	@Override
	protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
		final String command = getFinalArg(args, 0);
		final ItemStack itemStack = user.getItemInHand();
		powertool(server, user, user, commandLabel, itemStack, command);
	}

	@Override
	protected void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 3) //running from console means inserting a player and item before the standard syntax
		{
			throw new Exception("When running from console, usage is: /" + commandLabel + " <player> <itemid> <command>");
		}

		final User user = getPlayer(server, args, 0, true, true);
		final ItemStack itemStack = ess.getItemDb().get(args[1]);
		final String command = getFinalArg(args, 2);
		powertool(server, sender, user, commandLabel, itemStack, command);
	}

	protected void powertool(final Server server, final CommandSender sender, final User user, final String commandLabel, final ItemStack itemStack, String command) throws Exception
	{
		// check to see if this is a clear all command
		if (command != null && command.equalsIgnoreCase("d:"))
		{
			user.clearAllPowertools();
			sender.sendMessage(_("powerToolClearAll"));
			return;
		}

		if (itemStack == null || itemStack.getType() == Material.AIR)
		{
			throw new Exception(_("powerToolAir"));
		}

		final String itemName = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
		List<String> powertools = user.getPowertool(itemStack);
		if (command != null && !command.isEmpty())
		{
			if (command.equalsIgnoreCase("l:"))
			{
				if (powertools == null || powertools.isEmpty())
				{
					throw new Exception(_("powerToolListEmpty", itemName));
				}
				else
				{
					sender.sendMessage(_("powerToolList", StringUtil.joinList(powertools), itemName));
				}
				throw new NoChargeException();
			}
			if (command.startsWith("r:"))
			{
				command = command.substring(2);
				if (!powertools.contains(command))
				{
					throw new Exception(_("powerToolNoSuchCommandAssigned", command, itemName));
				}

				powertools.remove(command);
				sender.sendMessage(_("powerToolRemove", command, itemName));
			}
			else
			{
				if (command.startsWith("a:"))
				{
					if (sender instanceof User && !((User)sender).isAuthorized("essentials.powertool.append"))
					{
						throw new Exception(_("noPerm", "essentials.powertool.append"));
					}
					command = command.substring(2);
					if (powertools.contains(command))
					{
						throw new Exception(_("powerToolAlreadySet", command, itemName));
					}
				}
				else if (powertools != null && !powertools.isEmpty())
				{
					// Replace all commands with this one
					powertools.clear();
				}
				else
				{
					powertools = new ArrayList<String>();
				}

				powertools.add(command);
				sender.sendMessage(_("powerToolAttach", StringUtil.joinList(powertools), itemName));
			}
		}
		else
		{
			if (powertools != null)
			{
				powertools.clear();
			}
			sender.sendMessage(_("powerToolRemoveAll", itemName));
		}

		if (!user.arePowerToolsEnabled())
		{
			user.setPowerToolsEnabled(true);
			user.sendMessage(_("powerToolsEnabled"));
		}
		user.setPowertool(itemStack, powertools);
	}
}
